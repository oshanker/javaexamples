package com.oshanker.examples.leettype;

import java.util.Arrays;
import static com.oshanker.examples.leettype.CoinChangeMin.coinChange;

public class CoinChangeTestRunner {
    public static void main(String[] args) {
        CoinChangeTestRunner tester = new CoinChangeTestRunner();
        int passed = 0;
        int failed = 0;

        System.out.println("--- Running Coin Change Test Cases ---\n");

        // --- 1. Base Cases & Boundaries ---
        if (assertTestCase(tester, new int[]{1, 2, 5}, 0, 0, "Zero Amount")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{5}, 5, 1, "Single Coin Match")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{2}, 3, -1, "Single Coin Impossible")) passed++; else failed++;

        // --- 2. Standard & Greedy Counter-Examples ---
        if (assertTestCase(tester, new int[]{1, 2, 5}, 11, 3, "Standard Clean Divisibility")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{1, 3, 4}, 6, 2, "Greedy Failure 1 (Exp: 3+3=6)")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{1, 4, 5}, 12, 3, "Greedy Failure 2 (Exp: 4+4+4=12)")) passed++; else failed++;

        // --- 3. Edge Cases & Constraints ---
        if (assertTestCase(tester, new int[]{4, 6}, 9, -1, "No Combination Possible")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{5, 10}, 3, -1, "Amount Smaller Than Smallest Coin")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{5, 1, 2}, 11, 3, "Unsorted Denominations")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{10000}, 10000, 1, "Large Denomination Match")) passed++; else failed++;

        // --- 4. Stress & Large Input Tests ---
        if (assertTestCase(tester, new int[]{1}, 10000, 10000, "Deep Recursion / Large Amount")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{Integer.MAX_VALUE}, 2, -1, "Integer Overflow Prevention")) passed++; else failed++;
        
        // --- 5. Null Array Validation ---
        if (assertTestCase(tester, null, 5, -1, "Null Coins Array Object")) passed++; else failed++;
        if (assertTestCase(tester, new int[]{}, 5, -1, "Empty Coins Array")) passed++; else failed++;

        System.out.println("\n--------------------------------------");
        System.out.println("Test Results: " + passed + " Passed, " + failed + " Failed.");
        
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static boolean assertTestCase(CoinChangeTestRunner tester, 
    		int[] coins, int amount, int expected, String testName) {
        try {
            int actual = coinChange(coins, amount);
            if (actual == expected) {
                System.out.printf("[PASS] %s%n", testName);
                return true;
            } else {
                System.out.printf("[FAIL] %s | Input: coins=%s, amount=%d | Expected: %d, Got: %d%n", 
                                  testName, Arrays.toString(coins), amount, expected, actual);
                return false;
            }
        } catch (Exception e) {
            System.out.printf("[FAIL] %s | Threw unexpected exception: %s%n", testName, e.getMessage());
            return false;
        }
    }
}
