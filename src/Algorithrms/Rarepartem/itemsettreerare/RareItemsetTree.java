package Algorithrms.Rarepartem.itemsettreerare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;

/**
 * FIXED: RareItemsetTree with Mixed Mode Support
 * Key improvements:
 * 1. Mixed Mode: Accepts both frequent and rare items in patterns
 * 2. Size Constraints: minSize and maxSize parameters
 * 3. Proper Rare Pattern Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item
 */
public class RareItemsetTree {
    
    // Core parameters
    private int minRareSupport;      // MRT threshold
    private int maxRareSupport;      // MFT threshold
    private int minPatternSize;      // Minimum pattern size
    private int maxPatternSize;      // Maximum pattern size
    
    // Item classification
    private Set<Integer> rareItemsSet;        // Items with MRT < support <= MFT
    private Set<Integer> allValidItemsSet;    // Items with support > MRT (frequent + rare)
    private Map<Integer, Integer> itemSupports; // All item supports
    
    // Tree structure
    private TreeNode root;
    private Map<Integer, List<TreeNode>> headerTable;
    private int transactionCount;
    
    // Statistics
    private int nodeCount;
    private int candidateCount;
    private int prunedCount;
    
    /**
     * Constructor with mixed mode and size constraints
     */
    public RareItemsetTree(int minRareSupport, int maxRareSupport, 
                               int minPatternSize, int maxPatternSize) {
        this.minRareSupport = minRareSupport;
        this.maxRareSupport = maxRareSupport;
        this.minPatternSize = Math.max(1, minPatternSize);
        this.maxPatternSize = Math.max(minPatternSize, maxPatternSize);
        
        this.rareItemsSet = new HashSet<>();
        this.allValidItemsSet = new HashSet<>();
        this.itemSupports = new HashMap<>();
        this.headerTable = new HashMap<>();
        this.root = new TreeNode(-1); // Root node
        this.transactionCount = 0;
        this.nodeCount = 0;
        this.candidateCount = 0;
        this.prunedCount = 0;
        
        System.out.println("=== RARE ITEMSET TREE MIXED INITIALIZED ===");
        System.out.println("MinRareSupport (MRT): " + minRareSupport);
        System.out.println("MaxRareSupport (MFT): " + maxRareSupport);
        System.out.println("Pattern size range: [" + this.minPatternSize + ", " + this.maxPatternSize + "]");
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
        System.out.println("============================================");
    }
    
    /**
     * MODIFIED: Build tree with mixed mode support
     */
    public void buildTreeMixed(String inputFile) throws IOException {
        this.currentInputFile = inputFile; // Store for support calculation
        
        System.out.println("Phase 1: Scanning database to identify items...");
        
        // Phase 1: Scan database and classify items
        scanDatabaseAndClassifyItems(inputFile);
        
        System.out.println("Phase 2: Building mixed itemset tree...");
        
        // Phase 2: Build tree with valid items
        buildTreeFromTransactions(inputFile);
        
        System.out.println("Tree building completed successfully!");
        System.out.println("Nodes created: " + nodeCount);
    }
    
    /**
     * Phase 1: Scan database and classify items into rare/frequent/infrequent
     */
    private void scanDatabaseAndClassifyItems(String inputFile) throws IOException {
        Map<Integer, Integer> itemCounts = new HashMap<>();
        Set<Integer> transactionIds = new HashSet<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        // Skip header if exists
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Input file is empty or invalid");
        }
        
        // Count item frequencies
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            
            if (count > 0) {
                transactionIds.add(transactionId);
                itemCounts.put(itemId, itemCounts.getOrDefault(itemId, 0) + 1);
            }
        }
        reader.close();
        
        this.transactionCount = transactionIds.size();
        this.itemSupports = new HashMap<>(itemCounts);
        
        // Classify items
        int rareCount = 0, frequentCount = 0, infrequentCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
            int item = entry.getKey();
            int support = entry.getValue();
            
            if (support > minRareSupport && support <= maxRareSupport) {
                // Rare items: MRT < support <= MFT
                rareItemsSet.add(item);
                allValidItemsSet.add(item);
                rareCount++;
            } else if (support > maxRareSupport) {
                // Frequent items: support > MFT
                allValidItemsSet.add(item);  // CRITICAL: Include frequent items in mixed mode
                frequentCount++;
            } else {
                // Infrequent items: support <= MRT
                infrequentCount++;
            }
        }
        
        System.out.println("Item classification completed:");
        System.out.println("- Rare items (MRT < sup <= MFT): " + rareCount);
        System.out.println("- Frequent items (sup > MFT): " + frequentCount);
        System.out.println("- Infrequent items (sup <= MRT): " + infrequentCount);
        System.out.println("- Total valid items (rare + frequent): " + allValidItemsSet.size());
    }
    
    /**
     * Phase 2: Build tree from transactions using valid items
     */
    private void buildTreeFromTransactions(String inputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        
        // Read and convert transactions
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        reader.readLine(); // Skip header
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            
            // CRITICAL: Only include valid items (frequent + rare)
            if (count > 0 && allValidItemsSet.contains(itemId)) {
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                }
            }
        }
        reader.close();
        
        // Build tree from converted transactions
        for (List<Integer> transaction : transactionMap.values()) {
            if (!transaction.isEmpty()) {
                // Sort items by support (descending) for better tree structure
                transaction.sort((a, b) -> itemSupports.get(b) - itemSupports.get(a));
                insertTransaction(transaction);
            }
        }
        
        // Build header table
        buildHeaderTable();
    }
    
    /**
     * Insert transaction into tree
     */
    private void insertTransaction(List<Integer> transaction) {
        TreeNode currentNode = root;
        
        for (Integer item : transaction) {
            TreeNode child = currentNode.getChild(item);
            
            if (child == null) {
                // Create new node
                child = new TreeNode(item);
                child.parent = currentNode;
                currentNode.children.add(child);
                nodeCount++;
                
                // Update header table
                headerTable.computeIfAbsent(item, k -> new ArrayList<>()).add(child);
            }
            
            child.support++;
            currentNode = child;
        }
    }
    
    /**
     * Build header table for efficient access
     */
    private void buildHeaderTable() {
        // Header table is built during tree construction
        System.out.println("Header table built with " + headerTable.size() + " items");
    }
    
    /**
     * CORRECTED: Mine with balanced pruning (not over-aggressive)
     */
    public List<Itemset> mineAllMixedRareItemsetsWithPruning(String inputFile) throws IOException {
        this.currentInputFile = inputFile;
        
        System.out.println("Loading transactions to cache for fast support calculation...");
        loadTransactionsToCache(inputFile);
        
        List<Itemset> results = new ArrayList<>();
        
        System.out.println("Starting mixed rare itemsets mining...");
        System.out.println("Target: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("Size constraints: [" + minPatternSize + ", " + maxPatternSize + "]");
        
        // Mine 1-itemsets
        for (Integer item : allValidItemsSet) {
            int support = itemSupports.get(item);
            
            if (isValidRarePattern(new int[]{item}, 1, support)) {
                results.add(createItemset(new int[]{item}, support));
            }
        }
        
        // Mine larger itemsets - START FROM ALL VALID ITEMS (not just rare)
        if (maxPatternSize > 1) {
            for (Integer item : allValidItemsSet) {
                List<Integer> prefix = new ArrayList<>();
                prefix.add(item);
                mineRecursiveBalanced(prefix, results);
            }
        }
        
        System.out.println("Mining completed. Found " + results.size() + " mixed rare itemsets");
        System.out.println("Candidates evaluated: " + candidateCount);
        System.out.println("Candidates pruned: " + prunedCount);
        
        return results;
    }
    
    /**
     * BALANCED: Recursive mining without over-pruning
     */
    private void mineRecursiveBalanced(List<Integer> prefix, List<Itemset> results) {
        if (prefix.size() >= maxPatternSize) {
            return;
        }
        
        // Get candidates with lexicographic ordering
        int lastItem = prefix.get(prefix.size() - 1);
        List<Integer> candidates = new ArrayList<>();
        
        for (Integer item : allValidItemsSet) {
            if (item > lastItem) { // Lexicographic order to avoid duplicates
                candidates.add(item);
            }
        }
        
        for (Integer candidate : candidates) {
            candidateCount++;
            
            List<Integer> extended = new ArrayList<>(prefix);
            extended.add(candidate);
            
            int extendedSupport = calculateSupport(extended);
            
            // Basic pruning: Skip if support too low
            if (extendedSupport <= minRareSupport) {
                prunedCount++;
                continue;
            }
            
            // Check if forms valid rare pattern
            if (isValidRarePattern(extended.stream().mapToInt(i -> i).toArray(), 
                                  extended.size(), extendedSupport)) {
                results.add(createItemset(extended.stream().mapToInt(i -> i).toArray(), 
                                        extendedSupport));
            }
            
            // Continue recursion
            if (extended.size() < maxPatternSize && extendedSupport > minRareSupport) {
                mineRecursiveBalanced(extended, results);
            }
        }
    }
    
    /**
     * Get candidates for extending current prefix
     */
    private List<Integer> getCandidates(List<Integer> prefix) {
        List<Integer> candidates = new ArrayList<>();
        
        // Get the last item in prefix for lexicographic ordering
        int lastItem = prefix.isEmpty() ? -1 : prefix.get(prefix.size() - 1);
        
        for (Integer item : allValidItemsSet) {
            if (item > lastItem) { // Maintain lexicographic order
                candidates.add(item);
            }
        }
        
        return candidates;
    }
    
    // Cache transactions for faster support calculation
    private Map<Integer, Set<Integer>> cachedTransactions = null;
    
    /**
     * OPTIMIZED: Load all transactions into memory once
     */
    private void loadTransactionsToCache(String inputFile) throws IOException {
        if (cachedTransactions != null) return; // Already loaded
        
        cachedTransactions = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        reader.readLine(); // Skip header
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            
            if (count > 0) {
                cachedTransactions.computeIfAbsent(transactionId, k -> new HashSet<>()).add(itemId);
            }
        }
        reader.close();
        
        System.out.println("Loaded " + cachedTransactions.size() + " transactions to cache for fast support calculation");
    }
    
    /**
     * FAST: Calculate exact support using cached transactions
     */
    private int calculateSupport(List<Integer> itemset) {
        if (itemset.isEmpty()) return 0;
        if (cachedTransactions == null) {
            System.err.println("ERROR: Transactions not cached!");
            return 0;
        }
        
        // Count transactions containing all items in itemset
        int support = 0;
        for (Set<Integer> transaction : cachedTransactions.values()) {
            boolean containsAll = true;
            for (Integer item : itemset) {
                if (!transaction.contains(item)) {
                    containsAll = false;
                    break;
                }
            }
            if (containsAll) {
                support++;
            }
        }
        
        return support;
    }
    
    /**
     * DEPRECATED: Remove slow file-based calculation
     */
    private int calculateExactSupportFromFile(List<Integer> itemset) throws IOException {
        // This method is now replaced by fast cached version
        return calculateSupport(itemset);
    }
    
    // Store current input file for support calculation
    private String currentInputFile;
    private String getCurrentInputFile() { return currentInputFile; }
    
    /**
     * CRITICAL: Check if pattern is a valid mixed rare itemset
     */
    private boolean isValidRarePattern(int[] itemset, int size, int support) {
        // Check size constraints
        if (size < minPatternSize || size > maxPatternSize) {
            return false;
        }
        
        // Check support range: MRT < support <= MFT
        if (support <= minRareSupport || support > maxRareSupport) {
            return false;
        }
        
        // Check if contains at least one rare item
        boolean hasRareItem = false;
        for (int item : itemset) {
            if (rareItemsSet.contains(item)) {
                hasRareItem = true;
                break;
            }
        }
        
        return hasRareItem;
    }
    
    /**
     * Create Itemset object
     */
    private Itemset createItemset(int[] items, int support) {
        Itemset itemset = new Itemset(items.clone());
        itemset.support = support;
        return itemset;
    }
    
    // ===== UTILITY METHODS =====
    
    public void printStatistics() {
        System.out.println("=== RARE ITEMSET TREE MIXED STATISTICS ===");
        System.out.println("Transaction count: " + transactionCount);
        System.out.println("Tree nodes created: " + nodeCount);
        System.out.println("Rare items: " + rareItemsSet.size());
        System.out.println("All valid items: " + allValidItemsSet.size());
        System.out.println("Header table entries: " + headerTable.size());
        System.out.println("Pattern size range: [" + minPatternSize + ", " + maxPatternSize + "]");
        System.out.println("Memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println("==========================================");
    }
    
    // Getters
    public int getTransactionCount() { return transactionCount; }
    public int getRareItemsCount() { return rareItemsSet.size(); }
    public int getAllValidItemsCount() { return allValidItemsSet.size(); }
    public int getNodeCount() { return nodeCount; }
    public int getCandidateCount() { return candidateCount; }
    public int getPrunedCount() { return prunedCount; }
    
    // ===== INNER CLASSES =====
    
    /**
     * Tree node for itemset tree
     */
    public static class TreeNode {
        public int item;
        public int support;
        public TreeNode parent;
        public List<TreeNode> children;
        
        public TreeNode(int item) {
            this.item = item;
            this.support = 0;
            this.parent = null;
            this.children = new ArrayList<>();
        }
        
        public TreeNode getChild(int item) {
            for (TreeNode child : children) {
                if (child.item == item) {
                    return child;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            return "Node{item=" + item + ", support=" + support + ", children=" + children.size() + "}";
        }
    }
}