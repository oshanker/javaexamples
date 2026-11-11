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

	// Disjoint Set (Union-Find) data structure to detect cycles efficiently
	static class DisjointSet {
		int[] parent;
		int[] rank;

		public DisjointSet(int vertices) {
			parent = new int[vertices];
			rank = new int[vertices];
			// Initially, every vertex is its own parent (separate component)
			for (int i = 0; i < vertices; i++) {
				parent[i] = i;
				rank[i] = 0;
			}
		}

		// Find the root representative of a vertex with path compression
		public int find(int vertex) {
			if (parent[vertex] != vertex) {
				parent[vertex] = find(parent[vertex]); // Path compression
			}
			return parent[vertex];
		}

		/**
		 * Merges the two disjoint sets containing vertex1 and vertex2 into a single set.
		 *
		 * This method uses "Union by Rank" to prevent the tree from becoming tall and skewed
		 * (like a linked list), which keeps future find() operations nearly constant time, O(α(V)).
		 *
		 * @param vertex1 A vertex belonging to the first set.
		 * @param vertex2 A vertex belonging to the second set.
		 */
		public void union(int vertex1, int vertex2) {
			// Step 1: Locate the root representatives (top-level parents) of both vertices
			int root1 = find(vertex1);
			int root2 = find(vertex2);

			// Step 2: Only merge if they belong to different sets (different roots).
			// If root1 == root2, they are already connected, and merging would create a cycle.
			if (root1 != root2) {

				// Step 3: Attach the shorter tree under the root of the taller tree.
				// 'rank' acts as an upper bound on the height of each tree.
				if (rank[root1] < rank[root2]) {
					// root2's tree is taller, so make root2 the parent of root1.
					// The overall height of root2's tree does not increase.
					parent[root1] = root2;
				} else if (rank[root1] > rank[root2]) {
					// root1's tree is taller, so make root1 the parent of root2.
					// The overall height of root1's tree does not increase.
					parent[root2] = root1;
				} else {
					// Both trees have the exact same rank (height).
					// Arbitrarily make root1 the parent of root2, and increment root1's rank
					// because attaching an equal-height tree increases the total height by 1.
					parent[root2] = root1;
					rank[root1]++;
				}
			}
		}
	}


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
		edges.add(new Edge(0, 1, 10));
		edges.add(new Edge(0, 2, 6));
		edges.add(new Edge(0, 3, 5));
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
