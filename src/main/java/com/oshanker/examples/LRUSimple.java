package com.oshanker.examples;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUSimple<K,V> extends LinkedHashMap<K, V>
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
		super(initialCapacity);
	    this.capacity = initialCapacity;
	}


    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

	public static void main(String[] args) {
		LRUSimple<Integer, String> cache = new LRUSimple<>(3);

		cache.put(1, "A");
		cache.put(2, "B");
		cache.put(3, "C");
		System.out.println("Initial: " + cache);

		cache.get(1); // Access 1 to make it recently used
		cache.put(4, "D"); // Should evict 2 (least recently used)

		System.out.println("After accessing 1 and adding 4: " + cache);

		cache.get(3); // Access 3
		cache.put(5, "E"); // Should evict 1

		System.out.println("After accessing 3 and adding 5: " + cache);

		// Final cache state should contain keys: 4, 3, 5
		System.out.println("Contains 2? " + cache.containsKey(2)); // false
		System.out.println("Contains 1? " + cache.containsKey(1)); // false
		System.out.println("Contains 3? " + cache.containsKey(3)); // true
		System.out.println("Contains 4? " + cache.containsKey(4)); // true
		System.out.println("Contains 5? " + cache.containsKey(5)); // true
	}

}
