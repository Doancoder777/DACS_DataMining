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

/**
* Lớp triển khai cây itemset để lưu trữ và khai thác các mẫu thường xuyên.
*/
public class ItemsetTree extends AbstractItemsetTree implements Serializable {

   private static final long serialVersionUID = 1L;
   private int transactionCount = 0; // Biến để đếm số giao dịch
   private int minsup = 0; // Thêm ngưỡng hỗ trợ tối thiểu

   /**
    * Hàm khởi tạo mặc định
    */
   public ItemsetTree() {
       super();
   }
   
   /**
    * Hàm khởi tạo với minsup
    * @param minsup ngưỡng hỗ trợ tối thiểu
    */
   public ItemsetTree(int minsup) {
       super();
       this.minsup = minsup;
   }

   /**
    * Xây dựng cây itemset dựa trên một tệp đầu vào với định dạng:
    * - Dòng đầu: số giao dịch và số mục (phân tách bằng dấu cách)
    * - Các dòng tiếp theo: transaction_id item_id count (phân tách bằng dấu cách)
    * @param input một tệp đầu vào
    * @throws IOException ngoại lệ nếu có lỗi khi đọc tệp
    */
   public void buildTree(String input) throws IOException {
       // ghi lại thời gian bắt đầu
       startTimestamp = System.currentTimeMillis();
       
       // đặt lại thống kê sử dụng bộ nhớ
       MemoryLogger.getInstance().reset();
       
       // tạo một gốc rỗng cho cây
       root = new ItemsetTreeNode(null, 0);
       transactionCount = 0;

       // Quét cơ sở dữ liệu để đọc các giao dịch
       BufferedReader reader = new BufferedReader(new FileReader(input));
       String line;

       // Đọc dòng đầu tiên (số giao dịch và số mục)
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

       // Bước 1: Đọc tất cả giao dịch và đếm tần suất của mỗi mục
       Map<Integer, Integer> itemFrequency = new HashMap<>();
       Map<Integer, List<Integer>> transactionMap = new HashMap<>();

       // Đọc các dòng để nhóm các mục theo transaction_id và đếm tần suất
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
               // Thêm mục vào giao dịch
               transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(itemId);
               
               // Tăng tần suất của mục
               itemFrequency.put(itemId, itemFrequency.getOrDefault(itemId, 0) + 1);
           }
       }

       // Đóng tệp đầu vào
       reader.close();
       
       // Lưu số lượng giao dịch
       transactionCount = transactionMap.size();
       
       // Lọc các mục không thỏa mãn minsup
       HashSet<Integer> frequentItems = new HashSet<>();
       for (Map.Entry<Integer, Integer> entry : itemFrequency.entrySet()) {
           if (entry.getValue() >= minsup) {
               frequentItems.add(entry.getKey());
           }
       }
       
       System.out.println("Số mục thường xuyên: " + frequentItems.size() + " / " + itemFrequency.size());
       
       // Bước 2: Chỉ xây dựng cây với các mục thường xuyên
       for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
           List<Integer> transaction = entry.getValue();
           
           // Lọc bỏ các mục không thường xuyên khỏi giao dịch
           List<Integer> filteredTransaction = new ArrayList<>();
           for (Integer item : transaction) {
               if (frequentItems.contains(item)) {
                   filteredTransaction.add(item);
               }
           }
           
           // Sắp xếp các mục theo thứ tự tăng dần
           filteredTransaction.sort(null);
           
           // Chuyển danh sách thành mảng để thêm vào cây
           if (!filteredTransaction.isEmpty()) {
               int[] itemset = new int[filteredTransaction.size()];
               for (int i = 0; i < filteredTransaction.size(); i++) {
                   itemset[i] = filteredTransaction.get(i);
               }
               
               // Thêm giao dịch đã lọc vào cây
               construct(null, root, itemset);
           }
       }

       // kiểm tra sử dụng bộ nhớ
       MemoryLogger.getInstance().checkMemory();
       // ghi lại thời gian kết thúc
       endTimestamp = System.currentTimeMillis();
   }

   /**
    * Trả về số lượng giao dịch đã xử lý
    * @return số lượng giao dịch
    */
   public int getTransactionCount() {
       return transactionCount;
   }

   /**
    * Trả về ngưỡng hỗ trợ tối thiểu
    * @return ngưỡng hỗ trợ tối thiểu
    */
   public int getMinsup() {
       return minsup;
   }

   /**
    * Thiết lập ngưỡng hỗ trợ tối thiểu
    * @param minsup ngưỡng hỗ trợ tối thiểu
    */
   public void setMinsup(int minsup) {
       this.minsup = minsup;
   }

   /**
    * Thêm một giao dịch vào cây itemset.
    * @param transaction giao dịch cần thêm (mảng các số nguyên)
    */
   public void addTransaction(int[] transaction){
       // gọi thuật toán "construct" để thêm nó
       construct(null, root, transaction);
   }

   /**
    * Với gốc của một cây con, thêm một itemset vào vị trí thích hợp trong cây đó
    * @param r  gốc của cây con
    * @param s  itemset cần chèn
    */
   private void construct(ItemsetTreeNode parentOfR, ItemsetTreeNode r, int[] s) {
       // lấy itemset trong nút gốc
       int[] sr = r.itemset;
       
       // nếu itemset trong nút gốc giống với itemset cần chèn,
       // chúng ta chỉ cần tăng hỗ trợ và trả về.
       if(same(s, sr)){
           r.support++;
           return;
       }
       
       // nếu nút cần chèn là tổ tiên của itemset của nút gốc
       if(ancestorOf(s, sr)){
           // tạo một nút mới cho itemset cần chèn với hỗ trợ của
           // nút gốc + 1
           ItemsetTreeNode newNode = new ItemsetTreeNode(s, r.support +1);
           // thiết lập con trỏ con và cha.
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           return;  // trả về
       }
       
       // Ngược lại, tính toán tổ tiên chung lớn nhất
       // của itemset cần chèn và gốc của cây con
       int[] l = getLargestCommonAncestor(s, sr);
       if(l != null){ // nếu có một tổ tiên chung lớn nhất
           // tạo một nút mới với tổ tiên đó và hỗ trợ của
           // gốc + 1.
           ItemsetTreeNode newNode = new ItemsetTreeNode(l, r.support +1);
           // thiết lập con trỏ con và cha của nút
           newNode.childs.add(r);
           parentOfR.childs.remove(r);
           parentOfR.childs.add(newNode);
           // thêm con thứ hai là itemset cần thêm với
           // hỗ trợ là 1
           ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
           // cập nhật con trỏ cho nút mới
           newNode.childs.add(newNode2);
           return;
       }
       
       // nếu không, lấy độ dài của itemset gốc
       int indexLastItemOfR = (sr == null)? 0 : sr.length;
       // tăng hỗ trợ của gốc
       r.support++;
       // cho mỗi con của gốc
       for(ItemsetTreeNode ci : r.childs){
           
           // nếu một trong các con của gốc là itemset cần chèn s,
           // thì tăng hỗ trợ của nó và dừng lại
           if(same(s, ci.itemset)){ // trường hợp 2
               ci.support++;
               return;
           }
           
           // nếu itemset cần chèn là tổ tiên của con ci
           if(ancestorOf(s, ci.itemset)){ // trường hợp 3
               // tạo một nút mới giữa ci và r trong cây
               // và cập nhật con trỏ con/cha
               ItemsetTreeNode newNode = new ItemsetTreeNode(s, ci.support+ 1);
               newNode.childs.add(ci);
               r.childs.remove(ci);
               r.childs.add(newNode);
               return;
           }
           
           // nếu con ci là tổ tiên của s
           if(ancestorOf(ci.itemset, s)){ // trường hợp 4
               // thì gọi đệ quy construct để xử lý trường hợp này.
               construct(r, ci, s);
               return;
           }

           // trường hợp 5
           // nếu ci và s có một tổ tiên chung lớn hơn r:
           if(ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
               // tìm tổ tiên chung lớn nhất
               int[] ancestor = getLargestCommonAncestor(s, ci.itemset);
               // tạo một nút mới cho itemset tổ tiên vừa tìm được với hỗ trợ
               // của ci + 1
               ItemsetTreeNode newNode = new ItemsetTreeNode(ancestor, ci.support+ 1);
               // đặt r làm cha
               r.childs.add(newNode);
               // thêm ci làm con của nút mới
               newNode.childs.add(ci);
               r.childs.remove(ci);
               // tạo một nút mới khác cho s với hỗ trợ là 1, sẽ
               // là con của nút mới đầu tiên
               ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
               newNode.childs.add(newNode2);
               // kết thúc
               return;
           }
       }
       
       // Nếu không, trường hợp 1:
       // Một nút mới được tạo cho s với hỗ trợ là 1 và được thêm
       // dưới nút r.
       ItemsetTreeNode newNode = new ItemsetTreeNode(s, 1);
       r.childs.add(newNode);
   }

   /**
    * In thống kê về thời gian và sử dụng bộ nhớ tối đa cho việc xây dựng
    * cây itemset. 
    */
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

   /**
    * In cây ra System.out.
    */
   public void printTree() {
       System.out.println(root.toString(new StringBuilder(),""));
   }

   /**
    * Trả về một biểu diễn chuỗi của cây.
    */
   public String toString() {
       return root.toString(new StringBuilder(), "");
   }

   /**
    * Lấy hỗ trợ của một itemset đã cho s.
    * @param s itemset
    * @return hỗ trợ dưới dạng số nguyên.
    */
   public int getSupportOfItemset(int[] s) {
       return count(s, root);  // gọi phương thức count.
   }

   /**
    * Phương thức này tính toán hỗ trợ của một itemset bằng cách sử dụng một cây con
    * được xác định bởi gốc của nó.
    * 
    * Lưu ý: điều này được thực hiện dựa trên thuật toán "count" của Bảng 2 trong bài báo của Kubat et al.
    * Chú ý rằng có một vài vấn đề trong thuật toán trong bài báo.
    * Tôi đã phải thay đổi > thành < trong: ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
    * ngoài ra bộ đếm không chính xác nên tôi đã phải thay đổi một chút cách nó đếm hỗ trợ
    * bằng cách sử dụng += thay vì return.
    * 
    * @param s  itemset
    * @param root  gốc của cây con
    * @return  hỗ trợ dưới dạng số nguyên
    */
   private int count(int[] s, ItemsetTreeNode root) {
       // biến count sẽ được sử dụng để đếm hỗ trợ
       int count =0;
       // cho mỗi con của gốc
       for(ItemsetTreeNode ci : root.childs){
           // nếu mục đầu tiên của itemset mà chúng ta đang tìm kiếm
           // nhỏ hơn hoặc bằng mục đầu tiên của con, chúng ta cần tìm kiếm
           // sâu hơn trong cây đó.
           if(ci.itemset[0]  <= s[0]){
               // nếu s được bao gồm trong ci, thêm hỗ trợ của ci vào số đếm hiện tại.
               if(includedIn(s, ci.itemset)){
                   count += ci.support;
               }else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){  
                   // ngược lại, nếu mục cuối cùng của ci nhỏ hơn
                   // mục cuối cùng của s, thì thực hiện một cuộc gọi đệ quy để khám phá
                   // cây con nơi ci là gốc
                   count += count(s, ci);
               }
           }
       }
       // trả về tổng số đếm
       return count;
   }

   /**
    * Kiểm tra xem một itemset có chứa trong một itemset khác không
    * @param itemset1 itemset thứ nhất
    * @param itemset2 itemset thứ hai
    * @return true nếu có, ngược lại false
    */
   private boolean includedIn(int[] itemset1, int[] itemset2) {
       int count = 0; // vị trí hiện tại của itemset1 mà chúng ta muốn tìm trong itemset2
       
       // cho mỗi mục trong itemset2
       for(int i=0; i< itemset2.length; i++){
           // nếu chúng ta tìm thấy mục
           if(itemset2[i] == itemset1[count]){
               // chúng ta sẽ tìm kiếm mục tiếp theo của itemset1
               count++;
               // nếu chúng ta đã tìm thấy tất cả các mục rồi, trả về true
               if(count == itemset1.length){
                   return true;
               }
           }
       }
       // nó không được bao gồm, vì vậy trả về false!
       return false;
   }

   /**
    * Phương thức này đi qua cây itemset để lấy tất cả các itemset
    * đang chứa một itemset cho trước "s" và hỗ trợ của chúng. Lưu ý rằng
    * phương thức này cũng có thể trả về các itemset không thường xuyên có thể được lọc bằng 
    * xử lý bổ sung sau đó.
    * @param s itemset
    * @return một bảng băm chứa các itemset và hỗ trợ của chúng.
    */
   public HashTableIT getFrequentItemsetSubsuming(int[] s){
       // tạo một bảng băm để chứa các itemset để hiệu quả hơn
       // chúng ta đặt kích thước mặc định của mảng nội bộ thành 1000
       HashTableIT hash = new HashTableIT(1000);
       
       // tạo một hashset để lưu trữ các mục của itemset
       HashSet<Integer> seti = new HashSet<Integer>();
       for(int i=0; i< s.length; i++){
           seti.add(s[i]);
       }
       // gọi phương thức khai thác có chọn lọc để tìm các tập hợp chứa s
       selectiveMining(s, seti, root, hash);
       return hash;
   }

   /**
    * Phương thức này tìm các itemset chứa một itemset đã cho. Đó là một phương thức đệ quy
    * quét một cây con của cây itemset. Nó lưu trữ các itemset được tìm thấy trong một bảng băm cùng
    * với hỗ trợ của chúng. 
    * @param s  itemset s
    * @param seti  các mục từ itemset s được lưu trữ trong một HashSet<Integer> để hiệu quả hơn cho việc kiểm tra bao gồm
    * @param t  gốc của cây con
    * @param hash  bảng băm để lưu trữ kết quả
    * @return hỗ trợ tích lũy của các con trực tiếp của t. Điều này cần thiết để đảm bảo t được kết hợp chính xác vào bảng băm (và với hỗ trợ chính xác). 
    */
   private int selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNode t, HashTableIT hash) {
       // khởi tạo hỗ trợ tích lũy đang chạy của các con trực tiếp của t
       int childrenSup = 0;
       // cho tất cả các nút con của gốc đã cho của cây con
       for(ItemsetTreeNode ci : t.childs){
           // Thêm hỗ trợ của ci vào số đếm tích lũy
           childrenSup += ci.support;
           // nếu mục đầu tiên của s nhỏ hơn hoặc bằng
           // mục đầu tiên của con
           if(ci.itemset[0]  <= s[0]){
               // Kiểm tra xem s có được bao gồm trong ci không
               if(includedIn(s, ci.itemset)){
                   // nếu ci không có con, đặt s vào bảng băm với 
                   // hỗ trợ của ci, và sau đó
                   // gọi thêm đệ quy.
                   // Lưu ý: Phần này không được giải thích chính xác trong bài báo, 
                   // tôi đã phải tự tìm hiểu và sửa nó.
                   if(ci.childs.size() ==0){
                       hash.put(s, ci.support);
                       recursiveAdd(s, seti, ci.itemset, ci.support, hash, 0);
                   }else{
                       // ngược lại khám phá đệ quy cây con với ci làm gốc.
                       // Lưu ý, chúng ta trừ số đếm được trả về bởi selectiveMining (chứa hỗ trợ tích lũy 
                       // của các con trực tiếp của ci) từ hỗ trợ của ci
                       // remainingSup do đó chỉ ra bao nhiêu lần itemset của ci xuất hiện tự nó trong cơ sở dữ liệu) 
                       int remainingSup = ci.support - selectiveMining(s, seti, ci, hash);
                       
                       // Nếu remainingSup lớn hơn 0, thì có nghĩa là các con của ci không hoàn toàn chiếm hết tất cả các 
                       // lần xuất hiện của itemset ci. Nói cách khác, itemset của ci xuất hiện tự nó remainingSup lần
                       // Do đó, chúng ta cần đặt s vào bảng băm với remainingSup, và sau đó gọi recursiveAdd trên ci 
                       // với remainingSup.
                       if (remainingSup > 0)
                       {
                           hash.put(s, remainingSup);
                           recursiveAdd(s, seti, ci.itemset, remainingSup, hash, 0);
                       } 
                   }
               }
               else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
                   // nếu không, nếu mục cuối cùng của ci nhỏ hơn mục cuối cùng
                   // của s, chúng ta cũng cần khám phá đệ quy cây con 
                   // với ci làm gốc.
                   selectiveMining(s, seti, ci, hash);
               }
           }
       }
       return childrenSup;
   }

   /**
    * Thực hiện thêm đệ quy (dựa trên thủ tục được trình bày trong bài báo của Kubat et al.)
    * @param s  một itemset s
    * @param seti   các mục từ itemset s trong một HashSet của số nguyên
    * @param ci     một nút cây itemset ci
    * @param cisupport  hỗ trợ của itemset được liên kết với ci
    * @param hash   một bảng băm được sử dụng để lưu trữ itemset và hỗ trợ của chúng
    * @param pos   vị trí hiện tại trong itemset ci
    */
   private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableIT hash, int pos) {
       // nếu chúng ta đã đạt đến cuối của ci, thì dừng lại
       if(pos >= ci.length){
           return;
       }
       // nếu itemset i chứa mục ở vị trí pos trong ci
       if(!seti.contains(ci[pos])){
           // tạo một itemset mới "newS" bằng cách nối
           // mục ở vị trí pos trong ci với itemset s.
           
           // Lưu ý rằng itemset kết quả phải được sắp xếp theo thứ tự từ điển
           // vì vậy chúng ta sao chép từng mục một và kiểm tra xem mục tại
           // vị trí pos nên được chèn ở đâu.
           int[] newS = new int[s.length+1]; // tạo itemset mới
           int j=0;  // vị trí hiện tại
           boolean added = false;  // chỉ ra nếu chúng ta đã thêm mục ở pos rồi
           // cho mỗi mục trong s
           for(Integer item : s){
               // nếu đã thêm hoặc mục hiện tại nhỏ hơn mục ở pos
               if(added || item < ci[pos]){
                   // chúng ta thêm mục từ s
                   newS[j++] = item;
               }else{
                   // ngược lại, chúng ta chèn mục ở vị trí pos
                   newS[j++] = ci[pos];
                   newS[j++] = item;
                   added = true;  // chúng ta đặt biến đó thành true để không chèn nó hai lần!
               }
           }
           // nếu mục ở vị trí pos chưa được thêm, điều đó có nghĩa là
           // nó phải được chèn ở vị trí cuối cùng vì nó lớn hơn
           // tất cả các mục khác
           if(j < s.length+1){
               newS[j++] = ci[pos];
           }
           // thêm itemset mới vào bảng băm với hỗ trợ của ci
           hash.put(newS, cisupport);
           
           // thực hiện một cuộc gọi đệ quy với vị trí tiếp theo trong ci với itemset mới
           recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
       }
       // thực hiện một cuộc gọi đệ quy với vị trí tiếp theo trong ci với itemset "S"
       recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
   }
}