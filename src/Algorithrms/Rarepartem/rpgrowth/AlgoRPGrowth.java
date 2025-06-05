package Algorithrms.Rarepartem.rpgrowth;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

public class AlgoRPGrowth {
    private long startTimestamp;
    private long endTime;
    private int transactionCount = 0;
    private int itemsetCount;
    public int minRareSupportRelative;
    public int maxSupportRelative;
    
    // Thêm 2 tham số mới cho kích thước itemset
    private int minPatternLength = 1;
    private int maxPatternLength = 1000;
    
    BufferedWriter writer = null;
    protected Itemsets patterns = null;
    final int BUFFERS_SIZE = 2000;
    private int[] itemsetBuffer = null;
    private RPNode[] rpNodeTempBuffer = null;
    private int[] itemsetOutputBuffer = null;
    
    private Map<Integer, Integer> originalGlobalSupport = null;
    
    // THÊM: Set để track rare items cho việc kiểm tra mixed patterns
    private Set<Integer> rareItemsSet = null;

    public AlgoRPGrowth() {
    }

    /**
     * Phương thức chính với 4 tham số (giữ nguyên để tương thích)
     */
    public Itemsets runAlgorithm(String input, String output, double maxSupp, double minRareSupp) 
            throws FileNotFoundException, IOException {
        return runAlgorithm(input, output, maxSupp, minRareSupp, 1, 1000);
    }
    
    /**
     * Phương thức mới với 6 tham số bao gồm minSize và maxSize
     */
    public Itemsets runAlgorithm(String input, String output, double maxSupp, double minRareSupp, 
                                int minSize, int maxSize) throws FileNotFoundException, IOException {
        
        startTimestamp = System.currentTimeMillis();
        itemsetCount = 0;
        
        // Thiết lập kích thước min/max
        this.minPatternLength = Math.max(1, minSize);
        this.maxPatternLength = Math.max(minSize, maxSize);
        
        System.out.println("=== RP-GROWTH MIXED MODE PARAMETERS ===");
        System.out.println("MinSize: " + this.minPatternLength);
        System.out.println("MaxSize: " + this.maxPatternLength);
        System.out.println("MinRareSupport: " + minRareSupp);
        System.out.println("MaxSupport: " + maxSupp);
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
        System.out.println("======================================");
        
        MemoryLogger.getInstance().reset();
        MemoryLogger.getInstance().checkMemory();

        if(output == null){
            writer = null;
            patterns = new Itemsets("RARE ITEMSETS");
        }else{
            patterns = null;
            writer = new BufferedWriter(new FileWriter(output)); 
            itemsetOutputBuffer = new int[BUFFERS_SIZE];
        }

        final Map<Integer, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 
        this.originalGlobalSupport = new HashMap<>(mapSupport);
        this.minRareSupportRelative = (int) Math.ceil(minRareSupp * transactionCount) - 1;
        this.maxSupportRelative = (int) Math.ceil(maxSupp * transactionCount);
        
        // THÊM: Xác định rare items để check mixed patterns
        this.rareItemsSet = identifyRareItems(mapSupport);
        System.out.println("Số rare items được xác định: " + rareItemsSet.size());

        RPTreeMixed tree = new RPTreeMixed();  // Sử dụng RPTreeMixed
        buildMixedRPTreeFromTransactionMap(input, tree, mapSupport);
        tree.createHeaderList(mapSupport);

        if(tree.headerList.size() > 0) {
            itemsetBuffer = new int[BUFFERS_SIZE];
            rpNodeTempBuffer = new RPNode[BUFFERS_SIZE];
            rpgrowthMixed(tree, itemsetBuffer, 0, transactionCount, mapSupport);
        }

        if(writer != null){
            writer.close();
        }

        endTime = System.currentTimeMillis();
        MemoryLogger.getInstance().checkMemory();
        return patterns;
    }

    /**
     * THÊM: Xác định tập rare items dựa trên support thresholds
     */
    private Set<Integer> identifyRareItems(Map<Integer, Integer> mapSupport) {
        Set<Integer> rareItems = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : mapSupport.entrySet()) {
            int item = entry.getKey();
            int support = entry.getValue();
            if (support > minRareSupportRelative && support <= maxSupportRelative) {
                rareItems.add(item);
            }
        }
        return rareItems;
    }

    /**
     * THÊM: Kiểm tra xem itemset có chứa ít nhất 1 rare item không
     */
    private boolean containsAtLeastOneRareItem(int[] itemset, int itemsetLength) {
        for (int i = 0; i < itemsetLength; i++) {
            if (rareItemsSet.contains(itemset[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * MODIFIED: Kiểm tra rare itemset với điều kiện mixed
     */
    private boolean isRareItemsetMixed(int[] itemset, int itemsetLength, int support) {
        // Kiểm tra kích thước
        if (itemsetLength < minPatternLength || itemsetLength > maxPatternLength) {
            return false;
        }
        
        // Điều kiện 1: Pattern support phải nằm trong [MRT, MFT]
        boolean supportInRange = (support > minRareSupportRelative && support <= maxSupportRelative);
        
        // Điều kiện 2: Phải có ít nhất 1 rare item
        boolean hasRareItem = containsAtLeastOneRareItem(itemset, itemsetLength);
        
        return supportInRange && hasRareItem;
    }

    /**
     * MODIFIED: Build tree chấp nhận cả frequent và rare items
     */
    private void buildMixedRPTreeFromTransactionMap(String input, RPTreeMixed tree, Map<Integer, Integer> mapSupport) 
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Input file is empty or invalid");
        }

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
                }
            }
        }
        reader.close();

        int transactionsProcessed = 0;
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            
            // THAY ĐỔI: Chấp nhận transaction nếu có ít nhất 1 item có support đủ điều kiện
            List<Integer> validItemsInTransaction = new ArrayList<>();
            
            for (Integer item : transaction) {
                int itemSupport = mapSupport.get(item);
                // Chấp nhận item nếu support > MRT (bao gồm cả frequent và rare)
                if (itemSupport > minRareSupportRelative) {
                    validItemsInTransaction.add(item);
                }
            }

            if (!validItemsInTransaction.isEmpty()) {
                // Sort theo support descending
                Collections.sort(validItemsInTransaction, new Comparator<Integer>() {
                    public int compare(Integer item1, Integer item2) {
                        int compare = mapSupport.get(item2) - mapSupport.get(item1);
                        if (compare == 0) {
                            return (item1 - item2);
                        }
                        return compare;
                    }
                });
                tree.addTransaction(validItemsInTransaction);
                transactionsProcessed++;
            }
        }
        System.out.println("Transactions processed: " + transactionsProcessed);
    }

    /**
     * MODIFIED: RP-Growth với support cho mixed patterns
     */
    private void rpgrowthMixed(RPTreeMixed tree, int[] prefix, int prefixLength, int prefixSupport, 
                              Map<Integer, Integer> mapSupport) throws IOException {
        
        if(prefixLength >= maxPatternLength){
            return;
        }

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

        if(singlePath && position > 0){
            saveAllCombinationsOfPrefixPathMixed(rpNodeTempBuffer, position, prefix, prefixLength);
        } else {
            for(int i = tree.headerList.size()-1; i >= 0; i--){
                Integer item = tree.headerList.get(i);
                int conditionalSupport = mapSupport.get(item);
                prefix[prefixLength] = item;
                int betaSupport = (prefixSupport < conditionalSupport) ? prefixSupport : conditionalSupport;

                // THAY ĐỔI: Kiểm tra mixed rare pattern
                if (isRareItemsetMixed(prefix, prefixLength + 1, betaSupport)) {
                    saveItemset(prefix, prefixLength+1, betaSupport);
                }

                if(prefixLength+1 < maxPatternLength){
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

                    RPTreeMixed treeBeta = new RPTreeMixed();
                    for(List<RPNode> prefixPath : prefixPaths){
                        // THAY ĐỔI: Sử dụng addPrefixPathMixed
                        treeBeta.addPrefixPathMixed(prefixPath, mapSupportBeta, 
                                                   Math.max(1, (minRareSupportRelative + 1)/2)); 
                    }  
                    
                    if(treeBeta.root.childs.size() > 0){
                        treeBeta.createHeaderList(mapSupportBeta); 
                        rpgrowthMixed(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
                    }
                }
            }
        }
    }

    /**
     * MODIFIED: Save combinations với mixed support
     */
    private void saveAllCombinationsOfPrefixPathMixed(RPNode[] rpNodeTempBuffer, int position, 
            int[] prefix, int prefixLength) throws IOException {
        
        for (long i = 1, max = 1 << position; i < max; i++) {
            int newPrefixLength = prefixLength;
            int support = 0;
            
            for (int j = 0; j < position; j++) {
                int isSet = (int) i & (1 << j);
                if (isSet > 0) {
                    if(newPrefixLength >= maxPatternLength){
                        break;
                    }
                    prefix[newPrefixLength++] = rpNodeTempBuffer[j].itemID;
                    support = rpNodeTempBuffer[j].counter;
                }
            }
            
            // THAY ĐỔI: Sử dụng mixed rare itemset check
            if (newPrefixLength > prefixLength && 
                newPrefixLength >= minPatternLength &&
                newPrefixLength <= maxPatternLength &&
                isRareItemsetMixed(prefix, newPrefixLength, support)) {
                saveItemset(prefix, newPrefixLength, support);
            }
        }
    }

    // Các methods khác giữ nguyên...
    private Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
            throws FileNotFoundException, IOException {
        Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Input file is empty or invalid");
        }

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
        return mapSupport;
    }

    private void saveItemset(int[] itemset, int itemsetLength, int support) throws IOException {
        if(itemsetLength < minPatternLength || itemsetLength > maxPatternLength) {
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

    public void printStats() {
        System.out.println("=============  RP-GROWTH MIXED - STATS =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Transactions count from database : " + transactionCount);
        System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
        System.out.println(" Rare itemsets count : " + itemsetCount); 
        System.out.println(" Pattern size range: " + minPatternLength + " - " + maxPatternLength);
        System.out.println(" Rare items identified: " + (rareItemsSet != null ? rareItemsSet.size() : 0));
        System.out.println(" Mode: MIXED (Frequent + Rare Items)");
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("=========================================================");
    }

    public int getDatabaseSize() {
        return transactionCount;
    }

    public void setMaximumPatternLength(int length) {
        maxPatternLength = Math.max(1, length);
    }

    public void setMinimumPatternLength(int minPatternLength) {
        this.minPatternLength = Math.max(1, minPatternLength);
    }
    
    public int getMinimumPatternLength() {
        return minPatternLength;
    }
    
    public int getMaximumPatternLength() {
        return maxPatternLength;
    }
}