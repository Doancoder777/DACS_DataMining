package Rarepartem.itr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import Algo.ArraysAlgos;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;

public class MemoryEfficientItemsetTreeRare extends AbstractItemsetTreeRare implements Serializable {

	private static final long serialVersionUID = 1L;
	
	long sumBranchesLength; // tổng độ dài các nhánh
	int totalNumberOfBranches; // tổng số nhánh

	
	/**
	 * Hàm khởi tạo mặc định
	 */
	public MemoryEfficientItemsetTreeRare() {	
		super();
	}

	/**
	 * Xây dựng cây itemset dựa trên một tệp đầu vào chứa các giao dịch
	 * @param input một tệp đầu vào
	 * @throws IOException ngoại lệ nếu có lỗi khi đọc tệp
	 */
	public void buildTree(String input)
			throws IOException {
		// ghi lại thời gian bắt đầu
		startTimestamp = System.currentTimeMillis();
		
		// đặt lại thống kê sử dụng bộ nhớ
		MemoryLogger.getInstance().reset();
		
		// tạo một gốc rỗng cho cây
		root = new ItemsetTreeNodeRare(null, 0);

		// Quét cơ sở dữ liệu để đọc các giao dịch
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// cho mỗi dòng (giao dịch) cho đến khi kết thúc tệp
		while (((line = reader.readLine()) != null)) {
			// nếu dòng là một bình luận, trống hoặc là
			// một loại siêu dữ liệu
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// tách giao dịch thành các mục
			String[] lineSplited = line.split(" ");
			// tạo một cấu trúc để lưu trữ giao dịch
			int[] itemset = new int[lineSplited.length];
			// cho mỗi mục trong giao dịch
			for (int i=0; i< lineSplited.length; i++) {
				// chuyển đổi mục thành số nguyên và thêm vào cấu trúc
				itemset[i] = Integer.parseInt(lineSplited[i]);
				
				// Dòng tiếp theo bị comment và chỉ được sử dụng
				// để kiểm tra hiệu suất của các truy vấn ngẫu nhiên
				//items.add(itemset[i]);
			}
//			printTree();
			// gọi phương thức "construct" để thêm giao dịch vào cây
			construct(null, root, itemset, null);
//			System.out.println(".");
		}
		// đóng tệp đầu vào
		reader.close();
		
		// kiểm tra sử dụng bộ nhớ
		MemoryLogger.getInstance().checkMemory();
		// đóng tệp
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Thêm một giao dịch vào cây itemset.
	 * @param transaction giao dịch cần thêm (mảng các số nguyên)
	 */
	public void addTransaction(int[] transaction){
		// gọi thuật toán "construct" để thêm nó
		construct(null, root, transaction, null);
	}


	/**
	 * Với gốc của một cây con, thêm một itemset vào vị trí thích hợp trong cây đó
	 * @param r  gốc của cây con
	 * @param s  itemset cần chèn
	 * @param prefix các mục hiện tại đã được khám phá trong nhánh của cây này cho đến nút r hiện tại.
	 */
	private void construct(ItemsetTreeNodeRare parentOfR, ItemsetTreeNodeRare r, int[] s, int[] prefix) {
				
		// nếu itemset trong nút gốc giống với itemset cần chèn,
		// chúng ta chỉ cần tăng hỗ trợ và trả về.
 		if(same(s, prefix, r.itemset)){
			r.support++;
			return;
		}
		
 		int[] rprefix = append(prefix, r.itemset);
		
 		
		// nếu nút cần chèn là tổ tiên của itemset của nút gốc
		// thì chèn itemset giữa r và cha của nó
		// Trước:   parent_of_r --> r
		// Sau:    parent_of_r --> s --> r
		// ví dụ   cho một cây itemset thông thường
		//          {2}:4 --> {2,3,4,5,6}:6
		//  		 chúng ta chèn {2,3}
		//          {2}:4 --> {2,3}:7 --> {2,3,4,5,6}:6
		// ví dụ  cho một cây itemset nhỏ gọn 
		//           r_parent    r
		//          {2}:4 --> {3,4,5,6}:6
		// 			 chúng ta chèn s={2,3}
		//           r_parent    s'        r'
		//          {2}:4 --> {3}:7 --> {4,5,6}:6
		if(ancestorOf(s, rprefix)){
			//  Tính toán  s' và r'  bằng cách sử dụng tiền tố
			int[] sprime = copyItemsetWithoutItemsFrom(s, prefix);
			int[] rprime = copyItemsetWithoutItemsFrom(rprefix, sprime);
			
			// tạo một nút mới cho itemset cần chèn với hỗ trợ của
			// nút gốc cây con + 1
			ItemsetTreeNodeRare newNodeS = new ItemsetTreeNodeRare(sprime, r.support +1);
			// thiết lập con trỏ con và cha.
			newNodeS.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNodeS);
//			r.parent = newNodeS;
			r.itemset = rprime;
			return;  // trả về
		}
		
		// Ngược lại, tính toán tổ tiên chung lớn nhất
		// của itemset cần chèn và gốc của cây con
		int[] l = getLargestCommonAncestor(s, rprefix);
		if(l != null){ // nếu có một tổ tiên chung lớn nhất
			int[] sprime = copyItemsetWithoutItemsFrom(s, l);
			int[] rprime = copyItemsetWithoutItemsFrom(r.itemset, l);
			
			// tạo một nút mới với tổ tiên đó và hỗ trợ của
			// gốc + 1.
			ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(l, r.support +1);
			// thiết lập con trỏ con và cha của nút
			newNode.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNode);
//			parentOfR = newNode;
			r.itemset = rprime;
			// thêm con thứ hai là itemset cần thêm với
			// hỗ trợ là 1
			ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(sprime, 1);
			// cập nhật con trỏ cho nút mới
			newNode.childs.add(newNode2);
//			newNode2.parent = newNode;
			return;
		}
		
		// nếu không, lấy độ dài của itemset gốc
		int indexLastItemOfR = (rprefix == null)? 0 : rprefix.length;
		// tăng hỗ trợ của gốc
		r.support++;
		// cho mỗi con của gốc
		for(ItemsetTreeNodeRare ci : r.childs){
			int[] ciprefix = append(rprefix, ci.itemset);
			
			// nếu một trong các con của gốc là itemset cần chèn s,
			// thì tăng hỗ trợ của nó và dừng lại
			if(same(s, ciprefix)){ // trường hợp 2
				ci.support++;
				return;
			}
			
			// nếu itemset cần chèn là tổ tiên của con ci
			if(ancestorOf(s, ciprefix)){ // trường hợp 3
				int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix); 
				int[] ciprime = copyItemsetWithoutItemsFrom(ci.itemset, s); 
				
				// tạo một nút mới giữa ci và r trong cây
				// và cập nhật con trỏ con/cha
				ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(sprime, ci.support+ 1);
				newNode.childs.add(ci);
//				newNode.parent = r;
				r.childs.remove(ci);
				r.childs.add(newNode);
//				ci.parent = newNode;
				ci.itemset = ciprime;
				return;
			}
			
			// nếu con ci là tổ tiên của s
			if(ancestorOf(ciprefix, s)){ // trường hợp 4
				
				// thì gọi đệ quy construct để xử lý trường hợp này.
				construct(r, ci, s, rprefix);
				return;
			}

			// trường hợp 5
			// nếu ci và s có một tổ tiên chung lớn hơn r:
			if(ciprefix[indexLastItemOfR] == s[indexLastItemOfR]){
				// tìm tổ tiên chung lớn nhất
				int[] ancestor = getLargestCommonAncestor(s, ciprefix);
				// tạo một nút mới cho itemset tổ tiên vừa tìm được với hỗ trợ
				// của ci + 1
				
				int[] ancestorprime = copyItemsetWithoutItemsFrom(ancestor, rprefix);
				
				ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(ancestorprime, ci.support+ 1);
				// đặt r làm cha
//				newNode.parent = r;
				r.childs.add(newNode);
				// thêm ci làm con của nút mới
				ci.itemset = copyItemsetWithoutItemsFrom(ci.itemset, ancestorprime);
				newNode.childs.add(ci);
//				ci.parent = newNode;
				r.childs.remove(ci);
				// tạo một nút mới khác cho s với hỗ trợ là 1, sẽ
				// là con của nút mới đầu tiên
				int[] sprime = copyItemsetWithoutItemsFromArrays(s, ancestorprime, rprefix);
				ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(sprime, 1);
//				newNode2.parent = newNode;
				newNode.childs.add(newNode2);
				// kết thúc
				return;
			}
			
		}
		
		// Nếu không, trường hợp 1:
		// Một nút mới được tạo cho s với hỗ trợ là 1 và được thêm
		// dưới nút r.
		int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix);
		ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(sprime, 1);
//		newNode.parent = r;
		r.childs.add(newNode);
		
	}

	/**
	 * Tạo một bản sao của một itemset trong khi loại bỏ các mục xuất hiện trong
	 * hai itemset có tên "prefix" và "s".
	 * @param r  itemset
	 * @param prefix  itemset khác có tên "prefix"
	 * @param s  itemset khác có tên "s"
	 * @return itemset
	 */
	private int[] copyItemsetWithoutItemsFromArrays(int[] r,
			int[] prefix, int[] s) {
		
		// tạo một itemset rỗng
		List<Integer> rprime = new ArrayList<Integer>(r.length);
		
		// cho mỗi mục trong r
loop1:	for(Integer rvalue : r){
			// nếu itemset prefix khác không phải là null
			if(prefix != null){
				// cho mỗi mục từ tiền tố
				for(int pvalue : prefix){
					// nếu đó là mục hiện tại trong r
					if(pvalue == rvalue){
						// bỏ qua mục này từ r
						continue loop1;
					// nếu mục hiện tại từ prefix lớn hơn
					// mục hiện tại từ r,
				    // thì break vì các itemset được sắp xếp theo thứ tự từ điển
					// vì vậy sẽ không có kết quả phù hợp.
					}else if(pvalue > rvalue){
						break;
					}
				}
			}
			
			// nếu s không phải là null
			if(s != null){
				// cho mỗi mục trong s
				for(int svalue : s){
					// nếu mục này trong s là mục hiện tại trong r
					if(rvalue == svalue){
						// bỏ qua nó (không thêm nó vào itemset mới)
						continue loop1;
					// nếu mục hiện tại từ s lớn hơn
					// mục hiện tại từ r,
				    // thì break vì các itemset được sắp xếp theo thứ tự từ điển
					// vì vậy sẽ không có kết quả phù hợp.
					}else if(svalue > rvalue){
						break;
					}
				}
			}
			rprime.add(rvalue);
		}
		// chuyển đổi itemset mới "rprime" từ ArrayList 
		// sang một mảng.
		int[] rprimeArray = new int[rprime.size()];
		for(int i=0; i< rprime.size(); i++){
			rprimeArray[i] = rprime.get(i);
		}
		// trả về mảng
		return rprimeArray;
	}

	/**
	 * Tạo một bản sao của một itemset mà không có các mục từ itemset thứ hai.
	 * @param itemset1 itemset thứ nhất
	 * @param itemset2 itemset thứ hai
	 * @return itemset mới
	 */
	private int[] copyItemsetWithoutItemsFrom(int[] itemset1, int[] itemset2) {
		// nếu itemset thứ hai là null, chỉ cần trả về itemset thứ nhất
		if(itemset2 == null){
			return itemset1;
		}
		
		// tạo một itemset mới
		List<Integer> itemset1prime = new ArrayList<Integer>(itemset1.length);
		// cho mỗi mục trong itemset thứ nhất
loop1:	for(int i1value : itemset1){
			// cho mỗi mục trong itemset thứ hai
			for(int i2value : itemset2){
				// nếu các mục khớp, không thêm mục hiện tại 
				// từ itemset1 vào itemset mới
				if(i2value == i1value){
					continue loop1;
				// ngược lại, nếu mục hiện tại từ "itemset2"
				// lớn hơn mục hiện tại từ "itemset1"
				// sẽ không có kết quả phù hợp vì itemsets được 
				// sắp xếp theo thứ tự từ điển.
				}else if(i2value > i1value){
					break;
				}
			}
			// nếu mục hiện tại từ itemset1 không nằm trong itemset2,
			// thì thêm nó vào itemset mới
			itemset1prime.add(i1value);
		}
		// chuyển đổi itemset mới từ ArrayList sang mảng
		int[] itemset1primeArray = new int[itemset1prime.size()];
		for(int i=0; i< itemset1prime.size(); i++){
			itemset1primeArray[i] = itemset1prime.get(i);
		}
		// trả về mảng
		return itemset1primeArray;
	}


	/**
	 * Kiểm tra xem itemset1 có giống với sự nối của prefix và itemset2 không
	 * @param itemset1  itemset thứ nhất
	 * @param prefix  một tiền tố
	 * @param itemset2 một itemset khác
	 * @return true nếu giống, ngược lại false
	 */
	private boolean same(int[] itemset1, int[] prefix, int[] itemset2) {
		if(prefix == null) {
			return same(itemset1, itemset2);
		}
		// nếu một trong hai là null, thì trả về false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// nếu chúng không có cùng kích thước, thì chúng không thể
		// bằng nhau
		if(itemset1.length != itemset2.length + prefix.length){
			return false;
		}
		// ngược lại, vòng lặp trên các mục từ itemset1
		// và kiểm tra xem chúng có giống với itemset 2 không
		int i = 0;
		while(i < prefix.length){
			if(itemset1[i] != prefix[i]){
				// nếu một mục khác nhau thì chúng không giống nhau
				return false;
			}
			i++;
		}
		int j = 0;
		while(j< itemset2.length){
			if(itemset1[j++] != itemset2[i++]){
				// nếu một mục khác nhau thì chúng không giống nhau
				return false;
			}
		}
		
		// ngược lại chúng giống nhau
		return true;
	}
	
	/**
	 * Phương thức nối hai itemset để tạo một itemset lớn hơn
	 * @param a1  itemset thứ nhất
	 * @param a2  itemset thứ hai
	 * @return  itemset mới
	 */
	public int[] append(int[] a1, int[] a2){
		// nếu itemset thứ nhất là null, trả về itemset thứ hai
		if(a1 == null){
			return a2;
		}
		// nếu itemset thứ hai là null, trả về itemset thứ nhất
		if(a2 == null){
			return a1;
		}
		// tạo itemset mới
		int[] newArray = new int[a1.length + a2.length];
		
		// sao chép itemset thứ nhất vào itemset mới
		int i=0;
		for(; i< a1.length; i++){
			newArray[i] = a1[i];
		}
		// sao chép itemset thứ hai vào itemset mới
		for(int j =0; j< a2.length; j++){
			newArray[i++] = a2[j];
		}
		// trả về itemset mới
		return newArray;
	}
	
	/**
	 * In thống kê về thời gian và sử dụng bộ nhớ tối đa cho việc xây dựng
	 * cây itemset. 
	 */
	public void printStatistics() {
		System.gc();
		System.out.println("========== MEMORY EFFICIENT ITEMSET TREE CONSTRUCTION - STATS ============");
		System.out.println(" Thời gian xây dựng cây ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
		nodeCount = 0;
		totalItemCountInNodes = 0;
		sumBranchesLength = 0;
		totalNumberOfBranches = 0;
		recursiveStats(root, 1);
		System.out.println(" Số lượng nút: " + nodeCount);
		System.out.println(" Tổng các mục trong tất cả các nút: " + totalItemCountInNodes + " trung bình mỗi nút :" + totalItemCountInNodes / ((double)nodeCount));
		System.out.println("=====================================");
	}

	/**
	 * Phương thức đệ quy để tính toán thống kê về cây itemset
	 * @param root  nút gốc của cây con hiện tại  
	 * @param length tổng tích lũy độ dài của itemsets
	 */
	private void recursiveStats(ItemsetTreeNodeRare root, int length) {
		// nếu gốc không phải là null hoặc tập rỗng
		if(root != null && root.itemset!=null){
			// tăng số lượng nút
			nodeCount++;
			// tăng tổng số mục
			totalItemCountInNodes += root.itemset.length;
		}
		// cho mỗi nút con, thực hiện một cuộc gọi đệ quy
		for(ItemsetTreeNodeRare node : root.childs){
			recursiveStats(node, ++length);
		}
		// nếu không có con, nút này là một lá, vì vậy
		// thêm độ dài tích lũy của nhánh này vào tổng
		// và thêm 1 vào tổng số nhánh.
		if(root.childs.size() == 0) {
			sumBranchesLength += length;
			totalNumberOfBranches += 1;
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
		return count(s, root, new int[0]);  // gọi phương thức count.
	}

	/**
	 * Phương thức này tính toán hỗ trợ của một itemset bằng cách sử dụng một cây con
	 * được xác định bởi gốc của nó.
	 * 
	 * Lưu ý: điều này được thực hiện dựa trên thuật toán "count" của Bảng 2 trong bài báo của Kubat et al.
	// Chú ý rằng có một vài vấn đề trong thuật toán trong bài báo.
	// Tôi đã phải thay đổi > thành < trong: ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
	// ngoài ra bộ đếm không chính xác nên tôi đã phải thay đổi một chút cách nó đếm hỗ trợ
	// bằng cách sử dụng += thay vì return.
	 * 
	 * @param s  itemset
	 * @param root  gốc của cây con
	 * @param startFrom  các mục để khớp bắt đầu từ vị trí j trong s
	 * @return  hỗ trợ dưới dạng số nguyên
	 */
	private int count(int[] s, ItemsetTreeNodeRare root, int[] prefix) {
		// biến count sẽ được sử dụng để đếm hỗ trợ
		int count = 0;
		// cho mỗi con của gốc
		for(ItemsetTreeNodeRare ci : root.childs){
			// nếu mục đầu tiên của itemset mà chúng ta đang tìm kiếm
			// nhỏ hơn hoặc bằng mục đầu tiên của con, chúng ta cần tìm kiếm
			// sâu hơn trong cây đó.
			int[] ciprefix = append(prefix, ci.itemset);
			
			if(ciprefix[0]  <= s[0]){
				
				// nếu s được bao gồm trong ci, thêm hỗ trợ của ci vào số đếm hiện tại.
				if(ArraysAlgos.includedIn(s, ciprefix)){
					count += ci.support;
				}else if(ciprefix[ciprefix.length -1] < s[s.length -1]){  
					// ngược lại, nếu mục cuối cùng của ci nhỏ hơn
					// mục cuối cùng của s, thì thực hiện một cuộc gọi đệ quy để khám phá
					// cây con nơi ci là gốc
					count += count(s, ci, ciprefix);
				}
			}
		}
		// trả về tổng số đếm
		return count;
	}




	/**
	 * Lấy các itemset thường xuyên chứa một itemset cho trước với một ngưỡng hỗ trợ tối thiểu.
	 * @param is  itemset
	 * @param minsup ngưỡng hỗ trợ tối thiểu (số nguyên)
	 * @return một bảng băm chứa các itemset thường xuyên
	 */
	public HashTableITRare getFrequentItemsetSubsuming(int[] is, int minsup) {
		// gọi phương thức đệ quy 
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
		// sau khi tìm thấy các itemset, chúng ta thực hiện một vòng lặp để loại bỏ những itemset có hỗ trợ thấp hơn minsup
		// Điều này có vẻ không hiệu quả nhưng đó là cách các tác giả của bài báo thực hiện.
		
		// cho mỗi vị trí trong mảng nội bộ của bảng băm
		for(List<Itemset> list : hashTable.table){
			// nếu vị trí đó không rỗng
			if(list != null){
				// vòng lặp qua các itemset được lưu trữ tại vị trí đó
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					// nếu itemset không thường xuyên, loại bỏ nó
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		// sau đó chúng ta trả về bảng băm
		return hashTable;
	}
	
	
	/**
	 * Phương thức này đi qua cây itemset để lấy tất cả các itemset
	 * đang chứa một itemset cho trước "s" và hỗ trợ của chúng. Lưu ý rằng
	 * phương thức này cũng có thể trả về các itemset không thường xuyên có thể được lọc bằng 
	 * xử lý bổ sung sau đó.
	 * @param s itemset
	 * @return một bảng băm chứa các itemset và hỗ trợ của chúng.
	 */
	public HashTableITRare getFrequentItemsetSubsuming(int[] s){
		// tạo một bảng băm để chứa các itemset để hiệu quả hơn
		// chúng ta đặt kích thước mặc định của mảng nội bộ thành 1000
		HashTableITRare hash = new HashTableITRare(1000);
		
		// tạo một hashset để lưu trữ các mục của itemset
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		// gọi phương thức khai thác có chọn lọc để tìm các tập hợp chứa s
		selectiveMining(s, seti, root, hash, null);
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
	private int selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNodeRare t, HashTableITRare hash, int[] prefix) {
		// khởi tạo hỗ trợ tích lũy đang chạy của các con trực tiếp của t
		int childrenSup = 0;
		// cho tất cả các nút con của gốc đã cho của cây con
		for(ItemsetTreeNodeRare ci : t.childs){
			// Thêm hỗ trợ của ci vào số đếm tích lũy
			childrenSup += ci.support;
			int[] ciprefix = append(prefix, ci.itemset);
			
			// nếu mục đầu tiên của s nhỏ hơn hoặc bằng
			// mục đầu tiên của con
			if(ciprefix[0]  <= s[0]){
				// Kiểm tra xem s có được bao gồm trong ci không
				if(ArraysAlgos.includedIn(s, ciprefix)){
					// nếu ci không có con, đặt s vào bảng băm với 
					// hỗ trợ của ci, và sau đó
					// gọi thêm đệ quy.
					// Lưu ý: Phần này không được giải thích chính xác trong bài báo, 
					// tôi đã phải tự tìm hiểu và sửa nó.
					if(ci.childs.size() ==0){
						hash.put(s, ci.support);
						recursiveAdd(s, seti, ciprefix, ci.support, hash, 0);
					}else{
						// ngược lại khám phá đệ quy cây con với ci làm gốc.
						// Lưu ý, chúng ta trừ số đếm được trả về bởi selectiveMining (chứa hỗ trợ tích lũy 
						// của các con trực tiếp của ci) từ hỗ trợ của ci
						// remainingSup do đó chỉ ra bao nhiêu lần itemset của ci xuất hiện tự nó trong cơ sở dữ liệu) 
						int remainingSup = ci.support - selectiveMining(s, seti, ci, hash, ciprefix);
						
						// Nếu remainingSup lớn hơn 0, thì có nghĩa là các con của ci không hoàn toàn chiếm hết tất cả các 
						// lần xuất hiện của itemset ci. Nói cách khác, itemset của ci xuất hiện tự nó remainingSup lần
						// Do đó, chúng ta cần đặt s vào bảng băm với remainingSup, và sau đó gọi recursiveAdd trên ci 
						// với remainingSup.
						if (remainingSup > 0)
						{
							hash.put(s, remainingSup);
							recursiveAdd(s, seti, ciprefix, remainingSup, hash, 0);
						} 
					}
				}
				else if(ciprefix[ciprefix.length -1] < s[s.length -1]){ 
					// nếu không, nếu mục cuối cùng của ci nhỏ hơn mục cuối cùng
					// của s, chúng ta cũng cần khám phá đệ quy cây con 
					// với ci làm gốc.
					selectiveMining(s, seti, ci, hash, ciprefix);
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
	private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableITRare hash, int pos) {
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