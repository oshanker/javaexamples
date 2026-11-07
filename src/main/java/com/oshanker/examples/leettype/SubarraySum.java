package com.oshanker.examples.leettype;
import java.util.HashMap;
import java.util.Map;


public class SubarraySum {

	    public int subarraySum(int[] nums, int k) {
	        int count = 0; 
	        int currentSum = 0; 
	        
	        Map<Integer, Integer> prefixSums = new HashMap<>();
	        prefixSums.put(0, 1);
	        
	        for (int num : nums) {
	            currentSum += num;
	            count += prefixSums.getOrDefault(currentSum - k, 0);
	            prefixSums.put(currentSum, prefixSums.getOrDefault(currentSum, 0) + 1);
	        }
	        
	        return count;
	    }

	    public static void main(String[] args) {
	    	SubarraySum solver = new SubarraySum();

	        // Test Case 1: Simple sequence
	        int[] nums1 = {1, 2, 3};
	        int k1 = 3;
	        System.out.println("Test 1 Result: " + solver.subarraySum(nums1, k1) + " (Expected: 2)");

	        // Test Case 2: Array with duplicate elements
	        int[] nums2 = {1, 1, 1};
	        int k2 = 2;
	        System.out.println("Test 2 Result: " + solver.subarraySum(nums2, k2) + " (Expected: 2)");

	        // Test Case 3: Handling negative numbers
	        int[] nums3 = {1, -1, 0};
	        int k3 = 0;
	        System.out.println("Test 3 Result: " + solver.subarraySum(nums3, k3) + " (Expected: 3)");
	        // The 3 valid subarrays for Test 3 are: [1, -1], [0], and [1, -1, 0]
	    }
	}

