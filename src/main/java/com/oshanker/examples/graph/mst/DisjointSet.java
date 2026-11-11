package com.oshanker.examples.graph.mst;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *  Disjoint Set (Union-Find) data structure to detect cycles efficiently.
 *  Nodes are represented by ints from 0 to size-1.
 */
public class DisjointSet {
	int[] parent;
	// height - 1
	int[] rank;
	int size;

	public DisjointSet(int vertices) {
		parent = new int[vertices];
		rank = new int[vertices];
		size = vertices;
		// Initially, every vertex is its own parent (separate component)
		for (int i = 0; i < vertices; i++) {
			parent[i] = i;
			rank[i] = 0;
		}
	}

	/**
	 *  Find the root representative of a vertex with path compression
	 * @param vertex
	 * @return
	 */
	public int find(int vertex) {
		if (parent[vertex] != vertex) {
			parent[vertex] = find(parent[vertex]); // Path compression
		}
		return parent[vertex];
	}

	public List<Integer> findPath(int vertex) {
		List<Integer> path = new LinkedList<>();
		if(vertex < 0 || vertex >= size) {
			System.out.printf("invalid vertex %d%n", vertex);
			return path;
		}
		path.add(vertex);
		int node = vertex;
		while (parent[node] != node) {
			int parentOfNode = parent[node]; 
			path.add(parentOfNode);
			node = parentOfNode;
		}
		Collections.reverse(path);
		return path;
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

    public static void main(String[] args) {
    	System.out.println("==  ==");
        DisjointSet ds = new DisjointSet(5);

        // Merge two rank-0 trees (0 and 1) -> root0 becomes parent, rank[0] becomes 1
        ds.union(0, 1);
        System.out.println(ds.findPath(1)  + " Root of 1 should become 0");
        System.out.println(ds.rank[0]  + " Rank of 0 should increment to 1");
        System.out.println(ds.rank[1]  + " Rank of child 1 should stay 0");

        // Merge a rank-0 tree (2) into a rank-1 tree (0) -> rank[0] should NOT increase
        ds.union(2, 0);
        System.out.println(ds.findPath(2)  + " 2 should attach directly under taller tree root 0");
        System.out.println(ds.rank[0]  + " Rank of 0 should remain 1 when absorbing a shorter tree");
        System.out.println(ds.rank[2]  + " Rank of child 2 should stay 0");

        System.out.println(ds.findPath(3)  + " 3 should be root");
        System.out.println(ds.findPath(5)  + " invalid input");

        ds.union(3, 4);
        ds.union(3, 0);
        System.out.println(ds.findPath(2)  + " 2 should attach  under taller tree root 3");
        System.out.println("rank  " + Arrays.toString(ds.rank));
        System.out.println("Done");
    }
}