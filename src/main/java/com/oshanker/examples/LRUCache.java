package com.oshanker.examples;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LRUCache<K,V>  implements Cache<K,V>{
	/*
https://medium.com/@divitaphadakale21/building-a-thread-safe-lru-cache-from-scratch-in-java-17-2f05abdfb237 
https://codefarm0.medium.com/lets-implement-an-lru-cache-in-java-step-by-step-e0ef505db9f9 	 
https://medium.com/@kaustubh.saha/linkedhashmap-eccf50ea4a97

https://substack.com/home/post/p-147935421?utm_campaign=post&utm_medium=web 

	 put : put in hashmap if capacity, move entry to head.
	 
	 */

	
    @Override
	public String toString() {
    	StringBuffer ret = new StringBuffer();
    	ret.append("keys: ");
    	Node<K,V> node = head.next;
    	while (node.key != null) {
    	    ret.append(node.key );
    	    node = node.next;
    	}
		return ret.toString();
	}


	// Doubly Linked List Node structure
    private static class Node<K,V> {
        K key;
        V value;
        Node<K,V> prev;
        Node<K,V> next;
        
        @Override
		public String toString() {
			return "Node [key=" + key + ", value=" + value + "]";
		}

		Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final int capacity;


	private final Map<K, Node<K,V>> cacheMap;
    private final Node<K,V> head;
    private final Node<K,V> tail;

    // Initialize the cache with a positive capacity
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        
        // Initialize dummy head and tail nodes
        this.head = new Node<K,V>(null, null);
        this.tail = new Node<K,V>(null, null);
        
        // Connect head and tail together
        this.head.next = this.tail;
        this.tail.prev = this.head;
    }

    public int getCapacity() {
		return capacity;
	}

    // Get the value of the key if it exists, otherwise return -1
    public synchronized V get(K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        
        Node<K,V> node = cacheMap.get(key);
        // Move the accessed node to the front (Most Recently Used)
        moveToHead(node);
        return node.value;
    }

    // Update the value if key exists, or add a new key-value pair
    public synchronized V put(K key, V value) {
        if (cacheMap.containsKey(key)) {
            Node<K,V> node = cacheMap.get(key);
            node.value = value; // Update value
            moveToHead(node);   // Mark as Most Recently Used
        } else {
            // Check if capacity is reached
            if (cacheMap.size() >= capacity) {
                // Evict the Least Recently Used (node just before tail)
                Node<K,V> lruNode = tail.prev;
                removeNode(lruNode);
                cacheMap.remove(lruNode.key);
            }
            
            // Insert the new node
            Node<K,V> newNode = new Node<K,V>(key, value);
            addNodeToHead(newNode);
            cacheMap.put(key, newNode);
        }
        return value;
    }


	@Override
	public synchronized int size() {
		return cacheMap.size();
	}

	@Override
	public synchronized  V remove(K key) {
		if(!containsKey(key)) {
			return null;
		}
		Node<K, V> nodeKey = cacheMap.remove(key);
		V ret = nodeKey.value;
		removeNode(nodeKey);
		return ret;
	}

	@Override
	public synchronized Set<K> keySet() {
		// TODO Auto-generated method stub
		Set<K> ret = new LinkedHashSet<>();
    	Node<K,V> node = head.next;
    	while (node.key != null) {
    	    ret.add(node.key );
    	    node = node.next;
    	}
		return ret;

	}

	@Override
	public synchronized boolean containsKey(K key) {
		// TODO Auto-generated method stub
		return cacheMap.containsKey(key);
	}

 
    // --- Helper methods for Doubly Linked List Operations ---

    // Remove a node from its current position in the list
    private void removeNode(Node<K,V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Insert a node immediately after the dummy head node
    private void addNodeToHead(Node<K,V> node) {
        node.next = head.next;
        node.next.prev = node;
        
        head.next = node;
        node.prev = head;
    }

    // Helper to refresh a node's position to the head
    private void moveToHead(Node<K,V> node) {
        removeNode(node);
        addNodeToHead(node);
    }
   
	public static void main(String[] args) {
		Cache<Integer, String> cache = new LRUCache<>(3);
		LRUSimple.intKeys(cache);
		cache = new LRUCache<>(10);
        try {
        	LRUSimple.testSynchronization(cache, 16, 100_007);
            System.out.println("Synchronization stress test passed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Synchronization stress test interrupted", exception);
        }
		
	}
}
