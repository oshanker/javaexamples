package com.oshanker.examples.leettype;

import java.util.Arrays;

public class CoinChangeMin {
    public static int coinChange(int[] coins, int amount) {
        // dp[i] will store the minimum coins needed for amount i
        int[] dp = new int[amount + 1];
        
        // Fill the array with a value representing infinity (impossible state)
        // We use amount + 1 because the max possible coins needed would be 'amount' (all 1s)
        Arrays.fill(dp, amount + 1);
        
        // Base case: 0 coins are needed to make an amount of 0
        dp[0] = 0;
        
        // Loop through all amounts from 1 to the target amount
        for (int i = 1; i <= amount; i++) {
            // Check each coin denomination
            for (int coin : coins) {
                // If the coin is smaller than or equal to the current amount
                if (i - coin >= 0) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        
        // If dp[amount] is still greater than amount, it means the total was unreachable
        return dp[amount] > amount ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        int amount = 11;
        System.out.println("Minimum coins needed: " + coinChange(coins, amount)); // Output: 3 (5 + 5 + 1)
    }
}
