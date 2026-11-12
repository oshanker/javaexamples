package com.oshanker.examples.graph.mst;

import java.util.*;

import com.oshanker.examples.graph.mst.MinimumSpanningTreeUnionFind.Edge;

public class PrimsAlgorithm {
	/*
undirected, weighted graph
The most efficient way to implement Prim's algorithm in Java 
is by using an adjacency list and a PriorityQueue (min-heap). 
This greedy approach starts from an arbitrary node and 
repeatedly adds the lowest-weight edge that connects
 a visited vertex to an unvisited vertex until all nodes are included.

Key Details & Complexity

• Time Complexity. Runs in \(O(E \log V)\) time, where V is the 
number of vertices and E is the number of edges. 
Each edge insertion and extraction from the Java PriorityQueue 
takes \(O(\log E)\) time, which simplifies to \(O(\log V)\).
• Space Complexity. Requires O(V + E) space to store the 
adjacency list, the visited boolean array, and 
the elements inside the priority queue.

• Dense Graphs Alternative. 
If your graph is represented as an adjacency matrix instead of 
an adjacency list, you can omit the PriorityQueue and scan arrays 
linearly in O(V²) time. 
For further reading and visual step-throughs, 
see the GeeksforGeeks Prim's Algorithm Guide 
https://www.geeksforgeeks.org/java/prims-algorithm-with-a-java-implementation/ 
or 
the W3Schools DSA Prim's Tutorial.
https://www.w3schools.com/dsa/dsa_algo_mst_prim.php 
	 */

    // Represents a connection to a neighbor vertex with an edge weight
    static class Edge {
        int target;
        int weight;

        Edge(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    // Represents an entry in the PriorityQueue tracking the parent node
    static class Node implements Comparable<Node> {
        int vertex;
        int parent;
        int weight;

        Node(int vertex, int parent, int weight) {
            this.vertex = vertex;
            this.parent = parent;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    public static List<com.oshanker.examples.graph.mst.MinimumSpanningTreeUnionFind.Edge> findMST(int vertices, List<List<Edge>> adj) {
        // Tracks which vertices have already been finalized and added to the MST.
        // Using this array prevents cycles and ensures we don't process a node twice.
        boolean[] visited = new boolean[vertices];
        
        PriorityQueue<Node> pq = new PriorityQueue<>();
        List<MinimumSpanningTreeUnionFind.Edge>  mstEdges = new ArrayList<>();
        // Start from vertex 0 with no parent (-1) and 0 weight
        pq.add(new Node(0, -1, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int u = current.vertex;

            // Because multiple edges leading to the same vertex can be pushed into the
            // PriorityQueue before that vertex is finalized, a stale (heavier) entry
            // might be popped later. Skip it if the vertex is already in the MST.
            if (visited[u]) {
                continue;
            }

            // Permanently include vertex 'u' in the MST now that we've found
            // the minimum-weight edge connecting it to our growing tree.
            visited[u] = true;
            // Record the edge (skip the starting node which has parent -1)
            if (current.parent != -1) {
            	MinimumSpanningTreeUnionFind.Edge mstEdge = 
            			new MinimumSpanningTreeUnionFind.Edge(current.parent, u, current.weight);
                mstEdges.add(mstEdge );
            }

            // Explore all neighbors of 'u' and add edges leading to unvisited vertices.
            // Checking '!visited[edge.target]' avoids pushing redundant edges back into
            // the MST (which would create a cycle).
            for (Edge edge : adj.get(u)) {
                if (!visited[edge.target]) {
                    pq.add(new Node(edge.target, u, edge.weight));
                }
            }
        }

        return mstEdges;
    }

    // Helper method to add an undirected edge
    static void addEdge(List<List<Edge>> adj, int u, int v, int weight) {
        adj.get(u).add(new Edge(v, weight));
        adj.get(v).add(new Edge(u, weight));
    }

    public static void main(String[] args) {
        int vertices = 4;
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adj.add(new ArrayList<>());
        }

        // Constructing a sample weighted undirected graph
        addEdge(adj, 0, 1, 10);
        addEdge(adj, 0, 3, 5);
        addEdge(adj, 0, 2, 6);
        addEdge(adj, 1, 3, 15);
        addEdge(adj, 2, 3, 4);
        addEdge(adj, 1, 2, 11);

        List<MinimumSpanningTreeUnionFind.Edge>  mstEdges = findMST(vertices, adj);
        MinimumSpanningTreeUnionFind.printMST(mstEdges);
    }
}
