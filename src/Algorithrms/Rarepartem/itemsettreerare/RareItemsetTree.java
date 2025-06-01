package Algorithrms.Rarepartem.itemsettreerare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Iterator;

import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;

public class RareItemsetTree extends AbstractRareItemsetTree implements Serializable {

    private static final long serialVersionUID = 1L;
    private int transactionCount = 0;
    private int minRareSupport = 0;
    private int maxRareSupport = 0;
    
    // TỐI ƯU: Cache thông minh
    private final Map<String, Integer> supportCache = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> cache1Itemsets = new HashMap<>();
    private Map<Integer, Integer> itemFrequency = new HashMap<>();

    public RareItemsetTree() {
        super();
    }
    
    public RareItemsetTree(int minRareSupport, int maxRareSupport) {
        super();
        this.minRareSupport = minRareSupport;
        this.maxRareSupport = maxRareSupport;
    }

    // ==========================================
    // MẪU XÂY DỰNG CÂY HOÀN CHỈNH
    // ==========================================
    public void buildTree(String input) throws IOException {
        startTimestamp = System.currentTimeMillis();
        MemoryLogger.getInstance().reset();
        
        root = new RareItemsetTreeNode(null, 0);
        transactionCount = 0;

        BufferedReader reader = new BufferedReader(new FileReader(input), 65536);
        String line;

        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Tệp đầu vào rỗng hoặc không hợp lệ");
        }

        itemFrequency = new HashMap<>();
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();

        // MẪU ĐỌC DỮ LIỆU: ItemsetTree format
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
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(itemId);
                itemFrequency.merge(itemId, 1, Integer::sum);
            }
        }

        reader.close();
        transactionCount = transactionMap.size();
        
        // TỐI ƯU: Xây dựng cache cho 1-itemsets
        buildCache1Itemsets();
        
        // MẪU PHÂN LOẠI ITEMS: Rare vs Frequent vs Infrequent
        HashSet<Integer> rareItems = new HashSet<>();
        int frequentCount = 0;
        int infrequentCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
            int support = entry.getValue();
            if (support > minRareSupport && support <= maxRareSupport) {
                rareItems.add(entry.getKey());
            } else if (support > maxRareSupport) {
                frequentCount++;
            } else {
                infrequentCount++;
            }
        }
        
        System.out.println("=== PHÂN LOẠI ITEMS ===");
        System.out.println("- Rare items (MRT < support <= MFT): " + rareItems.size());
        System.out.println("- Frequent items (support > MFT): " + frequentCount);
        System.out.println("- Infrequent items (support <= MRT): " + infrequentCount);
        System.out.println("- Định nghĩa: " + minRareSupport + " < Support(X) <= " + maxRareSupport);
        
        // MẪU CẮT TỈA NHẸ: CHỈ loại transactions rỗng
        pruneLightTransactions(transactionMap);
        System.out.println("- Transactions sau cắt tỉa nhẹ: " + transactionMap.size());
        
        // MẪU XÂY DỰNG CÂY: Insert từng transaction
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            transaction.sort(null);
            
            if (!transaction.isEmpty()) {
                int[] itemset = new int[transaction.size()];
                for (int i = 0; i < transaction.size(); i++) {
                    itemset[i] = transaction.get(i);
                }
                construct(null, root, itemset);
            }
        }

        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }

    // TỐI ƯU: Xây dựng cache cho 1-itemsets
    private void buildCache1Itemsets() {
        for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
            cache1Itemsets.put(entry.getKey(), entry.getValue());
        }
    }

    // MẪU CẮT TỈA NHẸ: Chỉ loại transactions rỗng, GIỮ NGUYÊN tất cả items
    private void pruneLightTransactions(Map<Integer, List<Integer>> transactionMap) {
        Iterator<Map.Entry<Integer, List<Integer>>> iterator = transactionMap.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<Integer>> entry = iterator.next();
            List<Integer> transaction = entry.getValue();
            
            // CHỈ loại bỏ transaction rỗng
            if (transaction.size() < 1) {
                iterator.remove();
            }
        }
    }

    // ==========================================
    // MẪU THUẬT TOÁN CONSTRUCT HOÀN CHỈNH
    // ==========================================
    public void addTransaction(int[] transaction){
        construct(null, root, transaction);
    }

    private void construct(RareItemsetTreeNode parentOfR, RareItemsetTreeNode r, int[] s) {
        int[] sr = r.itemset;
        
        // MẪU 1: Nếu s == sr → tăng support
        if(same(s, sr)){
            r.support++;
            return;
        }
        
        // MẪU 2: Nếu s là ancestor của sr → tạo node mới cho s
        if(ancestorOf(s, sr)){
            RareItemsetTreeNode newNode = new RareItemsetTreeNode(s, r.support +1);
            newNode.childs.add(r);
            if(parentOfR != null) {
                parentOfR.childs.remove(r);
                parentOfR.childs.add(newNode);
            }
            return;
        }
        
        // MẪU 3: Nếu có common ancestor → tạo 2 nodes
        int[] l = getLargestCommonAncestor(s, sr);
        if(l != null){
            RareItemsetTreeNode newNode = new RareItemsetTreeNode(l, r.support +1);
            newNode.childs.add(r);
            if(parentOfR != null) {
                parentOfR.childs.remove(r);
                parentOfR.childs.add(newNode);
            }
            RareItemsetTreeNode newNode2 = new RareItemsetTreeNode(s, 1);
            newNode.childs.add(newNode2);
            return;
        }
        
        // MẪU 4: Duyệt children để tìm vị trí phù hợp
        int indexLastItemOfR = (sr == null)? 0 : sr.length;
        r.support++;
        
        // Clone để avoid ConcurrentModificationException
        List<RareItemsetTreeNode> childrenCopy = new ArrayList<>(r.childs);
        
        for(RareItemsetTreeNode ci : childrenCopy){
            
            if(same(s, ci.itemset)){
                ci.support++;
                return;
            }
            
            if(ancestorOf(s, ci.itemset)){
                RareItemsetTreeNode newNode = new RareItemsetTreeNode(s, ci.support+ 1);
                newNode.childs.add(ci);
                r.childs.remove(ci);
                r.childs.add(newNode);
                return;
            }
            
            if(ancestorOf(ci.itemset, s)){
                construct(r, ci, s);
                return;
            }

            if(ci.itemset != null && s != null && 
               ci.itemset.length > indexLastItemOfR && s.length > indexLastItemOfR &&
               ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
                int[] ancestor = getLargestCommonAncestor(s, ci.itemset);
                RareItemsetTreeNode newNode = new RareItemsetTreeNode(ancestor, ci.support+ 1);
                r.childs.add(newNode);
                newNode.childs.add(ci);
                r.childs.remove(ci);
                RareItemsetTreeNode newNode2 = new RareItemsetTreeNode(s, 1);
                newNode.childs.add(newNode2);
                return;
            }
        }
        
        // MẪU 5: Tạo node lá mới
        RareItemsetTreeNode newNode = new RareItemsetTreeNode(s, 1);
        r.childs.add(newNode);
    }

    // ==========================================
    // MẪU TÍNH SUPPORT VỚI TỐI ƯU
    // ==========================================
    @Override
    public int getSupportOfItemset(int[] s) {
        String key = java.util.Arrays.toString(s);
        Integer cached = supportCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        int result;
        
        // TỐI ƯU: Fast path cho 1-itemsets
        if (s.length == 1) {
            result = cache1Itemsets.getOrDefault(s[0], 0);
        }
        // MẪU GỐC: Cho itemsets lớn hơn
        else {
            result = count(s, root);
        }
        
        // Cache kết quả
        if (supportCache.size() < 50000) { // Giới hạn cache size
            supportCache.put(key, result);
        }
        
        return result;
    }

    // MẪU TÍNH SUPPORT TỪNG CÂY
    private int count(int[] s, RareItemsetTreeNode root) {
        int count = 0;
        for(RareItemsetTreeNode ci : root.childs){
            if(ci.itemset != null && ci.itemset.length > 0 && ci.itemset[0] <= s[0]){
                if(includedIn(s, ci.itemset)){
                    count += ci.support;
                }else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){  
                    count += count(s, ci);
                }
            }
        }
        return count;
    }

    private boolean includedIn(int[] itemset1, int[] itemset2) {
        int count = 0;
        for(int i=0; i< itemset2.length; i++){
            if(itemset2[i] == itemset1[count]){
                count++;
                if(count == itemset1.length){
                    return true;
                }
            }
        }
        return false;
    }

    // ==========================================
    // MẪU KHAI THÁC HOÀN CHỈNH - KHÔNG CẮT TỈA QUÁ MẠNH
    // ==========================================
    public List<Itemset> mineAllRareItemsetsWithPruning(String inputFile, int minRareSupport, int maxRareSupport) throws IOException {
        List<Itemset> allRareItemsets = new ArrayList<>();
        
        // MẪU TÌM TẤT CẢ ITEMS
        Set<Integer> allItems = findAllItems(inputFile);
        List<Integer> sortedItems = new ArrayList<>(allItems);
        Collections.sort(sortedItems);
        
        System.out.println("Tổng số items trong dữ liệu: " + sortedItems.size());
        
        // MẪU KHAI THÁC 1-ITEMSETS
        List<Integer> rareItems = new ArrayList<>();
        for (int item : sortedItems) {
            int[] itemset = new int[]{item};
            int support = getSupportOfItemset(itemset);
            
            // MẪU KIỂM TRA RARE: MRT < support <= MFT
            if (support > minRareSupport && support <= maxRareSupport) {
                rareItems.add(item);
                Itemset is = new Itemset(itemset);
                is.support = support;
                allRareItemsets.add(is);
            }
        }
        
        Collections.sort(rareItems);
        System.out.println("Tìm thấy " + rareItems.size() + " rare 1-itemsets");
        
        // MẪU KHAI THÁC K-ITEMSETS: Level-wise approach
        List<List<Integer>> currentLevel = new ArrayList<>();
        for (Integer item : rareItems) {
            List<Integer> singleItem = new ArrayList<>();
            singleItem.add(item);
            currentLevel.add(singleItem);
        }
        
        int k = 2;
        while (!currentLevel.isEmpty() && k <= rareItems.size()) {
            System.out.println("Đang tìm rare " + k + "-itemsets...");
            
            // MẪU SINH CANDIDATES: Cắt tỉa nhẹ nhàng
            List<List<Integer>> candidates = generateCandidatesWithLightPruning(currentLevel, k);
            List<List<Integer>> nextLevel = new ArrayList<>();
            
            System.out.println("Candidates sau cắt tỉa nhẹ: " + candidates.size());
            
            // MẪU KIỂM TRA SUPPORT CHO TỪNG CANDIDATE
            for (List<Integer> candidate : candidates) {
                int[] itemsetArray = candidate.stream().mapToInt(i -> i).toArray();
                int support = getSupportOfItemset(itemsetArray);
                
                if (support > minRareSupport && support <= maxRareSupport) {
                    Itemset is = new Itemset(itemsetArray);
                    is.support = support;
                    allRareItemsets.add(is);
                    nextLevel.add(candidate);
                }
            }
            
            System.out.println("Tìm thấy " + nextLevel.size() + " rare " + k + "-itemsets");
            currentLevel = nextLevel;
            k++;
        }
        
        return allRareItemsets;
    }

    // MẪU SINH CANDIDATES VỚI CẮT TỈA NHẸ
    private List<List<Integer>> generateCandidatesWithLightPruning(List<List<Integer>> previousLevel, int k) {
        List<List<Integer>> candidates = new ArrayList<>();
        
        // MẪU JOIN: Nối 2 itemsets từ level trước
        for (int i = 0; i < previousLevel.size(); i++) {
            for (int j = i + 1; j < previousLevel.size(); j++) {
                List<Integer> itemset1 = previousLevel.get(i);
                List<Integer> itemset2 = previousLevel.get(j);
                
                // CẮT TỈA NHẸ: Chỉ kiểm tra điều kiện join cơ bản
                if (canJoinBasicCheck(itemset1, itemset2, k)) {
                    List<Integer> candidate = joinItemsets(itemset1, itemset2, k);
                    if (candidate != null && !candidates.contains(candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        
        return candidates;
    }

    // MẪU KIỂM TRA JOIN CỞ BẢN (không quá strict)
    private boolean canJoinBasicCheck(List<Integer> itemset1, List<Integer> itemset2, int k) {
        if (k == 2) {
            return !itemset1.get(0).equals(itemset2.get(0));
        }
        
        // Kiểm tra k-2 items đầu có giống nhau không
        for (int i = 0; i < k - 2; i++) {
            if (!itemset1.get(i).equals(itemset2.get(i))) {
                return false;
            }
        }
        
        return !itemset1.get(k-2).equals(itemset2.get(k-2));
    }

    // MẪU JOIN HAI ITEMSETS
    private List<Integer> joinItemsets(List<Integer> itemset1, List<Integer> itemset2, int k) {
        List<Integer> candidate = new ArrayList<>();
        
        if (k == 2) {
            candidate.add(itemset1.get(0));
            candidate.add(itemset2.get(0));
        } else {
            candidate.addAll(itemset1);
            candidate.add(itemset2.get(k-2));
        }
        
        Collections.sort(candidate);
        return candidate;
    }

    // MẪU TÌM TẤT CẢ ITEMS TRONG FILE
    private Set<Integer> findAllItems(String inputFilePath) throws IOException {
        Set<Integer> items = new HashSet<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        
        reader.readLine(); // Skip header
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;
            }
            
            int itemId = Integer.parseInt(parts[1]);
            items.add(itemId);
        }
        
        reader.close();
        return items;
    }

    // ==========================================
    // GETTERS VÀ UTILITY METHODS
    // ==========================================
    public int getTransactionCount() {
        return transactionCount;
    }

    public int getMinRareSupport() {
        return minRareSupport;
    }

    public int getMaxRareSupport() {
        return maxRareSupport;
    }

    public void setRareSupport(int minRareSupport, int maxRareSupport) {
        this.minRareSupport = minRareSupport;
        this.maxRareSupport = maxRareSupport;
    }

    public void printStatistics() {
        System.out.println("========== RARE ITEMSET TREE CONSTRUCTION - STATS ============");
        System.out.println(" Thời gian xây dựng cây: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
        nodeCount = 0;
        totalItemCountInNodes = 0;
        recursiveStats(root);
        System.out.println(" Số lượng nút: " + nodeCount);
        System.out.println(" Tổng các mục trong tất cả các nút: " + totalItemCountInNodes + " trung bình mỗi nút :" + totalItemCountInNodes / ((double)nodeCount));
        System.out.println(" Định nghĩa rare: " + minRareSupport + " < Support(X) <= " + maxRareSupport);
        System.out.println(" Cache 1-itemsets: " + cache1Itemsets.size());
        System.out.println(" Cache tổng support: " + supportCache.size());
        System.out.println("=====================================");
    }

    private void recursiveStats(RareItemsetTreeNode root) {
        if(root != null && root.itemset!=null){
            nodeCount++;
            totalItemCountInNodes += root.itemset.length;
        }
        for(RareItemsetTreeNode node : root.childs){
            recursiveStats(node);
        }
    }

    @Override
    public RareHashTableIT getRareItemsetSubsuming(int[] s){
        RareHashTableIT hash = new RareHashTableIT(1000);
        
        HashSet<Integer> seti = new HashSet<Integer>();
        for(int i=0; i< s.length; i++){
            seti.add(s[i]);
        }
        selectiveMining(s, seti, root, hash);
        return hash;
    }

    private int selectiveMining(int[] s, HashSet<Integer> seti, RareItemsetTreeNode t, RareHashTableIT hash) {
        int childrenSup = 0;
        for(RareItemsetTreeNode ci : t.childs){
            childrenSup += ci.support;
            if(ci.itemset != null && ci.itemset.length > 0 && ci.itemset[0] <= s[0]){
                if(includedIn(s, ci.itemset)){
                    if(ci.childs.size() ==0){
                        hash.put(s, ci.support);
                        recursiveAdd(s, seti, ci.itemset, ci.support, hash, 0);
                    }else{
                        int remainingSup = ci.support - selectiveMining(s, seti, ci, hash);
                        
                        if (remainingSup > 0) {
                            hash.put(s, remainingSup);
                            recursiveAdd(s, seti, ci.itemset, remainingSup, hash, 0);
                        } 
                    }
                }
                else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
                    selectiveMining(s, seti, ci, hash);
                }
            }
        }
        return childrenSup;
    }

    private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, RareHashTableIT hash, int pos) {
        if(pos >= ci.length){
            return;
        }
        if(!seti.contains(ci[pos])){
            int[] newS = new int[s.length+1];
            int j=0;
            boolean added = false;
            for(Integer item : s){
                if(added || item < ci[pos]){
                    newS[j++] = item;
                }else{
                    newS[j++] = ci[pos];
                    newS[j++] = item;
                    added = true;
                }
            }
            if(j < s.length+1){
                newS[j++] = ci[pos];
            }
            hash.put(newS, cisupport);
            
            recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
        }
        recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
    }
    
    public List<Integer> getAllItems() {
        return itemFrequency.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }
}