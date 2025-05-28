package Rarepartem.eclatrptree;

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

/**
 * Lớp triển khai thuật toán Eclat_RPGrowth để khai thác mẫu hiếm
 * sử dụng biểu diễn dữ liệu theo chiều dọc với BitSet
 * 
 * @author Triển khai dựa trên bài báo "Eclat_RPGrowth: Tìm kiếm mẫu hiếm sử dụng khai thác dọc và cây mẫu hiếm"
 */
public class EclatRPTree {
    
    // Gốc của cây
    private RPTreeNode root = null;
    
    // Ngưỡng MFT (MinFrequentSupportThreshold)
    private double mft;
    
    // Ngưỡng MRT (MinRareSupportThreshold)
    private double mrt;
    
    // Ngưỡng hỗ trợ tuyệt đối
    private int mftAbsolute;
    private int mrtAbsolute;
    
    // Số lượng giao dịch trong cơ sở dữ liệu
    private int transactionCount = 0;
    
    // Biến để ghi lại thống kê
    private long startTimestamp;
    private long endTimestamp;
    
    // Số lượng nút trong cây
    private int nodeCount = 0;

    // Lưu trữ các tập mục hiếm được tìm thấy
    private List<RarePattern> rarePatterns = new ArrayList<>();
    
    // Lưu trữ thông tin về tính hiếm của các mục
    private Map<Integer, Boolean> itemRarityMap = new HashMap<>();
    
    /**
     * Hàm khởi tạo
     * @param mft ngưỡng hỗ trợ tối thiểu cho mục phổ biến (tỷ lệ phần trăm 0-100)
     * @param mrt ngưỡng hỗ trợ tối thiểu cho mục hiếm (tỷ lệ phần trăm 0-100)
     */
    public EclatRPTree(double mft, double mrt) {
        this.mft = mft / 100.0;
        this.mrt = mrt / 100.0;
    }
    
    /**
     * Xây dựng cây RPTree từ tệp đầu vào
     * @param input đường dẫn đến tệp dữ liệu
     * @throws IOException nếu có lỗi khi đọc tệp
     */
    public void buildTree(String input) throws IOException {
        // Ghi lại thời gian bắt đầu
        startTimestamp = System.currentTimeMillis();
        
        // Đặt lại thống kê sử dụng bộ nhớ
        MemoryLogger.getInstance().reset();
        
        // Đọc dữ liệu và chuyển đổi sang định dạng dọc
        Map<Integer, BitSet> verticalDB = readVerticalDB(input);
        
        // Tính ngưỡng hỗ trợ tuyệt đối
        mftAbsolute = (int) Math.ceil(mft * transactionCount);
        mrtAbsolute = (int) Math.ceil(mrt * transactionCount);
        
        // Tạo cây RPTree rỗng
        root = new RPTreeNode(null, null);
        
        // Xây dựng cây từ cơ sở dữ liệu dọc
        buildRPTreeFromVerticalDB(verticalDB);
        
        // Ghi lại thống kê bộ nhớ và thời gian
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Đọc cơ sở dữ liệu và tạo biểu diễn dọc với BitSet
     */
    private Map<Integer, BitSet> readVerticalDB(String input) throws IOException {
        // Phân tích dữ liệu giao dịch từ file input
        // Cấu trúc file: mỗi dòng có dạng "transaction_id item_id count"
        
        Map<Integer, BitSet> verticalDB = new HashMap<>();
        Set<Integer> allItems = new HashSet<>();
        Map<Integer, Set<Integer>> transactions = new HashMap<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        
        // Đọc dòng đầu tiên (số giao dịch và số mục)
        line = reader.readLine();
        if (line != null && !line.trim().isEmpty()) {
            String[] header = line.trim().split(" ");
            if (header.length >= 1) {
                try {
                    transactionCount = Integer.parseInt(header[0]);
                } catch (NumberFormatException e) {
                    // Sử dụng giá trị mặc định nếu không phân tích được
                }
            }
        }
        
        // Đọc từng dòng trong file
        while ((line = reader.readLine()) != null) {
            // Bỏ qua dòng trống hoặc dòng bình luận
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue; 
            }
            
            // Phân tích dòng thành các phần
            String[] parts = line.trim().split(" ");
            
            // Kiểm tra định dạng dòng
            if (parts.length < 2) {
                continue; // Dòng không hợp lệ
            }
            
            try {
                int tid = Integer.parseInt(parts[0]);
                int item = Integer.parseInt(parts[1]);
                
                // Lưu trữ mục
                allItems.add(item);
                
                // Lưu trữ giao dịch
                if (!transactions.containsKey(tid)) {
                    transactions.put(tid, new HashSet<>());
                }
                transactions.get(tid).add(item);
                
            } catch (NumberFormatException e) {
                continue; // Bỏ qua dòng không hợp lệ
            }
        }
        reader.close();
        
        // Cập nhật số lượng giao dịch
        if (transactionCount == 0) {
            transactionCount = transactions.size();
        }
        
        // Debug
        System.out.println("Số giao dịch đã đọc: " + transactions.size());
        System.out.println("Số mục duy nhất: " + allItems.size());
        
        // Tạo BitSet cho mỗi mục
        for (Integer item : allItems) {
            BitSet bitset = new BitSet(transactionCount + 1);
            
            // Đặt bit cho mỗi giao dịch có chứa mục này
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
    
    /**
     * Xây dựng cây RPTree từ cơ sở dữ liệu dọc
     */
    private void buildRPTreeFromVerticalDB(Map<Integer, BitSet> verticalDB) {
        // Tạo danh sách các mục đơn
        List<Item> items = new ArrayList<>();
        
        // Xóa bản đồ tính hiếm của mục và danh sách mẫu hiếm
        itemRarityMap.clear();
        rarePatterns.clear();
        
        for (Map.Entry<Integer, BitSet> entry : verticalDB.entrySet()) {
            int itemId = entry.getKey();
            BitSet bitset = entry.getValue();
            int support = bitset.cardinality();
            
            // Debug
            System.out.println("Mục " + itemId + " - support: " + support);
            
            // Phân loại mục dựa trên support
            boolean isRare = false;
            
            if (support >= mftAbsolute) {
                isRare = false; // Mục phổ biến
            } else if (support >= mrtAbsolute) {
                isRare = true;  // Mục hiếm
                
                // Thêm mục hiếm vào danh sách kết quả
                int[] itemset = new int[]{itemId};
                rarePatterns.add(new RarePattern(itemset, support));
            } else {
                continue; // Bỏ qua mục có support < MRT (nhiễu)
            }
            
            // Lưu thông tin tính hiếm của mục
            itemRarityMap.put(itemId, isRare);
            
            // Tạo đối tượng Item và thêm vào danh sách
            Item item = new Item(itemId, bitset);
            item.setRare(isRare);
            items.add(item);
        }
        
        // Debug
        System.out.println("itemRarityMap: " + itemRarityMap);
        
        // Sắp xếp các mục theo support giảm dần
        Collections.sort(items);
        
        // Thêm từng mục vào cây và tạo các nút cấp 1
        List<RPTreeNode> firstLevelNodes = new ArrayList<>();
        for (Item item : items) {
            RPTreeNode newNode = new RPTreeNode(new int[]{item.getItem()}, item.getBitset());
            root.addChild(newNode);
            firstLevelNodes.add(newNode);
            nodeCount++;
        }
        
        // Tìm các tập mục có 2 phần tử trở lên
        findHigherLevelItemsets(firstLevelNodes);
    }
    
    /**
     * Tìm các tập mục có 2 phần tử trở lên
     */
    private void findHigherLevelItemsets(List<RPTreeNode> firstLevelNodes) {
        System.out.println("Bắt đầu tìm tập mục 2 phần tử trở lên...");
        
        // Tạo tất cả các tập mục 2 phần tử 
        List<RPTreeNode> level2Nodes = new ArrayList<>();
        
        for (int i = 0; i < firstLevelNodes.size(); i++) {
            RPTreeNode node1 = firstLevelNodes.get(i);
            boolean node1HasRareItem = containsRareItem(node1.getItemset());
            
            for (int j = i + 1; j < firstLevelNodes.size(); j++) {
                RPTreeNode node2 = firstLevelNodes.get(j);
                boolean node2HasRareItem = containsRareItem(node2.getItemset());
                
                // Chỉ kết hợp nếu ít nhất một nút chứa mục hiếm
                if (node1HasRareItem || node2HasRareItem) {
                    // Kết hợp hai tập mục
                    int[] combinedItemset = combineItemsets(node1.getItemset(), node2.getItemset());
                    
                    // Tính support bằng phép AND bit
                    BitSet combinedBitset = (BitSet) node1.getBitset().clone();
                    combinedBitset.and(node2.getBitset());
                    int support = combinedBitset.cardinality();
                    
                    System.out.println("Kết hợp " + java.util.Arrays.toString(node1.getItemset()) + 
                                      " + " + java.util.Arrays.toString(node2.getItemset()) + 
                                      " = " + java.util.Arrays.toString(combinedItemset) + 
                                      " (support: " + support + ")");
                    
                    // Chỉ giữ lại tập mục nếu support nằm trong khoảng [MRT, MFT)
                    if (support >= mrtAbsolute && support < mftAbsolute) {
                        // Tạo nút mới và thêm vào cây
                        RPTreeNode combinedNode = new RPTreeNode(combinedItemset, combinedBitset);
                        level2Nodes.add(combinedNode);
                        
                        // Thêm mẫu hiếm vào danh sách kết quả
                        rarePatterns.add(new RarePattern(combinedItemset, support));
                    }
                }
            }
        }
        
        // Tạo các tập mục 3 phần tử trở lên
        if (!level2Nodes.isEmpty()) {
            findLevelNItemsets(level2Nodes, firstLevelNodes, 3);
        }
    }
    
    /**
     * Tìm các tập mục cấp N phần tử trở lên
     */
    private void findLevelNItemsets(List<RPTreeNode> prevLevelNodes, List<RPTreeNode> firstLevelNodes, int level) {
        System.out.println("Bắt đầu tìm tập mục " + level + " phần tử...");
        
        List<RPTreeNode> nextLevelNodes = new ArrayList<>();
        
        // Kết hợp các tập mục cấp trước với các tập mục cấp 1
        for (RPTreeNode prevNode : prevLevelNodes) {
            for (RPTreeNode firstLevelNode : firstLevelNodes) {
                // Không kết hợp nếu firstLevelNode đã có trong prevNode
                if (containsItem(prevNode.getItemset(), firstLevelNode.getItemset()[0])) {
                    continue;
                }
                
                // Ít nhất một trong hai phải chứa mục hiếm
                if (containsRareItem(prevNode.getItemset()) || containsRareItem(firstLevelNode.getItemset())) {
                    // Kết hợp hai tập mục
                    int[] combinedItemset = combineItemsets(prevNode.getItemset(), firstLevelNode.getItemset());
                    
                    // Tính support bằng phép AND bit
                    BitSet combinedBitset = (BitSet) prevNode.getBitset().clone();
                    combinedBitset.and(firstLevelNode.getBitset());
                    int support = combinedBitset.cardinality();
                    
                    System.out.println("Kết hợp " + java.util.Arrays.toString(prevNode.getItemset()) + 
                                      " + " + java.util.Arrays.toString(firstLevelNode.getItemset()) + 
                                      " = " + java.util.Arrays.toString(combinedItemset) + 
                                      " (support: " + support + ")");
                    
                    // Chỉ giữ lại tập mục nếu support nằm trong khoảng [MRT, MFT)
                    if (support >= mrtAbsolute && support < mftAbsolute) {
                        // Tạo nút mới và thêm vào danh sách
                        RPTreeNode combinedNode = new RPTreeNode(combinedItemset, combinedBitset);
                        nextLevelNodes.add(combinedNode);
                        
                        // Thêm mẫu hiếm vào danh sách kết quả
                        rarePatterns.add(new RarePattern(combinedItemset, support));
                    }
                }
            }
        }
        
        // Tiếp tục tìm các tập mục cấp cao hơn nếu còn nút
        if (!nextLevelNodes.isEmpty() && level < 10) { // Giới hạn độ sâu để tránh vòng lặp vô hạn
            findLevelNItemsets(nextLevelNodes, firstLevelNodes, level + 1);
        }
    }
    
    /**
     * Kiểm tra xem một tập mục có chứa một mục cụ thể không
     */
    private boolean containsItem(int[] itemset, int item) {
        for (int i : itemset) {
            if (i == item) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra xem một tập mục có chứa ít nhất một mục hiếm không
     */
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
    
    /**
     * Kết hợp hai tập mục thành một tập mục mới
     */
    private int[] combineItemsets(int[] itemset1, int[] itemset2) {
        if (itemset1 == null) return itemset2;
        if (itemset2 == null) return itemset1;
        
        Set<Integer> combinedSet = new HashSet<>();
        
        // Thêm tất cả mục từ itemset1
        for (int item : itemset1) {
            combinedSet.add(item);
        }
        
        // Thêm tất cả mục từ itemset2
        for (int item : itemset2) {
            combinedSet.add(item);
        }
        
        // Chuyển đổi thành mảng và sắp xếp
        int[] result = new int[combinedSet.size()];
        int index = 0;
        for (Integer item : combinedSet) {
            result[index++] = item;
        }
        java.util.Arrays.sort(result);
        
        return result;
    }
    
    /**
     * In thống kê về thời gian và sử dụng bộ nhớ tối đa cho việc xây dựng cây RPTree
     */
    public void printStatistics() {
        System.out.println("========== ECLAT RPTREE CONSTRUCTION - STATS ============");
        System.out.println(" Thời gian xây dựng cây ~: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
        System.out.println(" Số lượng nút: " + nodeCount);
        System.out.println(" Số lượng mẫu hiếm tìm thấy: " + rarePatterns.size());
        System.out.println("====================================");
    }
    
    /**
     * Lấy danh sách các mẫu hiếm tìm được
     * @return danh sách các mẫu hiếm
     */
    public List<RarePattern> getRarePatterns() {
        // Sắp xếp các mẫu hiếm theo kích thước và support
        Collections.sort(rarePatterns);
        return rarePatterns;
    }
    
    /**
     * Lấy số lượng giao dịch
     * @return số lượng giao dịch
     */
    public int getTransactionCount() {
        return transactionCount;
    }
    
    /**
     * Lấy ngưỡng MFT tuyệt đối
     * @return ngưỡng MFT tuyệt đối
     */
    public int getMftAbsolute() {
        return mftAbsolute;
    }
    
    /**
     * Lấy ngưỡng MRT tuyệt đối
     * @return ngưỡng MRT tuyệt đối
     */
    public int getMrtAbsolute() {
        return mrtAbsolute;
    }
    
    /**
     * Lấy số lượng nút trong cây
     * @return số lượng nút
     */
    public int getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Chuyển cây thành chuỗi để hiển thị
     * @return chuỗi biểu diễn cây
     */
    @Override
    public String toString() {
        if (root == null) return "{}";
        return root.toString();
    }
}