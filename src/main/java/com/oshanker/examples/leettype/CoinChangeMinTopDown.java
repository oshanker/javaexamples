package com.oshanker.examples.leettype;

public class CoinChangeMinTopDown {

    // Main entry point for the top-down approach
    public static int coinChange(int[] coins, int amount) {
        // Boundary checks and null protection
        if (coins == null || amount < 0) return -1;
        if (amount == 0) return 0;
        
        // memo[i] will store the min coins needed to form amount 'i'
        // Initialize with 0. 0 means uncomputed, -1 means impossible.
        int[] memo = new int[amount + 1];
        
        return coinChangeHelper(coins, amount, memo, 0);
    }

    private static int coinChangeHelper(int[] coins, 
    		int rem, int[] memo, int breakout) {
        // Base cases
        if (rem < 0) return -1;
        if (rem == 0) return 0;
        
        // If already computed, return the cached result
        if (memo[rem] != 0) return memo[rem];
        
        int minCoins = Integer.MAX_VALUE;

        for (int coin : coins) {
            // Check for coin > 0 to avoid infinite loops if an array includes 0, 
            // and skip if the coin value is larger than the remaining amount
            if(breakout > 3000) {
            	System.out.printf(
            			"too deep %d, breaking coin %d rem %d %n", 
            			breakout, coin, rem);
            	break;
            }
        	if (coin > 0 && coin <= rem) {
                int res = coinChangeHelper(coins, 
                		rem - coin, memo, ++breakout);
                
                // If a valid combination was found, track the minimum path
                if (res >= 0 && res < minCoins) {
                    minCoins = 1 + res;
                }
            }
        }
        
        // Cache the result into the memoization table
        memo[rem] = (minCoins == Integer.MAX_VALUE) ? -1 : minCoins;
        return memo[rem];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        int amount = 11;
        System.out.println("Minimum coins needed: " 
        + coinChange(coins, amount)); // Output: 3 (5 + 5 + 1)
    }
    
    /*

Bottom-Up vs. Top-Down Tradeoffs

While both execute at \(O(C \times A)\) time:
1. Bottom-Up DP (Iterative): Sweeps sequentially through the 
array. It is faster in practice because it bypasses CPU 
overhead from function call stacks 
and avoids StackOverflowError on massive targets.
2. Top-Down (Memoization): Evaluates only the specific numbers 
reachable via coin subtractions. It can be faster 
if the target amount is huge but only a tiny, sparse subset 
of combinations are valid.
Would you like to see how the bottom-up algorithm 
can be optimized to sort the coins array first, 
allowing it to break early out of the inner loop and 
run even faster?
     */
}
