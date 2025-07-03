package algorithms;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import main.BSTInterface;

public class BST implements BSTInterface {
    class Node {
        volatile int key;
        volatile Node right;
        volatile Node left;
        volatile boolean marked;
        volatile int generation;
        public Node(int key) { // Node constructor
            this.key = key;
            this.left = null;
            this.right = null;
            this.marked = false;
            this.generation = 0;
        }
    }

    class FindResult{
        Node pred;
        Node curr;

        public FindResult(Node pred, Node curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    final Node root = new Node(Integer.MIN_VALUE);
    volatile AtomicInteger generation = new AtomicInteger(0);
    volatile AtomicLong size = new AtomicLong(0);
    volatile AtomicLong keysum = new AtomicLong(0);

    private FindResult findInternal(int key, int searchGeneration) {
        Node curr = root;
        Node pred = null;

        while (curr != null) {
            if (curr.key == key) {
                break;
            } else if (curr.key < key) {
                pred = curr;
                curr = curr.right;
            } else {
                pred = curr;
                curr = curr.left;
            }

            if (pred.generation > searchGeneration) {
                return null;
            }
        }

        return new FindResult(pred, curr);
    }

    private FindResult find(int key) {
        while (true) { 
            int searchGeneration = this.generation.get();
            FindResult res = findInternal(key, searchGeneration);
            if (res != null) {
                return res;
            }
        }
    }

    public boolean contains(final int key) {
        FindResult res = find(key);
        return res.curr != null && res.curr.key == key && !res.curr.marked;
    }

    boolean validateLeaf(Node curr, int key) {
        return !curr.marked && (curr.key > key && curr.left == null || curr.key < key && curr.right == null);
    }

    boolean validate(Node pred, Node curr, boolean allow_pred_marked) {
        boolean result = (allow_pred_marked || !pred.marked) && !curr.marked && (pred.right == curr || pred.left == curr);
        return result;
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

    private boolean insertInternal(final int key) {
        while (true) {
            FindResult res = find(key);
            if (res.curr != null) {
                return false;
            }

            Node pred = res.pred;
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

    public boolean insert(final int key) {
        boolean result = insertInternal(key);
        if (result) {
            size.incrementAndGet();
            keysum.addAndGet(key);
        }
        return result;
    }

    private boolean removeRight(Node pred, Node curr) {
        if (curr.left == null) {
            curr.marked = true;
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.right = curr.right;
            return true;
        }

        if (curr.right == null) {
            curr.marked = true;
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.right = curr.left;
            return true;
        }

        Node smallest_in_right = curr.right;
        Node smallestPred = curr;
        Node smallest_in_right_next = smallest_in_right.left;
        while (smallest_in_right_next != null) {
            smallestPred = smallest_in_right;
            smallest_in_right = smallest_in_right_next;
            smallest_in_right_next = smallest_in_right.left;
        }
        
        synchronized (smallestPred) {
            synchronized (smallest_in_right) {
                if (!validate(smallestPred, smallest_in_right, curr == smallestPred) || smallest_in_right.left != null) {
                    return false; // validation failed, retry
                }


                curr.key = smallest_in_right.key;

                int changeGeneration = this.generation.incrementAndGet();
                if (smallest_in_right.right != null) {
                    smallest_in_right.right.generation = changeGeneration;
                } else {
                    smallestPred.generation = changeGeneration;
                }

                // remove smallest in right from the tree
                smallest_in_right.marked = true;

                if (curr != smallestPred) {
                    smallestPred.left = smallest_in_right.right;
                } else {
                    curr.right = smallest_in_right.right;
                }
            }
        }
        return true;
    }

    private boolean removeLeft(Node pred, Node curr) {
        if (curr.left == null)
        {
            curr.marked = true;
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.left = curr.right;
            return true;
        }

        if (curr.right == null) {
            curr.marked = true;
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.left = curr.left;
            return true;
        }

        Node biggest_in_left = curr.left;
        Node biggestPred = curr;
        Node biggest_in_left_next = biggest_in_left.right;
        while (biggest_in_left_next != null) {
            biggestPred = biggest_in_left;
            biggest_in_left = biggest_in_left_next;
            biggest_in_left_next = biggest_in_left.right;
        }

        synchronized (biggestPred) {
            synchronized (biggest_in_left) {
                if (!validate(biggestPred, biggest_in_left, curr == biggestPred) || biggest_in_left.right != null) {
                    return false; // validation failed, retry
                }

                curr.key = biggest_in_left.key;

                int changeGeneration = this.generation.incrementAndGet();
                if (biggest_in_left.left != null) {
                    biggest_in_left.left.generation = changeGeneration;
                } else {
                    biggestPred.generation = changeGeneration;
                }

                // remove biggest in left from the tree
                biggest_in_left.marked = true;

                if (curr != biggestPred) {
                    biggestPred.right = biggest_in_left.left;
                } else {
                    curr.left = biggest_in_left.left;
                }
            }
        }

        return true;
    }

    private boolean removeInternal(final int key) {
        while (true) {
            FindResult res = find(key);

            if (res.curr == null || res.curr.marked) {
                return false;
            }

            Node pred = res.pred;
            Node curr = res.curr;

            synchronized (pred) {
                synchronized (curr) {
                    if (!validate(pred, curr, false))
                        continue;

                    if (curr == pred.left) {
                        while (!removeLeft(pred, curr)) {}
                        return true;
                    } else {
                        while (!removeRight(pred, curr)) {}
                        return true;
                    }
                }
            }
        }
    }

    public boolean remove(final int key) {
        boolean result = removeInternal(key);
        if (result) {
            size.decrementAndGet();
            keysum.addAndGet(-key);
        }

        return result;
    }

    public int size() {
        return this.size.intValue();
    }

    public String getName() {
        return "";
    }

    public long getKeysum() {
        return this.keysum.get();
    }
}