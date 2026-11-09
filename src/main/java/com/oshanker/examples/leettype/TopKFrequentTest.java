package com.oshanker.examples.leettype;
//import static com.oshanker.examples.leettype.TopKFrequentMinHeap.topKFrequent;
import static com.oshanker.examples.leettype.TopKFrequentBucketSort.topKFrequent;

import java.util.Arrays;
public class TopKFrequentTest {
    // Test utility: compare results irrespective of order
    private static void assertEquals(
            int[] expected, int[] actual, String testName) {
        System.out.println("testing: " + testName);

        Arrays.sort(expected);
        Arrays.sort(actual);

        if (!Arrays.equals(expected, actual)) {
        	//System.out.println
        	throw new AssertionError
        	(
                testName + " FAILED. Expected: " +
                Arrays.toString(expected) +
                ", Actual: " + Arrays.toString(actual)
            );
        }

        System.out.println("PASS: " + testName);
    }
	
    public static void main(String[] args) {

        // 1. Standard example
        assertEquals(
            new int[]{1, 2},
            topKFrequent(
                new int[]{1, 1, 1, 2, 2, 3}, 2),
            "Standard example"
        );

        // 2. K = 1
        assertEquals(
            new int[]{1},
            topKFrequent(
                new int[]{1, 1, 1, 2, 2, 3}, 1),
            "K = 1"
        );

        // 3. K equals number of distinct elements
        assertEquals(
            new int[]{1, 2, 3},
            topKFrequent(
                new int[]{1, 1, 2, 2, 3, 3}, 3),
            "K equals distinct count"
        );

        // 4. K greater than distinct count
        int[] returnValues = topKFrequent(
		    new int[]{1, 1, 2, 2}, 10);
		assertEquals(
            new int[]{1, 2},
            returnValues,
            "K greater than distinct count"
        );

        // 5. Negative numbers
        assertEquals(
            new int[]{-1, -2},
            topKFrequent(
                new int[]{-1, -1, -1, -2, -2, 3}, 2),
            "Negative numbers"
        );

        // 6. Single element
        assertEquals(
            new int[]{5},
            topKFrequent(new int[]{5}, 1),
            "Single element"
        );

        // 7. All elements have equal frequency
        // Any two elements are valid.
        int[] result = topKFrequent(
            new int[]{1, 1, 2, 2, 3, 3}, 2);

        if (result.length != 2) {
            throw new AssertionError("Tie case FAILED");
        }

        System.out.println("PASS: Equal frequency tie");

        // 8. Empty array
        assertEquals(
            new int[]{},
            topKFrequent(new int[]{}, 2),
            "Empty array"
        );

        // 9. Null array
        assertEquals(
            new int[]{},
            topKFrequent(null, 2),
            "Null array"
        );

        // 10. K = 0
        assertEquals(
            new int[]{},
            topKFrequent(new int[]{1, 1, 2}, 0),
            "K = 0"
        );

        // 11. Negative K
        assertEquals(
            new int[]{},
            topKFrequent(new int[]{1, 1, 2}, -1),
            "Negative K"
        );

        System.out.println("\nALL TESTS PASSED");
    }

}
