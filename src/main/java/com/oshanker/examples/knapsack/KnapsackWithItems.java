package com.oshanker.examples.knapsack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnapsackWithItems {

    public static void knapsack(int capacity, int[] weights, int[] values) {
        int n = weights.length;
        
        // dp[i][w] stores the maximum value achievable using the first 'i' items 
        // with a maximum weight limit of 'w'.
        int[][] dp = new int[n + 1][capacity + 1];

        // 1. Build the DP table bottom-up
        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= capacity; w++) {
                // Value if we do NOT pick the current item (i - 1)
                int excludeItem = dp[i - 1][w];
                int includeItem = 0;

                // Check if the current item can fit in the remaining capacity 'w'
                if (weights[i - 1] <= w) {
                    // Value of current item + best value for remaining capacity
                    includeItem = values[i - 1] + dp[i - 1][w - weights[i - 1]];
                }

                // Store the better of the two choices
                dp[i][w] = Math.max(excludeItem, includeItem);
            }
        }

        // The bottom-right cell holds the maximum possible value
        int maxValue = dp[n][capacity];
        System.out.println("Maximum Value: " + maxValue);

        // 2. Backtrack through the table to find which items were picked
        List<Integer> selectedIndices = new ArrayList<>();
        int remainingCapacity = capacity;

        for (int i = n; i > 0 && remainingCapacity > 0; i--) {
            // If the value came from the row above (dp[i - 1][remainingCapacity]), 
            // it means item (i - 1) was NOT included.
            // If the values differ, the item WAS included.
            if (dp[i][remainingCapacity] != dp[i - 1][remainingCapacity]) {
                int itemIndex = i - 1;
                selectedIndices.add(itemIndex);
                
                // Subtract the item's weight to trace the remaining capacity
                remainingCapacity -= weights[itemIndex];
            }
        }

        // Reverse the list so items print in their original order
        Collections.reverse(selectedIndices);

        // 3. Print the selected items
        System.out.println("Selected Items:");
        for (int index : selectedIndices) {
            System.out.println("- Item " + index + " (Weight: " + weights[index] + ", Value: " + values[index] + ")");
        }
    }

    public static void main(String[] args) {
        int[] values = {60, 100, 120};
        int[] weights = {10, 20, 30};
        int capacity = 50;

        knapsack(capacity, weights, values);
    }
}
