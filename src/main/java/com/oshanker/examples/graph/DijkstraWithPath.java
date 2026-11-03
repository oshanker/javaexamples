package com.oshanker.examples.graph;

import java.util.*;

public class DijkstraWithPath {

    public static class Edge {
        int target;
        int weight;

        public Edge(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    public static class Pair {
        int node;
        int distance;

        public Pair(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    /**
     * Extended Dijkstra that tracks paths using a 'parent' array.
     */
    public static void dijkstraWithPaths(int numVertices, List<List<Edge>> adjList,
    		int source) {
        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[source] = 0;

        // NEW: Array to track the step right before reaching a node
        int[] parent = new int[numVertices];
        Arrays.fill(parent, -1); // -1 means unreached or starting node

        PriorityQueue<Pair> minHeap = new PriorityQueue<>(Comparator.comparingInt(p -> p.distance));
        minHeap.add(new Pair(source, 0));

        while (!minHeap.isEmpty()) {
            Pair current = minHeap.poll();
            int u = current.node;
            int distU = current.distance;

            if (distU > distances[u]) {
                continue;
            }

            for (Edge edge : adjList.get(u)) {
                int v = edge.target;
                int weightUV = edge.weight;

                if (distances[u] + weightUV < distances[v]) {
                    distances[v] = distances[u] + weightUV;
                    parent[v] = u; // NEW: u is the best way to get to v
                    minHeap.add(new Pair(v, distances[v]));
                }
            }
        }

        // Print results and paths
        printResults(source, distances, parent);
    }

    /**
     * Backtracks from a target node to the source using the parent array
     * to reconstruct the full path sequence.
     */
    private static List<Integer> reconstructPath(int target, int[] parent, int source) {
        List<Integer> path = new ArrayList<>();
        
        // If the target is unreachable, return an empty path
        if (parent[target] == -1 && target != source) {
            return path; 
        }

        // Trace backward from target to source
        for (int curr = target; curr != -1; curr = parent[curr]) {
            path.add(curr);
        }
        
        // Reverse it so it reads from Source -> Target
        Collections.reverse(path);
        return path;
    }

    private static void printResults(int source, int[] distances, int[] parent) {
        System.out.println("Shortest Paths from Source " + source + ":\n");
        System.out.printf("%-10s %-10s %-20s%n", "Target", "Distance", "Path Sequence");
        System.out.println("----------------------------------------------");

        for (int i = 0; i < distances.length; i++) {
            String distStr = (distances[i] == Integer.MAX_VALUE) ? "INF" : String.valueOf(distances[i]);
            List<Integer> path = reconstructPath(i, parent, source);
            
            String pathStr = path.isEmpty() ? "No Path" : path.toString().replace(",", " ->");
            System.out.printf("%-10d %-10s %-20s%n", i, distStr, pathStr);
        }
    }

    public static void main(String[] args) {
        int numVertices = 5;
        List<List<Edge>> adjList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            adjList.add(new ArrayList<>());
        }

        // Graph Initialization
        adjList.get(0).add(new Edge(1, 4));
        adjList.get(0).add(new Edge(2, 1));
        adjList.get(2).add(new Edge(1, 2));
        adjList.get(1).add(new Edge(3, 1));
        adjList.get(2).add(new Edge(3, 5));
        adjList.get(3).add(new Edge(4, 3));

        dijkstraWithPaths(numVertices, adjList, 0);
    }
}

