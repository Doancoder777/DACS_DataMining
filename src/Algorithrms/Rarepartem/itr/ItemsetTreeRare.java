package Algorithrms.Rarepartem.itr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;
public class ItemsetTreeRare extends AbstractItemsetTreeRare implements Serializable {
   private static final long serialVersionUID = 1L;
   private int transactionCount = 0; // Biến để đếm số giao dịch
   public ItemsetTreeRare() {
       super();
   }
   public ItemsetTreeRare(int mrt, int mft) {
       super();
       setRareThresholds(mrt, mft);
   }
   public void buildTree(String input) throws IOException {
       startTimestamp = System.currentTimeMillis();
       MemoryLogger.getInstance().reset();
       root = new ItemsetTreeNodeRare(null, 0);
       transactionCount = 0;
       BufferedReader reader = new BufferedReader(new FileReader(input));
       String line;
       line = reader.readLine();
       if (line == null || line.trim().isEmpty()) {
           reader.close();
           throw new IOException("Tệp đầu vào rỗng hoặc không hợp lệ");
       }
       String[] header = line.trim().split(" ");
       if (header.length != 2) {
           reader.close();
           throw new IOException("Dòng đầu tiên phải chứa số giao dịch và số mục");
       }
       Map<Integer, Integer> itemFrequency = new HashMap<>();
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
               itemFrequency.put(itemId, itemFrequency.getOrDefault(itemId, 0) + 1);
           }
       }
       reader.close();
       transactionCount = transactionMap.size();
       System.out.println("=== RARE PATTERN MINING - ITEM CLASSIFICATION ===");
       System.out.println("MRT: " + mrt + ", MFT: " + mft);
       int rareItemCount = 0;
       int frequentItemCount = 0;  
       int noiseItemCount = 0;
       for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
           int support = entry.getValue();
           if (isRareItem(support)) {
               rareItemCount++;
           } else if (isFrequentItem(support)) {
               frequentItemCount++;
           } else {
               noiseItemCount++;
           }
       }
       System.out.println("Rare items (support > " + mrt + " and <= " + mft + "): " + rareItemCount);
       System.out.println("Frequent items (support > " + mft + "): " + frequentItemCount);
       System.out.println("Noise items (support <= " + mrt + "): " + noiseItemCount);
       System.out.println("Total items: " + itemFrequency.size());
       System.out.println("=== BUILDING TREE WITH ALL ITEMS (No Filtering) ===");
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
   public int getTransactionCount() {
       return transactionCount;
   }
   public int getMRT() {
       return mrt;
   }
   public int getMFT() {
       return mft;
   }
   public void addTransaction(int[] transaction){
       construct(null, root, transaction);
   }
   private void construct(ItemsetTreeNodeRare parentOfR, ItemsetTreeNodeRare r, int[] s) {
       int[] sr = r.itemset;
       if(same(s, sr)){
           r.support++;
           return;
       }
       if(ancestorOf(s, sr)){
           ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(s, r.support +1);
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           return;  // trả về
       }
       int[] l = getLargestCommonAncestor(s, sr);
       if(l != null){ // nếu có một tổ tiên chung lớn nhất
           ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(l, r.support +1);
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(s, 1);
           newNode.childs.add(newNode2);
           return;
       }
       int indexLastItemOfR = (sr == null)? 0 : sr.length;
       r.support++;
       for(ItemsetTreeNodeRare ci : r.childs){
           if(same(s, ci.itemset)){ // trường hợp 2
               ci.support++;
               return;
           }
           if(ancestorOf(s, ci.itemset)){ // trường hợp 3
               ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(s, ci.support+ 1);
               newNode.childs.add(ci);
               r.childs.remove(ci);
               r.childs.add(newNode);
               return;
           }
           if(ancestorOf(ci.itemset, s)){ // trường hợp 4
               construct(r, ci, s);
               return;
           }
           if(ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
               int[] ancestor = getLargestCommonAncestor(s, ci.itemset);
               ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(ancestor, ci.support+ 1);
               r.childs.add(newNode);
               newNode.childs.add(ci);
               r.childs.remove(ci);
               ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(s, 1);
               newNode.childs.add(newNode2);
               return;
           }
       }
       ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(s, 1);
       r.childs.add(newNode);
   }
   public void printStatistics() {
       System.out.println("========== RARE PATTERN MINING - STATS ============");
       System.out.println(" Thời gian xây dựng cây: " + (endTimestamp - startTimestamp) + " ms");
       System.out.println(" Bộ nhớ tối đa: " + MemoryLogger.getInstance().getMaxMemory());
       System.out.println(" Số giao dịch: " + transactionCount);
       System.out.println(" MRT (Minimum Rare Threshold): " + mrt);
       System.out.println(" MFT (Maximum Frequent Threshold): " + mft);
       System.out.println(" Rare range: " + (mrt + 1) + " <= support <= " + mft);
       nodeCount = 0;
       totalItemCountInNodes = 0;
       recursiveStats(root);
       System.out.println(" Số lượng nút: " + nodeCount);
       System.out.println(" Tổng các mục trong tất cả các nút: " + totalItemCountInNodes + 
                         " (TB/nút: " + (nodeCount > 0 ? totalItemCountInNodes / (double)nodeCount : 0) + ")");
       System.out.println("=================================================");
   }
   private void recursiveStats(ItemsetTreeNodeRare root) {
       if(root != null && root.itemset!=null){
           nodeCount++;
           totalItemCountInNodes += root.itemset.length;
       }
       for(ItemsetTreeNodeRare node : root.childs){
           recursiveStats(node);
       }
   }
   public void printTree() {
       System.out.println(root.toString(new StringBuilder(),""));
   }
   public String toString() {
       return root.toString(new StringBuilder(), "");
   }
   public int getSupportOfItemset(int[] s) {
       return count(s, root);  // gọi phương thức count.
   }
   private int count(int[] s, ItemsetTreeNodeRare root) {
       int count =0;
       for(ItemsetTreeNodeRare ci : root.childs){
           if(ci.itemset[0]  <= s[0]){
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
       int count = 0; // vị trí hiện tại của itemset1 mà chúng ta muốn tìm trong itemset2
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
   public HashTableITRare getFrequentItemsetSubsuming(int[] s){
       HashTableITRare hash = new HashTableITRare(1000);
       HashSet<Integer> seti = new HashSet<Integer>();
       for(int i=0; i< s.length; i++){
           seti.add(s[i]);
       }
       selectiveMining(s, seti, root, hash);
       return hash;
   }
   private int selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNodeRare t, HashTableITRare hash) {
       int childrenSup = 0;
       for(ItemsetTreeNodeRare ci : t.childs){
           childrenSup += ci.support;
           if(ci.itemset[0]  <= s[0]){
               if(includedIn(s, ci.itemset)){
                   if(ci.childs.size() ==0){
                       hash.put(s, ci.support);
                       recursiveAdd(s, seti, ci.itemset, ci.support, hash, 0);
                   }else{
                       int remainingSup = ci.support - selectiveMining(s, seti, ci, hash);
                       if (remainingSup > 0)
                       {
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
   private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableITRare hash, int pos) {
       if(pos >= ci.length){
           return;
       }
       if(!seti.contains(ci[pos])){
           int[] newS = new int[s.length+1]; // tạo itemset mới
           int j=0;  // vị trí hiện tại
           boolean added = false;  // chỉ ra nếu chúng ta đã thêm mục ở pos rồi
           for(Integer item : s){
               if(added || item < ci[pos]){
                   newS[j++] = item;
               }else{
                   newS[j++] = ci[pos];
                   newS[j++] = item;
                   added = true;  // chúng ta đặt biến đó thành true để không chèn nó hai lần!
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
}
