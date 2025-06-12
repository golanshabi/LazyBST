import java.util.concurrent.*;

public class BSTTestWithTimeout {
    private static final int TIMEOUT_SECONDS = 2;
    
    public static void main(String[] args) {
        BSTTestWithTimeout test = new BSTTestWithTimeout();
        test.runAllTests();
    }
    
    public void runAllTests() {
        System.out.println("Running BST Tests with Timeout Protection...\n");
        
        try {
            testBasicAddWithTimeout();
            testDuplicateKeysWithTimeout();
            testMultipleInsertionsWithTimeout();
            
            System.out.println("\nAll tests passed! ✅");
        } catch (Exception e) {
            System.err.println("\n❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void testBasicAddWithTimeout() throws Exception {
        System.out.println("Test: Basic Add with Timeout Protection");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            BST bst = new BST();
            
            // These operations should complete quickly
            boolean result1 = bst.add(10);
            if (!result1) throw new RuntimeException("Failed to add 10");
            
            boolean result2 = bst.add(5);
            if (!result2) throw new RuntimeException("Failed to add 5");
            
            boolean result3 = bst.add(15);
            if (!result3) throw new RuntimeException("Failed to add 15");
            
            return null;
        });
        
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("✅ Basic add completed within timeout");
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("INFINITE LOOP DETECTED: Basic add operation timed out after " + 
                                     TIMEOUT_SECONDS + " seconds - likely due to traversal bug!");
        } finally {
            executor.shutdown();
        }
    }
    
    public void testDuplicateKeysWithTimeout() throws Exception {
        System.out.println("Test: Duplicate Keys with Timeout Protection");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            BST bst = new BST();
            
            // Add initial element
            boolean result1 = bst.add(10);
            if (!result1) throw new RuntimeException("Failed to add initial 10");
            
            // Try to add duplicate - this should complete quickly
            boolean result2 = bst.add(10);
            if (result2) throw new RuntimeException("Duplicate add should return false");
            
            return null;
        });
        
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("✅ Duplicate key test completed within timeout");
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("INFINITE LOOP DETECTED: Duplicate key test timed out after " + 
                                     TIMEOUT_SECONDS + " seconds - likely due to traversal bug!");
        } finally {
            executor.shutdown();
        }
    }
    
    public void testMultipleInsertionsWithTimeout() throws Exception {
        System.out.println("Test: Multiple Insertions with Timeout Protection");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            BST bst = new BST();
            
            // These should all complete quickly if implementation is correct
            int[] values = {50, 30, 70, 20, 40, 60, 80};
            
            for (int value : values) {
                boolean result = bst.add(value);
                if (!result) {
                    throw new RuntimeException("Failed to add value: " + value);
                }
            }
            
            return null;
        });
        
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("✅ Multiple insertions completed within timeout");
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("INFINITE LOOP DETECTED: Multiple insertions timed out after " + 
                                     TIMEOUT_SECONDS + " seconds - likely due to traversal bug!");
        } finally {
            executor.shutdown();
        }
    }
    
    // Test that specifically targets the bug you introduced
    public void testSpecificBugScenario() throws Exception {
        System.out.println("Test: Specific Bug Scenario (adding smaller values)");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            BST bst = new BST();
            
            // This sequence will trigger the bug if curr.left was changed to curr.right
            bst.add(100);  // This will work
            bst.add(50);   // This will likely cause infinite loop with your bug
            
            return null;
        });
        
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("✅ Bug scenario test passed");
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("BUG DETECTED: When adding smaller values, the traversal logic fails! " +
                                     "Check if you accidentally changed 'curr = curr.left' to 'curr = curr.right'");
        } finally {
            executor.shutdown();
        }
    }
} 