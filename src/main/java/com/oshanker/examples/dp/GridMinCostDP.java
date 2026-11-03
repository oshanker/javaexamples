package com.oshanker.examples.dp;

public class GridMinCostDP {

    
    public static int minPathSum(int[][] grid) {
        // Edge case: if grid is empty, return 0
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            return 0;
        }
        
        int m = grid.length;
        int n = grid[0].length;
        
        // DP array to store minimum cost for the current row
        int[] dp = new int[n];
        
        // Base case: initialize the top-left starting cell
        dp[0] = grid[0][0];
        
        // Base case: fill out the rest of the first row (can only move right)
        for (int j = 1; j < n; j++) {
            dp[j] = dp[j - 1] + grid[0][j];
        }
        
        // Process the remaining rows
        for (int i = 1; i < m; i++) {
            // Update the first element of the current row (can only move down)
            dp[0] += grid[i][0];
            
            // Update the rest of the columns for the current row
            for (int j = 1; j < n; j++) {
                // dp[j] (before update) holds the value from the cell directly above (i-1, j)
                // dp[j-1] holds the value from the cell directly to the left (i, j-1)
                dp[j] = grid[i][j] + Math.min(dp[j], dp[j - 1]);
            }
        }
        
        // The last element contains the absolute minimum path cost
        return dp[n - 1];
    }

    public static void main(String[] args) {
        int[][] matrix = {
            {1, 3, 1},
            {1, 5, 1},
            {4, 2, 1}
        };
        
        int result = minPathSum(matrix);
        System.out.println("Minimum Cost: " + result); 
        // Output: 7 (Path: 1 -> 3 -> 1 -> 1 -> 1)
    }
}
