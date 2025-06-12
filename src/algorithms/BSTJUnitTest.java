import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * JUnit-style tests for BST class
 * To run with JUnit, you'll need to add JUnit to your classpath
 * For basic testing, run: javac BSTJUnitTest.java && java BSTJUnitTest
 */
public class BSTJUnitTest {
    
    public static void main(String[] args) {
        BSTJUnitTest test = new BSTJUnitTest();
        test.runAllTests();
    }
    
    public void runAllTests() {
        System.out.println("Running Advanced BST Tests...\n");
        
        testBasicOperations();
        testConcurrentInsertions();
        testLargeDataset();
        testPerformance();
        testBoundaryConditions();
        
        System.out.println("\nAll advanced tests completed!");
    }
    
    public void testBasicOperations() {
        System.out.println("Test: Basic Operations");
        BST bst = new BST();
        
        // Test empty tree
        assertFalse(bst.contains(10), "Empty tree should not contain any elements");
        
        // Test single insertion
        assertTrue(bst.add(10), "Should successfully add first element");
        assertFalse(bst.add(10), "Should not add duplicate element");
        
        // Test multiple insertions
        assertTrue(bst.add(5), "Should add left child");
        assertTrue(bst.add(15), "Should add right child");
        assertTrue(bst.add(3), "Should add left-left child");
        assertTrue(bst.add(7), "Should add left-right child");
        
        System.out.println("✓ Basic operations work correctly");
    }
    
    public void testConcurrentInsertions() {
        System.out.println("Test: Concurrent Insertions");
        BST bst = new BST();
        final int numThreads = 10;
        final int numInsertsPerThread = 100;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Create tasks for concurrent insertions
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < numInsertsPerThread; j++) {
                    int value = threadId * numInsertsPerThread + j;
                    if (bst.add(value)) {
                        successCount.incrementAndGet();
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int expectedSuccess = numThreads * numInsertsPerThread;
        assertEquals(expectedSuccess, successCount.get(), 
                    "All unique insertions should succeed in concurrent environment");
        
        System.out.println("✓ Concurrent insertions work correctly");
    }
    
    public void testLargeDataset() {
        System.out.println("Test: Large Dataset");
        BST bst = new BST();
        
        // Insert large number of elements
        int size = 10000;
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            values.add(i);
        }
        
        // Shuffle to avoid worst-case insertion order
        Collections.shuffle(values);
        
        // Insert all values
        for (int value : values) {
            assertTrue(bst.add(value), "Should successfully add value: " + value);
        }
        
        // Try to insert duplicates
        for (int value : values) {
            assertFalse(bst.add(value), "Should not add duplicate value: " + value);
        }
        
        System.out.println("✓ Large dataset handling works correctly");
    }
    
    public void testPerformance() {
        System.out.println("Test: Performance Measurement");
        BST bst = new BST();
        
        int numOperations = 100000;
        long startTime = System.nanoTime();
        
        // Insert elements
        for (int i = 0; i < numOperations; i++) {
            bst.add(i);
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        
        System.out.println("✓ Inserted " + numOperations + " elements in " + 
                          String.format("%.2f", duration) + " ms");
        System.out.println("✓ Average time per insertion: " + 
                          String.format("%.6f", duration / numOperations) + " ms");
    }
    
    public void testBoundaryConditions() {
        System.out.println("Test: Boundary Conditions");
        BST bst = new BST();
        
        // Test with extreme values
        assertTrue(bst.add(Integer.MAX_VALUE), "Should handle Integer.MAX_VALUE");
        assertTrue(bst.add(Integer.MIN_VALUE + 1), "Should handle Integer.MIN_VALUE + 1");
        assertTrue(bst.add(0), "Should handle zero");
        assertTrue(bst.add(-1), "Should handle -1");
        assertTrue(bst.add(1), "Should handle 1");
        
        // Test duplicates of extreme values
        assertFalse(bst.add(Integer.MAX_VALUE), "Should not add duplicate MAX_VALUE");
        assertFalse(bst.add(Integer.MIN_VALUE + 1), "Should not add duplicate MIN_VALUE + 1");
        assertFalse(bst.add(0), "Should not add duplicate zero");
        
        System.out.println("✓ Boundary conditions handled correctly");
    }
    
    // Test concurrent operations with mixed add/contains operations
    public void testConcurrentMixedOperations() {
        System.out.println("Test: Concurrent Mixed Operations");
        BST bst = new BST();
        
        // Pre-populate with some values
        for (int i = 0; i < 100; i++) {
            bst.add(i * 2); // Add even numbers
        }
        
        final int numThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger addCount = new AtomicInteger(0);
        AtomicInteger containsCount = new AtomicInteger(0);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    int value = threadId * 100 + j;
                    
                    // Mix of add and contains operations
                    if (j % 2 == 0) {
                        if (bst.add(value)) {
                            addCount.incrementAndGet();
                        }
                    } else {
                        if (bst.contains(value)) {
                            containsCount.incrementAndGet();
                        }
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✓ Mixed concurrent operations completed");
        System.out.println("  - Successful adds: " + addCount.get());
        System.out.println("  - Successful contains: " + containsCount.get());
    }
    
    // Helper assertion methods (simple JUnit-style)
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError("Assertion failed: " + message + 
                                   " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
} 