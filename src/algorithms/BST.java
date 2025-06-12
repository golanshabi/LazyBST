class BST {
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
        // IMPLEMENT
        return false;
    }

    boolean validateLeaf(Node curr, int key) {
        return !curr.marked && (curr.key > key && curr.left == null || curr.key < key && curr.right == null);
    }

    boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && (pred.right == curr || pred.left == curr);
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