package com.oshanker.examples.graph;

import java.util.*;

public class GridEdgeWeightsDijkstra {

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

    public static class Point {
        int r, c;
        public Point(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    /**
     * @param numRows Total rows in the grid
     * @param numCols Total columns in the grid
     * @param horizontalEdges Weights of horizontal paths. Size: numRows x (numCols - 1)
     *                        horizontalEdges[r][c] is the weight between (r, c) and (r, c+1)
     * @param verticalEdges   Weights of vertical paths. Size: (numRows - 1) x numCols
     *                        verticalEdges[r][c] is the weight between (r, c) and (r+1, c)
     */
    public static void findShortestPathByEdges(int numRows, int numCols, int[][] horizontalEdges, int[][] verticalEdges) {
        
        // 1. Setup Distance tracking and Parent tracking matrices
        int[][] cheapestPathToCell = new int[numRows][numCols];
        for (int[] row : cheapestPathToCell) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        cheapestPathToCell[0][0] = 0; // Starting point itself costs 0 because the cells are free!

        Point[][] parent = new Point[numRows][numCols];

        PriorityQueue<ExplorerState> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(state -> state.totalCostSoFar)
        );
        minHeap.add(new ExplorerState(0, 0, 0));

        // Direction vectors: UP, DOWN, LEFT, RIGHT
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        boolean reachedEnd = false;

        while (!minHeap.isEmpty()) {
            ExplorerState current = minHeap.poll();
            int r = current.row;
            int c = current.col;
            int currentCost = current.totalCostSoFar;

            if (r == numRows - 1 && c == numCols - 1) {
                reachedEnd = true;
                break;
            }

            if (currentCost > cheapestPathToCell[r][c]) {
                continue;
            }

            for (int[] dir : directions) {
                int nextRow = r + dir[0];
                int nextCol = c + dir[1];

                // Boundary check
                if (nextRow >= 0 && nextRow < numRows && nextCol >= 0 && nextCol < numCols) {
                    int edgeWeight = -1;
                    /*
                     * 
The Solution: "Name the Edge after the Upper Row"

To store these edge weights cleanly in a 2D array, we establish a rule: A vertical edge is always indexed using the number of the top-most row it touches.
Let's look at how this handles movement in both directions (moving down vs. moving up):

Scenario A: Moving DOWN (From Row 1 to Row 2)

• Your current position: r = 1
• Your destination: nextRow = 2
• Math.min(1, 2) gives you 1.
• You lookup verticalEdges[1][c]. This is perfectly correct because the street connecting Row 1 and Row 2 is assigned to index 1.

Scenario B: Moving UP (From Row 2 to Row 1)

• Your current position: r = 2
• Your destination: nextRow = 1
• Math.min(2, 1) still gives you 1.
• You lookup verticalEdges[1][c].

Why is this ideal for interviews?

By using Math.min(), you completely eliminate the need to write messy, separate logic blocks for traveling backwards vs. traveling forwards. No matter whether you are climbing up or dropping down, Math.min(r, nextRow) always isolates the top-most row coordinate, pointing you seamlessly to the correct edge weight every single time.
This exact same logic applies to columns when moving horizontally: int minCol = Math.min(c, nextCol);.
                     */

                    // 2. CRITICAL STEP: Extract weight from the correct edge matrix based on movement
                    if (dir[0] == 0) { 
                        // Horizontal movement (Left / Right)
                        // The edge index corresponds to the smaller column index between the two points
                        int minCol = Math.min(c, nextCol);
                        edgeWeight = horizontalEdges[r][minCol];
                    } else { 
                        // Vertical movement (Up / Down)
                        // The edge index corresponds to the smaller row index between the two points
                        int minRow = Math.min(r, nextRow);
                        edgeWeight = verticalEdges[minRow][c];
                    }

                    int costToReachNeighbor = cheapestPathToCell[r][c] + edgeWeight;

                    // Relaxation
                    if (costToReachNeighbor < cheapestPathToCell[nextRow][nextCol]) {
                        cheapestPathToCell[nextRow][nextCol] = costToReachNeighbor;
                        parent[nextRow][nextCol] = new Point(r, c);
                        minHeap.add(new ExplorerState(nextRow, nextCol, costToReachNeighbor));
                    }
                }
            }
        }

        // 3. Path Reconstruction
        if (reachedEnd) {
            List<String> pathWithCosts = new ArrayList<>();
            int currR = numRows - 1;
            int currC = numCols - 1;

            while (currR != 0 || currC != 0) {
                pathWithCosts.add("(" + currR + "," + currC + ") [Cost: " + cheapestPathToCell[currR][currC] + "]");
                Point p = parent[currR][currC];
                currR = p.r;
                currC = p.c;
            }
            pathWithCosts.add("(0,0) [Cost: 0]");
            Collections.reverse(pathWithCosts);

            System.out.println("Absolute Minimum Cost: " + cheapestPathToCell[numRows - 1][numCols - 1]);
            System.out.println("\nOptimal Path Sequence with Edge Weights:");
            System.out.println(String.join(" -> ", pathWithCosts));
        } else {
            System.out.println("Destination unreachable.");
        }
    }

    public static void main(String[] args) {
        int numRows = 3;
        int numCols = 3;

        // Weights of horizontal connections between columns
        // (0,0) -[4]-> (0,1) -[1]-> (0,2)
        // (1,0) -[5]-> (1,1) -[1]-> (1,2)
        // (2,0) -[1]-> (2,1) -[1]-> (2,2)
        int[][] horizontalEdges = {
            {4, 1},
            {5, 1},
            {1, 1}
        };

        // Weights of vertical connections between rows
        // (0,0)         (0,1)         (0,2)
        //  [1]           [9]           [2]
        // (1,0)         (1,1)         (1,2)
        //  [1]           [9]           [3]
        // (2,0)         (2,1)         (2,2)
        int[][] verticalEdges = {
            {1, 9, 2},
            {1, 9, 3}
        };

        findShortestPathByEdges(numRows, numCols, horizontalEdges, verticalEdges);
    }
}
