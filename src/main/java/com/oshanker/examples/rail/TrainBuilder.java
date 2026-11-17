package com.oshanker.examples.rail;


import java.lang.invoke.MethodHandles;
import java.util.*;

import static com.oshanker.examples.rail.TrainOptimizer.Train;
import static com.oshanker.examples.rail.TrainOptimizer.MAX_TRAIN_LENGTH;
import static com.oshanker.examples.rail.TrainOptimizer.MIN_TRAIN_LENGTH;
import static com.oshanker.examples.rail.TrainOptimizer.Car;
/**
 * Builds trains from a pool of locomotives and cars.
 *
 * Goals:  (1) every car allocated
 *         (2) every train length within [MIN_LEN, MAX_LEN] and balanced
 *         (3) every locomotive used (each loco is always placed in a train)
 *
 * Approach: try every possible train count T, distribute locomotives 
 * evenly by
 * pull capacity, greedily load cars, then hill-climb by moving cars between
 * trains. Keep the T with the best score.
 */
public class TrainBuilder {


    static class Locomotive {
        String id;
        int pullCapacityTons;
        int lengthFt; // Locomotives also contribute to total train length

        public Locomotive(String id, int pullCapacityTons, int lengthFt) {
            this.id = id;
            this.pullCapacityTons = pullCapacityTons;
            this.lengthFt = lengthFt;
        }
    }


    static class Plan {
        final List<Train> trains = new ArrayList<>();
        final List<Car> unallocatedCars = new ArrayList<>();

        double score() {
            
            return TrainOptimizer.score(unallocatedCars, trains);
        }

        boolean feasible() {
            if (!unallocatedCars.isEmpty()) return false;
            return trains.stream().allMatch(
            		t -> t.length >= MIN_TRAIN_LENGTH && t.length <= MAX_TRAIN_LENGTH);
        }
    }

    static Plan build(List<Locomotive> locos, List<Car> cars, int numTrains) {
        Plan p = new Plan();
        for (int i = 0; i < numTrains; i++) {
        	p.trains.add(new Train("Train-" + (i + 1)));
        }

        // Distribute locomotives: strongest first, 
        // always to the weakest train (so all are used).
        List<Locomotive> sortedLocos = new ArrayList<>(locos);
        sortedLocos.sort((a, b) -> Integer.compare(b.pullCapacityTons, 
        		a.pullCapacityTons));
        PriorityQueue<Train> pq = new PriorityQueue<Train>(
        		Comparator.comparing(t -> t.capacity));
        pq.addAll(p.trains);
        for (Locomotive l : sortedLocos) {
            Train t = pq.poll();
            t.addLoco(l);
            pq.add(t);
        }

        // Greedy car loading: heaviest first, onto the shortest train 
        // that can take it.
        List<Car> sortedCars = new ArrayList<>(cars);
        sortedCars.sort((a, b) -> {
            int byWeight = Double.compare(b.weightTons, a.weightTons);
            return byWeight != 0 ? byWeight : Integer.compare(
            		b.lengthFt, a.lengthFt);
        });

        
        for (Car c : sortedCars) {
            Train best = null;
            for (Train t : p.trains)
                if (t.canAddCar(c) && (best == null || t.length < best.length)) best = t;
            if (best != null) best.addCar(c); else p.unallocatedCars.add(c);
        }

        improve(p);
        return p;
    }

    /** Hill climbing: place leftover cars, then move cars between
     *  trains while the score improves. */
    static void improve(Plan p) {
        boolean improved = true;
        for (int guard = 0; improved && guard < 500; guard++) {
            improved = false;

            for (Iterator<Car> it = p.unallocatedCars.iterator(); it.hasNext(); ) {
                Car c = it.next();
                Train best = null;
                for (Train t : p.trains)
                    if (t.canAddCar(c) && (best == null || t.length < best.length)) best = t;
                if (best != null) { best.addCar(c); it.remove(); improved = true; }
            }

            double cur = p.score();
            for (Train a : p.trains) {
                for (int i = a.cars.size() - 1; i >= 0; i--) {
                    if (i >= a.cars.size()) continue;
                    Car c = a.cars.get(i);
                    for (Train b : p.trains) {
                        if (a == b || !b.canAddCar(c)) continue;
                        a.removeCarAt(i);
                        b.addCar(c);
                        double s = p.score();
                        if (s < cur - 1e-9) {
                        	cur = s; improved = true; break; }
                        b.removeCarAt(b.cars.size() - 1);   // undo
                        a.insertCarAt(i, c);
                    }
                }
            }
        }
    }

    static Plan optimize(List<Locomotive> locos, List<Car> cars) {
        Plan best = null;
        double bestScore = Double.MAX_VALUE;
        for (int t = 1; t <= locos.size(); t++) {         
        	// every train needs >= 1 locomotive
            Plan p = build(locos, cars, t);
            double s = p.score();
            if (s < bestScore) { bestScore = s; best = p; }
        }
        return best;
    }

    static void print(Plan p) {
        int n = 1;
        for (Train t : p.trains) {
            System.out.printf("Train %d: %d locos, capacity %d t, "
            		+ "load %d t, %d cars, length %d ft %s%n",
                    n++, t.locomotives.size(), t.capacity, t.load, t.cars.size(), t.length,
                    (t.length >= MIN_TRAIN_LENGTH && t.length <= MAX_TRAIN_LENGTH) ? "OK" : "OUT OF RANGE");
        }
        System.out.printf("Unallocated cars: %d score %f %n", 
        		p.unallocatedCars.size(), p.score());
        System.out.println("Feasible: " + p.feasible());
    }
    

    /**
     * Quick necessary-condition checks on the input. Passing does not guarantee a solution,
     * but failing guarantees that no plan can meet all goals.
     */
    static boolean checkFeasibility(List<Locomotive> locos, List<Car> cars) {
        int totalPull = 0, locoLen = 0;
        for (Locomotive l : locos) { 
        	totalPull += l.pullCapacityTons; locoLen += l.lengthFt; }
        double carWeight = 0;
        long carLen = 0;
        for (Car c : cars) { carWeight += c.weightTons; 
        carLen += c.lengthFt; }

        long totalLen = carLen + locoLen;   // every car allocated and every locomotive used
        int minTrains = Math.max(1, (int) Math.ceil(totalLen / MAX_TRAIN_LENGTH));
        int maxTrains = (int) (totalLen / MIN_TRAIN_LENGTH);

        System.out.printf("Input: %d locos (%d t pull, %d ft), %d cars (%.0f t, %d ft)%n",
                locos.size(), totalPull, locoLen, cars.size(), carWeight, carLen);
        System.out.printf("Trains possible by length: %d to %d%n", minTrains, maxTrains);

        boolean ok = true;

        if (carWeight > totalPull) {
            ok = false;
            double avgPull = locos.isEmpty() ? 0 : (double) totalPull / locos.size();
            String extra = avgPull > 0
                    ? " (about " + (int) Math.ceil((carWeight - totalPull) / avgPull) + " more locomotives at the current average)"
                    : "";
            System.out.printf("INFEASIBLE: cars weigh %.0f t but total pull is only %d t%s%n",
                    carWeight, totalPull, extra);
        }
        if (minTrains > maxTrains) {
            ok = false;
            System.out.printf("INFEASIBLE: %d ft of train cannot be split into trains of %d-%d ft%n",
                    totalLen, MIN_TRAIN_LENGTH, MAX_TRAIN_LENGTH);
        }
        if (locos.size() < minTrains) {
            ok = false;
            System.out.printf("INFEASIBLE: need at least %d trains by length but only %d locomotives%n",
                    minTrains, locos.size());
        }

        System.out.println(ok ? "Feasibility check passed (necessary conditions only)\n"
                              : "Continuing with a best-effort plan\n");
        return ok;
    }

    public static void main(String[] args) {
    	System.out.println(MethodHandles.lookup().lookupClass());
        Random rnd = new Random(42);
        List<Locomotive> locos = new ArrayList<>();
        for (int i = 0; i < 160; i++) {  // different pull weights; 
        	//150 t is typical
            locos.add(new Locomotive("L" + i, 120 + rnd.nextInt(61), 70));
        }

        List<Car> cars = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            boolean longCar = rnd.nextBoolean();
            cars.add(new Car("C" + i, longCar ? 40 : 20, longCar ? 15 : 8)); // weight in tons
        }
        checkFeasibility(locos, cars);

        print(optimize(locos, cars));
    }
}
