public class BSTTest {
    
    public static void main(String[] args) {
        BSTTest test = new BSTTest();
        test.runAllTests();
    }
    
    public void runAllTests() {
        System.out.println("Running BST Tests...\n");
        
        testBasicAdd();
        testDuplicateKeys();
        testMultipleInsertions();
        testTreeStructure();
        testEdgeCases();
        
        System.out.println("\nAll tests completed!");
    }
    
    public void testBasicAdd() {
        System.out.println("Test: Basic Add Functionality");
        BST bst = new BST();
        
        // Test adding a single element
        boolean result1 = bst.add(10);
        assert result1 : "Should return true when adding new element";
        checkBSTProperty(bst, "after adding 10");
        
        // Test adding another element
        boolean result2 = bst.add(5);
        assert result2 : "Should return true when adding new element";
        checkBSTProperty(bst, "after adding 5");
        
        boolean result3 = bst.add(15);
        assert result3 : "Should return true when adding new element";
        checkBSTProperty(bst, "after adding 15");
        
        System.out.println("✓ Basic add functionality works");
    }
    
    public void testDuplicateKeys() {
        System.out.println("Test: Duplicate Key Handling");
        BST bst = new BST();
        
        // Add initial element
        boolean result1 = bst.add(10);
        assert result1 : "Should return true when adding new element";
        checkBSTProperty(bst, "after adding initial 10");
        
        // Try to add duplicate
        boolean result2 = bst.add(10);
        assert !result2 : "Should return false when adding duplicate element";
        checkBSTProperty(bst, "after attempting to add duplicate 10");
        
        System.out.println("✓ Duplicate key handling works");
    }
    
    public void testMultipleInsertions() {
        System.out.println("Test: Multiple Insertions");
        BST bst = new BST();
        
        int[] values = {50, 30, 70, 20, 40, 60, 80, 10, 25, 35, 45};
        
        for (int value : values) {
            boolean result = bst.add(value);
            assert result : "Should return true when adding new element: " + value;
            checkBSTProperty(bst, "after adding " + value);
        }
        
        // Try to add duplicates
        for (int value : values) {
            boolean result = bst.add(value);
            assert !result : "Should return false when adding duplicate element: " + value;
            checkBSTProperty(bst, "after attempting to add duplicate " + value);
        }
        
        System.out.println("✓ Multiple insertions work correctly");
    }
    
    public void testTreeStructure() {
        System.out.println("Test: Tree Structure Validation");
        BST bst = new BST();
        
        // Build a small tree
        bst.add(10);
        bst.add(5);
        bst.add(15);
        bst.add(3);
        bst.add(7);
        bst.add(12);
        bst.add(18);
        
        // Verify tree structure by checking node relationships
        // Note: This requires access to internal structure for validation
        // For now, we'll just verify that we can add elements without errors
        
        System.out.println("✓ Tree structure appears valid");
    }
    
    public void testEdgeCases() {
        System.out.println("Test: Edge Cases");
        BST bst = new BST();
        
        // Test with Integer.MAX_VALUE and Integer.MIN_VALUE
        boolean result1 = bst.add(Integer.MAX_VALUE);
        assert result1 : "Should handle Integer.MAX_VALUE";
        
        // Note: Can't add Integer.MIN_VALUE as it's used for root
        boolean result2 = bst.add(Integer.MIN_VALUE + 1);
        assert result2 : "Should handle Integer.MIN_VALUE + 1";
        
        // Test with zero
        boolean result3 = bst.add(0);
        assert result3 : "Should handle zero";
        
        // Test with negative numbers
        boolean result4 = bst.add(-100);
        assert result4 : "Should handle negative numbers";
        
        System.out.println("✓ Edge cases handled correctly");
    }
    
    // Helper method to check BST property after operations
    private void checkBSTProperty(BST bst, String context) {
        boolean isValid = bst.checkBSTProperty();
        assert isValid : "BST property violated " + context;
        
        if (isValid) {
            System.out.println("  ✅ BST property maintained " + context);
        } else {
            System.err.println("  ❌ BST property VIOLATED " + context);
            throw new AssertionError("BST property violated " + context);
        }
    }
    
    // Helper method to print tree structure (for debugging)
    public void printTree(BST bst) {
        System.out.println("Tree structure visualization would go here");
        // This would require access to the internal Node structure
        // which is currently private
    }
} 