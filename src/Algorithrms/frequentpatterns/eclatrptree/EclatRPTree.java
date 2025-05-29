package frequentpatterns.eclatrptree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.MemoryLogger;

public class EclatRPTree {
    
    private RPTreeNode root = null;
    
    private double mft;
    
    private double mrt;
    
    private int mftAbsolute;
    private int mrtAbsolute;
    
    private int transactionCount = 0;
    
    private long startTimestamp;
    private long endTimestamp;
    
    private int nodeCount = 0;

    private List<RarePattern> rarePatterns = new ArrayList<>();
    
    private Map<Integer, Boolean> itemRarityMap = new HashMap<>();
    
    public EclatRPTree(double mft, double mrt) {
        this.mft = mft / 100.0;
        this.mrt = mrt / 100.0;
    }
    
    public void buildTree(String input) throws IOException {
        startTimestamp = System.currentTimeMillis();
        
        MemoryLogger.getInstance().reset();
        
        Map<Integer, BitSet> verticalDB = readVerticalDB(input);
        
        mftAbsolute = (int) Math.ceil(mft * transactionCount);
        mrtAbsolute = (int) Math.ceil(mrt * transactionCount);
        
        root = new RPTreeNode(null, null);
        
        buildRPTreeFromVerticalDB(verticalDB);
        
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }
    
    private Map<Integer, BitSet> readVerticalDB(String input) throws IOException {
        
        Map<Integer, BitSet> verticalDB = new HashMap<>();
        Set<Integer> allItems = new HashSet<>();
        Map<Integer, Set<Integer>> transactions = new HashMap<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        
        line = reader.readLine();
        if (line != null && !line.trim().isEmpty()) {
            String[] header = line.trim().split(" ");
            if (header.length >= 1) {
                try {
                    transactionCount = Integer.parseInt(header[0]);
                } catch (NumberFormatException e) {
                    
                }
            }
        }
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue; 
            }
            
            String[] parts = line.trim().split(" ");
            
            if (parts.length < 2) {
                continue;
            }
            
            try {
                int tid = Integer.parseInt(parts[0]);
                int item = Integer.parseInt(parts[1]);
                
                allItems.add(item);
                
                if (!transactions.containsKey(tid)) {
                    transactions.put(tid, new HashSet<>());
                }
                transactions.get(tid).add(item);
                
            } catch (NumberFormatException e) {
                continue;
            }
        }
        reader.close();
        
        if (transactionCount == 0) {
            transactionCount = transactions.size();
        }
        
        System.out.println("Số giao dịch đã đọc: " + transactions.size());
        System.out.println("Số mục duy nhất: " + allItems.size());
        
        for (Integer item : allItems) {
            BitSet bitset = new BitSet(transactionCount + 1);
            
            for (Map.Entry<Integer, Set<Integer>> entry : transactions.entrySet()) {
                int tid = entry.getKey();
                if (entry.getValue().contains(item)) {
                    bitset.set(tid);
                }
            }
            
            verticalDB.put(item, bitset);
        }
        
        return verticalDB;
    }
    
    private void buildRPTreeFromVerticalDB(Map<Integer, BitSet> verticalDB) {
        List<Item> items = new ArrayList<>();
        
        itemRarityMap.clear();
        rarePatterns.clear();
        
        for (Map.Entry<Integer, BitSet> entry : verticalDB.entrySet()) {
            int itemId = entry.getKey();
            BitSet bitset = entry.getValue();
            int support = bitset.cardinality();
            
            System.out.println("Mục " + itemId + " - support: " + support);
            
            boolean isRare = false;
            
            if (support >= mftAbsolute) {
                isRare = false;
            } else if (support >= mrtAbsolute) {
                isRare = true;
                
                int[] itemset = new int[]{itemId};
                rarePatterns.add(new RarePattern(itemset, support));
            } else {
                continue;
            }
            
            itemRarityMap.put(itemId, isRare);
            
            Item item = new Item(itemId, bitset);
            item.setRare(isRare);
            items.add(item);
        }
        
        System.out.println("itemRarityMap: " + itemRarityMap);
        
        Collections.sort(items);
        
        List<RPTreeNode> firstLevelNodes = new ArrayList<>();
        for (Item item : items) {
            RPTreeNode newNode = new RPTreeNode(new int[]{item.getItem()}, item.getBitset());
            root.addChild(newNode);
            firstLevelNodes.add(newNode);
            nodeCount++;
        }
        
        findHigherLevelItemsets(firstLevelNodes);
    }
    
    private void findHigherLevelItemsets(List<RPTreeNode> firstLevelNodes) {
        System.out.println("Bắt đầu tìm tập mục 2 phần tử trở lên...");
        
        List<RPTreeNode> level2Nodes = new ArrayList<>();
        
        for (int i = 0; i < firstLevelNodes.size(); i++) {
            RPTreeNode node1 = firstLevelNodes.get(i);
            boolean node1HasRareItem = containsRareItem(node1.getItemset());
            
            for (int j = i + 1; j < firstLevelNodes.size(); j++) {
                RPTreeNode node2 = firstLevelNodes.get(j);
                boolean node2HasRareItem = containsRareItem(node2.getItemset());
                
                if (node1HasRareItem || node2HasRareItem) {
                    int[] combinedItemset = combineItemsets(node1.getItemset(), node2.getItemset());
                    
                    BitSet combinedBitset = (BitSet) node1.getBitset().clone();
                    combinedBitset.and(node2.getBitset());
                    int support = combinedBitset.cardinality();
                    
                    System.out.println("Kết hợp " + java.util.Arrays.toString(node1.getItemset()) + 
                                      " + " + java.util.Arrays.toString(node2.getItemset()) + 
                                      " = " + java.util.Arrays.toString(combinedItemset) + 
                                      " (support: " + support + ")");
                    
                    if (support >= mrtAbsolute && support < mftAbsolute) {
                        RPTreeNode combinedNode = new RPTreeNode(combinedItemset, combinedBitset);
                        level2Nodes.add(combinedNode);
                        
                        rarePatterns.add(new RarePattern(combinedItemset, support));
                    }
                }
            }
        }
        
        if (!level2Nodes.isEmpty()) {
            findLevelNItemsets(level2Nodes, firstLevelNodes, 3);
        }
    }
    
    private void findLevelNItemsets(List<RPTreeNode> prevLevelNodes, List<RPTreeNode> firstLevelNodes, int level) {
        System.out.println("Bắt đầu tìm tập mục " + level + " phần tử...");
        
        List<RPTreeNode> nextLevelNodes = new ArrayList<>();
        
        for (RPTreeNode prevNode : prevLevelNodes) {
            for (RPTreeNode firstLevelNode : firstLevelNodes) {
                if (containsItem(prevNode.getItemset(), firstLevelNode.getItemset()[0])) {
                    continue;
                }
                
                if (containsRareItem(prevNode.getItemset()) || containsRareItem(firstLevelNode.getItemset())) {
                    int[] combinedItemset = combineItemsets(prevNode.getItemset(), firstLevelNode.getItemset());
                    
                    BitSet combinedBitset = (BitSet) prevNode.getBitset().clone();
                    combinedBitset.and(firstLevelNode.getBitset());
                    int support = combinedBitset.cardinality();
                    
                    System.out.println("Kết hợp " + java.util.Arrays.toString(prevNode.getItemset()) + 
                                      " + " + java.util.Arrays.toString(firstLevelNode.getItemset()) + 
                                      " = " + java.util.Arrays.toString(combinedItemset) + 
                                      " (support: " + support + ")");
                    
                    if (support >= mrtAbsolute && support < mftAbsolute) {
                        RPTreeNode combinedNode = new RPTreeNode(combinedItemset, combinedBitset);
                        nextLevelNodes.add(combinedNode);
                        
                        rarePatterns.add(new RarePattern(combinedItemset, support));
                    }
                }
            }
        }
        
        if (!nextLevelNodes.isEmpty() && level < 10) {
            findLevelNItemsets(nextLevelNodes, firstLevelNodes, level + 1);
        }
    }
    
    private boolean containsItem(int[] itemset, int item) {
        for (int i : itemset) {
            if (i == item) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsRareItem(int[] itemset) {
        if (itemset == null) return false;
        
        for (int item : itemset) {
            Boolean isRare = itemRarityMap.get(item);
            if (isRare != null && isRare) {
                return true;
            }
        }
        return false;
    }
    
    private int[] combineItemsets(int[] itemset1, int[] itemset2) {
        if (itemset1 == null) return itemset2;
        if (itemset2 == null) return itemset1;
        
        Set<Integer> combinedSet = new HashSet<>();
        
        for (int item : itemset1) {
            combinedSet.add(item);
        }
        
        for (int item : itemset2) {
            combinedSet.add(item);
        }
        
        int[] result = new int[combinedSet.size()];
        int index = 0;
        for (Integer item : combinedSet) {
            result[index++] = item;
        }
        java.util.Arrays.sort(result);
        
        return result;
    }
    
    public void printStatistics() {
        System.out.println("========== ECLAT RPTREE CONSTRUCTION - STATS ============");
        System.out.println(" Thời gian xây dựng cây ~: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
        System.out.println(" Số lượng nút: " + nodeCount);
        System.out.println(" Số lượng mẫu hiếm tìm thấy: " + rarePatterns.size());
        System.out.println("====================================");
    }
    
    public List<RarePattern> getRarePatterns() {
        Collections.sort(rarePatterns);
        return rarePatterns;
    }
    
    public int getTransactionCount() {
        return transactionCount;
    }
    
    public int getMftAbsolute() {
        return mftAbsolute;
    }
    
    public int getMrtAbsolute() {
        return mrtAbsolute;
    }
    
    public int getNodeCount() {
        return nodeCount;
    }
    
    @Override
    public String toString() {
        if (root == null) return "{}";
        return root.toString();
    }
}