package com.oshanker.examples.graph.mst;
import java.util.*;


public class MinimumSpanningTreeUnionFind {
	/*
You can read more about variations of this approach in the 
GeeksforGeeks Kruskal's MST Guide 
https://www.geeksforgeeks.org/dsa/kruskals-minimum-spanning-tree-algorithm-greedy-algo-2/ 
or 
compare it with priority-queue implementations in the 
USACO Guide on Minimum Spanning Trees.
https://usaco.guide/gold/mst?lang=java 

https://www.geeksforgeeks.org/dsa/difference-between-prims-and-kruskals-algorithm-for-mst/
	 */

	// Class to represent a weighted, undirected edge in the graph
	static class Edge implements Comparable<Edge> {
		int source;
		int destination;
		int weight;

		public Edge(int source, int destination, int weight) {
			this.source = source;
			this.destination = destination;
			this.weight = weight;
		}

		// Sort edges in ascending order based on their weight
		@Override
		public int compareTo(Edge other) {
			return Integer.compare(this.weight, other.weight);
		}
	}

	static final boolean DEBUG = true;
	
	// Method to construct and print the Minimum Spanning Tree
	public static List<Edge>  findMST(int vertices, List<Edge> edges) {
		// Step 1: Sort all edges from lowest to highest weight
		Collections.sort(edges);

		DisjointSet disjointSet = new DisjointSet(vertices);
		List<Edge> mst = new ArrayList<>();
		// Step 2: Iterate through sorted edges and pick the smallest ones
		for (Edge edge : edges) {
			int rootSource = disjointSet.find(edge.source);
			int rootDest = disjointSet.find(edge.destination);

			// If adding this edge does not form a cycle, include it in the MST
			if (rootSource != rootDest) {
				mst.add(edge);
				disjointSet.union(rootSource, rootDest);
			} else if (DEBUG ){
				System.out.printf("cycle for edge %d -- %d root %d %n" , edge.source ,  edge.destination,
						rootSource);
			}

			// Stop early once we have (V - 1) edges in the MST
			if (mst.size() == vertices - 1) {
				break;
			}
		}
        return mst;
	}

	public static void main(String[] args) {
		int vertices = 4; // Number of vertices (0 to 3)
		List<Edge> edges = new ArrayList<>();

		// Add edges: (source, destination, weight)
		edges.add(new Edge(1, 0, 10));
		edges.add(new Edge(0, 2, 6));
		edges.add(new Edge(3, 0, 5));
		edges.add(new Edge(1, 3, 15));
		edges.add(new Edge(2, 3, 4));

		// Run Kruskal's algorithm
		List<Edge> mst = findMST(vertices, edges);

		// Print the resulting Minimum Spanning Tree
		int totalWeight = 0;
		System.out.println("Edges in the Minimum Spanning Tree:");
		for (Edge edge : mst) {
			System.out.println(edge.source + " -- " + edge.destination + " (Weight: " + edge.weight + ")");
			totalWeight += edge.weight;
		}
		System.out.println("Total MST Weight: " + totalWeight);

	}
}
