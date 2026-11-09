package com.oshanker.examples.leettype;

import java.util.*;

public class TopKFrequentMinHeap {
    public static int[] topKFrequent(int[] nums, int k) {
    	if(nums == null) {
    		return new int[] {};
    	}
        // Step 1: Build the frequency map
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }

        // Step 2: Keep a Min-Heap of size k, ordered by frequency values
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(
//            (a, b) -> freqMap.get(a) - freqMap.get(b)
        		(a, b) -> Integer.compare(freqMap.get(a), freqMap.get(b)) 
        );

        // Step 3: Maintain only the top k frequent elements in the heap
        for (int num : freqMap.keySet()) {
            minHeap.add(num);
            if (minHeap.size() > k) {
                minHeap.poll(); // Evict the element with the lowest frequency
            }
        }

        // Step 4: Convert the heap into the final output array
        int outputCount = minHeap.size();
        int[] result = new int[outputCount];
        for (int i = outputCount - 1; i >= 0; i--) {
            result[i] = minHeap.poll();
        }
        
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {1, 1, 1, 2, 2, 3};
        int k = 2;
        System.out.println(Arrays.toString(topKFrequent(nums, k))); // Output: [1, 2]
    }
}
