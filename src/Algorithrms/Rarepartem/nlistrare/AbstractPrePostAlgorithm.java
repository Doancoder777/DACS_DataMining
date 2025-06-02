package Algorithrms.Rarepartem.nlistrare;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import tools.MemoryLogger;

public abstract class AbstractPrePostAlgorithm {
    
    protected long startTimestamp;
    protected long endTimestamp;
    protected int outputCount;
    protected int resultCount;
    protected int nlLenSum;
    protected int nlNodeCount;
    protected int numOfTrans;
    
    protected BufferedWriter writer = null;
    
    protected int[][] bf;
    protected int bf_cursor;
    protected int bf_size;
    protected int bf_col;
    protected int bf_currentSize;
    
    protected int numOfRareItem;
    protected int minSuppRelative;
    protected int maxSuppRelative;
    
    protected Item[] item;
    protected PPCTreeNode ppcRoot;
    protected NodeListTreeNode nlRoot;
    protected PPCTreeNode[] headTable;
    protected int[] headTableLen;
    protected int[] itemsetCount;
    
    protected int[] result;
    protected int resultLen;
    protected int[] sameItems;
    
    protected static Comparator<Item> comp = new Comparator<Item>() {
        public int compare(Item a, Item b) {
            return ((Item) b).num - ((Item) a).num;
        }
    };
    
    public AbstractPrePostAlgorithm() {
        MemoryLogger.getInstance().reset();
    }
    
    public abstract void runAlgorithm(String filename, double minsup, double maxsup, String output)
            throws IOException;
    
    protected void initializeBuffer() {
        bf_size = 1000000;
        bf = new int[100000][];
        bf_currentSize = bf_size * 10;
        bf[0] = new int[bf_currentSize];
        bf_cursor = 0;
        bf_col = 0;
    }
    
    protected void initializeAlgorithm() {
        outputCount = 0;
        nlNodeCount = 0;
        ppcRoot = new PPCTreeNode();
        nlRoot = new NodeListTreeNode();
        resultLen = 0;
        resultCount = 0;
        nlLenSum = 0;
        startTimestamp = System.currentTimeMillis();
    }
    
    protected void finalizeExecution() throws IOException {
        if (writer != null) {
            writer.close();
        }
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }
    
    protected void ensureBufferCapacity(int requiredSize) {
        if (bf_cursor + requiredSize > bf_currentSize) {
            bf_col++;
            bf_cursor = 0;
            bf_currentSize = Math.max(bf_size, requiredSize * 1000);
            bf[bf_col] = new int[bf_currentSize];
        }
    }
    
    protected void cleanupBuffer(int from_col) {
        for (int c = bf_col; c > from_col; c--) {
            bf[c] = null;
        }
    }
    
    protected void computeAbsoluteThresholds(double minsup, double maxsup) {
        this.minSuppRelative = (int) Math.ceil(minsup * numOfTrans) - 1;
        this.maxSuppRelative = (int) Math.ceil(maxsup * numOfTrans);
    }
    
    protected boolean isInSupportRange(int support) {
        return support > minSuppRelative && support <= maxSuppRelative;
    }
    
    protected abstract void findTargetItems(String filename, double minsup, double maxsup) throws IOException;
    protected abstract void buildTree(String filename) throws IOException;
    protected abstract void initializeTree();
    protected abstract void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot, 
                                   int level, int sameCount) throws IOException;
    
    public void printStats() {
        System.out.println("========== ALGORITHM STATS ============");
        System.out.println(" Transactions count from database: " + numOfTrans);
        System.out.println(" MinSupport: " + minSuppRelative);
        System.out.println(" MaxSupport: " + maxSuppRelative);
        System.out.println(" Number of target items: " + numOfRareItem);
        System.out.println(" Number of itemsets found: " + outputCount);
        System.out.println(" Maximum memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Total time: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println("=====================================================");
    }
    
    public int getDatabaseSize() { return numOfTrans; }
    public int getOutputCount() { return outputCount; }
    public long getExecutionTime() { return endTimestamp - startTimestamp; }
    
    protected class IntegerByRef {
        public int count;
        public IntegerByRef() { this.count = 0; }
        public IntegerByRef(int count) { this.count = count; }
    }
    
    protected class Item {
        public int index;
        public int num;
        
        public Item() {}
        
        public Item(int index, int num) {
            this.index = index;
            this.num = num;
        }
    }
    
    protected class NodeListTreeNode {
        public int label;
        public NodeListTreeNode firstChild;
        public NodeListTreeNode next;
        public int support;
        public int NLStartinBf;
        public int NLLength;
        public int NLCol;
        
        public NodeListTreeNode() {}
        
        public NodeListTreeNode(int label) {
            this.label = label;
            this.support = 0;
            this.firstChild = null;
            this.next = null;
        }
    }
    
    protected class PPCTreeNode {
        public int label;
        public PPCTreeNode firstChild;
        public PPCTreeNode rightSibling;
        public PPCTreeNode labelSibling;
        public PPCTreeNode father;
        public int count;
        public int foreIndex;
        public int backIndex;
        
        public PPCTreeNode() {
            this.count = 0;
            this.foreIndex = -1;
            this.backIndex = -1;
        }
        
        public PPCTreeNode(int label) {
            this();
            this.label = label;
        }
    }
}