package com.oshanker.examples.rail;


import java.util.*;
import static com.oshanker.examples.rail.TrainOptimizer.MAX_TRAIN_LENGTH;
import static com.oshanker.examples.rail.TrainOptimizer.MIN_TRAIN_LENGTH;

/**
 * Plain Java tests for TrainBuilder (no JUnit needed).
 * Put it in the same package/folder as TrainBuilder.java and run main().
 * Exits with status 1 if any check fails.
 */
public class TrainBuilderTest {

    private static int passed = 0;
    private static int failed = 0;

    // ---------- tiny assertion helpers ----------

    static void check(boolean condition, String message) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("  FAIL: " + message);
        }
    }

    static void checkEquals(Object expected, Object actual, String message) {
        check(Objects.equals(expected, actual), message + " (expected " + expected + ", got " + actual + ")");
    }

    // ---------- test data builders ----------

    static List<TrainBuilder.Locomotive> locos(int count, int pullTons, int lengthFt) {
        List<TrainBuilder.Locomotive> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(new TrainBuilder.Locomotive("L" + i, pullTons, lengthFt));
        return list;
    }

    static List<TrainOptimizer.Car> cars(int count, int lengthFt, int weightTons) {
        List<TrainOptimizer.Car> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(new TrainOptimizer.Car("C" + i, lengthFt, weightTons));
        return list;
    }
    // ---------- test data builders ----------

    // ---------- invariants that must hold for ANY plan ----------

    static void assertValidPlan(TrainBuilder.Plan plan, int totalLocos, int totalCars) {
        Set<String> locoIds = new HashSet<>();
        Set<String> carIds = new HashSet<>();
        int locoCount = 0, carCount = 0;

        for (TrainOptimizer.Train t : plan.trains) {
            check(!t.locomotives.isEmpty(), "every train has at least one locomotive");
            check(t.load <= t.capacity + 1e-9, "train load " + t.load + " within capacity " + t.capacity);
            check(t.length <= MAX_TRAIN_LENGTH, "train length " + t.length + " within max");

            int recomputed = 0;
            for (TrainBuilder.Locomotive l : t.locomotives) {
            	recomputed += l.lengthFt; locoIds.add(l.id); locoCount++; }
            for (TrainOptimizer.Car c : t.cars) { recomputed += c.lengthFt; carIds.add(c.id); carCount++; }
            checkEquals(recomputed, t.length, "tracked length matches contents");
        }
        for (TrainOptimizer.Car c : plan.unallocatedCars) { carIds.add(c.id); carCount++; }

        checkEquals(totalLocos, locoCount, "all locomotives appear exactly once (count)");
        checkEquals(totalLocos, locoIds.size(), "no locomotive is duplicated");
        checkEquals(totalCars, carCount, "every car is either allocated or unallocated (count)");
        checkEquals(totalCars, carIds.size(), "no car is duplicated");
    }

    // ---------- tests ----------

    /** 12 locos (1800 t, 840 ft) + 120 cars (1200 t, 4800 ft) -> exactly one valid train of 5640 ft. */
    static void testSingleTrainFeasible() {
        System.out.println("testSingleTrainFeasible");
        List<TrainBuilder.Locomotive> l = locos(12, 150, 70);
        List<TrainOptimizer.Car> c = cars(120, 40, 10);

        check(TrainBuilder.checkFeasibility(l, c), "feasibility check passes");
        TrainBuilder.Plan plan = TrainBuilder.optimize(l, c);

        assertValidPlan(plan, 12, 120);
        check(plan.feasible(), "plan is feasible");
        checkEquals(1, plan.trains.size(), "one train");
        checkEquals(0, plan.unallocatedCars.size(), "no unallocated cars");
        checkEquals(5640, plan.trains.get(0).length, "train length");
    }

    /** Too much length for one train, so at least two are needed; all goals must still be met. */
    static void testMultipleTrains() {
        System.out.println("testMultipleTrains");
        List<TrainBuilder.Locomotive> l = locos(30, 150, 70);   // 4500 t, 2100 ft
        List<TrainOptimizer.Car> c = cars(400, 40, 10);           // 4000 t, 16000 ft -> 18100 ft total

        check(TrainBuilder.checkFeasibility(l, c), "feasibility check passes");
        TrainBuilder.Plan plan = TrainBuilder.optimize(l, c);

        assertValidPlan(plan, 30, 400);
        check(plan.feasible(), "plan is feasible");
        check(plan.trains.size() >= 2 && plan.trains.size() <= 3,
                "2 or 3 trains (got " + plan.trains.size() + ")");
        for (TrainOptimizer.Train t : plan.trains) {
            check(t.length >= MIN_TRAIN_LENGTH && t.length <= MAX_TRAIN_LENGTH,
                    "train length " + t.length + " in range");
        }
    }

    /** Cars weigh more than the locomotive can pull: check must fail and 10 cars are left over. */
    static void testNotEnoughPull() {
        System.out.println("testNotEnoughPull");
        List<TrainBuilder.Locomotive> l = locos(1, 100, 70);
        List<TrainOptimizer.Car> c = cars(20, 40, 10);            // 200 t of cars vs 100 t of pull

        check(!TrainBuilder.checkFeasibility(l, c), "feasibility check fails");
        TrainBuilder.Plan plan = TrainBuilder.optimize(l, c);

        assertValidPlan(plan, 1, 20);
        check(!plan.feasible(), "plan is not feasible");
        checkEquals(10, plan.unallocatedCars.size(), "10 cars cannot be pulled");
    }

    /** Plenty of pull but not enough total length to make even one 5280 ft train. */
    static void testTooShortForAnyTrain() {
        System.out.println("testTooShortForAnyTrain");
        List<TrainBuilder.Locomotive> l = locos(2, 150, 70);
        List<TrainOptimizer.Car> c = cars(10, 20, 8);             // 340 ft total

        check(!TrainBuilder.checkFeasibility(l, c), "feasibility check fails (length)");
        TrainBuilder.Plan plan = TrainBuilder.optimize(l, c);

        assertValidPlan(plan, 2, 10);
        check(!plan.feasible(), "plan is not feasible");
    }

    /** Locomotives with different pull weights are all used and spread across trains. */
    static void testMixedLocomotivePulls() {
        System.out.println("testMixedLocomotivePulls");
        List<TrainBuilder.Locomotive> l = new ArrayList<>();
        for (int i = 0; i < 30; i++) l.add(new TrainBuilder.Locomotive("L" + i, 120 + (i % 7) * 10, 70));
        List<TrainOptimizer.Car> c = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            boolean longCar = i % 2 == 0;
            c.add(new TrainOptimizer.Car("C" + i, longCar ? 40 : 20, longCar ? 12 : 6));   // 3600 t vs 4450 t of pull
        }

        TrainBuilder.Plan plan = TrainBuilder.optimize(l, c);
        assertValidPlan(plan, 30, 400);
        checkEquals(0, plan.unallocatedCars.size(), "no unallocated cars");
    }

    // ---------- runner ----------

    public static void main(String[] args) {
        testSingleTrainFeasible();
        testMultipleTrains();
        testNotEnoughPull();
        testTooShortForAnyTrain();
        testMixedLocomotivePulls();

        System.out.println();
        System.out.println("Passed checks: " + passed + ", failed checks: " + failed);
        if (failed > 0) System.exit(1);
    }
}
