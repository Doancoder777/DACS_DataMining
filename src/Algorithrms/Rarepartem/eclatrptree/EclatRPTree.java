package Algorithrms.Rarepartem.eclatrptree;

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
    
    public EclatRPTree(double mrt, double mft) {
        this.mrt = mrt / 100.0;
        this.mft = mft / 100.0;
    }
    
    public void buildTree(String input) throws IOException {
        startTimestamp = System.currentTimeMillis();
        
        MemoryLogger.getInstance().reset();
        
        // Đọc dữ liệu và tạo vertical database
        Map<Integer, BitSet> verticalDB = readVerticalDB(input);
        
        // Tính ngưỡng tuyệt đối - FIXED: Sử dụng định nghĩa đúng
        // MRT < Support(X) <= MFT
        mrtAbsolute = (int) Math.ceil(mrt * transactionCount) - 1;
        mftAbsolute = (int) Math.ceil(mft * transactionCount);
        
        System.out.println("=== ECLAT RPTREE THRESHOLD CALCULATION ===");
        System.out.println("- Database size: " + transactionCount);
        System.out.println("- MRT: " + (mrt*100) + "% → mrt*count=" + (mrt * transactionCount) + " → ceil-1=" + mrtAbsolute);
        System.out.println("- MFT: " + (mft*100) + "% → mft*count=" + (mft * transactionCount) + " → ceil=" + mftAbsolute);
        System.out.println("- Định nghĩa: " + mrtAbsolute + " < Support(X) <= " + mftAbsolute);
        System.out.println("- Range: (" + mrtAbsolute + ", " + mftAbsolute + "]");
        System.out.println("===============================================");
        
        root = new RPTreeNode(null, null);
        
        buildRPTreeFromVerticalDB(verticalDB);
        
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }
    
    private Map<Integer, BitSet> readVerticalDB(String input) throws IOException {
        Map<Integer, BitSet> verticalDB = new HashMap<>();
        List<List<Integer>> transactions = new ArrayList<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        
        // Skip header nếu có
        reader.readLine();
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue; 
            }
            
            // ĐỌC THEO TRANSACTION FORMAT GIỐNG CÁC THUẬT TOÁN KHÁC
            String[] items = line.trim().split(" ");
            List<Integer> transaction = new ArrayList<>();
            
            for (String itemString : items) {
                if (!itemString.trim().isEmpty()) {
                    try {
                        int item = Integer.parseInt(itemString.trim());
                        transaction.add(item);
                    } catch (NumberFormatException e) {
                        // Skip invalid items
                        continue;
                    }
                }
            }
            
            if (!transaction.isEmpty()) {
                transactions.add(transaction);
            }
        }
        reader.close();
        
        transactionCount = transactions.size();
        System.out.println("Số giao dịch đã đọc: " + transactionCount);
        
        // Tìm tất cả items duy nhất
        Set<Integer> allItems = new HashSet<>();
        for (List<Integer> transaction : transactions) {
            allItems.addAll(transaction);
        }
        
        System.out.println("Số items duy nhất: " + allItems.size());
        
        // Tạo vertical database
        for (Integer item : allItems) {
            BitSet bitset = new BitSet(transactionCount);
            
            for (int tid = 0; tid < transactions.size(); tid++) {
                if (transactions.get(tid).contains(item)) {
                    bitset.set(tid);
                }
            }
            
            verticalDB.put(item, bitset);
        }
        
        return verticalDB;
    }
    
    private void buildRPTreeFromVerticalDB(Map<Integer, BitSet> verticalDB) {
        List<Item> rareItems = new ArrayList<>();
        rarePatterns.clear();
        
        System.out.println("=== PHÂN LOẠI ITEMS (DEBUG) ===");
        int rareCount = 0, frequentCount = 0, infrequentCount = 0;
        
        // DEBUG: In ra một số items để kiểm tra
        System.out.println("DEBUG: MRT=" + mrtAbsolute + ", MFT=" + mftAbsolute);
        
        // Phân loại items
        for (Map.Entry<Integer, BitSet> entry : verticalDB.entrySet()) {
            int itemId = entry.getKey();
            BitSet bitset = entry.getValue();
            int support = bitset.cardinality();
            
            // DEBUG: In ra support của một số items để so sánh với PrePost
            if (rareCount + frequentCount + infrequentCount < 20) {
                String category = "";
                if (support > mrtAbsolute && support <= mftAbsolute) {
                    category = "RARE ✓";
                } else if (support > mftAbsolute) {
                    category = "FREQUENT";
                } else {
                    category = "INFREQUENT";
                }
                System.out.println("Item " + itemId + ": support=" + support + " → " + category);
            }
            
            // FIXED: Sử dụng định nghĩa đúng MRT < Support(X) <= MFT
            if (support > mrtAbsolute && support <= mftAbsolute) {
                // Rare item
                Item item = new Item(itemId, bitset);
                item.setRare(true);
                rareItems.add(item);
                
                // Thêm vào rare patterns
                int[] itemset = new int[]{itemId};
                rarePatterns.add(new RarePattern(itemset, support));
                rareCount++;
                
                System.out.println("→ Item " + itemId + " là RARE (support=" + support + ")");
            } else if (support > mftAbsolute) {
                frequentCount++;
                if (frequentCount <= 5) {
                    System.out.println("→ Item " + itemId + " là FREQUENT (support=" + support + ")");
                }
            } else {
                infrequentCount++;
                if (infrequentCount <= 5) {
                    System.out.println("→ Item " + itemId + " là INFREQUENT (support=" + support + ")");
                }
            }
        }
        
        System.out.println("- Rare items (MRT < support <= MFT): " + rareCount);
        System.out.println("- Frequent items (support > MFT): " + frequentCount);
        System.out.println("- Infrequent items (support <= MRT): " + infrequentCount);
        
        // Sắp xếp rare items theo support giảm dần
        Collections.sort(rareItems);
        
        // Tạo first level nodes
        List<RPTreeNode> firstLevelNodes = new ArrayList<>();
        for (Item item : rareItems) {
            RPTreeNode newNode = new RPTreeNode(new int[]{item.getItem()}, item.getBitset());
            root.addChild(newNode);
            firstLevelNodes.add(newNode);
            nodeCount++;
        }
        
        // Khai thác các itemsets lớn hơn
        if (firstLevelNodes.size() > 1) {
            findHigherLevelItemsets(firstLevelNodes);
        }
    }
    
    private void findHigherLevelItemsets(List<RPTreeNode> nodes) {
        List<RPTreeNode> nextLevelNodes = new ArrayList<>();
        
        // Kết hợp từng cặp nodes
        for (int i = 0; i < nodes.size(); i++) {
            RPTreeNode node1 = nodes.get(i);
            
            for (int j = i + 1; j < nodes.size(); j++) {
                RPTreeNode node2 = nodes.get(j);
                
                // Kết hợp 2 itemsets
                int[] combinedItemset = combineItemsets(node1.getItemset(), node2.getItemset());
                
                // Tính intersection của bitsets
                BitSet combinedBitset = (BitSet) node1.getBitset().clone();
                combinedBitset.and(node2.getBitset());
                int support = combinedBitset.cardinality();
                
                // FIXED: Kiểm tra điều kiện rare đúng
                if (support > mrtAbsolute && support <= mftAbsolute) {
                    RPTreeNode combinedNode = new RPTreeNode(combinedItemset, combinedBitset);
                    nextLevelNodes.add(combinedNode);
                    
                    // Thêm vào rare patterns
                    rarePatterns.add(new RarePattern(combinedItemset, support));
                    nodeCount++;
                }
            }
        }
        
        // Đệ quy cho level tiếp theo
        if (!nextLevelNodes.isEmpty()) {
            findHigherLevelItemsets(nextLevelNodes);
        }
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
        System.out.println(" Thời gian xây dựng: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Bộ nhớ tối đa: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Số lượng nút: " + nodeCount);
        System.out.println(" Số rare itemsets tìm được: " + rarePatterns.size());
        System.out.println(" Định nghĩa rare: " + mrtAbsolute + " < Support(X) <= " + mftAbsolute);
        System.out.println("=========================================================");
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