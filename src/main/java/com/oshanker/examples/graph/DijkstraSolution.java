package com.oshanker.examples.graph;


import java.util.*;

public class DijkstraSolution {

    // Define a class to represent directed, weighted edges
    public static class Edge {
        int target;
        int weight;

        public Edge(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    // Define a class for the Min-Heap entries
    public static class Pair {
        int node;
        int distance;

        public Pair(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    /**
     * Finds the shortest path from a source node to all other nodes.
     * @param numVertices Total number of vertices in the graph (0 to numVertices - 1)
     * @param adjList Adjacency list representation of the graph
     * @param source The starting node
     * @return An array containing the shortest distances from the source to every node
     */
    public static int[] dijkstra(int numVertices, List<List<Edge>> adjList, int source) {
        // Track the shortest distance from the source to every other node
        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[source] = 0;

        // Min-Heap prioritized by the shortest distance from the source
        PriorityQueue<Pair> minHeap = new PriorityQueue<>(Comparator.comparingInt(p -> p.distance));
        minHeap.add(new Pair(source, 0));

        while (!minHeap.isEmpty()) {
            Pair current = minHeap.poll();
            int u = current.node;
            int distU = current.distance;

            // CRITICAL INTERVIEW OPTIMIZATION: Stale entry check.
            // If we already found a shorter path to 'u', discard this older heap item.
            if (distU > distances[u]) {
                continue;
            }

            // Explore all neighbors of node 'u'
            for (Edge edge : adjList.get(u)) {
                int v = edge.target;
                int weightUV = edge.weight;

                // Relaxation step: check if a shorter path to 'v' exists via 'u'
                if (distances[u] + weightUV < distances[v]) {
                    distances[v] = distances[u] + weightUV;
                    minHeap.add(new Pair(v, distances[v]));
                }
            }
        }

        return distances;
    }

    // Example Usage / Test Case
    public static void main(String[] args) {
        int numVertices = 5;
        List<List<Edge>> adjList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            adjList.add(new ArrayList<>());
        }

        // Graph edges initialization: u -> v with weight w
        adjList.get(0).add(new Edge(1, 4));
        adjList.get(0).add(new Edge(2, 1));
        adjList.get(2).add(new Edge(1, 2));
        adjList.get(1).add(new Edge(3, 1));
        adjList.get(2).add(new Edge(3, 5));
        adjList.get(3).add(new Edge(4, 3));

        int source = 0;
        int[] result = dijkstra(numVertices, adjList, source);

        System.out.println("Shortest distances from source " + source + ":");
        for (int i = 0; i < result.length; i++) {
            String distStr = (result[i] == Integer.MAX_VALUE) ? "INF" : String.valueOf(result[i]);
            System.out.println("To node " + i + " -> " + distStr);
        }
    }
}

