package com.oshanker.examples.rail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RailDefectDetectorC3 {

    public record TelemetryPoint(
        Instant timestamp,
        double sensorAValue,
        double sensorBValue,
        String imageFramePath
    ) {}

    public record DefectEvent(
        String trainId,
        String sensorPairId,
        Instant timestamp,
        double variance,
        String imageFramePath
    ) {
    	
    }

    public List<DefectEvent> detectDefects(
            String trainId, 
            String sensorPairId, 
            List<TelemetryPoint> stream, 
            double tolerance) {
        
        List<DefectEvent> defects = new ArrayList<>();

        for (TelemetryPoint point : stream) {
            double variance = Math.abs(point.sensorAValue() - point.sensorBValue());
            
            if (variance > tolerance) {
                defects.add(new DefectEvent(
                    trainId,
                    sensorPairId,
                    point.timestamp(),
                    variance,
                    point.imageFramePath()
                ));
            }
        }
        return defects;
    }

    // Example Usage with your dataset
    public static void main(String[] args) {
        double[] a1 = {1, 2, 3, 4, 5, 6, 5, 5, 5, 5, 5, 20};
        double[] b1 = {1, 2, 13, 4, 0, 6, 5, 5, 5, 5, 15, 20};
        Instant[] timestamp = new Instant[a1.length];

        List<TelemetryPoint> batch = new ArrayList<>();
        Instant start = Instant.parse("2026-11-17T08:00:00Z");
        for (int i = 0; i < timestamp.length; i++) {
        	timestamp[i] = start.plusSeconds(10*i);
        }
        for (int i = 0; i < a1.length; i++) {
            batch.add(new TelemetryPoint(
            	timestamp[i],
                a1[i],
                b1[i],
                "s3://rail-images/train-42/frame_" + i + ".jpg"
            ));
        }

        RailDefectDetectorC3 detector = new RailDefectDetectorC3();
        List<DefectEvent> flagged = detector.detectDefects(
        		"TRAIN-42", "PAIR-1", batch, 0.5);
        String prefix = "\nFlagged item: ";
        flagged.forEach(item -> System.out.println(prefix + item));

        //flagged.forEach(System.out::println);
    }
}
