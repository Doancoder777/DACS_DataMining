package Rarepartem.nlistrare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tools.MemoryLogger;

/**
 * Implementation of PrePost-based algorithm for Rare Pattern Mining
 * This algorithm finds rare itemsets with support in range (minSup, maxSup]
 * 
 * Based on PrePost+ algorithm but modified for rare pattern mining
 * Rare Item Definition: MRT < Support(X) <= MFT
 * where MRT = ceil(minsup * |D|) - 1 and MFT = ceil(maxsup * |D|)
 * 
 * @author Modified for Rare Pattern Mining
 */
public class PrePostRare {

    // the start time and end time of the last algorithm execution
    long startTimestamp;
    long endTimestamp;

    // number of rare itemsets found
    public int outputCount;

    // object to write the output file
    BufferedWriter writer = null;

    public int[][] bf;
    public int bf_cursor;
    public int bf_size;
    public int bf_col;
    public int bf_currentSize;

    public int numOfRareItem; // Number of rare items
    public int minSuppRelative; // minimum support threshold (MRT)
    public int maxSuppRelative; // maximum support threshold (MFT)
    public Item[] item; // list of items sorted by support

    public int[] result; // the current itemset
    public int resultLen; // the size of the current itemset
    public int resultCount;
    public int nlLenSum; // node list length of the current itemset

    // Tree stuff
    public PPCTreeNode ppcRoot;
    public NodeListTreeNode nlRoot;
    public PPCTreeNode[] headTable;
    public int[] headTableLen;
    public int[] itemsetCount;
    public int[] sameItems;
    public int nlNodeCount;
    
    private int numOfTrans; // number of transactions

    /**
     * Comparator to sort items by decreasing order of frequency
     */
    static Comparator<Item> comp = new Comparator<Item>() {
        public int compare(Item a, Item b) {
            return ((Item) b).num - ((Item) a).num;
        }
    };

    /**
     * Run the algorithm for rare pattern mining
     * 
     * @param filename the input file path
     * @param minsup the minimum support threshold as percentage (double)
     * @param maxsup the maximum support threshold as percentage (double)
     * @param output the output file path
     * @throws IOException if error while reading/writing to file
     */
    public void runAlgorithm(String filename, double minsup, double maxsup, String output)
            throws IOException {
        outputCount = 0;
        nlNodeCount = 0;
        ppcRoot = new PPCTreeNode();
        nlRoot = new NodeListTreeNode();
        resultLen = 0;
        resultCount = 0;
        nlLenSum = 0;

        MemoryLogger.getInstance().reset();

        // create object for writing the output file
        writer = new BufferedWriter(new FileWriter(output));

        // record the start time
        startTimestamp = System.currentTimeMillis();

        bf_size = 1000000;
        bf = new int[100000][];
        bf_currentSize = bf_size * 10;
        bf[0] = new int[bf_currentSize];

        bf_cursor = 0;
        bf_col = 0;

        // Read Dataset and find rare items
        getRareItems(filename, minsup, maxsup);

        resultLen = 0;
        result = new int[numOfRareItem];

        // Build tree with rare items only
        buildTree(filename);

        nlRoot.label = numOfRareItem;
        nlRoot.firstChild = null;
        nlRoot.next = null;

        // Initialize tree
        initializeTree();
        sameItems = new int[numOfRareItem];

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;

        // Recursively traverse the tree
        NodeListTreeNode curNode = nlRoot.firstChild;
        NodeListTreeNode next = null;
        while (curNode != null) {
            next = curNode.next;
            // call the recursive "traverse" method
            traverse(curNode, nlRoot, 1, 0);
            for (int c = bf_col; c > from_col; c--) {
                bf[c] = null;
            }
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;
            curNode = next;
        }

        writer.close();
        MemoryLogger.getInstance().checkMemory();

        // record the end time
        endTimestamp = System.currentTimeMillis();
    }

    /**
     * Build the tree with rare items only
     * 
     * @param filename the input filename
     * @throws IOException if an exception while reading/writing to file
     */
    void buildTree(String filename) throws IOException {
        ppcRoot.label = -1;

        // READ THE FILE
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        // we will use a buffer to store each transaction that is read.
        Item[] transaction = new Item[1000];

        // for each line (transaction) until the end of the file
        while (((line = reader.readLine()) != null)) {
            // if the line is a comment, is empty or is a kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#'
                    || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            // split the line into items
            String[] lineSplited = line.split(" ");

            // for each item in the transaction
            int tLen = 0; // tLen
            for (String itemString : lineSplited) {
                // get the item
                int itemX = Integer.parseInt(itemString);

                // add each item from the transaction only if it's rare
                for (int j = 0; j < numOfRareItem; j++) {
                    // if the item appears in the list of rare items, we add it
                    if (itemX == item[j].index) {
                        transaction[tLen] = new Item();
                        transaction[tLen].index = itemX; // the item
                        transaction[tLen].num = 0 - j;
                        tLen++;
                        break;
                    }
                }
            }

            // sort the transaction by support (descending)
            Arrays.sort(transaction, 0, tLen, comp);

            // Build the tree path for this transaction
            int curPos = 0;
            PPCTreeNode curRoot = (ppcRoot);
            PPCTreeNode rightSibling = null;
            
            while (curPos != tLen) {
                PPCTreeNode child = curRoot.firstChild;
                while (child != null) {
                    if (child.label == 0 - transaction[curPos].num) {
                        curPos++;
                        child.count++;
                        curRoot = child;
                        break;
                    }
                    if (child.rightSibling == null) {
                        rightSibling = child;
                        child = null;
                        break;
                    }
                    child = child.rightSibling;
                }
                if (child == null)
                    break;
            }
            
            // Create new nodes for remaining items
            for (int j = curPos; j < tLen; j++) {
                PPCTreeNode ppcNode = new PPCTreeNode();
                ppcNode.label = 0 - transaction[j].num;
                if (rightSibling != null) {
                    rightSibling.rightSibling = ppcNode;
                    rightSibling = null;
                } else {
                    curRoot.firstChild = ppcNode;
                }
                ppcNode.rightSibling = null;
                ppcNode.firstChild = null;
                ppcNode.father = curRoot;
                ppcNode.labelSibling = null;
                ppcNode.count = 1;
                curRoot = ppcNode;
            }
        }
        reader.close();

        // Create a header table for rare items
        headTable = new PPCTreeNode[numOfRareItem];
        headTableLen = new int[numOfRareItem];
        PPCTreeNode[] tempHead = new PPCTreeNode[numOfRareItem];
        itemsetCount = new int[(numOfRareItem - 1) * numOfRareItem / 2];

        // Build header table and calculate 2-itemset supports
        PPCTreeNode root = ppcRoot.firstChild;
        int pre = 0;
        int last = 0;
        
        while (root != null) {
            root.foreIndex = pre;
            pre++;

            if (headTable[root.label] == null) {
                headTable[root.label] = root;
                tempHead[root.label] = root;
            } else {
                tempHead[root.label].labelSibling = root;
                tempHead[root.label] = root;
            }
            headTableLen[root.label]++;

            // Calculate support for 2-itemsets
            PPCTreeNode temp = root.father;
            while (temp.label != -1) {
                itemsetCount[root.label * (root.label - 1) / 2 + temp.label] += root.count;
                temp = temp.father;
            }
            
            // Tree traversal with back index
            if (root.firstChild != null) {
                root = root.firstChild;
            } else {
                root.backIndex = last;
                last++;
                if (root.rightSibling != null) {
                    root = root.rightSibling;
                } else {
                    root = root.father;
                    while (root != null) {
                        root.backIndex = last;
                        last++;
                        if (root.rightSibling != null) {
                            root = root.rightSibling;
                            break;
                        }
                        root = root.father;
                    }
                }
            }
        }
    }

    /**
     * Initialize the tree with rare items
     */
    void initializeTree() {
        NodeListTreeNode lastChild = null;
        for (int t = numOfRareItem - 1; t >= 0; t--) {
            if (bf_cursor > bf_currentSize - headTableLen[t] * 3) {
                bf_col++;
                bf_cursor = 0;
                bf_currentSize = 10 * bf_size;
                bf[bf_col] = new int[bf_currentSize];
            }

            NodeListTreeNode nlNode = new NodeListTreeNode();
            nlNode.label = t;
            nlNode.support = 0;
            nlNode.NLStartinBf = bf_cursor;
            nlNode.NLLength = 0;
            nlNode.NLCol = bf_col;
            nlNode.firstChild = null;
            nlNode.next = null;
            
            PPCTreeNode ni = headTable[t];
            while (ni != null) {
                nlNode.support += ni.count;
                bf[bf_col][bf_cursor++] = ni.foreIndex;
                bf[bf_col][bf_cursor++] = ni.backIndex;
                bf[bf_col][bf_cursor++] = ni.count;
                nlNode.NLLength++;
                ni = ni.labelSibling;
            }
            
            if (nlRoot.firstChild == null) {
                nlRoot.firstChild = nlNode;
                lastChild = nlNode;
            } else {
                lastChild.next = nlNode;
                lastChild = nlNode;
            }
        }
    }

    /**
     * Read the input file to find rare items based on support range
     * 
     * @param filename input file name
     * @param minsup minimum support threshold as percentage
     * @param maxsup maximum support threshold as percentage
     * @throws IOException
     */
    void getRareItems(String filename, double minsup, double maxsup) throws IOException {
        numOfTrans = 0;

        // Scan the database and count the support of each item
        Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        
        while (((line = reader.readLine()) != null)) {
            if (line.isEmpty() == true || line.charAt(0) == '#'
                    || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            numOfTrans++;

            String[] lineSplited = line.split(" ");
            for (String itemString : lineSplited) {
                Integer item = Integer.parseInt(itemString);
                Integer count = mapItemCount.get(item);
                if (count == null) {
                    mapItemCount.put(item, 1);
                } else {
                    mapItemCount.put(item, ++count);
                }
            }
        }
        reader.close();

        // Calculate rare thresholds: MRT < Support(X) <= MFT
        this.minSuppRelative = (int) Math.ceil(minsup * numOfTrans) - 1;
        this.maxSuppRelative = (int) Math.ceil(maxsup * numOfTrans);

        System.out.println("PrePost Rare Mining - Fixed Thresholds:");
        System.out.println("- Input minsup: " + (minsup * 100) + "%");
        System.out.println("- Input maxsup: " + (maxsup * 100) + "%");
        System.out.println("- Database size: " + numOfTrans);
        System.out.println("- MinRareSupport (MRT): " + minSuppRelative);
        System.out.println("- MaxSupport (MFT): " + maxSuppRelative);
        System.out.println("- Rare Item Definition: " + minSuppRelative + " < Support(X) <= " + maxSuppRelative);

        // Filter rare items
        numOfRareItem = mapItemCount.size();
        Item[] tempItems = new Item[numOfRareItem];
        int i = 0;
        
        for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
            // Check if item is rare: MRT < support <= MFT
            if (entry.getValue() > minSuppRelative && entry.getValue() <= maxSuppRelative) {
                tempItems[i] = new Item();
                tempItems[i].index = entry.getKey();
                tempItems[i].num = entry.getValue();
                i++;
            }
        }

        // Copy only rare items
        item = new Item[i];
        System.arraycopy(tempItems, 0, item, 0, i);
        numOfRareItem = item.length;

        // Sort items by support (descending)
        Arrays.sort(item, comp);
        
        System.out.println("- Found " + numOfRareItem + " rare items");
    }

    /**
     * Check if itemset formed by ni and nj is rare
     */
    NodeListTreeNode isRareItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj,
            int level, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {

        if (bf_cursor + ni.NLLength * 3 > bf_currentSize) {
            bf_col++;
            bf_cursor = 0;
            bf_currentSize = bf_size > ni.NLLength * 1000 ? bf_size : ni.NLLength * 1000;
            bf[bf_col] = new int[bf_currentSize];
        }

        NodeListTreeNode nlNode = new NodeListTreeNode();
        nlNode.support = 0;
        nlNode.NLStartinBf = bf_cursor;
        nlNode.NLCol = bf_col;
        nlNode.NLLength = 0;

        int cursor_i = ni.NLStartinBf;
        int cursor_j = nj.NLStartinBf;
        int col_i = ni.NLCol;
        int col_j = nj.NLCol;
        int last_cur = -1;
        
        // Intersect the node lists
        while (cursor_i < ni.NLStartinBf + ni.NLLength * 3
                && cursor_j < nj.NLStartinBf + nj.NLLength * 3) {
            if (bf[col_i][cursor_i] > bf[col_j][cursor_j]
                    && bf[col_i][cursor_i + 1] < bf[col_j][cursor_j + 1]) {
                if (last_cur == cursor_j) {
                    bf[bf_col][bf_cursor - 1] += bf[col_i][cursor_i + 2];
                } else {
                    bf[bf_col][bf_cursor++] = bf[col_j][cursor_j];
                    bf[bf_col][bf_cursor++] = bf[col_j][cursor_j + 1];
                    bf[bf_col][bf_cursor++] = bf[col_i][cursor_i + 2];
                    nlNode.NLLength++;
                }
                nlNode.support += bf[col_i][cursor_i + 2];
                last_cur = cursor_j;
                cursor_i += 3;
            } else if (bf[col_i][cursor_i] < bf[col_j][cursor_j]) {
                cursor_i += 3;
            } else if (bf[col_i][cursor_i + 1] > bf[col_j][cursor_j + 1]) {
                cursor_j += 3;
            }
        }
        
        // Check if the itemset is rare: MRT < support <= MFT
        if (nlNode.support > minSuppRelative && nlNode.support <= maxSuppRelative) {
            if (ni.support == nlNode.support && nlNode.NLLength == 1) {
                sameItems[sameCountRef.count++] = nj.label;
                bf_cursor = nlNode.NLStartinBf;
                nlNode = null;
            } else {
                nlNode.label = nj.label;
                nlNode.firstChild = null;
                nlNode.next = null;
                if (ni.firstChild == null) {
                    ni.firstChild = nlNode;
                    lastChild = nlNode;
                } else {
                    lastChild.next = nlNode;
                    lastChild = nlNode;
                }
            }
            return lastChild;
        } else {
            bf_cursor = nlNode.NLStartinBf;
            nlNode = null;
        }
        return lastChild;
    }

    /**
     * Recursively traverse the tree to find rare itemsets
     */
    public void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot,
            int level, int sameCount) throws IOException {

        MemoryLogger.getInstance().checkMemory();

        NodeListTreeNode sibling = curNode.next;
        NodeListTreeNode lastChild = null;
        
        while (sibling != null) {
            if (level > 1 || (level == 1 && itemsetCount[(curNode.label - 1)
                    * curNode.label / 2 + sibling.label] > minSuppRelative
                    && itemsetCount[(curNode.label - 1) * curNode.label / 2 + sibling.label] <= maxSuppRelative)) {
                
                IntegerByRef sameCountTemp = new IntegerByRef();
                sameCountTemp.count = sameCount;
                lastChild = isRareItemSetFreq(curNode, sibling, level, lastChild, sameCountTemp);
                sameCount = sameCountTemp.count;
            }
            sibling = sibling.next;
        }
        
        resultCount += Math.pow(2.0, sameCount);
        nlLenSum += Math.pow(2.0, sameCount) * curNode.NLLength;

        result[resultLen++] = curNode.label;

        // Write rare itemset(s) to file
        writeRareItemsetsToFile(curNode, sameCount);

        nlNodeCount++;

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;
        NodeListTreeNode child = curNode.firstChild;
        NodeListTreeNode next = null;
        
        while (child != null) {
            next = child.next;
            traverse(child, curNode, level + 1, sameCount);
            for (int c = bf_col; c > from_col; c--) {
                bf[c] = null;
            }
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;
            child = next;
        }
        resultLen--;
    }

    /**
     * Write rare itemsets to file
     */
    private void writeRareItemsetsToFile(NodeListTreeNode curNode, int sameCount)
            throws IOException {

        StringBuilder buffer = new StringBuilder();
        
        // Check if current itemset is rare
        if (curNode.support > minSuppRelative && curNode.support <= maxSuppRelative) {
            outputCount++;

            // Append items from the itemset to the StringBuilder
            for (int i = 0; i < resultLen; i++) {
                buffer.append(item[result[i]].index);
                buffer.append(' ');
            }
            // Append the support of the itemset
            buffer.append("#SUP: ");
            buffer.append(curNode.support);
            buffer.append("\n");
        }
        
        // Write all combinations that can be made using the node list
        if (sameCount > 0) {
            // Generate all subsets of the node list except the empty set
            for (long i = 1, max = 1 << sameCount; i < max; i++) {
                for (int k = 0; k < resultLen; k++) {
                    buffer.append(item[result[k]].index);
                    buffer.append(' ');
                }

                // Create a new subset
                for (int j = 0; j < sameCount; j++) {
                    // Check if the j bit is set to 1
                    int isSet = (int) i & (1 << j);
                    if (isSet > 0) {
                        // If yes, add it to the set
                        buffer.append(item[sameItems[j]].index);
                        buffer.append(' ');
                    }
                }
                buffer.append("#SUP: ");
                buffer.append(curNode.support);
                buffer.append("\n");
                outputCount++;
            }
        }
        
        // Write the stringbuffer to file
        writer.write(buffer.toString());
    }

    /**
     * Print statistics about the latest execution of the algorithm
     */
    public void printStats() {
        System.out.println("========== PREPOST RARE - STATS (FIXED) ============");
        System.out.println(" Transactions count from database: " + numOfTrans);
        System.out.println(" MinRareSupport (MRT): " + minSuppRelative);
        System.out.println(" MaxSupport (MFT): " + maxSuppRelative);
        System.out.println(" Number of rare items: " + numOfRareItem);
        System.out.println(" Number of rare itemsets: " + outputCount);
        System.out.println(" Maximum memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Total time: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Definition: MRT < Support(X) <= MFT (FIXED)");
        System.out.println("=====================================================");
    }

    /**
     * Get the number of transactions in the database
     */
    public int getDatabaseSize() {
        return numOfTrans;
    }

    /**
     * Class to pass an integer by reference
     */
    class IntegerByRef {
        int count;
    }

    class Item {
        public int index;
        public int num;
    }

    class NodeListTreeNode {
        public int label;
        public NodeListTreeNode firstChild;
        public NodeListTreeNode next;
        public int support;
        public int NLStartinBf;
        public int NLLength;
        public int NLCol;
    }

    class PPCTreeNode {
        public int label;
        public PPCTreeNode firstChild;
        public PPCTreeNode rightSibling;
        public PPCTreeNode labelSibling;
        public PPCTreeNode father;
        public int count;
        public int foreIndex;
        public int backIndex;
    }
}