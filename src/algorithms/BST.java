class BST {
    class Node {
        final int key;
        volatile Node right;
        volatile Node left;
        volatile boolean marked;
    }

    final Node root;

    public Node(int key) { // Node constructor
        this.key = key;
        this.left = null;
        this.rigt = null;
        this.marked = false;
    }

    boolean contains(int key) {
        // IMPLEMENT
    }

    boolean validate(Node pred, Node curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }    

    boolean add(int key) {
        while (true) {
            Node pred = head;
            Node curr = pred.next; 
            while (curr.key < key) {
                pred = curr; curr = curr.next;
            }
            synchronized(pred) {
                synchronized(curr) {
                    if (validate (pred, curr)) {
                        if (curr.key == key) {
                            return false;
                        } else {
                            Node node = new Node(key);
                            node.next = curr;
                            pred.next = node;
                            return true;
                        } 
                    } 
                } 
            } 
        } 
    }
}