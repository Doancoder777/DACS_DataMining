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
import java.util.Arrays;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;
public class RareItemsetTree extends AbstractRareItemsetTree implements Serializable {
    private static final long serialVersionUID = 1L;
    private int transactionCount = 0;
    private int minRareSupport = 0;
    private int maxRareSupport = 0;
    private final Map<String, Integer> cacheManhMe = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> cacheSupportDon = new HashMap<>(); // Cache cho 1-itemsets
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
        xayDungCacheSupportDon();
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
        catTiaTransactions(transactionMap, rareItems);
        System.out.println("- Transactions sau cắt tỉa: " + transactionMap.size());
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
        nenCay();
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();
    }
    private void xayDungCacheSupportDon() {
        System.out.println("Xây dựng cache support cho 1-itemsets...");
        for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
            cacheSupportDon.put(entry.getKey(), entry.getValue());
        }
        System.out.println("Đã cache " + cacheSupportDon.size() + " 1-itemsets");
    }
    private void nenCay() {
        System.out.println("Đang nén cây để tối ưu truy cập...");
        nenCayDeQuy(root);
        System.out.println("Đã hoàn thành nén cây");
    }
    private void nenCayDeQuy(RareItemsetTreeNode node) {
        if (node.childs.isEmpty()) return;
        List<RareItemsetTreeNode> conDaSapXep = new ArrayList<>(node.childs);
        conDaSapXep.sort((a, b) -> {
            if (a.itemset == null || b.itemset == null) return 0;
            return Arrays.compare(a.itemset, b.itemset);
        });
        node.childs = new ArrayList<>(conDaSapXep);
        for (RareItemsetTreeNode child : node.childs) {
            nenCayDeQuy(child);
        }
    }
    private void catTiaTransactions(Map<Integer, List<Integer>> transactionMap, Set<Integer> rareItems) {
        Iterator<Map.Entry<Integer, List<Integer>>> iterator = transactionMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<Integer>> entry = iterator.next();
            List<Integer> transaction = entry.getValue();
            transaction.removeIf(item -> !rareItems.contains(item));
            if (transaction.size() < 1) {
                iterator.remove();
            }
        }
    }
    @Override
    public int getSupportOfItemset(int[] s) {
        String key = Arrays.toString(s);
        Integer cached = cacheManhMe.get(key);
        if (cached != null) {
            return cached;
        }
        int result;
        if (s.length == 1) {
            result = cacheSupportDon.getOrDefault(s[0], 0);
        }
        else if (s.length == 2) {
            result = tinhHaiItemsetNhanh(s[0], s[1], root);
        }
        else {
            result = countTuuHoa(s, root);
        }
        if (cacheManhMe.size() < 100000) { // Giới hạn cache size
            cacheManhMe.put(key, result);
        }
        return result;
    }
    private int tinhHaiItemsetNhanh(int item1, int item2, RareItemsetTreeNode node) {
        int count = 0;
        if (node.childs.isEmpty()) return 0;
        for (RareItemsetTreeNode child : node.childs) {
            if (child.itemset == null) continue;
            if (chuaItem(child.itemset, item1) && chuaItem(child.itemset, item2)) {
                count += child.support;
            } else if (child.itemset.length > 0 && child.itemset[child.itemset.length - 1] >= Math.max(item1, item2)) {
                count += tinhHaiItemsetNhanh(item1, item2, child);
            }
        }
        return count;
    }
    private int countTuuHoa(int[] s, RareItemsetTreeNode root) {
        int count = 0;
        for (RareItemsetTreeNode ci : root.childs) {
            if (ci.itemset == null) continue;
            if (ci.itemset.length > 0 && ci.itemset[0] > s[s.length - 1]) {
                break; // Không cần kiểm tra con nữa vì đã sắp xếp
            }
            if (ci.itemset.length > 0 && ci.itemset[0] <= s[0]) {
                if (includedIn(s, ci.itemset)) {
                    count += ci.support;
                } else if (ci.itemset[ci.itemset.length - 1] < s[s.length - 1]) {
                    count += countTuuHoa(s, ci);
                }
            }
        }
        return count;
    }
    private boolean chuaItem(int[] itemset, int item) {
        for (int i : itemset) {
            if (i == item) return true;
        }
        return false;
    }
    public Map<String, Integer> tinhSupportTheoLo(List<int[]> itemsets) {
        Map<String, Integer> ketQua = new HashMap<>();
        Map<Integer, List<int[]>> nhomTheoKichThuoc = new HashMap<>();
        for (int[] itemset : itemsets) {
            int size = itemset.length;
            nhomTheoKichThuoc.computeIfAbsent(size, k -> new ArrayList<>()).add(itemset);
        }
        for (Map.Entry<Integer, List<int[]>> entry : nhomTheoKichThuoc.entrySet()) {
            int kichThuoc = entry.getKey();
            List<int[]> cungKichThuoc = entry.getValue();
            if (kichThuoc == 1) {
                for (int[] itemset : cungKichThuoc) {
                    String key = Arrays.toString(itemset);
                    ketQua.put(key, cacheSupportDon.getOrDefault(itemset[0], 0));
                }
            } else {
                for (int[] itemset : cungKichThuoc) {
                    String key = Arrays.toString(itemset);
                    ketQua.put(key, getSupportOfItemset(itemset));
                }
            }
        }
        return ketQua;
    }
    public List<Itemset> mineAllRareItemsetsWithPruning(String inputFile, int minRareSupport, int maxRareSupport) throws IOException {
        List<Itemset> allRareItemsets = new ArrayList<>();
        Set<Integer> allItems = findAllItems(inputFile);
        List<Integer> sortedItems = new ArrayList<>(allItems);
        Collections.sort(sortedItems);
        System.out.println("Tổng số items trong dữ liệu: " + sortedItems.size());
        List<Integer> rareItems = new ArrayList<>();
        List<int[]> itemsets1 = new ArrayList<>();
        for (int item : sortedItems) {
            itemsets1.add(new int[]{item});
        }
        Map<String, Integer> supports1 = tinhSupportTheoLo(itemsets1);
        for (int item : sortedItems) {
            String key = Arrays.toString(new int[]{item});
            int support = supports1.get(key);
            if (support > minRareSupport && support <= maxRareSupport) {
                rareItems.add(item);
                Itemset is = new Itemset(new int[]{item});
                is.support = support;
                allRareItemsets.add(is);
            }
        }
        Collections.sort(rareItems);
        System.out.println("Tìm thấy " + rareItems.size() + " rare 1-itemsets");
        List<List<Integer>> currentLevel = new ArrayList<>();
        for (Integer item : rareItems) {
            List<Integer> singleItem = new ArrayList<>();
            singleItem.add(item);
            currentLevel.add(singleItem);
        }
        int k = 2;
        while (!currentLevel.isEmpty() && k <= rareItems.size()) {
            System.out.println("Đang tìm rare " + k + "-itemsets...");
            List<List<Integer>> candidates = generateCandidatesWithPruning(currentLevel, k, minRareSupport, maxRareSupport);
            List<List<Integer>> nextLevel = new ArrayList<>();
            System.out.println("Candidates sau cắt tỉa: " + candidates.size());
            List<int[]> candidateArrays = candidates.stream()
                .map(list -> list.stream().mapToInt(i -> i).toArray())
                .collect(Collectors.toList());
            Map<String, Integer> candidateSupports = tinhSupportTheoLo(candidateArrays);
            for (int i = 0; i < candidates.size(); i++) {
                List<Integer> candidate = candidates.get(i);
                String key = Arrays.toString(candidateArrays.get(i));
                Integer support = candidateSupports.get(key);
                if (support != null && support > minRareSupport && support <= maxRareSupport) {
                    Itemset is = new Itemset(candidateArrays.get(i));
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
    public int getTransactionCount() { return transactionCount; }
    public int getMinRareSupport() { return minRareSupport; }
    public int getMaxRareSupport() { return maxRareSupport; }
    public void setRareSupport(int minRareSupport, int maxRareSupport) {
        this.minRareSupport = minRareSupport;
        this.maxRareSupport = maxRareSupport;
    }
    public void addTransaction(int[] transaction){ construct(null, root, transaction); }
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
            if(ci.itemset.length > indexLastItemOfR && s.length > indexLastItemOfR && 
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
        System.out.println(" Cache 1-itemsets: " + cacheSupportDon.size());
        System.out.println(" Cache tổng support: " + cacheManhMe.size());
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
    private int count(int[] s, RareItemsetTreeNode root) {
        return countTuuHoa(s, root);
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
    private List<List<Integer>> generateCandidatesWithPruning(List<List<Integer>> previousLevel, int k, int minRareSupport, int maxRareSupport) {
        List<List<Integer>> candidates = new ArrayList<>();
        for (int i = 0; i < previousLevel.size(); i++) {
            for (int j = i + 1; j < previousLevel.size(); j++) {
                List<Integer> itemset1 = previousLevel.get(i);
                List<Integer> itemset2 = previousLevel.get(j);
                if (!canJoinQuickCheck(itemset1, itemset2, k)) {
                    continue;
                }
                List<Integer> candidate = joinItemsets(itemset1, itemset2, k);
                if (candidate == null) continue;
                if (hasRareSubsets(candidate, previousLevel)) {
                    int upperBound = estimateUpperBoundSupport(candidate);
                    if (upperBound > minRareSupport) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        return candidates;
    }
    private boolean canJoinQuickCheck(List<Integer> itemset1, List<Integer> itemset2, int k) {
        if (k == 2) {
            return !itemset1.get(0).equals(itemset2.get(0));
        }
        for (int i = 0; i < k - 2; i++) {
            if (!itemset1.get(i).equals(itemset2.get(i))) {
                return false;
            }
        }
        return !itemset1.get(k-2).equals(itemset2.get(k-2));
    }
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
    private Set<Integer> findAllItems(String inputFilePath) throws IOException {
        Set<Integer> items = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        reader.readLine();
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
