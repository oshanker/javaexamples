package com.oshanker.examples.leettype;

import static com.oshanker.examples.leettype.CoinChangeWays.change;
public class CoinChangeWaysTest {

	    // ==========================================
	    // 2. Simple Test Suite Runner
	    // ==========================================
	    public static void main(String[] args) {
	        System.out.println("--- Starting Coin Change Ways Tests ---");
	        
	        testStandardCase();
	        testImpossibleChange();
	        testTargetZero();
	        testSingleDenomination();
	        testEmptyAndNullCoins();
	        
	        System.out.println("--- Tests Completed Successfully! ---");
	    }

	    // ==========================================
	    // 3. Individual Test Cases
	    // ==========================================
	    
	    private static void testStandardCase() {
	        int[] coins = {1, 2, 5};
	        int target = 5;
	        int expected = 4; // Ways: {1,1,1,1,1}, {1,1,1,2}, {1,2,2}, {5}
	        
	        int actual = change(target, coins);
	        assertResult("Standard Case (Target 5 with {1,2,5})", expected, actual);
	    }

	    private static void testImpossibleChange() {
	        int[] coins = {2};
	        int target = 3;
	        int expected = 0;
	        
	        int actual = change(target, coins);
	        assertResult("Impossible Change (Target 3 with only {2})", expected, actual);
	    }

	    private static void testTargetZero() {
	        int[] coins = {1, 2, 3};
	        int target = 0;
	        int expected = 1; // 1 way: choosing no coins
	        
	        int actual = change(target, coins);
	        assertResult("Target Zero (Target 0 with {1,2,3})", expected, actual);
	    }

	    private static void testSingleDenomination() {
	        int[] coins = {10};
	        int target = 30;
	        int expected = 1; // 1 way: {10, 10, 10}
	        
	        int actual = change(target, coins);
	        assertResult("Single Denomination (Target 30 with {10})", expected, actual);
	    }

	    private static void testEmptyAndNullCoins() {
	        int target = 10;
	        
	        int actualEmpty = change(target, new int[]{});
	        assertResult("Empty Coins Array", 0, actualEmpty);
	        
	        int actualNull = change(target, null);
	        assertResult("Null Coins Reference", 0, actualNull);
	        
	        actualEmpty = change(0, new int[]{});
	        assertResult("0  target, Empty Coins Array", 1, actualEmpty);
	        
	        actualNull = change(0, null);
	        assertResult("Null Coins Reference target 0", 1, actualNull);
	    }

	    // Helper method to verify values and report errors plainly
	    private static void assertResult(String testName, int expected, int actual) {
	        if (actual == expected) {
	            System.out.println("✅ PASS: " + testName);
	        } else {
	            System.out.println("❌ FAIL: " + testName);
	            System.out.println("   Expected: " + expected + ", but got: " + actual);
	            // Throws a runtime error to immediately halt execution if a test fails
	            throw new RuntimeException("Test failed: " + testName);
	        }
	    }
	}
