package com.oshanker.examples.graph.mst;

public class DisjointSetTest {

    public static void main(String[] args) {
        testInitialState();
        testBasicUnionAndTransitivity();
        testUnionByRankBehavior();
        testDuplicateAndSelfUnion();
        testPathCompression();

        System.out.println("All DisjointSet unit tests passed successfully!");
    }

    // Test 1: Every vertex starts as its own root with rank 0
    private static void testInitialState() {
    	System.out.println("==  ==");
 
        DisjointSet ds = new DisjointSet(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i, ds.find(i), "Vertex " + i + " should initially be its own root");
            assertEquals(0, ds.rank[i], "Vertex " + i + " should initially have rank 0");
        }
    }

    // Test 2: Merging (0, 1) and (1, 2) means 0 and 2 share the same root
    private static void testBasicUnionAndTransitivity() {
    	System.out.println("== Merging (0, 1) and (1, 2) means 0 and 2 share the same root ==");
        DisjointSet ds = new DisjointSet(4);

        ds.union(0, 1);
        assertEquals(ds.find(0), ds.find(1), "0 and 1 should share the same root");
        assertTrue(ds.find(0) != ds.find(2), "0 and 2 should still be in separate sets");

        // Merge 1 and 2 -> transitively connects 0 and 2
        ds.union(1, 2);
        assertEquals(ds.find(0), ds.find(2), "0 and 2 should now share the same root");
        assertTrue(ds.find(0) != ds.find(3), "3 should remain isolated");
    }

    // Test 3: Verify rank increases only when two trees of equal rank merge
    private static void testUnionByRankBehavior() {
    	System.out.println("==  ==");
        DisjointSet ds = new DisjointSet(4);

        // Merge two rank-0 trees (0 and 1) -> root0 becomes parent, rank[0] becomes 1
        ds.union(0, 1);
        assertEquals(0, ds.find(1), "Root of 1 should become 0");
        assertEquals(1, ds.rank[0], "Rank of 0 should increment to 1");
        assertEquals(0, ds.rank[1], "Rank of child 1 should stay 0");

        // Merge a rank-0 tree (2) into a rank-1 tree (0) -> rank[0] should NOT increase
        ds.union(2, 0);
        assertEquals(0, ds.find(2), "2 should attach directly under taller tree root 0");
        assertEquals(1, ds.rank[0], "Rank of 0 should remain 1 when absorbing a shorter tree");
    }

    // Test 4: Unioning a vertex with itself or an already-connected vertex does nothing
    private static void testDuplicateAndSelfUnion() {
    	System.out.println("==  ==");
        DisjointSet ds = new DisjointSet(3);

        ds.union(0, 0); // Self-union
        assertEquals(0, ds.rank[0], "Self-union should not change rank");

        ds.union(0, 1); // Rank becomes 1
        int rankAfterFirstUnion = ds.rank[0];

        ds.union(0, 1); // Duplicate union
        ds.union(1, 0); // Reverse duplicate union
        assertEquals(rankAfterFirstUnion, ds.rank[0], "Duplicate unions should not increment rank again");
    }

    // Test 5: Path compression flattens deep parent chains upon calling find()
    private static void testPathCompression() {
    	System.out.println("==  ==");
        DisjointSet ds = new DisjointSet(4);

        // Manually construct a skewed chain: 3 -> 2 -> 1 -> 0 (root)
        ds.parent[3] = 2;
        ds.parent[2] = 1;
        ds.parent[1] = 0;

        // Calling find(3) should resolve to 0 AND re-point 3 and 2 directly to 0
        int root = ds.find(3);
        assertEquals(0, root, "Root of 3 should be 0");
        assertEquals(0, ds.parent[3], "Path compression should point parent[3] directly to root 0");
        assertEquals(0, ds.parent[2], "Path compression should point parent[2] directly to root 0");
    }

    // Assertion helpers
    private static void assertEquals(int expected, int actual, String message) {
        System.out.println(message);
        if (expected != actual) {
            throw new AssertionError(message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        System.out.println(message);
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
