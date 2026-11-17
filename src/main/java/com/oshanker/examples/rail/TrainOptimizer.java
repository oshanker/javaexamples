package com.oshanker.examples.rail;

import java.lang.invoke.MethodHandles;
import java.util.*;


import static com.oshanker.examples.rail.TrainBuilder.Locomotive;

public class TrainOptimizer {

    // Constants for length constraints (in feet)
    static final int MIN_TRAIN_LENGTH = 5280;
    static final int MAX_TRAIN_LENGTH = 15000;


    static class Car {
        String id;
        int lengthFt; // 20ft or 40ft
        int weightTons;

        public Car(String id, int lengthFt, int weightTons) {
            this.id = id;
            this.lengthFt = lengthFt;
            this.weightTons = weightTons;
        }
    }

    /*
    */
    static class Train {
        String name;
        List<Locomotive> locomotives = new ArrayList<>();
        List<Car> cars = new ArrayList<>();

        public Train(String name) {
            this.name = name;
        }

        int capacity = 0, load = 0;
        int length = 0;

        void addLoco(Locomotive l) { 
        	locomotives.add(l); capacity += l.pullCapacityTons; 
        	length += l.lengthFt; 
        }
        boolean canAddCar(Car c) { 
        	
        	return load + c.weightTons <= capacity && 
        			length + c.lengthFt <= MAX_TRAIN_LENGTH; }
        void addCar(Car c) { 
        	cars.add(c); load += c.weightTons; length += c.lengthFt; }
        Car removeCarAt(int i) { 
        	Car c = cars.remove(i); load -= c.weightTons; 
        	length -= c.lengthFt; return c; }
        void insertCarAt(int i, Car c) { 
        	cars.add(i, c); load += c.weightTons; length += c.lengthFt; }
        public int getTotalPullCapacity() {
            return locomotives.stream().mapToInt(l -> l.pullCapacityTons).sum();
        }

        public int getCurrentCargoWeight() {
            return cars.stream().mapToInt(c -> c.weightTons).sum();
        }

        public int getTotalLength() {
            int locoLength = locomotives.stream().mapToInt(l -> l.lengthFt).sum();
            int carLength = cars.stream().mapToInt(c -> c.lengthFt).sum();
            return locoLength + carLength;
        }


        public boolean isValidLength() {
            int len = getTotalLength();
            return len >= MIN_TRAIN_LENGTH && len <= MAX_TRAIN_LENGTH;
        }

        @Override
        public String toString() {
            return String.format("%s | Locos: %d | Cars: %d | Length: %d ft (Valid: %b) | Weight: %d/%d tons",
                    name, locomotives.size(), cars.size(), getTotalLength(),
                    isValidLength(), getCurrentCargoWeight(), getTotalPullCapacity());
        }
    }
    
    

    static double score(List<Car> unallocatedCars,
    		List<Train>  trains) {
        double s = unallocatedCars.size() * 1_000_000.0;               // goal 1: allocate all cars
        double avg = trains.stream().mapToInt(t -> t.length).average()
        		.orElse(0);
        for (Train t : trains) {
            if (t.length < MIN_TRAIN_LENGTH) s += 100.0 * (MIN_TRAIN_LENGTH - t.length); // goal 2: length limits
            if (t.length > MAX_TRAIN_LENGTH) s += 100.0 * (t.length - MAX_TRAIN_LENGTH);
            s += 0.01 * (t.length - avg) * (t.length - avg);           // goal 2: balanced lengths
        }
        return s;
    }

    public static List<Train> buildOptimalTrains(List<Locomotive> locos, List<Car> cars) {
        // 1. Sort locomotives heaviest pull capacity first
        locos.sort((a, b) -> Integer.compare(b.pullCapacityTons, a.pullCapacityTons));

        // 2. Sort cars largest/heaviest first (First-Fit Decreasing heuristic)
        cars.sort((a, b) -> {
            if (b.lengthFt != a.lengthFt) return Integer.compare(b.lengthFt, a.lengthFt);
            return Integer.compare(b.weightTons, a.weightTons);
        });

        // Estimate the maximum number of valid trains we can actually sustain
        int totalCarLength = cars.stream().mapToInt(c -> c.lengthFt).sum();
        int maxPossibleTrainsByLength = Math.max(1, totalCarLength / MIN_TRAIN_LENGTH);
        int trainCount = Math.min(locos.size(), maxPossibleTrainsByLength);

        List<Train> trains = new ArrayList<>();
        for (int i = 0; i < trainCount; i++) {
            trains.add(new Train("Train-" + (i + 1)));
        }

        // 3. Distribute ALL locomotives across the trains 
        //(Snake/Round-Robin to balance pull power)
        for (int i = 0; i < locos.size(); i++) {
            int trainIndex = i % trainCount;
            trains.get(trainIndex).addLoco(locos.get(i));
        }

        // 4. Allocate cars to trains
        List<Car> unallocatedCars = new ArrayList<>();
        for (Car car : cars) {
            // Sort trains so we prioritize trains that haven't reached MIN_TRAIN_LENGTH yet,
            // and otherwise pick the shortest train to balance lengths.
            trains.sort((t1, t2) -> {
                boolean t1NeedsMin = t1.getTotalLength() < MIN_TRAIN_LENGTH;
                boolean t2NeedsMin = t2.getTotalLength() < MIN_TRAIN_LENGTH;
                if (t1NeedsMin != t2NeedsMin) return t1NeedsMin ? -1 : 1;
                return Integer.compare(t1.getTotalLength(), t2.getTotalLength());
            });

            boolean placed = false;
            for (Train train : trains) {
                if (train.canAddCar(car)) {
                    train.addCar(car);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                unallocatedCars.add(car);
            }
        }
        
        System.out.println("score " + score(unallocatedCars, trains));

        if (!unallocatedCars.isEmpty()) {
            System.out.println("Warning: " + unallocatedCars.size() + " cars could not be allocated due to weight/length limits.");
        }

        return trains;
    }

    public static void main(String[] args) {
    	System.out.println(MethodHandles.lookup().lookupClass());
        // Sample Data
        List<Locomotive> locomotives = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            // Mix of 150-ton and 200-ton locomotives, each 70ft long
            int capacity = (i % 2 == 0) ? 200 : 150;
            locomotives.add(new Locomotive("L-" + i, capacity, 70));
        }

        List<Car> cars = new ArrayList<>();
        for (int i = 1; i <= 400; i++) {
            // Mix of 20ft (1 ton) and 40ft (2 tons) cars
            int length = (i % 3 == 0) ? 40 : 20;
            int weight = (length == 40) ? 2 : 1;
            cars.add(new Car("C-" + i, length, weight));
        }
        TrainBuilder. checkFeasibility(locomotives, cars);

        List<Train> optimizedFleet = buildOptimalTrains(locomotives, cars);
        for (Train t : optimizedFleet) {
            System.out.println(t);
        }
    }
}
