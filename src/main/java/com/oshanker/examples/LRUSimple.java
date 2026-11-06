package com.oshanker.examples;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUSimple<K,V> extends LinkedHashMap<K, V> implements Cache<K,V>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int capacity;
	
	
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
        super(initialCapacity, 0.75f, true);
	    this.capacity = initialCapacity;
	}


    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
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
		//testAccessOrder();
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

		cache.get(3); // Access 3
		System.out.println("After accessing 3 : " + cache);
		cache.put(5, "E"); // Should evict 1

		System.out.println("After accessing 3 and adding 5: " + cache);
        printKeyOrder(cache);

		// Final cache state should contain keys: 4, 3, 5
		System.out.println("Contains 2? " + cache.containsKey(2)); // false
		System.out.println("Contains 1? " + cache.containsKey(1)); // false
		System.out.println("Contains 3? " + cache.containsKey(3)); // true
		System.out.println("Contains 4? " + cache.containsKey(4)); // true
		System.out.println("Contains 5? " + cache.containsKey(5)); // true
	}


	private static <K, V> void printKeyOrder(Cache<K, V> cache) {
		StringBuilder order = new StringBuilder();
        for (K key : cache.keySet()) {
            order.append(key);
        }
        System.out.println("Key order: " + order.toString());
	}

}
