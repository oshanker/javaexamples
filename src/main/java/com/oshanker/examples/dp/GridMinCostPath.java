package com.oshanker.examples.dp;

/*To print the actual path taken through the grid alongside calculating the minimum cost, we must revert to using a 2D DP table (or a parent tracking table). This is because we need to look backwards from the final destination (m-1, n-1) to trace our steps back to the start (0,0).
Here is the complete Java implementation that calculates the minimum cost, tracks the choices, and prints the path steps sequentially.

Java Implementation
*/


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridMinCostPath {
    
    public static void printMinCostAndPath(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            System.out.println("Empty Grid");
            return;
        }
        
        int m = grid.length;
        int n = grid[0].length;
        
        // 2D DP matrix to store the minimum cost to reach each cell
        int[][] dp = new int[m][n];
        
        // 1. Fill out the DP Table
        dp[0][0] = grid[0][0];
        
        // Fill first row (only coming from left)
        for (int j = 1; j < n; j++) {
            dp[0][j] = dp[0][j - 1] + grid[0][j];
        }
        
        // Fill first column (only coming from above)
        for (int i = 1; i < m; i++) {
            dp[i][0] = dp[i - 1][0] + grid[i][0];
        }
        
        // Fill the rest of the grid
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = grid[i][j] + Math.min(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        
        System.out.println("Minimum Cost: " + dp[m - 1][n - 1]);
        
        // 2. Backtrack to reconstruct the path
        List<String> path = new ArrayList<>();
        int i = m - 1;
        int j = n - 1;
        
        while (i > 0 || j > 0) {
            path.add(String.format("(%d, %d) [val: %d]", i, j, grid[i][j]));
            
            if (i == 0) {
                // If we are on the top row, we can only move left
                j--;
            } else if (j == 0) {
                // If we are on the leftmost column, we can only move up
                i--;
            } else {
                // Look at upper neighbor vs left neighbor; go with the cheaper DP path
                if (dp[i - 1][j] < dp[i][j - 1]) {
                    i--;
                } else {
                    j--;
                }
            }
        }
        
        // Add the starting cell
        path.add(String.format("(0, 0) [val: %d]", grid[0][0]));
        
        // Reverse the list since we tracked it backwards from the destination
        Collections.reverse(path);
        
        // Print the final path sequence
        System.out.println("Path taken: " + String.join(" -> ", path));
    }

    public static void main(String[] args) {
        int[][] matrix = {
            {1, 3, 1},
            {1, 5, 1},
            {4, 2, 1}
        };
        
        printMinCostAndPath(matrix);
    }
}
/*
Use code with caution.

Output

text
Minimum Cost: 7
Path taken: (0, 0) [val: 1] -> (0, 1) [val: 3] -> (0, 2) [val: 1] -> (1, 2) [val: 1] -> (2, 2) [val: 1]
Use code with caution.

How Backtracking Works

1. Start at the End: We position our pointers i and j at the bottom-right cell (m-1, n-1).
2. Compare Neighbors: To figure out which cell led to our current state optimally, we compare dp[i-1][j] (the cell directly above) and dp[i][j-1] (the cell directly to the left).
3. Move to the Lowest DP Value: We step into the cell that had the smaller aggregate weight score.
4. Reverse the Output: Because we walked backwards from the destination to the starting line, we flip the collected path array around to display it chronological order.
Let me know if you would like to:
• Format the path output differently (like printing directional steps "Right -> Right -> Down" instead of coordinates).
• See how to modify this tracking behavior if diagonal moves are permitted.
public class GridMinCostPath {

}
*/
