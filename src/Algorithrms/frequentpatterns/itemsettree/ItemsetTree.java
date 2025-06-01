package Algorithrms.frequentpatterns.itemsettree;
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
public class ItemsetTree extends AbstractItemsetTree implements Serializable {
   private static final long serialVersionUID = 1L;
   private int transactionCount = 0; // Biến để đếm số giao dịch
   private int minsup = 0; // Thêm ngưỡng hỗ trợ tối thiểu
   public ItemsetTree() {
       super();
   }
   public ItemsetTree(int minsup) {
       super();
       this.minsup = minsup;
   }
   public void buildTree(String input) throws IOException {
       startTimestamp = System.currentTimeMillis();
       MemoryLogger.getInstance().reset();
       root = new ItemsetTreeNode(null, 0);
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
       HashSet<Integer> frequentItems = new HashSet<>();
       for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
           if (entry.getValue() >= minsup) {
               frequentItems.add(entry.getKey());
           }
       }
       System.out.println("Số mục thường xuyên: " + frequentItems.size() + " / " + itemFrequency.size());
       for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
           List<Integer> transaction = entry.getValue();
           List<Integer> filteredTransaction = new ArrayList<>();
           for (Integer item : transaction) {
               if (frequentItems.contains(item)) {
                   filteredTransaction.add(item);
               }
           }
           filteredTransaction.sort(null);
           if (!filteredTransaction.isEmpty()) {
               int[] itemset = new int[filteredTransaction.size()];
               for (int i = 0; i < filteredTransaction.size(); i++) {
                   itemset[i] = filteredTransaction.get(i);
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
   public int getMinsup() {
       return minsup;
   }
   public void setMinsup(int minsup) {
       this.minsup = minsup;
   }
   public void addTransaction(int[] transaction){
       construct(null, root, transaction);
   }
   private void construct(ItemsetTreeNode parentOfR, ItemsetTreeNode r, int[] s) {
       int[] sr = r.itemset;
       if(same(s, sr)){
           r.support++;
           return;
       }
       if(ancestorOf(s, sr)){
           ItemsetTreeNode newNode = new ItemsetTreeNode(s, r.support +1);
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           return;  // trả về
       }
       int[] l = getLargestCommonAncestor(s, sr);
       if(l != null){ // nếu có một tổ tiên chung lớn nhất
           ItemsetTreeNode newNode = new ItemsetTreeNode(l, r.support +1);
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
           newNode.childs.add(newNode2);
           return;
       }
       int indexLastItemOfR = (sr == null)? 0 : sr.length;
       r.support++;
       for(ItemsetTreeNode ci : r.childs){
           if(same(s, ci.itemset)){ // trường hợp 2
               ci.support++;
               return;
           }
           if(ancestorOf(s, ci.itemset)){ // trường hợp 3
               ItemsetTreeNode newNode = new ItemsetTreeNode(s, ci.support+ 1);
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
               ItemsetTreeNode newNode = new ItemsetTreeNode(ancestor, ci.support+ 1);
               r.childs.add(newNode);
               newNode.childs.add(ci);
               r.childs.remove(ci);
               ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
               newNode.childs.add(newNode2);
               return;
           }
       }
       ItemsetTreeNode newNode = new ItemsetTreeNode(s, 1);
       r.childs.add(newNode);
   }
   public void printStatistics() {
       System.out.println("========== ITEMSET TREE CONSTRUCTION - STATS ============");
       System.out.println(" Thời gian xây dựng cây ~: " + (endTimestamp - startTimestamp)
               + " ms");
       System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
       nodeCount = 0;
       totalItemCountInNodes = 0;
       recursiveStats(root);
       System.out.println(" Số lượng nút: " + nodeCount);
       System.out.println(" Tổng các mục trong tất cả các nút: " + totalItemCountInNodes + " trung bình mỗi nút :" + totalItemCountInNodes / ((double)nodeCount));
       System.out.println("=====================================");
   }
   private void recursiveStats(ItemsetTreeNode root) {
       if(root != null && root.itemset!=null){
           nodeCount++;
           totalItemCountInNodes += root.itemset.length;
       }
       for(ItemsetTreeNode node : root.childs){
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
   private int count(int[] s, ItemsetTreeNode root) {
       int count =0;
       for(ItemsetTreeNode ci : root.childs){
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
   public HashTableIT getFrequentItemsetSubsuming(int[] s){
       HashTableIT hash = new HashTableIT(1000);
       HashSet<Integer> seti = new HashSet<Integer>();
       for(int i=0; i< s.length; i++){
           seti.add(s[i]);
       }
       selectiveMining(s, seti, root, hash);
       return hash;
   }
   private int selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNode t, HashTableIT hash) {
       int childrenSup = 0;
       for(ItemsetTreeNode ci : t.childs){
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
   private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableIT hash, int pos) {
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
