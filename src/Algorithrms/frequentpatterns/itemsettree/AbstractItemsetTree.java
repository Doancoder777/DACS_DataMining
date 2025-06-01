package Algorithrms.frequentpatterns.itemsettree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import patterns.itemset_array_integers_with_count.Itemset;


abstract class AbstractItemsetTree {
	
	// gốc của cây itemset
	ItemsetTreeNode root = null;

	// thống kê về việc xây dựng cây
	int nodeCount; // số lượng nút trong cây (được tính lại bởi printStatistics())
	long totalItemCountInNodes;  // tổng số mục được lưu trữ trong các nút (được tính lại bởi printStatistics())
	
	long startTimestamp;  // thời gian bắt đầu xây dựng cây (buildTree())
	long endTimestamp;   // thời gian kết thúc xây dựng cây (buildTree())

	/**
	 * Phương thức để tính tổ tiên chung lớn nhất của hai itemset cho trước
	 * (như được định nghĩa trong bài báo).
	 * @param itemset1  itemset thứ nhất
	 * @param itemset2  itemset thứ hai
	 * @return một itemset mới là tổ tiên chung lớn nhất hoặc null nếu đó là tập rỗng
	 */
	protected int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		// nếu một trong các itemset là null, trả về null
		if(itemset2 == null || itemset1 == null){
			return null;
		}
	
		// tìm độ dài tối thiểu của các itemset
		int minI = itemset1.length < itemset2.length ? itemset1.length : itemset2.length;
		
		int count = 0;  // để đếm kích thước của tổ tiên chung
		
		// cho mỗi vị trí trong các itemset từ 0 đến độ dài tối đa -1
		for(int i=0; i < minI; i++){   
			// nếu hai mục khác nhau, chúng ta dừng lại vì
			// thứ tự từ điển
			if(itemset1[i] != itemset2[i]){
				break;
			}else{
				// ngược lại, chúng ta tăng bộ đếm chỉ ra số lượng các mục chung
				// trong tiền tố
				count++;
			}
		}
		// nếu có một tổ tiên chung có kích thước >0
		// (chúng ta không muốn tập rỗng!)
		if(count >0 && count < minI){
			// tạo itemset bằng cách sao chép "count" phần tử đầu tiên của
			// itemset1 và trả về nó
			int[] common = new int[count];
			System.arraycopy(itemset1, 0, common, 0, count);
			return common;
		}
		else{
			// ngược lại, trả về null vì tổ tiên chung là tập rỗng
			return null;
		}
	}
	
	/**
	 * Kiểm tra xem itemset thứ nhất có phải là tổ tiên của itemset thứ hai không
	 * @param itemset1  itemset thứ nhất
	 * @param itemset2 itemset thứ hai
	 * @return true, nếu đúng, ngược lại, false.
	 */
	protected boolean ancestorOf(int[] itemset1, int[] itemset2) {
		// nếu itemset thứ hai là null (tập rỗng), trả về false
		if(itemset2 == null){
			return false;
		}
		// nếu itemset thứ nhất là null (tập rỗng), trả về true
		if(itemset1 == null){
			return true;
		}
		// nếu độ dài của itemset 1 lớn hơn độ dài của
		// itemset2, nó không thể là tổ tiên, vì vậy trả về false
		if(itemset1.length >= itemset2.length){
			return false;
		}
		// ngược lại, vòng lặp trên các mục từ itemset1
		// và kiểm tra xem chúng có giống với itemset 2 không
		for(int i=0; i< itemset1.length; i++){
			// nếu một mục khác nhau, itemset1 không phải là tổ tiên
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		// ngược lại itemset1 là tổ tiên của itemset2
		return true;
	}
	
	/**
	 * Phương thức để kiểm tra xem hai itemset có bằng nhau không
	 * @param itemset1 itemset thứ nhất
	 * @param itemset2 itemset thứ hai
	 * @return true nếu chúng giống nhau hoặc false nếu ngược lại
	 */
	protected boolean same(int[] itemset1, int[] itemset2) {
		// nếu một trong hai là null, thì trả về false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// nếu chúng không có cùng kích thước, thì chúng không thể
		// bằng nhau
		if(itemset1.length != itemset2.length){
			return false;
		}
		// ngược lại, vòng lặp trên các mục từ itemset1
		// và kiểm tra xem chúng có giống với itemset 2 không
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				// nếu một mục khác nhau thì chúng không giống nhau
				return false;
			}
		}
		// ngược lại chúng giống nhau
		return true;
	}
	
	/**
	 * Lấy các itemset thường xuyên chứa một itemset cho trước với một ngưỡng hỗ trợ tối thiểu.
	 * @param is  itemset
	 * @param minsup ngưỡng hỗ trợ tối thiểu (số nguyên)
	 * @return một bảng băm chứa các itemset thường xuyên
	 */
	public HashTableIT getFrequentItemsetSubsuming(int[] is, int minsup) {
		// gọi phương thức đệ quy 
		HashTableIT hashTable = getFrequentItemsetSubsuming(is);
		// sau khi tìm thấy các itemset, chúng ta thực hiện một vòng lặp để loại bỏ những itemset có hỗ trợ thấp hơn minsup
		
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
	abstract protected HashTableIT getFrequentItemsetSubsuming(int[] s);
	
	/**
	 * Tạo ra tất cả các quy tắc kết hợp với một itemset cho trước làm tiền đề.
	 * @param s  itemset được sử dụng làm tiền đề
	 * @param minsup  ngưỡng minsup được sử dụng
	 * @param minconf ngưỡng minconf được sử dụng
	 * @return danh sách các quy tắc kết hợp
	 */
	public List<AssociationRuleIT> generateRules(int[] s, int minsup, double minconf) {
		// tạo danh sách các quy tắc kết hợp để lưu trữ kết quả
		List<AssociationRuleIT> rules = new ArrayList<AssociationRuleIT>();
		
		// đưa các mục từ itemset vào một hashset
		// để kiểm tra nhanh việc bao gồm mục
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		// tính toán hỗ trợ của itemset
		// (nó sẽ được sử dụng để tính toán độ tin cậy)
		int suppS = getSupportOfItemset(s);
		
		// lấy tất cả các itemset thường xuyên
		HashTableIT frequentItemsets = getFrequentItemsetSubsuming(s, minsup);
		// cho mỗi vị trí trong bảng băm
		for(List<Itemset> list : frequentItemsets.table){
			// nếu vị trí không rỗng
			if(list != null){
				// lặp qua tất cả các itemset trong cùng một bucket trong bảng băm
				for(Itemset c : list){
					// nếu chúng ta đã tìm thấy một itemset có cùng kích thước với S,
					// chúng ta tiếp tục vì chúng ta muốn tìm một itemset C để tạo
					// các quy tắc bằng cách thực hiện C - S và điều đó sẽ dẫn đến tập rỗng.
					if(c.size() == s.length){ 
						continue;
					}
					// Thử tạo một quy tắc bằng 
					// tạo một itemset mới l cho kết quả như C - S.
					int[] l = new int[c.itemset.length - s.length];
					int pos =0;
					// chúng ta sao chép sang l các mục từ c mà không có trong S.
					for(Integer item : c.itemset){
						if(!seti.contains(item)){
							l[pos++] = item;
						}
					}
					// tính toán độ tin cậy của S --> C - S
					int suppC = getSupportOfItemset(c.itemset);
					
					// Lưu ý: công thức tính độ tin cậy sai trong bài báo.
					// Không phải là g(l) / g(c) mà phải là g(c) / g(s).
					double conf = (double)suppC / suppS;  
					// nếu độ tin cậy không nhỏ hơn minconf
					if(conf >= minconf){
						// tạo một quy tắc mới S --> L
						AssociationRuleIT rule = new AssociationRuleIT();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						// thêm nó vào danh sách các quy tắc đã tìm thấy.
						rules.add(rule);
					}
				}
			}
		}
		// trả về kết quả
		return rules;
	}
	
	/**
	 * Lấy hỗ trợ của một itemset đã cho s.
	 * @param s itemset
	 * @return hỗ trợ dưới dạng số nguyên.
	 */
	public abstract int getSupportOfItemset(int[] s);
}