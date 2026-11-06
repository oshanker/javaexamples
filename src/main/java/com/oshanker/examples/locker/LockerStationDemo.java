package com.oshanker.examples.locker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LockerStationDemo {
    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        happyPathAndBadCodes();
        upsizingAndFullStation();
        parcelTooBig();
        expiryBoundary();
        reclaimWhenFull();
        constructorValidation();
        ConcurrentDemo.concurrentDepositsAndPickups();   // new
        System.out.println(failures == 0 ? "\nAll checks passed." : "\n" + failures + " check(s) FAILED.");
    }

    // 1. Deposit, pickup, wrong code, double pickup
    private static void happyPathAndBadCodes() throws Exception {
        System.out.println("== Happy path and bad codes ==");
        List<Locker> lockers = List.of(new Locker("S1", Size.SMALL));
        LockerStation station = new LockerStation(Clock.systemUTC(), lockers);

        Parcel parcel = new Parcel(Size.SMALL);
        String code = station.depositPackage(parcel);
        check("code is 6 digits", code.matches("\\d{6}"));
        check("locker S1 holds the parcel", "S1".equals(lockerHolding(lockers, code)));

        check("pickup returns the same parcel", station.pickupPackage(code) == parcel);
        check("locker freed after pickup", lockers.get(0).isFree());

        expectThrows("double pickup", InvalidCodeException.class, () -> station.pickupPackage(code));
        expectThrows("wrong code", InvalidCodeException.class, () -> station.pickupPackage("000000"));
        expectThrows("null code", InvalidCodeException.class, () -> station.pickupPackage(null));
        expectThrows("null_parcel", NullPointerException.class, () -> station.depositPackage(null));
    }

    // 2. Smallest fit first, then upsize, then full
    private static void upsizingAndFullStation() throws Exception {
        System.out.println("\n== Upsizing and full station ==");
        List<Locker> lockers = List.of(
                new Locker("S1", Size.SMALL),
                new Locker("M1", Size.MEDIUM),
                new Locker("L1", Size.LARGE));
        LockerStation station = new LockerStation(Clock.systemUTC(), lockers);

        String c1 = station.depositPackage(new Parcel(Size.SMALL));
        String c2 = station.depositPackage(new Parcel(Size.SMALL));
        String c3 = station.depositPackage(new Parcel(Size.SMALL));
        check("1st small parcel -> SMALL locker", "S1".equals(lockerHolding(lockers, c1)));
        check("2nd small parcel upsizes to MEDIUM", "M1".equals(lockerHolding(lockers, c2)));
        check("3rd small parcel upsizes to LARGE", "L1".equals(lockerHolding(lockers, c3)));

        expectThrows("station full", NoAvailableLockerException.class,
                () -> station.depositPackage(new Parcel(Size.SMALL)));

        station.pickupPackage(c1); // frees S1
        String c4 = station.depositPackage(new Parcel(Size.SMALL));
        check("freed locker is reused", "S1".equals(lockerHolding(lockers, c4)));
    }

    // 3. Never downsize
    private static void parcelTooBig() {
        System.out.println("\n== Parcel too big ==");
        LockerStation station = new LockerStation(Clock.systemUTC(),
                List.of(new Locker("S1", Size.SMALL), new Locker("M1", Size.MEDIUM)));
        expectThrows("LARGE parcel, no LARGE locker", NoAvailableLockerException.class,
                () -> station.depositPackage(new Parcel(Size.LARGE)));
    }

    // 4. Expiry at 3 days minus 1 second (ok) and exactly 3 days (expired)
    private static void expiryBoundary() throws Exception {
        System.out.println("\n== Expiry boundary ==");
        MutableClock clock = new MutableClock(Instant.parse("2026-11-06T10:00:00Z"), ZoneOffset.UTC);
        List<Locker> lockers = List.of(new Locker("S1", Size.SMALL), new Locker("S2", Size.SMALL));
        LockerStation station = new LockerStation(clock, lockers);

        String justInTime = station.depositPackage(new Parcel(Size.SMALL));
        String tooLate = station.depositPackage(new Parcel(Size.SMALL));

        clock.advance(Duration.ofDays(3).minusSeconds(1));
        check("pickup 1s before expiry succeeds", station.pickupPackage(justInTime) != null);

        clock.advance(Duration.ofSeconds(1)); // now exactly 3 days after deposit
        expectThrows("pickup at exactly 3 days", ExpiredCodeException.class,
                () -> station.pickupPackage(tooLate));
    }

    // 5. Full station recovers by reclaiming expired lockers on deposit
    private static void reclaimWhenFull() throws Exception {
        System.out.println("\n== Reclaim when full ==");
        MutableClock clock = new MutableClock(Instant.parse("2026-11-06T10:00:00Z"), ZoneOffset.UTC);
        List<Locker> lockers = List.of(new Locker("L1", Size.LARGE));
        LockerStation station = new LockerStation(clock, lockers);

        String oldCode = station.depositPackage(new Parcel(Size.LARGE));
        expectThrows("full before expiry", NoAvailableLockerException.class,
                () -> station.depositPackage(new Parcel(Size.LARGE)));

        clock.advance(Duration.ofDays(3));
        String newCode = station.depositPackage(new Parcel(Size.LARGE)); // triggers the sweep
        check("deposit succeeds after reclaim", "L1".equals(lockerHolding(lockers, newCode)));
        expectThrows("old code is gone after reclaim", InvalidCodeException.class,
                () -> station.pickupPackage(oldCode));

        clock.advance(Duration.ofDays(3));
        List<Parcel> returned = station.reclaimExpired();
        check("explicit sweep returns 1 parcel", returned.size() == 1);
        check("locker free after sweep", lockers.get(0).isFree());
    }

    // 6. Constructor validation
    private static void constructorValidation() {
        System.out.println("\n== Constructor validation ==");
        expectThrows("null clock", NullPointerException.class,
                () -> new LockerStation(null, List.of(new Locker("S1", Size.SMALL))));
        expectThrows("empty locker list", IllegalArgumentException.class,
                () -> new LockerStation(Clock.systemUTC(), new ArrayList<>()));
        expectThrows("duplicate locker ids", IllegalArgumentException.class,
                () -> new LockerStation(Clock.systemUTC(),
                        List.of(new Locker("S1", Size.SMALL), new Locker("S1", Size.MEDIUM))));
    }

    // ---- helpers ----

    private static String lockerHolding(List<Locker> lockers, String code) {
        for (Locker locker : lockers) {
            boolean holdsCode = locker.getReservation()
                    .map(r -> r.getCode().equals(code))
                    .orElse(false);
            if (holdsCode) {
                return locker.getId();
            }
        }
        return "none";
    }

    protected static void check(String name, boolean condition) {
        System.out.println((condition ? "PASS  " : "FAIL  ") + name);
        if (!condition) {
            failures++;
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    private static void expectThrows(String name, Class<? extends Exception> expected, ThrowingAction action) {
        try {
            action.run();
            check(name + " (no exception thrown)", false);
        } catch (Exception e) {
            check(name + " -> " + e.getClass().getSimpleName(), expected.isInstance(e));
        }
    }

    /** Controllable clock for tests: time only moves when advance() is called. */
    private static class MutableClock extends Clock {
        private Instant now;
        private final ZoneId zone;

        MutableClock(Instant start, ZoneId zone) {
            this.now = start;
            this.zone = zone;
        }

        void advance(Duration duration) { now = now.plus(duration); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId z) { return new MutableClock(now, z); }
    }
}