package com.oshanker.examples.rail.assethealth;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oshanker.examples.rail.assethealth.AssetHealth.State;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AssetHealthJson {

    // Matches the top-level shape: { "events": [...], "now": 160 }
    public record Input(List<AssetHealth.Event> events, int now) {}

    // Corrected JSON: missing comma added, duplicated HST-7 block removed
    private static final String SAMPLE = """
        {
          "events": [
            { "assetId": "LOCO-1973", "ts": 100, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 105, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 110, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 115, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 120, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 125, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 130, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 135, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 140, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 145, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 150, "heartbeat": true,  "errCode": 0 },
            { "assetId": "LOCO-1973", "ts": 155, "heartbeat": true,  "errCode": 0 },

            { "assetId": "CRANE-A12", "ts": 135, "heartbeat": true,  "errCode": 1001 },
            { "assetId": "CRANE-A12", "ts": 135, "heartbeat": true,  "errCode": 0 },
            { "assetId": "CRANE-A12", "ts": 140, "heartbeat": true,  "errCode": 0 },
            { "assetId": "CRANE-A12", "ts": 145, "heartbeat": true,  "errCode": 0 },
            { "assetId": "CRANE-A12", "ts": 150, "heartbeat": true,  "errCode": 0 },
            { "assetId": "CRANE-A12", "ts": 155, "heartbeat": true,  "errCode": 0 },

            { "assetId": "HST-7", "ts": 140, "heartbeat": false, "errCode": 0 },
            { "assetId": "HST-7", "ts": 145, "heartbeat": false, "errCode": 0 },
            { "assetId": "HST-7", "ts": 150, "heartbeat": false, "errCode": 0 },
            { "assetId": "HST-7", "ts": 155, "heartbeat": false, "errCode": 0 }
          ],
          "now": 160
        }
        """;

    public static void main(String[] args) throws Exception {
    	System.out.println(MethodHandles.lookup().lookupClass());
        // Use a file if one is passed as an argument, otherwise the embedded sample
        String json = (args.length > 0) ? Files.readString(Path.of(args[0])) : SAMPLE;
        int A = (args.length > 1) ? Integer.parseInt(args[1]) : 5;

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Input input = mapper.readValue(json, Input.class);

        final int B = 60 / A;
        Map<String, State> assets = AssetHealth.assetHealth(input.events(), input.now(), A);
		System.out.println(AssetHealth. printOutput(B, assets));
    }
}