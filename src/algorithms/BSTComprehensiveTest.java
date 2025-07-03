package algorithms;

import main.BSTInterface;

public class BSTComprehensiveTest {
    
    public static void main(String[] args) {
        System.out.println("Starting BST Comprehensive Tests...");
        
        testEmptyBST();
        testSingleElement();
        testBasicOperations();
        testDuplicateInsertions();
        testRemovals();
        testComplexScenarios();
        testEdgeCases();
        testBSTProperty();
        testSizeAndKeysum();
        testBoundaryValues();
        
        System.out.println("All tests completed successfully!");
    }
    
    private static void testEmptyBST() {
        System.out.println("Testing empty BST...");
        BSTInterface bst = new BST();
        
        assert bst.size() == 0 : "Empty BST should have size 0";
        assert bst.getKeysum() == 0 : "Empty BST should have keysum 0";
        assert !bst.contains(5) : "Empty BST should not contain any key";
        assert !bst.remove(5) : "Removing from empty BST should return false";
        assert bst.getName().equals("208944355") : "Name should match expected value";
        
        System.out.println("Empty BST tests passed");
    }
    
    private static void testSingleElement() {
        System.out.println("Testing single element operations...");
        BSTInterface bst = new BST();
        
        // Test insertion
        assert bst.insert(10) : "Should successfully insert 10";
        assert bst.size() == 1 : "Size should be 1 after inserting one element";
        assert bst.getKeysum() == 10 : "Keysum should be 10";
        assert bst.contains(10) : "Should contain 10";
        assert !bst.contains(5) : "Should not contain 5";
        assert !bst.contains(15) : "Should not contain 15";
        
        // Test removal
        assert bst.remove(10) : "Should successfully remove 10";
        assert bst.size() == 0 : "Size should be 0 after removal";
        assert bst.getKeysum() == 0 : "Keysum should be 0 after removal";
        assert !bst.contains(10) : "Should not contain 10 after removal";
        
        System.out.println("Single element tests passed");
    }
    
    private static void testBasicOperations() {
        System.out.println("Testing basic operations...");
        BSTInterface bst = new BST();
        
        // Insert multiple elements
        int[] values = {5, 3, 7, 1, 9, 4, 6};
        for (int val : values) {
            assert bst.insert(val) : "Should successfully insert " + val;
        }
        
        // Check size and keysum
        assert bst.size() == 7 : "Size should be 7";
        assert bst.getKeysum() == 35 : "Keysum should be 35";
        
        // Check contains for all inserted values
        for (int val : values) {
            assert bst.contains(val) : "Should contain " + val;
        }
        
        // Check contains for non-existent values
        assert !bst.contains(0) : "Should not contain 0";
        assert !bst.contains(2) : "Should not contain 2";
        assert !bst.contains(8) : "Should not contain 8";
        assert !bst.contains(10) : "Should not contain 10";
        
        System.out.println("Basic operations tests passed");
    }
    
    private static void testDuplicateInsertions() {
        System.out.println("Testing duplicate insertions...");
        BSTInterface bst = new BST();
        
        // Insert same value multiple times
        assert bst.insert(5) : "Should successfully insert 5";
        assert !bst.insert(5) : "Should not insert duplicate 5";
        assert !bst.insert(5) : "Should not insert duplicate 5 again";
        
        assert bst.size() == 1 : "Size should remain 1 after duplicate insertions";
        assert bst.getKeysum() == 5 : "Keysum should remain 5";
        assert bst.contains(5) : "Should still contain 5";
        
        // Test with multiple values
        assert bst.insert(3) : "Should successfully insert 3";
        assert bst.insert(7) : "Should successfully insert 7";
        assert !bst.insert(3) : "Should not insert duplicate 3";
        assert !bst.insert(7) : "Should not insert duplicate 7";
        
        assert bst.size() == 3 : "Size should be 3";
        assert bst.getKeysum() == 15 : "Keysum should be 15";
        
        System.out.println("Duplicate insertions tests passed");
    }
    
    private static void testRemovals() {
        System.out.println("Testing removal operations...");
        BSTInterface bst = new BST();
        
        // Build a tree: 5 -> (3, 7) -> (1, 4, 6, 9)
        int[] values = {5, 3, 7, 1, 4, 6, 9};
        for (int val : values) {
            bst.insert(val);
        }
        
        // Test removing leaf nodes
        assert bst.remove(1) : "Should remove leaf node 1";
        assert bst.size() == 6 : "Size should be 6";
        assert bst.getKeysum() == 34 : "Keysum should be 34";
        assert !bst.contains(1) : "Should not contain 1";
        
        assert bst.remove(4) : "Should remove leaf node 4";
        assert bst.size() == 5 : "Size should be 5";
        assert bst.getKeysum() == 30 : "Keysum should be 30";
        assert !bst.contains(4) : "Should not contain 4";
        
        // Test removing nodes with one child
        assert bst.remove(3) : "Should remove node 3 (has left child 1, but 1 was removed)";
        assert bst.size() == 4 : "Size should be 4";
        assert bst.getKeysum() == 27 : "Keysum should be 27";
        assert !bst.contains(3) : "Should not contain 3";
        
        // Test removing nodes with two children
        assert bst.remove(5) : "Should remove root node 5";
        assert bst.size() == 3 : "Size should be 3";
        assert bst.getKeysum() == 22 : "Keysum should be 22";
        assert !bst.contains(5) : "Should not contain 5";
        
        // Test removing non-existent elements
        assert !bst.remove(1) : "Should not remove already removed 1";
        assert !bst.remove(10) : "Should not remove non-existent 10";
        assert bst.size() == 3 : "Size should remain 3";
        
        System.out.println("Removal tests passed");
    }
    
    private static void testComplexScenarios() {
        System.out.println("Testing complex scenarios...");
        BSTInterface bst = new BST();
        
        // Build a complex tree
        int[] insertOrder = {10, 5, 15, 3, 7, 12, 18, 1, 4, 6, 8, 11, 13, 16, 19};
        for (int val : insertOrder) {
            bst.insert(val);
        }
        
        assert bst.size() == 15 : "Size should be 15";
        long expectedSum = 0;
        for (int val : insertOrder) expectedSum += val;
        assert bst.getKeysum() == expectedSum : "Keysum should match expected";
        
        // Test removing internal nodes with two children
        assert bst.remove(10) : "Should remove root 10";
        assert bst.size() == 14 : "Size should be 14";
        assert !bst.contains(10) : "Should not contain 10";
        
        assert bst.remove(5) : "Should remove node 5";
        assert bst.size() == 13 : "Size should be 13";
        assert !bst.contains(5) : "Should not contain 5";
        
        assert bst.remove(15) : "Should remove node 15";
        assert bst.size() == 12 : "Size should be 12";
        assert !bst.contains(15) : "Should not contain 15";
        
        // Verify remaining elements are still accessible
        int[] remaining = {3, 7, 12, 18, 1, 4, 6, 8, 11, 13, 16, 19};
        for (int val : remaining) {
            assert bst.contains(val) : "Should still contain " + val;
        }
        
        System.out.println("Complex scenarios tests passed");
    }
    
    private static void testEdgeCases() {
        System.out.println("Testing edge cases...");
        BSTInterface bst = new BST();
        
        // Test with negative numbers
        assert bst.insert(-5) : "Should insert negative number -5";
        assert bst.insert(-10) : "Should insert negative number -10";
        assert bst.insert(-3) : "Should insert negative number -3";
        assert bst.size() == 3 : "Size should be 3";
        assert bst.contains(-5) : "Should contain -5";
        assert bst.contains(-10) : "Should contain -10";
        assert bst.contains(-3) : "Should contain -3";
        
        // Test with zero
        assert bst.insert(0) : "Should insert 0";
        assert bst.size() == 4 : "Size should be 4";
        assert bst.contains(0) : "Should contain 0";
        
        // Test with large numbers
        assert bst.insert(1000000) : "Should insert large number";
        assert bst.insert(999999) : "Should insert large number";
        assert bst.size() == 6 : "Size should be 6";
        assert bst.contains(1000000) : "Should contain large number";
        assert bst.contains(999999) : "Should contain large number";
        
        // Test removing negative numbers
        assert bst.remove(-5) : "Should remove negative number -5";
        assert bst.size() == 5 : "Size should be 5";
        assert !bst.contains(-5) : "Should not contain -5";
        
        System.out.println("Edge cases tests passed");
    }
    
    private static void testBSTProperty() {
        System.out.println("Testing BST property...");
        BSTInterface bst = new BST();
        
        // Build a tree and verify BST property is maintained
        int[] values = {8, 4, 12, 2, 6, 10, 14, 1, 3, 5, 7, 9, 11, 13, 15};
        for (int val : values) {
            bst.insert(val);
        }
        
        // The BST should maintain the binary search tree property
        // This is implicitly tested by the contains method working correctly
        for (int val : values) {
            assert bst.contains(val) : "Should contain " + val;
        }
        
        // Test that non-existent values are not found
        assert !bst.contains(0) : "Should not contain 0";
        assert !bst.contains(16) : "Should not contain 16";
        assert !bst.contains(8) : "Should not contain 8 after removal";
        
        // Remove some elements and verify BST property is still maintained
        bst.remove(8);
        bst.remove(4);
        bst.remove(12);
        
        // Verify remaining elements are still accessible
        int[] remaining = {2, 6, 10, 14, 1, 3, 5, 7, 9, 11, 13, 15};
        for (int val : remaining) {
            assert bst.contains(val) : "Should still contain " + val;
        }
        
        System.out.println("BST property tests passed");
    }
    
    private static void testSizeAndKeysum() {
        System.out.println("Testing size and keysum consistency...");
        BSTInterface bst = new BST();
        
        long expectedSum = 0;
        assert bst.size() == 0 : "Initial size should be 0";
        assert bst.getKeysum() == expectedSum : "Initial keysum should be 0";
        
        // Insert elements and track sum
        int[] values = {10, 20, 30, 40, 50};
        for (int val : values) {
            bst.insert(val);
            expectedSum += val;
            assert bst.size() == expectedSum / 10 : "Size should match number of insertions";
            assert bst.getKeysum() == expectedSum : "Keysum should match expected sum";
        }
        
        // Remove elements and track sum
        for (int val : values) {
            bst.remove(val);
            expectedSum -= val;
            assert bst.size() == expectedSum / 10 : "Size should match remaining elements";
            assert bst.getKeysum() == expectedSum : "Keysum should match expected sum";
        }
        
        assert bst.size() == 0 : "Final size should be 0";
        assert bst.getKeysum() == 0 : "Final keysum should be 0";
        
        System.out.println("Size and keysum tests passed");
    }
    
    private static void testBoundaryValues() {
        System.out.println("Testing boundary values...");
        BSTInterface bst = new BST();
        
        // Test with Integer.MIN_VALUE and Integer.MAX_VALUE
        assert bst.insert(Integer.MIN_VALUE + 1) : "Should insert near MIN_VALUE";
        assert bst.insert(Integer.MAX_VALUE - 1) : "Should insert near MAX_VALUE";
        assert bst.insert(0) : "Should insert 0";
        
        assert bst.size() == 3 : "Size should be 3";
        assert bst.contains(Integer.MIN_VALUE + 1) : "Should contain near MIN_VALUE";
        assert bst.contains(Integer.MAX_VALUE - 1) : "Should contain near MAX_VALUE";
        assert bst.contains(0) : "Should contain 0";
        
        // Test removing boundary values
        assert bst.remove(Integer.MIN_VALUE + 1) : "Should remove near MIN_VALUE";
        assert bst.remove(Integer.MAX_VALUE - 1) : "Should remove near MAX_VALUE";
        assert bst.size() == 1 : "Size should be 1";
        assert bst.contains(0) : "Should still contain 0";
        
        // Test with very large negative and positive numbers
        assert bst.insert(-1000000000) : "Should insert large negative number";
        assert bst.insert(1000000000) : "Should insert large positive number";
        assert bst.size() == 3 : "Size should be 3";
        
        System.out.println("Boundary values tests passed");
    }
} 