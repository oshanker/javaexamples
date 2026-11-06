package com.oshanker.examples;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LRUSimple<K,V>  implements Cache<K,V>
{

	private int capacity;
    private final LinkedHashMap<K, V> entries;

	
	
	/*
An LRU cache can be implemented by extending LinkedHashMap 
and overriding the removeEldestEntry method to return true 
when the map's size exceeds the defined capacity. 

https://docs.oracle.com/javase/8/docs/api/java/util/LinkedHashMap.html
	 */


	public LRUSimple(int initialCapacity) {
        // the last param is the ordering mode. 
    	// So, by passing in true, we turned on access-order, 
    	// whereas the default was insertion-order.
	    this.capacity = initialCapacity;
	    this.entries = new LinkedHashMap<>(capacity, 0.75f, true)
	    {
	    	@Override
	    	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	    		return size() > LRUSimple.this.capacity;
	    	}
	    };	
    }
	
    public int getCapacity() {
		return capacity;
	}

    /** Returns the value, or null when the key is absent. */
    public synchronized V get(K key) {
        return entries.get(key);
    }

    /** Returns true when the key exists, and marks it recently used. */
    public synchronized boolean containsKey(K key) {
        return entries.get(key) != null || entries.containsKey(key);
    }


    public synchronized V remove(K key) {
        return entries.remove(key);
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
    }

    /** A snapshot ordered from least to most recently used. */
    public synchronized Map<K, V> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    /** Adds or replaces an entry and 
     * evicts the least recently used entry if needed. */
	public synchronized V put(K key, V value) {
	    return entries.put(Objects.requireNonNull(key, "key"), value);
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return entries.keySet();
	}
	
    public static void testAccessOrder() {
        Cache<String, String> cache = new LRUSimple<>(3);
        cache.put("A", "Apple");
        cache.put("B", "Banana");
        cache.put("C", "Cherry");
		System.out.println("Initial: " + cache);

        // Access key "A" to move it to end
        cache.get("A");
		System.out.println("After accessing A " + cache);

        // Access key "B" to move it to end
        cache.get("B");
		System.out.println("After accessing B " + cache);

        // Expected order: C → A → B
        StringBuilder order = new StringBuilder();
        for (String key : cache.keySet()) {
            order.append(key);
        }

        System.out.println("CAB");
        System.out.println(order.toString());
    }
    
	public static void main(String[] args) {
		Cache<Integer, String> cache = new LRUSimple<>(3);
		intKeys(cache);
		cache = new LRUSimple<>(10);
		
		//testAccessOrder();
        try {
            testSynchronization(cache, 16, 100_007);
            System.out.println("Synchronization stress test passed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Synchronization stress test interrupted", exception);
        }
    }


	static void intKeys(Cache<Integer, String> cache) {

		cache.put(1, "A");
		cache.put(2, "B");
		cache.put(3, "C");
		System.out.println("Initial: " + cache);

		cache.get(2); // Access 2 to make it recently used
		System.out.println("After accessing 2 : " + cache);
		cache.put(4, "D"); // Should evict 1 (least recently used)

		System.out.println("After accessing 2 and adding 4: " + cache);
		System.out.println("Evict 1");

		cache.get(3); // Access 3
		System.out.println("After accessing 3 : " + cache);
		cache.put(5, "E"); // Should evict 2

		System.out.println("After accessing 3 and adding 5: " + cache);
		System.out.println("Evict 2");
        printKeyOrder(cache);

		// Final cache state should contain keys: 4, 3, 5
		System.out.println("Contains 2? " + cache.containsKey(2)); // false
		System.out.println("Contains 1? " + cache.containsKey(1)); // false
		System.out.println("Contains 3? " + cache.containsKey(3)); // true
		System.out.println("Contains 4? " + cache.containsKey(4)); // true
		System.out.println("Contains 5? " + cache.containsKey(5)); // true
	}

    /**
     * Exercises concurrent reads, writes, and removals against a shared cache.
     * Throws AssertionError if a worker fails or the capacity invariant is broken.
     */
    public static void testSynchronization(
    		Cache<Integer, String> cache, int threadCount, int operationsPerThread)
            throws InterruptedException {
        if (threadCount <= 0 || operationsPerThread <= 0) {
            throw new IllegalArgumentException("threadCount and operationsPerThread must be positive");
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        /*
ready tracks whether every worker is prepared.
start is a one-time gate, initially closed.         */
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        /*
Workers arrive ──► ready.countDown()
                      │
Main waits ◄──────────┘
       │ all workers ready
       ▼
Main calls start.countDown()
       │
       ▼
All workers leave start.await() and hit the cache concurrently         * 
         */

        try {
            Future<?>[] workers = new Future<?>[threadCount];
            for (int worker = 0; worker < threadCount; worker++) {
                final int workerId = worker;
                workers[worker] = pool.submit(() -> {
                	ready.countDown(); // announces: “I’m ready”
                	start.await();     // waits for the gate to open                    ready.countDown();
                	for (int operation = 0; operation < operationsPerThread; operation++) {
                		int key = (workerId * operationsPerThread + operation/3) % 250;
                		switch (operation % 3) {
                		case 0 -> {
                			cache.put(key, Integer.toString(operation));
                            //System.out.println(operation + " cache.size(): " + cache.size());
                		}
                		case 1 -> cache.get(key);
                		//default -> {}
                		default -> cache.remove(key);
                		}
                	}
                	
                	return null;
                });
            }
/*
ready.await();     // waits until all workers are ready
start.countDown(); // opens the gate for everyone
 */
            ready.await();
            start.countDown();
            for (Future<?> worker : workers) {
                worker.get();
            }
            System.out.println("cache.size(): " + cache.size());
            
            if (cache.size() > cache.getCapacity()) {
                throw new AssertionError("cache exceeded capacity: " + cache.size());
            }
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new AssertionError("concurrent cache operation failed", exception.getCause());
        } finally {
            pool.shutdownNow();
        }
    }

	private static <K, V> void printKeyOrder(Cache<K, V> cache) {
		StringBuilder order = new StringBuilder();
        for (K key : cache.keySet()) {
            order.append(key);
        }
        System.out.println("Key order: " + order.toString());
	}

}
