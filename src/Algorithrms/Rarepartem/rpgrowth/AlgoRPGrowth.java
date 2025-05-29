package Rarepartem.rpgrowth;

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

public class AlgoRPGrowth {

    private long startTimestamp;
    private long endTime;
    private int transactionCount = 0;
    private int itemsetCount;
    
    public int minRareSupportRelative;
    public int maxSupportRelative;
    
    BufferedWriter writer = null;
    
    protected Itemsets patterns = null;
        
    final int BUFFERS_SIZE = 2000;
    
    private int[] itemsetBuffer = null;
    private RPNode[] rpNodeTempBuffer = null;
    private int[] itemsetOutputBuffer = null;
    
    private int maxPatternLength = 1000;
    
    private int minPatternLength = 0;
    
    private Map<Integer, Integer> originalGlobalSupport = null;

    public AlgoRPGrowth() {
        
    }

    public Itemsets runAlgorithm(String input, String output, double maxSupp, double minRareSupp) 
            throws FileNotFoundException, IOException {
        startTimestamp = System.currentTimeMillis();
        itemsetCount = 0;
        
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
        
        RPTree tree = new RPTree();
        buildRPTreeFromTransactionMap(input, tree, mapSupport);
        
        tree.createHeaderList(mapSupport);
        
        if(tree.headerList.size() > 0) {
            itemsetBuffer = new int[BUFFERS_SIZE];
            rpNodeTempBuffer = new RPNode[BUFFERS_SIZE];
            
            rpgrowth(tree, itemsetBuffer, 0, transactionCount, mapSupport);
        }
        
        if(writer != null){
            writer.close();
        }
        endTime = System.currentTimeMillis();
        
        MemoryLogger.getInstance().checkMemory();
        
        return patterns;
    }

    private boolean isRareItemGlobal(int item) {
        Integer support = originalGlobalSupport.get(item);
        if (support == null) return false;
        return (support > minRareSupportRelative && support <= maxSupportRelative);
    }

    private boolean isRareItemset(int[] itemset, int itemsetLength) {
        if (itemsetLength == 1) {
            return isRareItemGlobal(itemset[0]);
        } else {
            return true;
        }
    }

    private void buildRPTreeFromTransactionMap(String input, RPTree tree, Map<Integer, Integer> mapSupport) 
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

        int transactionsWithRareItems = 0;
        
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            
            List<Integer> rareItemsInTransaction = new ArrayList<>();
            for (Integer item : transaction) {
                if (isRareItemGlobal(item)) {
                    rareItemsInTransaction.add(item);
                }
            }

            if (!rareItemsInTransaction.isEmpty()) {
                Collections.sort(rareItemsInTransaction, new Comparator<Integer>() {
                    public int compare(Integer item1, Integer item2) {
                        int compare = mapSupport.get(item2) - mapSupport.get(item1);
                        if (compare == 0) {
                            return (item1 - item2);
                        }
                        return compare;
                    }
                });

                tree.addTransaction(rareItemsInTransaction);
                transactionsWithRareItems++;
            }
        }
    }

    private void rpgrowth(RPTree tree, int[] prefix, int prefixLength, int prefixSupport, 
                         Map<Integer, Integer> mapSupport) throws IOException {
        
        if(prefixLength == maxPatternLength){
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
            saveAllCombinationsOfPrefixPath(rpNodeTempBuffer, position, prefix, prefixLength);
        } else {
            for(int i = tree.headerList.size()-1; i >= 0; i--){
                Integer item = tree.headerList.get(i);
                int conditionalSupport = mapSupport.get(item);

                prefix[prefixLength] = item;
                int betaSupport = (prefixSupport < conditionalSupport) ? prefixSupport : conditionalSupport;
                
                if (conditionalSupport > minRareSupportRelative &&
                    conditionalSupport <= maxSupportRelative &&
                    isRareItemset(prefix, prefixLength + 1)) {
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

                    RPTree treeBeta = new RPTree();
                    for(List<RPNode> prefixPath : prefixPaths){
                        treeBeta.addPrefixPath(prefixPath, mapSupportBeta, Math.max(1, (minRareSupportRelative + 1)/2)); 
                    }  
                    
                    if(treeBeta.root.childs.size() > 0){
                        treeBeta.createHeaderList(mapSupportBeta); 
                        rpgrowth(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
                    }
                }
            }
        }
    }

    private void saveAllCombinationsOfPrefixPath(RPNode[] rpNodeTempBuffer, int position, 
            int[] prefix, int prefixLength) throws IOException {

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
            if (newPrefixLength > prefixLength && 
                support > minRareSupportRelative &&
                support <= maxSupportRelative &&
                isRareItemset(prefix, newPrefixLength)) {
                saveItemset(prefix, newPrefixLength, support);
            }
        }
    }

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

    public void printStats() {
        System.out.println("=============  RP-GROWTH - STATS =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Transactions count from database : " + transactionCount);
        System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
        System.out.println(" Rare itemsets count : " + itemsetCount); 
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" Definition: MRT < Support(X) <= MFT");
        System.out.println("============================================================");
    }

    public int getDatabaseSize() {
        return transactionCount;
    }

    public void setMaximumPatternLength(int length) {
        maxPatternLength = length;
    }
    
    public void setMinimumPatternLength(int minPatternLength) {
        this.minPatternLength = minPatternLength;
    }
}