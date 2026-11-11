package com.oshanker.examples.graph.mst;

import java.util.*;

public class MinimumSpanningTreeTest {

    public static void main(String[] args) {
        testStandardGraph();
        testGraphWithSelfLoopsAndParallelEdges();
        testDisconnectedGraph();
        testSingleVertexGraph();

        System.out.println("All tests passed successfully!");
    }

    // Test 1: Standard connected graph (4 vertices, expected MST weight = 19)
    private static void testStandardGraph() {
        int vertices = 4;
        List<MinimumSpanningTreeUnionFind.Edge> edges = Arrays.asList(
            new MinimumSpanningTreeUnionFind.Edge(0, 1, 10),
            new MinimumSpanningTreeUnionFind.Edge(0, 2, 6),
            new MinimumSpanningTreeUnionFind.Edge(0, 3, 5),
            new MinimumSpanningTreeUnionFind.Edge(1, 3, 15),
            new MinimumSpanningTreeUnionFind.Edge(2, 3, 4)
        );

        List<MinimumSpanningTreeUnionFind.Edge> mst = MinimumSpanningTreeUnionFind.findMST(vertices, edges);

        assertEquals(3, mst.size(), "Standard graph should have V - 1 = 3 edges");
        assertEquals(19, calculateTotalWeight(mst), "Total MST weight should be 19");
    }

    // Test 2: Self-loops (0 -> 0) and duplicate parallel edges with different weights
    private static void testGraphWithSelfLoopsAndParallelEdges() {
        int vertices = 3;
        List<MinimumSpanningTreeUnionFind.Edge> edges = Arrays.asList(
            new MinimumSpanningTreeUnionFind.Edge(0, 0, 1),  // Self-loop (must be ignored)
            new MinimumSpanningTreeUnionFind.Edge(0, 1, 10), // Heavier parallel edge
            new MinimumSpanningTreeUnionFind.Edge(0, 1, 3),  // Lighter parallel edge (should be chosen)
            new MinimumSpanningTreeUnionFind.Edge(1, 2, 4),
            new MinimumSpanningTreeUnionFind.Edge(0, 2, 8)
        );

        List<MinimumSpanningTreeUnionFind.Edge> mst = MinimumSpanningTreeUnionFind.findMST(vertices, edges);

        assertEquals(2, mst.size(), "MST should have 2 edges");
        assertEquals(7, calculateTotalWeight(mst), "Total MST weight should be 3 + 4 = 7");
    }

    // Test 3: Disconnected graph produces a Minimum Spanning Forest (< V - 1 edges)
    private static void testDisconnectedGraph() {
        int vertices = 4;
        List<MinimumSpanningTreeUnionFind.Edge> edges = Arrays.asList(
            new MinimumSpanningTreeUnionFind.Edge(0, 1, 5), // Component 1
            new MinimumSpanningTreeUnionFind.Edge(2, 3, 2)  // Component 2 (isolated from 0 and 1)
        );

        List<MinimumSpanningTreeUnionFind.Edge> mst = MinimumSpanningTreeUnionFind.findMST(vertices, edges);

        assertEquals(2, mst.size(), "Disconnected graph of 2 components should have 2 edges");
        assertEquals(7, calculateTotalWeight(mst), "Total forest weight should be 7");
    }

    // Test 4: Edge case with 1 vertex and 0 edges
    private static void testSingleVertexGraph() {
        int vertices = 1;
        List<MinimumSpanningTreeUnionFind.Edge> edges = Collections.emptyList();

        List<MinimumSpanningTreeUnionFind.Edge> mst = MinimumSpanningTreeUnionFind.findMST(vertices, edges);

        assertEquals(0, mst.size(), "Single vertex graph should have 0 edges");
        assertEquals(0, calculateTotalWeight(mst), "Total weight should be 0");
    }

    // Helper method to sum edge weights in the returned MST
    private static int calculateTotalWeight(List<MinimumSpanningTreeUnionFind.Edge> mst) {
        int total = 0;
        for (MinimumSpanningTreeUnionFind.Edge edge : mst) {
            total += edge.weight;
        }
        return total;
    }

    // Simple assertion helper (works without needing the -ea JVM flag)
    private static void assertEquals(int expected, int actual, 
    		String message) {
        System.out.println(message);
    	if (expected != actual) {
            throw new AssertionError(message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }
}
