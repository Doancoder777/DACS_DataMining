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
    
    private final Map<String, Integer> supportCache = new ConcurrentHashMap<>();
    private Map<Integer, Integer> itemFrequency = new HashMap<>();

    public RareItemsetTree() {
        super();
    }
    
    public RareItemsetTree(int minRareSupport, int maxRareSupport) {
        super();
        this.minRareSupport = minRareSupport;
        this.maxRareSupport = maxRareSupport;
    }

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

        // Đọc dữ liệu
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
        
        // Xác định rare items
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
        
        // CẮT TỈA TRANSACTIONS: Loại bỏ items không rare và transactions rỗng
        pruneTransactions(transactionMap, rareItems);
        System.out.println("- Transactions sau cắt tỉa: " + transactionMap.size());
        
        // Xây dựng cây
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

    // CẮT TỈA TRANSACTIONS
    private void pruneTransactions(Map<Integer, List<Integer>> transactionMap, Set<Integer> rareItems) {
        Iterator<Map.Entry<Integer, List<Integer>>> iterator = transactionMap.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<Integer>> entry = iterator.next();
            List<Integer> transaction = entry.getValue();
            
            // Loại bỏ items không rare
            transaction.removeIf(item -> !rareItems.contains(item));
            
            // Loại bỏ transaction có ít hơn 1 rare item
            if (transaction.size() < 1) {
                iterator.remove();
            }
        }
    }

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

    public void addTransaction(int[] transaction){
        construct(null, root, transaction);
    }

    private void construct(RareItemsetTreeNode parentOfR, RareItemsetTreeNode r, int[] s) {
        int[] sr = r.itemset;
        
        if(same(s, sr)){
            r.support++;
            return;
        }
        
        if(ancestorOf(s, sr)){
            RareItemsetTreeNode newNode = new RareItemsetTreeNode(s, r.support +1);
            newNode.childs.add(r);
            parentOfR.childs.remove(r);
            parentOfR.childs.add(newNode);
            return;
        }
        
        int[] l = getLargestCommonAncestor(s, sr);
        if(l != null){
            RareItemsetTreeNode newNode = new RareItemsetTreeNode(l, r.support +1);
            newNode.childs.add(r);
            parentOfR.childs.remove(r);
            parentOfR.childs.add(newNode);
            RareItemsetTreeNode newNode2 = new RareItemsetTreeNode(s, 1);
            newNode.childs.add(newNode2);
            return;
        }
        
        int indexLastItemOfR = (sr == null)? 0 : sr.length;
        r.support++;
        for(RareItemsetTreeNode ci : r.childs){
            
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

            if(ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
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
        
        RareItemsetTreeNode newNode = new RareItemsetTreeNode(s, 1);
        r.childs.add(newNode);
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
    public int getSupportOfItemset(int[] s) {
        String key = java.util.Arrays.toString(s);
        Integer cached = supportCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        int result = count(s, root);
        
        if (supportCache.size() < Integer.MAX_VALUE) {
            supportCache.put(key, result);
        }
        
        return result;
    }

    private int count(int[] s, RareItemsetTreeNode root) {
        int count = 0;
        for(RareItemsetTreeNode ci : root.childs){
            if(ci.itemset[0] <= s[0]){
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
            if(ci.itemset[0] <= s[0]){
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

    // KHAI THÁC VỚI CẮT TỈA - THAY THẾ FUNCTION GỐC
    public List<Itemset> mineAllRareItemsetsWithPruning(String inputFile, int minRareSupport, int maxRareSupport) throws IOException {
        List<Itemset> allRareItemsets = new ArrayList<>();
        
        Set<Integer> allItems = findAllItems(inputFile);
        List<Integer> sortedItems = new ArrayList<>(allItems);
        Collections.sort(sortedItems);
        
        System.out.println("Tổng số items trong dữ liệu: " + sortedItems.size());
        
        // Tìm rare 1-itemsets
        List<Integer> rareItems = new ArrayList<>();
        for (int item : sortedItems) {
            int[] itemset = new int[]{item};
            int support = getSupportOfItemset(itemset);
            
            if (support > minRareSupport && support <= maxRareSupport) {
                rareItems.add(item);
                Itemset is = new Itemset(itemset);
                is.support = support;
                allRareItemsets.add(is);
            }
        }
        
        Collections.sort(rareItems);
        System.out.println("Tìm thấy " + rareItems.size() + " rare 1-itemsets");
        
        // Khai thác từ 2-itemsets trở lên với cắt tỉa
        List<List<Integer>> currentLevel = new ArrayList<>();
        for (Integer item : rareItems) {
            List<Integer> singleItem = new ArrayList<>();
            singleItem.add(item);
            currentLevel.add(singleItem);
        }
        
        int k = 2;
        while (!currentLevel.isEmpty() && k <= rareItems.size()) {
            System.out.println("Đang tìm rare " + k + "-itemsets...");
            
            // SINH CANDIDATES VỚI CẮT TỈA
            List<List<Integer>> candidates = generateCandidatesWithPruning(currentLevel, k, minRareSupport, maxRareSupport);
            List<List<Integer>> nextLevel = new ArrayList<>();
            
            System.out.println("Candidates sau cắt tỉa: " + candidates.size());
            
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

    // SINH CANDIDATES VỚI CẮT TỈA
    private List<List<Integer>> generateCandidatesWithPruning(List<List<Integer>> previousLevel, int k, int minRareSupport, int maxRareSupport) {
        List<List<Integer>> candidates = new ArrayList<>();
        
        for (int i = 0; i < previousLevel.size(); i++) {
            for (int j = i + 1; j < previousLevel.size(); j++) {
                List<Integer> itemset1 = previousLevel.get(i);
                List<Integer> itemset2 = previousLevel.get(j);
                
                // CẮT TỈA: Kiểm tra điều kiện join nhanh
                if (!canJoinQuickCheck(itemset1, itemset2, k)) {
                    continue;
                }
                
                List<Integer> candidate = joinItemsets(itemset1, itemset2, k);
                if (candidate == null) continue;
                
                // CẮT TỈA: Kiểm tra tất cả subsets có rare không
                if (hasRareSubsets(candidate, previousLevel)) {
                    // CẮT TỈA: Ước tính upper bound support
                    int upperBound = estimateUpperBoundSupport(candidate);
                    if (upperBound > minRareSupport) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        
        return candidates;
    }

    // KIỂM TRA ĐIỀU KIỆN JOIN NHANH
    private boolean canJoinQuickCheck(List<Integer> itemset1, List<Integer> itemset2, int k) {
        if (k == 2) {
            return !itemset1.get(0).equals(itemset2.get(0));
        }
        
        // Kiểm tra k-2 items đầu có giống nhau không
        for (int i = 0; i < k - 2; i++) {
            if (!itemset1.get(i).equals(itemset2.get(i))) {
                return false;
            }
        }
        
        // Item cuối cùng phải khác nhau
        return !itemset1.get(k-2).equals(itemset2.get(k-2));
    }

    // JOIN HAI ITEMSETS
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

    // KIỂM TRA TẤT CẢ SUBSETS CÓ RARE KHÔNG
    private boolean hasRareSubsets(List<Integer> candidate, List<List<Integer>> previousLevel) {
        for (int i = 0; i < candidate.size(); i++) {
            List<Integer> subset = new ArrayList<>(candidate);
            subset.remove(i);
            
            boolean found = false;
            for (List<Integer> prev : previousLevel) {
                if (prev.equals(subset)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // ƯỚC TÍNH UPPER BOUND SUPPORT
    private int estimateUpperBoundSupport(List<Integer> candidate) {
        int minSupport = Integer.MAX_VALUE;
        
        for (int i = 0; i < candidate.size(); i++) {
            List<Integer> subset = new ArrayList<>(candidate);
            subset.remove(i);
            
            int[] subsetArray = subset.stream().mapToInt(x -> x).toArray();
            int support = getSupportOfItemset(subsetArray);
            
            if (support < minSupport) {
                minSupport = support;
            }
        }
        
        return minSupport == Integer.MAX_VALUE ? 0 : minSupport;
    }

    // TÌM TẤT CẢ ITEMS
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
}