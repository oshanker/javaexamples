package com.oshanker.examples.knapsack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KnapsackTest {

    // Simple helper class to hold both max value and selected indices for testing
    static class Result {
        int maxValue;
        List<Integer> selectedIndices;

        Result(int maxValue, List<Integer> selectedIndices) {
            this.maxValue = maxValue;
            this.selectedIndices = selectedIndices;
        }
    }

    // --- 1D Implementation Under Test ---
    public static int knapSack1D(int capacity, int[] weights, int[] values) {
//        return com.oshanker.examples.knapsack.Knapsack01.knapsack(
//        		capacity, weights, values);
        return com.oshanker.examples.knapsack.KnapsackWithItems.knapsack(
        		capacity, weights, values);
    }

    // --- 2D Implementation Under Test (Returns Result for Verification) ---
    public static Result knapSack2D(int capacity, int[] weights, int[] values) {
        if (capacity <= 0 || weights == null || values == null || weights.length == 0) {
            return new Result(0, new ArrayList<>());
        }
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= capacity; w++) {
                int exclude = dp[i - 1][w];
                int include = (weights[i - 1] <= w) ? values[i - 1] + dp[i - 1][w - weights[i - 1]] : 0;
                dp[i][w] = Math.max(exclude, include);
            }
        }

        List<Integer> selected = new ArrayList<>();
        int remCap = capacity;
        for (int i = n; i > 0 && remCap > 0; i--) {
            if (dp[i][remCap] != dp[i - 1][remCap]) {
                selected.add(i - 1);
                remCap -= weights[i - 1];
            }
        }
        Collections.reverse(selected);
        return new Result(dp[n][capacity], selected);
    }

    // --- Test Cases ---
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // Test 1: Standard example
        if (runTest("Standard Case", 50, new int[]{10, 20, 30}, new int[]{60, 100, 120}, 220, Arrays.asList(1, 2))) passed++; else failed++;

        // Test 2: Zero capacity
        if (runTest("Zero Capacity", 0, new int[]{10, 20}, new int[]{60, 100}, 0, Collections.emptyList())) passed++; else failed++;

        // Test 3: Empty items
        if (runTest("Empty Items", 50, new int[]{}, new int[]{}, 0, Collections.emptyList())) passed++; else failed++;

        // Test 4: All items too heavy
        if (runTest("All Items Too Heavy", 5, new int[]{10, 20, 30}, new int[]{60, 100, 120}, 0, Collections.emptyList())) passed++; else failed++;

        // Test 5: All items fit
        if (runTest("All Items Fit", 100, new int[]{10, 20, 30}, new int[]{60, 100, 120}, 280, Arrays.asList(0, 1, 2))) passed++; else failed++;

        // Test 6: Greedy trap (highest value-to-weight ratio is NOT optimal)
        // Item 0: w=3, v=50 (ratio 16.6) -> Greedy picks this, leaving cap 1 (total 50)
        // Items 1 & 2: w=2, v=30 each -> Optimal picks both for w=4, v=60
        if (runTest("Greedy Trap", 4, new int[]{3, 2, 2}, new int[]{50, 30, 30}, 60, Arrays.asList(1, 2))) passed++; else failed++;

        // Test : Null items (null weights, null values, and both null)
        if (runTest("Null Weights", 50, null, new int[]{60, 100}, 0, Collections.emptyList())) passed++; else failed++;
        if (runTest("Null Values", 50, new int[]{10, 20}, null, 0, Collections.emptyList())) passed++; else failed++;
        if (runTest("Both Null", 50, null, null, 0, Collections.emptyList())) passed++; else failed++;

        // Summary
        System.out.println("\n--- Test Summary ---");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    // --- Test Runner Helper ---
    private static boolean runTest(String testName, int cap, int[] w, int[] v, int expectedVal, List<Integer> expectedItems) {
        int actual1D = knapSack1D(cap, w, v);
        Result actual2D = knapSack2D(cap, w, v);

        boolean val1DMatches = (actual1D == expectedVal);
        boolean val2DMatches = (actual2D.maxValue == expectedVal);
        boolean itemsMatch = actual2D.selectedIndices.equals(expectedItems);

        if (val1DMatches && val2DMatches && itemsMatch) {
            System.out.println("[PASS] " + testName);
            return true;
        } else {
            System.out.println("[FAIL] " + testName);
            if (!val1DMatches) System.out.println("   1D Value -> Expected: " + expectedVal + ", Got: " + actual1D);
            if (!val2DMatches) System.out.println("   2D Value -> Expected: " + expectedVal + ", Got: " + actual2D.maxValue);
            if (!itemsMatch)   System.out.println("   Items    -> Expected: " + expectedItems + ", Got: " + actual2D.selectedIndices);
            return false;
        }
    }
}
