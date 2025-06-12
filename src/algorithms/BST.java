public class BST {
    class Node {
        final int key;
        volatile Node right;
        volatile Node left;
        volatile boolean marked;
        public Node(int key) { // Node constructor
            this.key = key;
            this.left = null;
            this.right = null;
            this.marked = false;
        }
    }

    final Node root = new Node(Integer.MIN_VALUE);

    boolean contains(int key) {
        Node curr = root;
        while (curr != null) {
            if (curr.key == key) {
                return !curr.marked;
            } else if (curr.key < key) {
                curr = curr.right;
            } else {
                curr = curr.left;
            }
        }
        return false;
    }

    boolean validateLeaf(Node curr, int key) {
        return !curr.marked && (curr.key > key && curr.left == null || curr.key < key && curr.right == null);
    }

    boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && (pred.right == curr || pred.left == curr);
    }

    public boolean checkBSTProperty() {
        return checkBSTProperty(root.left) && checkBSTProperty(root.right);
    }
    
    private boolean checkBSTProperty(Node node) {
        if (node == null || node.marked) {
            return true;
        }
        
        if (node.left != null && !node.left.marked) {
            if (node.left.key >= node.key || !checkBSTProperty(node.left)) {
                return false;
            }
        }
        
        if (node.right != null && !node.right.marked) {
            if (node.right.key <= node.key || !checkBSTProperty(node.right)) {
                return false;
            }
        }
        
        return true;
    }    

    boolean add(int key) {
        while (true) {
            Node pred = root;
            Node curr = root.key < key ? root.right : root.left;
            boolean foundNode = false;
            while (curr != null) {
                if (curr.key > key) {
                    pred = curr;
                    curr = curr.left;
                } else if (curr.key < key) {
                    pred = curr;
                    curr = curr.right;
                } else {
                    return false;
                }
            }
            synchronized(pred) {
                if (validateLeaf(pred, key)) {
                    Node node = new Node(key);
                    if (key < pred.key) {
                        pred.left = node;
                    } else {
                        pred.right = node;
                    }
                    return true;
                }
            } 
        }
    }
}