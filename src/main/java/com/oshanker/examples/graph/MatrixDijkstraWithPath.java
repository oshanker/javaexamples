package com.oshanker.examples.graph;

import java.util.*;

public class MatrixDijkstraWithPath {

    // Helper class to store 2D coordinates cleanly
    public static class Point {
        int r, c;
        public Point(int r, int c) {
            this.r = r;
            this.c = c;
        }
        @Override
        public String toString() {
            return "(" + r + "," + c + ")";
        }
    }

    public static class ExplorerState {
        int row;
        int col;
        int totalCostSoFar;

        public ExplorerState(int row, int col, int totalCostSoFar) {
            this.row = row;
            this.col = col;
            this.totalCostSoFar = totalCostSoFar;
        }
    }

    public static void findAndPrintCheapestPath(int[][] grid) {
        int totalRows = grid.length;
        int totalCols = grid[0].length;

        int[][] cheapestPathToCell = new int[totalRows][totalCols];
        for (int[] row : cheapestPathToCell) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        cheapestPathToCell[0][0] = grid[0][0];

        // NEW: 2D array of Points to track the parent cell for every coordinate
        Point[][] parent = new Point[totalRows][totalCols];

        PriorityQueue<ExplorerState> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(state -> state.totalCostSoFar)
        );
        minHeap.add(new ExplorerState(0, 0, grid[0][0]));

        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

        boolean reachedEnd = false;

        while (!minHeap.isEmpty()) {
            ExplorerState current = minHeap.poll();
            int r = current.row;
            int c = current.col;
            int currentCost = current.totalCostSoFar;

            // Early exit optimization
            if (r == totalRows - 1 && c == totalCols - 1) {
                reachedEnd = true;
                break; 
            }

            if (currentCost > cheapestPathToCell[r][c]) {
                continue;
            }

            for (int[] dir : directions) {
                int nextRow = r + dir[0];
                int nextCol = c + dir[1];

                if (nextRow >= 0 && nextRow < totalRows && nextCol >= 0 && nextCol < totalCols) {
                    int costToReachNeighbor = cheapestPathToCell[r][c] + grid[nextRow][nextCol];

                    if (costToReachNeighbor < cheapestPathToCell[nextRow][nextCol]) {
                        cheapestPathToCell[nextRow][nextCol] = costToReachNeighbor;
                        
                        // NEW: Record that we reached (nextRow, nextCol) coming from (r, c)
                        parent[nextRow][nextCol] = new Point(r, c);
                        
                        minHeap.add(new ExplorerState(nextRow, nextCol, costToReachNeighbor));
                    }
                }
            }
        }

        if (reachedEnd) {
        	System.out.println("cheapestPathToCell" + 
                     Arrays.deepToString(cheapestPathToCell));

            // Reconstruct path backtracking from bottom-right to top-left
            List<String> pathWithCosts = new ArrayList<>();
            List<Point> path = new ArrayList<>();
            int currR = totalRows - 1;
            int currC = totalCols - 1;
            
            while (currR != 0 || currC != 0) {
                int costAtThisStep = cheapestPathToCell[currR][currC];
                pathWithCosts.add("(" + currR + "," + currC + ") [Cost: " + costAtThisStep + "]");
                path.add(new Point(currR, currC));
                Point prev = parent[currR][currC];
                currR = prev.r;
                currC = prev.c;
            }
            path.add(new Point(0, 0)); // Add the start coordinate
            Collections.reverse(path); // Flip to read Start -> Finish
            // Don't forget to append the starting node's details
            pathWithCosts.add("(0,0) [Cost: " + cheapestPathToCell[0][0] + "]");
            
            // Flip the list to read chronologically from Start -> Finish
            Collections.reverse(pathWithCosts);


            // Print the clean outputs
            System.out.println("Absolute Minimum Cost: " + cheapestPathToCell[totalRows - 1][totalCols - 1]);
            System.out.println("\nOptimal Coordinate Path Sequence:");
            System.out.println(path.toString());//.replace(",", " ->"));
            System.out.println("\nOptimal Path Sequence with Costs:");
            System.out.println(String.join(" -> ", pathWithCosts));
       } else {
            System.out.println("Destination is completely unreachable.");
        }
    }

    public static void main(String[] args) {
        int[][] mapGrid = {
            {1, 3, 1},
            {1, 5, 1},
            {4, 2, 1}
        };

    	System.out.println("mapGrid" );//+ 
                //Arrays.deepToString(mapGrid));
    	for (int i = 0; i < mapGrid.length; i++) {
    		System.out.println(Arrays.toString(mapGrid[i]));
			
		}
        findAndPrintCheapestPath(mapGrid);
    }
}

