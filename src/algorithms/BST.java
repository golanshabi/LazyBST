package algorithms;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import main.BSTInterface;

public class BST implements BSTInterface {
    class Node {
        final int key;
        volatile Node right;
        volatile Node left;
        volatile boolean marked;
        volatile int generation;
        volatile boolean is_bad;
        public Node(int key) { // Node constructor
            this.key = key;
            this.left = null;
            this.right = null;
            this.marked = false;
            this.generation = 0;
            this.is_bad = false;
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
            if (curr.is_bad) {
                return null;
            }
            if (curr.key == key) {
                if (curr.generation > searchGeneration) {
                    return null;
                }    
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
        // if (!result) {
            // System.out.println("validate_pred.marked=" + pred.marked + " curr.marked=" + curr.marked + " pred.right=" + pred.right + " pred.left=" + pred.left + " curr=" + curr);
        // }
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
                    // System.out.println("inserting node=" + node + " pred=" + pred);
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
        // System.out.println("insert_start");
        boolean result = insertInternal(key);
        if (result) {
            size.incrementAndGet();
            keysum.addAndGet(key);
        }
        // System.out.println("insert_end");

        return result;
    }

    private boolean removeRight(Node pred, Node curr) {
        if (pred.right != curr) {
            // System.out.println("pred.right != curr");
        }
        // System.out.println("removeRight_start");
        if (curr.left == null) {
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.right = curr.right;
            return true;
        }

        if (curr.right == null) {
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

        Node swap_node = new Node(smallest_in_right.key);
        swap_node.right = curr.right;
        swap_node.left = curr.left;
        // if (swap_node.left == smallest_in_right || swap_node.right == smallest_in_right) {
        //     System.out.println("FUCK swap_node.left=" + swap_node.left + " swap_node.right=" + swap_node.right + " smallest_in_right=" + smallest_in_right);
        //     System.exit(1);
        // }

        if (swap_node.left == smallest_in_right) {
            swap_node.left = null;
        }
        if (swap_node.right == smallest_in_right) {
            swap_node.right = smallest_in_right.right;
        }
        
        
        synchronized (smallestPred) {
            synchronized (smallest_in_right) {
                if (!validate(smallestPred, smallest_in_right, curr == smallestPred) || smallest_in_right.left != null) {
                    // if (smallest_in_right.marked) {
                    //     System.out.println("marked smallest_in_right=" + smallest_in_right + " smallestPred=" + smallestPred + " curr=" + curr + " smallest_in_right.left=" + smallest_in_right.left + " smallest_in_right.right=" + smallest_in_right.right);
                    // }
                    // System.out.println("removeRight_validation_failed smallest_in_right.left=" + smallest_in_right.left);
                    return false; // validation failed, retry
                }
                pred.right = swap_node;

                int changeGeneration = this.generation.incrementAndGet();
                if (smallest_in_right.right != null) {
                    smallest_in_right.right.generation = changeGeneration;
                } else {
                    smallestPred.generation = changeGeneration;
                }

                // remove smallest in right from the tree
                if (curr != smallestPred) {
                    smallestPred.left = smallest_in_right.right;
                }

                // smallest_in_right.is_bad = true;
                // smallest_in_right.right = null;
                // smallest_in_right.left = null;
            }
        }
        // System.out.println("removeRight_end");
        return true;
    }

    private boolean removeLeft(Node pred, Node curr) {
        if (curr.left == null)
        {
            pred.generation = Integer.max(pred.generation, curr.generation);
            pred.left = curr.right;
            return true;
        }

        if (curr.right == null) {
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

        Node swap_node = new Node(biggest_in_left.key);
        swap_node.left = curr.left;
        swap_node.right = curr.right;
        // if (swap_node.left == biggest_in_left || swap_node.right == biggest_in_left) {
            // System.out.println("FUCK swap_node.left=" + swap_node.left + " swap_node.right=" + swap_node.right + " biggest_in_left=" + biggest_in_left);
            // System.exit(1);
        // }
        if (swap_node.left == biggest_in_left) {
            swap_node.left = biggest_in_left.left;
        }
        if (swap_node.right == biggest_in_left) {
            swap_node.right = null;
        }

        synchronized (biggestPred) {
            synchronized (biggest_in_left) {
                if (!validate(biggestPred, biggest_in_left, curr == biggestPred) || biggest_in_left.right != null) {
                    // if (biggest_in_left.marked) {
                    //     System.out.println("marked biggest_in_left=" + biggest_in_left + " biggestPred=" + biggestPred + " curr=" + curr + " biggest_in_left.left=" + biggest_in_left.left + " biggest_in_left.right=" + biggest_in_left.right);
                    // }
                    return false; // validation failed, retry
                }
                pred.left = swap_node;

                int changeGeneration = this.generation.incrementAndGet();
                if (biggest_in_left.left != null) {
                    biggest_in_left.left.generation = changeGeneration;
                } else {
                    biggestPred.generation = changeGeneration;
                }

                // remove biggest in left from the tree
                if (curr != biggestPred) {
                    biggestPred.right = biggest_in_left.left;
                }
                // System.out.println("biggestPred.left=" + biggestPred.left + " biggestPred.right=" + biggestPred.right + " biggest_in_left=" + biggest_in_left + " biggestPred=" + biggestPred);
                // biggest_in_left.is_bad = true;
                // biggest_in_left.right = null;
                // biggest_in_left.left = null;
            }
        }

        return true;
    }

    private boolean removeInternal(final int key) {
        int i = 1;
        while (true) {
            // System.out.println("remove_find_start");
            FindResult res = find(key);
            // System.out.println("remove_find_end");

            if (res.curr == null || res.curr.marked) {
                return false;
            }

            // System.out.println("remove_sync_start");
            Node pred = res.pred;
            Node curr = res.curr;
            if (i % 1000 == 0) {
                System.out.println("removing pred=" + pred + " curr=" + curr + " curr.left=" + curr.left + " curr.right=" + curr.right);
            }
            i++;

            // System.out.println("curr=" + curr);
            synchronized (pred) {
                // System.out.println("remove_sync_end");
                // System.out.println("remove_sync_start");
                synchronized (curr) {
                    // System.out.println("remove_sync_end");
                    if (!validate(pred, curr, false))
                        continue;

                    curr.marked = true;

                    if (curr == pred.left) {
                        // System.out.println("remove_removeLeft_start");
                        int attempts = 0;
                        while (!removeLeft(pred, curr)) {
                            attempts++;
                            if (attempts % 1000 == 0) {
                                System.out.println("removeLeft attempt " + attempts);
                            }
                        }
                        if (pred.left == curr || pred.right == curr) {
                            // System.out.println("pred.left == curr BAD");
                            System.exit(1);
                        }
                        // System.out.println("remove_removeLeft_end");
                        return true;
                    } else {
                        // System.out.println("remove_removeRight_start");
                        int attempts = 0;
                        while (!removeRight(pred, curr)) {
                            attempts++;
                            if (attempts % 1000 == 0) {
                                System.out.println("removeRight attempt " + attempts);
                            }
                        }
                        if (pred.right == curr || pred.left == curr) {
                            // System.out.println("pred.right == curr BAD");
                            System.exit(1);
                        }
                        // System.out.println("remove_removeRight_end");
                        return true;
                    }
                }
            }
        }
    }

    public boolean remove(final int key) {
        // System.out.println("remove_start");
        boolean result = removeInternal(key);
        if (result) {
            size.decrementAndGet();
            keysum.addAndGet(-key);
        }
        // System.out.println("remove_end");

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