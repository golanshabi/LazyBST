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

    private boolean removeRight(Node pred, Node curr) {
        if (curr.right == null) {
            pred.right = curr.left;
            return true;
        }

        Node smallest_in_right = curr.right;
        Node smallest_pred = curr;
        while (smallest_in_right.left != null) {
            smallest_pred = smallest_in_right;
            smallest_in_right = smallest_in_right.left;
        }

        Node biggest_in_smallest_subtree = smallest_in_right;
        while (biggest_in_smallest_subtree.right != null) {
            biggest_in_smallest_subtree = biggest_in_smallest_subtree.right;
        }

        synchronized (smallest_in_right) {
            if (smallest_in_right.left != null || smallest_in_right.marked)
                return false;

            synchronized (biggest_in_smallest_subtree) {
                if (biggest_in_smallest_subtree.right != null || biggest_in_smallest_subtree.marked)
                    return false;

                pred.right = smallest_in_right;
                smallest_in_right.left = curr.left;
                biggest_in_smallest_subtree.right = curr.right;
                smallest_pred.left = null;
                return true;
            }
        }
    }

    private boolean removeLeft(Node pred, Node curr) {
        if (curr.left == null)
        {
            pred.left = curr.right;
            return true;
        }

        Node biggest_in_left = curr.left;

        Node biggest_pred = curr;
        while (biggest_in_left.right != null) {
            biggest_pred = biggest_in_left;
            biggest_in_left = biggest_in_left.right;
        }

        Node smallest_in_biggest_subtree = biggest_in_left;
        while (smallest_in_biggest_subtree.left != null) {
            smallest_in_biggest_subtree = smallest_in_biggest_subtree.left;
        }

        synchronized (biggest_in_left) {
            if (biggest_in_left.right != null || biggest_in_left.marked)
                return false;

            synchronized (smallest_in_biggest_subtree) {
                if (smallest_in_biggest_subtree.left != null || smallest_in_biggest_subtree.marked)
                    return false;

                pred.left = biggest_in_left;
                biggest_in_left.right = curr.right;
                smallest_in_biggest_subtree.left = curr.left;
                // this is true for everyone except curr, but if biggest_pred == curr,
                // then we don't really care about biggest_pred since curr will now be removed
                biggest_pred.right = null;
                return true;
            }
        }
    }

    boolean remove(int key) {
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
                    foundNode = true;
                    break;
                }
            }

            if (!foundNode)
                return false;

            synchronized (pred) {
                synchronized (curr) {
                    if (!validate(pred, curr))
                        continue;

                    curr.marked = true;

                    if (curr == pred.left) {
                        if (removeLeft(pred, curr))
                            return true;
                    } else {
                        if (removeRight(pred, curr))
                            return true;
                    }
                }
            }
        }
    }
}