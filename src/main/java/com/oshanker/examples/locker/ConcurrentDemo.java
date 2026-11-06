package com.oshanker.examples.locker;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentDemo extends LockerStationDemo {
	// 7. Concurrency: many threads deposit, 
	// then many threads race to pick up the same codes
	static void concurrentDepositsAndPickups() throws Exception {
	    System.out.println("\n== Concurrency ==");
	    final int lockerCount = 50;
	    final int depositThreads = 200;
	    final int pickupThreadsPerCode = 4;

	    List<Locker> lockers = new ArrayList<>();
	    for (int i = 0; i < lockerCount; i++) {
	        lockers.add(new Locker("S" + i, Size.SMALL));
	    }
	    LockerStation station = new LockerStation(Clock.systemUTC(), lockers);

	    // ---- Phase 1: concurrent deposits ----
	    Set<String> codes = ConcurrentHashMap.newKeySet();
	    AtomicInteger full = new AtomicInteger();
	    AtomicInteger unexpected = new AtomicInteger();

	    runConcurrently(depositThreads, () -> {
	        try {
	            codes.add(station.depositPackage(new Parcel(Size.SMALL)));
	        } catch (NoAvailableLockerException e) {
	            full.incrementAndGet();
	        } catch (Exception e) {
	            unexpected.incrementAndGet();
	            e.printStackTrace();
	        }
	    });

	    long occupied = lockers.stream().filter(l -> !l.isFree()).count();
	    Set<String> distinctLockers = new HashSet<>();
	    for (Locker locker : lockers) {
	        locker.getReservation().
	           ifPresent(r -> distinctLockers.add(locker.getId()));
	    }

	    check("exactly " + lockerCount + " deposits succeeded", 
	    		codes.size() == lockerCount);
	    check("remaining deposits rejected as full", 
	    		full.get() == depositThreads - lockerCount);
	    check("no unexpected exceptions during deposits",
	    		unexpected.get() == 0);
	    check("every locker occupied exactly once", 
	    		occupied == lockerCount && distinctLockers.size() == lockerCount);

	    // ---- Phase 2: concurrent pickups, several threads per code ----
	    List<String> codeList = new ArrayList<>(codes);
	    ConcurrentHashMap<String, AtomicInteger> successes = new ConcurrentHashMap<>();
	    AtomicInteger rejected = new AtomicInteger();
	    AtomicInteger unexpectedPickup = new AtomicInteger();
	    AtomicInteger index = new AtomicInteger();

	    runConcurrently(codeList.size() * pickupThreadsPerCode, () -> {
	        String code = codeList.get(index.getAndIncrement() % codeList.size());
	        try {
	            station.pickupPackage(code);
	            successes.computeIfAbsent(code, c -> new AtomicInteger()).incrementAndGet();
	        } catch (InvalidCodeException e) {
	            rejected.incrementAndGet();
	        } catch (Exception e) {
	            unexpectedPickup.incrementAndGet();
	            e.printStackTrace();
	        }
	    });

	    boolean eachOnce = codeList.stream()
	            .allMatch(c -> successes.containsKey(c) && successes.get(c).get() == 1);
	    check("each code picked up exactly once", eachOnce);
	    check("losing pickups rejected as invalid",
	            rejected.get() == codeList.size() * (pickupThreadsPerCode - 1));
	    check("no unexpected exceptions during pickups", unexpectedPickup.get() == 0);
	    check("all lockers free afterwards", lockers.stream().allMatch(Locker::isFree));

	    // ---- Phase 3: station is fully reusable after the storm ----
	    for (int i = 0; i < lockerCount; i++) {
	        station.depositPackage(new Parcel(Size.SMALL));
	    }
	    check("all lockers reusable after concurrent run", lockers.stream().noneMatch(Locker::isFree));
	}

	/** Runs the task on {@code threads} threads, releasing them all at once. */
	private static void runConcurrently(int threads, Runnable task) throws Exception {
	    ExecutorService pool = Executors.newFixedThreadPool(threads); // one thread per task so all can wait at the gate
	    CountDownLatch ready = new CountDownLatch(threads);
	    CountDownLatch go = new CountDownLatch(1);
	    List<Future<?>> futures = new ArrayList<>();

	    for (int i = 0; i < threads; i++) {
	        futures.add(pool.submit(() -> {
	            ready.countDown();
	            try {
	                go.await();               // wait for the starting gun
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                return;
	            }
	            task.run();
	        }));
	    }

	    ready.await();                         // all threads are parked at the gate
	    go.countDown();
	    // release them together
	    for (Future<?> future : futures) {
	        future.get(30, TimeUnit.SECONDS);  // surface any exception; fail on a hang or deadlock
	    }
	    pool.shutdown();
	}
}
