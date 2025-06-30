package algorithms;

import main.BSTInterface;

public class BSTSimpleTest {
    public static void main(String[] args) {
        System.out.println("=== BST Simple Test: Adding 3, 2, 1, 0 ===");
        
        // Create a new BST instance
        BSTInterface bst = new BST();
        
        // Test values to insert
        int[] values = {3, 2, 0, 1};
        bst.insert(4);
        bst.insert(2);
        bst.insert(0);
        bst.insert(3);
        bst.insert(-1);

        bst.remove(2);
    }
} 