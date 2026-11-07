package com.oshanker.examples.leettype;

public class CoinChangeWays {
    public static int change(int amount, int[] coins) {
        // dp[i] will store the number of ways to make change for amount i
        int[] dp = new int[amount + 1];
        
        // Base case: There is 1 way to make an amount of 0 (using no coins)
        dp[0] = 1;
        
        // Loop through each coin denomination first to avoid counting duplicate combinations
        for (int coin : coins) {
            // Update the dp array for all amounts from the current coin's value to the target amount
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i - coin];
            }
        }
        
        return dp[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        int amount = 5;
        System.out.println("Total distinct ways: " + change(amount, coins)); // Output: 4
        // Ways: {1,1,1,1,1}, {1,1,1,2}, {1,2,2}, {5}
    }
}
