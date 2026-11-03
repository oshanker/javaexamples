package com.oshanker.examples.graph;
import java.util.*;

public class MatrixDijkstraExplained {



	    // This class represents our position on the map and our current score/cost
	    public static class ExplorerState {
	        int row;
	        int col;
	        int totalCostSoFar; // The accumulated cost to reach this specific cell

	        public ExplorerState(int row, int col, int totalCostSoFar) {
	            this.row = row;
	            this.col = col;
	            this.totalCostSoFar = totalCostSoFar;
	        }
	    }

	    public static int findCheapestPath(int[][] grid) {
	        // 'grid' is a 2D array where grid[row][col] is the price/weight to step on that tile.
	        int totalRows = grid.length;
	        int totalCols = grid[0].length;

	        // 1. CREATE A RECORD BOOK FOR SHORTEST DISTANCES
	        // This table tracks the cheapest price found so far to reach any given coordinate.
	        int[][] cheapestPathToCell = new int[totalRows][totalCols];
	        for (int[] row : cheapestPathToCell) {
	            Arrays.fill(row, Integer.MAX_VALUE); // Start everyone at Infinity
	        }
	        
	        // The cost to start at the top-left tile (0,0) is simply the value written on that tile.
	        cheapestPathToCell[0][0] = grid[0][0];

	        // 2. SET UP THE MIN-HEAP (PRIORITY QUEUE)
	        // The heap acts as a smart queue that always hands us the explorer who has paid the LEAST money so far.
	        PriorityQueue<ExplorerState> minHeap = new PriorityQueue<>(
	            Comparator.comparingInt(state -> state.totalCostSoFar)
	        );
	        
	        // Put our starting position into the heap
	        minHeap.add(new ExplorerState(0, 0, grid[0][0]));

	        // 3. DEFINE MOVEMENT DIRECTIONS
	        // Instead of writing 4 separate if-statements, we use direction vectors:
	        // {-1, 0} = Up,    {1, 0} = Down,    {0, -1} = Left,    {0, 1} = Right
	        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

	        // 4. BEGIN EXPLORATION
	        while (!minHeap.isEmpty()) {
	            // Pull out the explorer who has the lowest cost route right now
	            ExplorerState current = minHeap.poll();
	            int r = current.row;
	            int c = current.col;
	            int currentCost = current.totalCostSoFar;

	            // EARLY EXIT: If this lowest-cost option happens to be the bottom-right corner, 
	            // Dijkstra guarantees this is the absolute cheapest way to get here. We are done!
	            if (r == totalRows - 1 && c == totalCols - 1) {
	            	System.out.println(Arrays.deepToString(cheapestPathToCell));
	                return currentCost;
	            }

	            // STALE PATH FILTER: If we already found a cheaper way to get to cell (r, c) 
	            // via a different path, ignore this older, more expensive heap entry.
	            if (currentCost > cheapestPathToCell[r][c]) {
	                continue;
	            }

	            // 5. LOOK AT THE 4 NEIGHBORING TILES
	            for (int[] dir : directions) {
	                int nextRow = r + dir[0];
	                int nextCol = c + dir[1];

	                // BOUNDARY CHECK: Make sure the neighbor is actually inside the map boundaries
	                if (nextRow >= 0 && nextRow < totalRows && nextCol >= 0 && nextCol < totalCols) {
	                    
	                    // HERE IS WHERE THE GRID VARIABLE IS USED AS A WEIGHT:
	                    // The cost to move to the neighbor is: (My current total cost) + (The value written on the neighbor tile)
	                    int costToReachNeighbor = cheapestPathToCell[r][c] + grid[nextRow][nextCol];

	                    // RELAXATION: Is this new route cheaper than any route we previously found for that neighbor?
	                    if (costToReachNeighbor < cheapestPathToCell[nextRow][nextCol]) {
	                        // Update our record book with the new, cheaper price
	                        cheapestPathToCell[nextRow][nextCol] = costToReachNeighbor;
	                        
	                        // Send a new explorer state into the heap to explore further from that tile
	                        minHeap.add(new ExplorerState(nextRow, nextCol, costToReachNeighbor));
	                    }
	                }
	            }
	        }
	        
	        

	        return -1; // Return -1 if the bottom-right corner is physically blocked/unreachable
	    }

	    public static void main(String[] args) {
	        // A sample 3x3 map
	        int[][] mapGrid = {
	            {1, 3, 1},
	            {1, 5, 1},
	            {4, 2, 1}
	        };

	        int result = findCheapestPath(mapGrid);
	        System.out.println("The absolute cheapest total cost to cross the grid is: " + result);
	        
	        /* 
	         How the math worked using the 'mapGrid' values:
	         Start at (0,0) -> Cost = 1
	         Move to (0,1)  -> Cost = 1 + 3 = 4
	         Move to (0,2)  -> Cost = 4 + 1 = 5
	         Move to (1,2)  -> Cost = 5 + 1 = 6
	         Move to (2,2)  -> Cost = 6 + 1 = 7 (This is the bottom-right destination!)
	        */
	    }
	}
