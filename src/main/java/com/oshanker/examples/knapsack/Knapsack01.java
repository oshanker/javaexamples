package com.oshanker.examples.knapsack;

public class Knapsack01 {

    public static int knapsack(int capacity, int[] weights, int[] values) {
        if (capacity <= 0 || weights == null || values == null || weights.length == 0) {
            return 0;
        }
        int n = weights.length;

        // dp[w] stores the maximum value achievable with a knapsack capacity of 'w'.
        // Initialized to 0 by default (0 value when capacity is 0 or no items are considered).
        int[] dp = new int[capacity + 1];

        // Iterate through each item one by one
        for (int i = 0; i < n; i++) {
            
// Traverse capacities BACKWARDS from maximum capacity down 
        	// to the current item's weight.
// Why backwards? Because dp[w - weights[i]] still holds 
        	// the optimal value from the 
// PREVIOUS item iteration. Going forwards would overwrite 
        	// values early and allow 
// the same item to be picked multiple times
        	// (Unbounded Knapsack).
            for (int w = capacity; w >= weights[i]; w--) {
                
                // Choice 1: Do not include the current item (keep existing dp[w])
                int excludeItem = dp[w];
                
                // Choice 2: Include the current item (add its value to the best value 
                // achievable with the remaining capacity)
                int includeItem = values[i] + dp[w - weights[i]];

                // Update dp[w] with the better of the two choices
                dp[w] = Math.max(excludeItem, includeItem);
            }
        }

        // The last element holds the maximum value for the full capacity
        return dp[capacity];
    }

    public static void main(String[] args) {
        int[] values = {60, 100, 120};
        int[] weights = {10, 20, 30};
        int capacity = 50;

        int maxProfit = knapsack(capacity, weights, values);
        System.out.println("Maximum value: " + maxProfit); // Output: 220
    }
}
