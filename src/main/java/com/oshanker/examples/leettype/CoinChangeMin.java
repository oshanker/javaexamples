package com.oshanker.examples.leettype;

import java.util.Arrays;

public class CoinChangeMin {
	/*
	To optimize the memory usage of the Bottom-Up DP approach 
	down to a minimal footprint, we look at the array lookups:
	 to compute dp[i], 
	we only need to look backward at dp[i - coin].
Unlike the 0/1 Knapsack problem where we can drop a matrix 
dimension to achieve exactly O(capacity) space, 
the Coin Change problem (an unbounded knapsack variant) 
already operates on a single-dimensional array of 
size amount + 1.
However, we can dramatically optimize the memory 
if the maximum coin denomination is small compared
 to a massive target amount. For instance, 
 if your highest coin value is 5, you never need to
  look back further than 5 steps!
Instead of allocating an array of size 1,000,000 
for a million-dollar target, 
we can use a Sliding Window / Circular Array 
optimized down to a fixed size equal to the 
maximum coin value + 1.
	 */

	public static int coinChange(int[] coins, int amount) {
        // Null protection check
        if (coins == null || amount < 0) return -1;
        if (amount == 0) return 0;
        int[] dps = new int[amount + 1];
        
        // Fill the array with a value representing infinity (impossible state)
        // We use amount + 1 because the max possible coins needed would be 'amount' (all 1s)
        Arrays.fill(dps, amount + 1);
        // Clone to avoid side effects mutating the original array passed by the caller
        int[] sortedCoins = coins.clone();
        Arrays.sort(sortedCoins);
        
        // Base case: 0 coins are needed to make an amount of 0
        dps[0] = 0;
        
        // Loop through all amounts from 1 to the target amount
        for (int i = 1; i <= amount; i++) {
            // Check each coin denomination
            for (int coin : sortedCoins) {
                // If the coin is smaller than or equal to the current amount
                if (coin > 0 && i - coin >= 0) {
                    dps[i] = Math.min(dps[i], dps[i - coin] + 1);
                }
                // If the current coin exceeds the target sub-problem,
                // all remaining coins are guaranteed to be larger. Break early!
                if (coin > i) {
                    break; 
                }
             }
        }
        
        // If dp[amount] is still greater than amount, it means the total was unreachable
        return dps[amount] > amount ? -1 : dps[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        int amount = 11;
        System.out.println("Minimum coins needed: " + coinChange(coins, amount)); // Output: 3 (5 + 5 + 1)
    }
}
