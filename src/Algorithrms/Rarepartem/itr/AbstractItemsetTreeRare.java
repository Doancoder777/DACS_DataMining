package Algorithrms.Rarepartem.itr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import patterns.itemset_array_integers_with_count.Itemset;

public abstract class AbstractItemsetTreeRare {
	
	// gốc của cây itemset
	ItemsetTreeNodeRare root = null;

	// thống kê về việc xây dựng cây
	int nodeCount; // số lượng nút trong cây (được tính lại bởi printStatistics())
	long totalItemCountInNodes;  // tổng số mục được lưu trữ trong các nút (được tính lại bởi printStatistics())
	
	long startTimestamp;  // thời gian bắt đầu xây dựng cây (buildTree())
	long endTimestamp;   // thời gian kết thúc xây dựng cây (buildTree())

	// ADDED: Rare mining thresholds
	protected int mrt = 1; // Minimum Rare Threshold  
	protected int mft = 5; // Maximum Frequent Threshold
	
	// ADDED: Rule type classification mode
	public enum RuleTypeMode {
		INDIVIDUAL_ITEMS,    // Check individual items in itemsets
		COMPOSITE_ITEMSET    // Check composite itemset support (default)
	}
	
	private RuleTypeMode ruleTypeMode = RuleTypeMode.COMPOSITE_ITEMSET; // Default mode
	
	/**
	 * ADDED: Set rule type classification mode
	 */
	public void setRuleTypeMode(RuleTypeMode mode) {
		this.ruleTypeMode = mode;
	}
	
	/**
	 * ADDED: Get current rule type classification mode
	 */
	public RuleTypeMode getRuleTypeMode() {
		return this.ruleTypeMode;
	}
	
	/**
	 * ADDED: Thiết lập các ngưỡng cho rare mining
	 */
	public void setRareThresholds(int mrt, int mft) {
		this.mrt = mrt;
		this.mft = mft;
	}
	
	/**
	 * ADDED: Kiểm tra rare item theo định nghĩa: MRT < Support(X) <= MFT
	 */
	public boolean isRareItem(int support) {
		return support > mrt && support <= mft;
	}
	
	/**
	 * ADDED: Kiểm tra frequent item theo định nghĩa: Support(X) > MFT
	 */
	public boolean isFrequentItem(int support) {
		return support > mft;
	}
	
	/**
	 * ADDED: Kiểm tra noise item theo định nghĩa: Support(X) <= MRT
	 */
	public boolean isNoiseItem(int support) {
		return support <= mrt;
	}

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
	 * MODIFIED: Lấy các rare itemset chứa một itemset cho trước với ngưỡng rare.
	 * @param is  itemset
	 * @return một bảng băm chứa các rare itemset
	 */
	public HashTableITRare getRareItemsetSubsuming(int[] is) {
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
		
		// Lọc chỉ giữ lại rare itemsets (MRT < support <= MFT)
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(!isRareItem(itemset.support)){
						it.remove();
					}
				}
			}
		}
		return hashTable;
	}
	
	/**
	 * ADDED: Lấy tất cả itemsets trong một khoảng support cho trước
	 * @param is itemset
	 * @param minSupport ngưỡng support tối thiểu
	 * @param maxSupport ngưỡng support tối đa
	 * @return bảng băm chứa các itemsets trong khoảng
	 */
	public HashTableITRare getItemsetSubsumingInRange(int[] is, int minSupport, int maxSupport) {
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
		
		// Lọc giữ lại các itemsets có support trong khoảng [minSupport, maxSupport]
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minSupport || itemset.support > maxSupport){
						it.remove();
					}
				}
			}
		}
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
	abstract protected HashTableITRare getFrequentItemsetSubsuming(int[] s);
	
	/**
	 * MODIFIED: Tạo ra rare association rules với itemset cho trước làm tiền đề.
	 * @param s  itemset được sử dụng làm tiền đề
	 * @param minconf ngưỡng minconf được sử dụng
	 * @return danh sách các rare association rules
	 */
	public List<AssociationRuleITRare> generateRareRules(int[] s, double minconf) {
		// tạo danh sách các quy tắc kết hợp để lưu trữ kết quả
		List<AssociationRuleITRare> rules = new ArrayList<AssociationRuleITRare>();
		
		// đưa các mục từ itemset vào một hashset
		// để kiểm tra nhanh việc bao gồm mục
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		// tính toán hỗ trợ của itemset
		// (nó sẽ được sử dụng để tính toán độ tin cậy)
		int suppS = getSupportOfItemset(s);
		
		// MODIFIED: Chỉ tạo rules nếu antecedent có liên quan đến rare patterns
		// (có thể là rare hoặc frequent, tùy yêu cầu)
		
		// lấy tất cả các itemset chứa s (không giới hạn support)
		HashTableITRare allItemsets = getFrequentItemsetSubsuming(s);
		
		// cho mỗi vị trí trong bảng băm
		for(List<Itemset> list : allItemsets.table){
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
					
					// ADDED: Chỉ tạo rules với rare itemsets
					if(!isRareItem(c.support)) {
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
						AssociationRuleITRare rule = new AssociationRuleITRare();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						// MODIFIED: Xác định loại rule với mode được chọn
						rule.ruleType = determineRuleType(s, l);
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
	 * ENHANCED: Xác định loại rule dựa trên mode được chọn
	 */
	private String determineRuleType(int[] antecedent, int[] consequent) {
		if (ruleTypeMode == RuleTypeMode.INDIVIDUAL_ITEMS) {
			return determineRuleTypeByIndividualItems(antecedent, consequent);
		} else {
			return determineRuleTypeByCompositeItemset(antecedent, consequent);
		}
	}
	
	/**
	 * ADDED: Xác định rule type dựa trên individual items
	 */
	private String determineRuleTypeByIndividualItems(int[] antecedent, int[] consequent) {
		// Phân tích antecedent
		boolean hasRareAnt = false, hasFrequentAnt = false, hasNoiseAnt = false;
		for(int item : antecedent) {
			int[] singleItem = {item};
			int itemSupport = getSupportOfItemset(singleItem);
			if(isRareItem(itemSupport)) hasRareAnt = true;
			if(isFrequentItem(itemSupport)) hasFrequentAnt = true;
			if(isNoiseItem(itemSupport)) hasNoiseAnt = true;
		}
		
		// Phân tích consequent  
		boolean hasRareCons = false, hasFrequentCons = false, hasNoiseCons = false;
		for(int item : consequent) {
			int[] singleItem = {item};
			int itemSupport = getSupportOfItemset(singleItem);
			if(isRareItem(itemSupport)) hasRareCons = true;
			if(isFrequentItem(itemSupport)) hasFrequentCons = true;
			if(isNoiseItem(itemSupport)) hasNoiseCons = true;
		}
		
		// Xác định dominant type cho antecedent
		String antType = "Mixed";
		if(hasRareAnt && !hasFrequentAnt && !hasNoiseAnt) antType = "Rare";
		else if(!hasRareAnt && hasFrequentAnt && !hasNoiseAnt) antType = "Frequent";
		else if(!hasRareAnt && !hasFrequentAnt && hasNoiseAnt) antType = "Noise";
		else if(hasRareAnt && hasFrequentAnt) antType = "Mixed";
		
		// Xác định dominant type cho consequent
		String consType = "Mixed";
		if(hasRareCons && !hasFrequentCons && !hasNoiseCons) consType = "Rare";
		else if(!hasRareCons && hasFrequentCons && !hasNoiseCons) consType = "Frequent";
		else if(!hasRareCons && !hasFrequentCons && hasNoiseCons) consType = "Noise";
		else if(hasRareCons && hasFrequentCons) consType = "Mixed";
		
		return antType + "-to-" + consType;
	}
	
	/**
	 * ADDED: Xác định rule type dựa trên composite itemset support (original approach)
	 */
	private String determineRuleTypeByCompositeItemset(int[] antecedent, int[] consequent) {
		int suppAnt = getSupportOfItemset(antecedent);
		int suppCons = getSupportOfItemset(consequent);
		
		boolean antRare = isRareItem(suppAnt);
		boolean consRare = isRareItem(suppCons);
		boolean antFrequent = isFrequentItem(suppAnt);
		boolean consFrequent = isFrequentItem(suppCons);
		
		if(antRare && consRare) return "Rare-to-Rare";
		if(antRare && consFrequent) return "Rare-to-Frequent";
		if(antFrequent && consRare) return "Frequent-to-Rare";
		if(antFrequent && consFrequent) return "Frequent-to-Frequent";
		
		// Handle noise cases
		boolean antNoise = isNoiseItem(suppAnt);
		boolean consNoise = isNoiseItem(suppCons);
		
		if(antNoise && consRare) return "Noise-to-Rare";
		if(antNoise && consFrequent) return "Noise-to-Frequent";
		if(antRare && consNoise) return "Rare-to-Noise";
		if(antFrequent && consNoise) return "Frequent-to-Noise";
		if(antNoise && consNoise) return "Noise-to-Noise";
		
		return "Other"; // fallback
	}
	
	/**
	 * ADDED: Kiểm tra Perfect Rare ItemSet
	 * @param itemset itemset cần kiểm tra
	 * @return true nếu tất cả items trong itemset đều là rare
	 */
	public boolean isPerfectRareItemset(int[] itemset) {
		for(int item : itemset) {
			int[] singleItem = {item};
			int support = getSupportOfItemset(singleItem);
			if(!isRareItem(support)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * ADDED: Kiểm tra Rare Item ItemSet  
	 * @param itemset itemset cần kiểm tra
	 * @return true nếu có ít nhất một rare item trong itemset
	 */
	public boolean isRareItemItemset(int[] itemset) {
		for(int item : itemset) {
			int[] singleItem = {item};
			int support = getSupportOfItemset(singleItem);
			if(isRareItem(support)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Lấy hỗ trợ của một itemset đã cho s.
	 * @param s itemset
	 * @return hỗ trợ dưới dạng số nguyên.
	 */
	public abstract int getSupportOfItemset(int[] s);
}