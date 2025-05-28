package Rarepartem.rpgrowth;

/* This is an implementation of the RP-GROWTH algorithm for Rare Pattern Mining
 * Based on FP-Growth but modified to mine rare patterns in the range (minRareSupp, maxSupp]
 * 
 * CORRECT DEFINITION: MRT < Support(X) <= MFT
 * FINAL FIX: Proper rare itemset mining logic that respects original support values
 * 
 * Reference: Rare Pattern Mining concepts from various papers on rare itemset mining
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

/**
 * This is an implementation of the RP-GROWTH algorithm for mining rare patterns.
 * The algorithm mines itemsets with support in the range (minRareSupport, maxSupport].
 * 
 * Key differences from FP-Growth:
 * 1. Uses TWO thresholds: minRareSupp and maxSupp
 * 2. Filters items to keep only "rare items" (support in range)
 * 3. Mines patterns containing at least one rare item
 * 
 * CORRECT DEFINITION: Rare Item has MRT < Support(X) <= MFT
 * FINAL FIX: Maintains original support definitions throughout mining process
 * 
 * @author Based on FP-Growth by Philippe Fournier-Viger, Modified for RP-Growth
 */
public class AlgoRPGrowth {

    // for statistics
    private long startTimestamp; // start time of the latest execution
    private long endTime; // end time of the latest execution
    private int transactionCount = 0; // transaction count in the database
    private int itemsetCount; // number of rare itemsets found
    
    // RP-Growth specific parameters
    public int minRareSupportRelative; // minimum rare support threshold
    public int maxSupportRelative;     // maximum support threshold (start of frequent)
    
    BufferedWriter writer = null; // object to write the output file
    
    // The patterns that are found 
    protected Itemsets patterns = null;
        
    // Buffer sizes
    final int BUFFERS_SIZE = 2000;
    
    // buffers for mining
    private int[] itemsetBuffer = null;
    private RPNode[] rpNodeTempBuffer = null;
    private int[] itemsetOutputBuffer = null;
    
    /** maximum pattern length */
    private int maxPatternLength = 1000;
    
    /** minimum pattern length */
    private int minPatternLength = 0;
    
    /** Original global support map - maintained throughout mining */
    private Map<Integer, Integer> originalGlobalSupport = null;

    /**
     * Constructor
     */
    public AlgoRPGrowth() {
        
    }

    /**
     * Method to run the RP-Growth algorithm for rare pattern mining.
     * @param input the path to an input file containing a transaction database.
     * @param output the output file path for saving the result (if null, the result 
     *        will be returned by the method instead of being saved).
     * @param maxSupp the maximum support threshold (start of frequent items).
     * @param minRareSupp the minimum rare support threshold.
     * @return the result if no output file path is provided.
     * @throws IOException exception if error reading or writing files
     */
    public Itemsets runAlgorithm(String input, String output, double maxSupp, double minRareSupp) 
            throws FileNotFoundException, IOException {
        // record start time
        startTimestamp = System.currentTimeMillis();
        // number of itemsets found
        itemsetCount = 0;
        
        //initialize tool to record memory usage
        MemoryLogger.getInstance().reset();
        MemoryLogger.getInstance().checkMemory();
        
        // if the user want to keep the result into memory
        if(output == null){
            writer = null;
            patterns = new Itemsets("RARE ITEMSETS");
        }else{ // if the user want to save the result to a file
            patterns = null;
            writer = new BufferedWriter(new FileWriter(output)); 
            itemsetOutputBuffer = new int[BUFFERS_SIZE];
        }
        
        // (1) PREPROCESSING: Initial database scan to determine the frequency of each item
        final Map<Integer, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 
        
        // Store original global support for rare item checking
        this.originalGlobalSupport = new HashMap<>(mapSupport);

        // convert the support thresholds from percentage to absolute values
        this.minRareSupportRelative = (int) Math.ceil(minRareSupp * transactionCount);
        this.maxSupportRelative = (int) Math.ceil(maxSupp * transactionCount);
        
        System.out.println("\n=== RP-GROWTH FINAL FIX VERSION ===");
        System.out.println("RP-Growth thresholds:");
        System.out.println("- MinRareSupport: " + minRareSupportRelative);
        System.out.println("- MaxSupport: " + maxSupportRelative);
        System.out.println("- Rare Item Definition: " + minRareSupportRelative + " < Support(X) <= " + maxSupportRelative);
        
        // DEBUG: Print all items and their support
        System.out.println("\nAll items and their support (ORIGINAL DATABASE):");
        for (Map.Entry<Integer, Integer> entry : mapSupport.entrySet()) {
            int item = entry.getKey();
            int support = entry.getValue();
            boolean isRare = isRareItemGlobal(item);
            System.out.println("  Item " + item + ": support = " + support + 
                             " (" + String.format("%.1f", support * 100.0 / transactionCount) + "%) - " + 
                             (isRare ? "RARE ✅" : "NOT RARE ❌"));
        }
        
        // (2) Build RP-Tree with only rare items
        RPTree tree = new RPTree();
        buildRPTreeFromTransactionMap(input, tree, mapSupport);
        
        // (3) Create header list for rare items only
        tree.createHeaderList(mapSupport);
        
        System.out.println("\nInitial RP-Tree header list: " + tree.headerList);
        
        // (4) Start mining rare patterns
        if(tree.headerList.size() > 0) {
            // initialize the buffers
            itemsetBuffer = new int[BUFFERS_SIZE];
            rpNodeTempBuffer = new RPNode[BUFFERS_SIZE];
            
            System.out.println("\n=== STARTING RP-GROWTH MINING (CONCEPT CORRECT) ===");
            // recursively generate rare itemsets using the rp-tree
            rpgrowth(tree, itemsetBuffer, 0, transactionCount, mapSupport);
        } else {
            System.out.println("❌ No rare items found - RP-Tree is empty!");
        }
        
        // close the output file if the result was saved to a file
        if(writer != null){
            writer.close();
        }
        // record the execution end time
        endTime = System.currentTimeMillis();
        
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();
        
        System.out.println("\n=== RP-GROWTH MINING COMPLETED ===");
        System.out.println("Total itemsets found: " + itemsetCount);
        
        // return the result (if saved to memory)
        return patterns;
    }

    /**
     * Check if an item is rare based on ORIGINAL global support
     */
    private boolean isRareItemGlobal(int item) {
        Integer support = originalGlobalSupport.get(item);
        if (support == null) return false;
        return (support > minRareSupportRelative && support <= maxSupportRelative);
    }

    /**
     * Check if an itemset should be saved as rare itemset
     */
    private boolean isRareItemset(int[] itemset, int itemsetLength) {
        if (itemsetLength == 1) {
            // For 1-itemsets: check if the single item is rare in original database
            return isRareItemGlobal(itemset[0]);
        } else {
            // For k-itemsets (k > 1): check if at least one item is rare
            // This is guaranteed by our tree construction (only rare items in tree)
            // So we just need to check minimum support threshold
            return true; // If we reach here through our tree, itemset contains rare items
        }
    }

    /**
     * Build RP-Tree using ItemsetTree transaction map format
     * Only includes rare items (support in (minRareSupp, maxSupp])
     */
    private void buildRPTreeFromTransactionMap(String input, RPTree tree, Map<Integer, Integer> mapSupport) 
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        // Read first line (number of transactions and items)
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Input file is empty or invalid");
        }

        // Step 1: Read all transactions and group items by transaction_id
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();

        // Read lines to group items by transaction_id
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;
            }

            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;

            if (count > 0) {
                // Add item to transaction (avoid duplicates)
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                }
            }
        }

        reader.close();

        // Step 2: Process each transaction and add to RP-Tree
        int transactionsWithRareItems = 0;
        
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            
            // Filter transaction: keep only rare items
            List<Integer> rareItemsInTransaction = new ArrayList<>();
            for (Integer item : transaction) {
                if (isRareItemGlobal(item)) {
                    rareItemsInTransaction.add(item);
                }
            }

            // Only add transaction if it contains rare items
            if (!rareItemsInTransaction.isEmpty()) {
                // Sort rare items in descending order of support
                Collections.sort(rareItemsInTransaction, new Comparator<Integer>() {
                    public int compare(Integer item1, Integer item2) {
                        int compare = mapSupport.get(item2) - mapSupport.get(item1);
                        if (compare == 0) {
                            return (item1 - item2);
                        }
                        return compare;
                    }
                });

                // Add the sorted transaction to the RP-tree
                tree.addTransaction(rareItemsInTransaction);
                transactionsWithRareItems++;
            }
        }
        
        System.out.println("RP-Tree built with " + transactionsWithRareItems + "/" + transactionMap.size() + 
                          " transactions containing rare items");
        
        // Print rare items found
        System.out.println("Rare items in range (" + minRareSupportRelative + ", " + maxSupportRelative + "]:");
        int rareItemCount = 0;
        for (Map.Entry<Integer, Integer> entry : mapSupport.entrySet()) {
            int item = entry.getKey();
            if (isRareItemGlobal(item)) {
                System.out.println("  Item " + item + ": support = " + entry.getValue());
                rareItemCount++;
            }
        }
        System.out.println("Total rare items: " + rareItemCount);
    }

    /**
     * Mine an RP-Tree to find rare patterns - CONCEPT CORRECT VERSION
     */
    private void rpgrowth(RPTree tree, int[] prefix, int prefixLength, int prefixSupport, 
                         Map<Integer, Integer> mapSupport) throws IOException {
        
        if(prefixLength == maxPatternLength){
            return;
        }
        
        // Check if the RP-tree contains a single path
        boolean singlePath = true;
        int position = 0;
        
        if(tree.root.childs.size() > 1) {
            singlePath = false;
        } else if (tree.root.childs.size() == 1) {
            RPNode currentNode = tree.root.childs.get(0);
            while(true){
                if(currentNode.childs.size() > 1) {
                    singlePath = false;
                    break;
                }
                rpNodeTempBuffer[position] = currentNode;
                position++;
                if(currentNode.childs.size() == 0) {
                    break;
                }
                currentNode = currentNode.childs.get(0);
            }
        }
        
        // Case 1: single path - generate all combinations
        if(singlePath && position > 0){
            saveAllCombinationsOfPrefixPath(rpNodeTempBuffer, position, prefix, prefixLength);
        } else {
            // Case 2: multiple paths - recursive mining
            for(int i = tree.headerList.size()-1; i >= 0; i--){
                Integer item = tree.headerList.get(i);
                int conditionalSupport = mapSupport.get(item);

                // Create new prefix by adding current item
                prefix[prefixLength] = item;
                int betaSupport = (prefixSupport < conditionalSupport) ? prefixSupport : conditionalSupport;
                
                // CONCEPT CORRECT: Check if itemset should be saved
                // For rare itemset mining, we save itemsets that:
                // 1. Contain at least one rare item (guaranteed by tree construction)
                // 2. Meet minimum support threshold (use conditional support)
                if (conditionalSupport >= minRareSupportRelative && isRareItemset(prefix, prefixLength + 1)) {
                    saveItemset(prefix, prefixLength+1, betaSupport);
                }
                
                if(prefixLength+1 < maxPatternLength){
                    // Construct conditional pattern base
                    List<List<RPNode>> prefixPaths = new ArrayList<List<RPNode>>();
                    RPNode path = tree.mapItemNodes.get(item);
                    
                    Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
                    
                    while(path != null){
                        if(path.parent.itemID != -1){
                            List<RPNode> prefixPath = new ArrayList<RPNode>();
                            prefixPath.add(path);
                            
                            int pathCount = path.counter;
                            RPNode parent = path.parent;
                            while(parent.itemID != -1){
                                prefixPath.add(parent);
                                
                                if(mapSupportBeta.get(parent.itemID) == null){
                                    mapSupportBeta.put(parent.itemID, pathCount);
                                }else{
                                    mapSupportBeta.put(parent.itemID, 
                                        mapSupportBeta.get(parent.itemID) + pathCount);
                                }
                                parent = parent.parent;
                            }
                            prefixPaths.add(prefixPath);
                        }
                        path = path.nodeLink;
                    }

                    // Construct conditional RP-Tree
                    RPTree treeBeta = new RPTree();
                    for(List<RPNode> prefixPath : prefixPaths){
                        // CONCEPT CORRECT: Use minimum threshold for conditional tree
                        // to ensure we don't miss rare itemsets
                        treeBeta.addPrefixPath(prefixPath, mapSupportBeta, Math.max(1, minRareSupportRelative/2)); 
                    }  
                    
                    // Mine recursively
                    if(treeBeta.root.childs.size() > 0){
                        treeBeta.createHeaderList(mapSupportBeta); 
                        rpgrowth(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
                    }
                }
            }
        }
    }

    /**
     * Save all combinations of a prefix path
     */
    private void saveAllCombinationsOfPrefixPath(RPNode[] rpNodeTempBuffer, int position, 
            int[] prefix, int prefixLength) throws IOException {

        // Generate all subsets of the prefixPath except the empty set
        for (long i = 1, max = 1 << position; i < max; i++) {
            int newPrefixLength = prefixLength;
            int support = 0;
            
            for (int j = 0; j < position; j++) {
                int isSet = (int) i & (1 << j);
                if (isSet > 0) {
                    if(newPrefixLength == maxPatternLength){
                        break;
                    }
                    prefix[newPrefixLength++] = rpNodeTempBuffer[j].itemID;
                    support = rpNodeTempBuffer[j].counter;
                }
            }
            // save the itemset if it meets criteria
            if (newPrefixLength > prefixLength && 
                support >= minRareSupportRelative && 
                isRareItemset(prefix, newPrefixLength)) {
                saveItemset(prefix, newPrefixLength, support);
            }
        }
    }

    /**
     * Scan database to determine frequency of single items (ItemsetTree format)
     */
    private Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
            throws FileNotFoundException, IOException {
        Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
        
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        
        // Read header
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Input file is empty or invalid");
        }
        
        // Read all transactions and count item frequency
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }

            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;
            }

            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;

            if (count > 0) {
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                    mapSupport.put(itemId, mapSupport.getOrDefault(itemId, 0) + 1);
                }
            }
        }
        
        transactionCount = transactionMap.size();
        reader.close();

        System.out.println("Scanned " + transactionCount + " unique transactions");
        System.out.println("Found " + mapSupport.size() + " unique items");
        
        return mapSupport;
    }

    /**
     * Save a rare itemset
     */
    private void saveItemset(int[] itemset, int itemsetLength, int support) throws IOException {
        if(itemsetLength < minPatternLength) {
            return;
        }
        
        itemsetCount++;
        
        if(writer != null){
            System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
            Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
            
            StringBuilder buffer = new StringBuilder();
            for(int i = 0; i < itemsetLength; i++){
                buffer.append(itemsetOutputBuffer[i]);
                if(i != itemsetLength-1){
                    buffer.append(' ');
                }
            }
            buffer.append(" #SUP: ");
            buffer.append(support);
            writer.write(buffer.toString());
            writer.newLine();
            
        } else {
            int[] itemsetArray = new int[itemsetLength];
            System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);
            Arrays.sort(itemsetArray);
            
            Itemset itemsetObj = new Itemset(itemsetArray);
            itemsetObj.setAbsoluteSupport(support);
            patterns.addItemset(itemsetObj, itemsetLength);
        }
    }

    /**
     * Print statistics about the algorithm execution
     */
    public void printStats() {
        System.out.println("=============  RP-GROWTH - STATS (CONCEPT CORRECT) =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Transactions count from database : " + transactionCount);
        System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
        System.out.println(" Rare itemsets count : " + itemsetCount); 
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" Definition: MRT < Support(X) <= MFT (Original Database)");
        System.out.println("============================================================");
    }

    /**
     * Get the number of transactions in the database
     * @return the number of transactions.
     */
    public int getDatabaseSize() {
        return transactionCount;
    }

    /** Set the maximum pattern length */
    public void setMaximumPatternLength(int length) {
        maxPatternLength = length;
    }
    
    /** Set the minimum pattern length */
    public void setMinimumPatternLength(int minPatternLength) {
        this.minPatternLength = minPatternLength;
    }
}