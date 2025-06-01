package Algorithrms.Rarepartem.itr;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import patterns.itemset_array_integers_with_count.Itemset;
public abstract class AbstractItemsetTreeRare {
	ItemsetTreeNodeRare root = null;
	int nodeCount; // số lượng nút trong cây (được tính lại bởi printStatistics())
	long totalItemCountInNodes;  // tổng số mục được lưu trữ trong các nút (được tính lại bởi printStatistics())
	long startTimestamp;  // thời gian bắt đầu xây dựng cây (buildTree())
	long endTimestamp;   // thời gian kết thúc xây dựng cây (buildTree())
	protected int mrt = 1; // Minimum Rare Threshold  
	protected int mft = 5; // Maximum Frequent Threshold
	public enum RuleTypeMode {
		INDIVIDUAL_ITEMS,    // Check individual items in itemsets
		COMPOSITE_ITEMSET    // Check composite itemset support (default)
	}
	private RuleTypeMode ruleTypeMode = RuleTypeMode.COMPOSITE_ITEMSET; // Default mode
	public void setRuleTypeMode(RuleTypeMode mode) {
		this.ruleTypeMode = mode;
	}
	public RuleTypeMode getRuleTypeMode() {
		return this.ruleTypeMode;
	}
	public void setRareThresholds(int mrt, int mft) {
		this.mrt = mrt;
		this.mft = mft;
	}
	public boolean isRareItem(int support) {
		return support > mrt && support <= mft;
	}
	public boolean isFrequentItem(int support) {
		return support > mft;
	}
	public boolean isNoiseItem(int support) {
		return support <= mrt;
	}
	protected int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return null;
		}
		int minI = itemset1.length < itemset2.length ? itemset1.length : itemset2.length;
		int count = 0;  // để đếm kích thước của tổ tiên chung
		for(int i=0; i < minI; i++){   
			if(itemset1[i] != itemset2[i]){
				break;
			}else{
				count++;
			}
		}
		if(count >0 && count < minI){
			int[] common = new int[count];
			System.arraycopy(itemset1, 0, common, 0, count);
			return common;
		}
		else{
			return null;
		}
	}
	protected boolean ancestorOf(int[] itemset1, int[] itemset2) {
		if(itemset2 == null){
			return false;
		}
		if(itemset1 == null){
			return true;
		}
		if(itemset1.length >= itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		return true;
	}
	protected boolean same(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		if(itemset1.length != itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		return true;
	}
	public HashTableITRare getRareItemsetSubsuming(int[] is) {
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
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
	public HashTableITRare getItemsetSubsumingInRange(int[] is, int minSupport, int maxSupport) {
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
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
	abstract protected HashTableITRare getFrequentItemsetSubsuming(int[] s);
	public List<AssociationRuleITRare> generateRareRules(int[] s, double minconf) {
		List<AssociationRuleITRare> rules = new ArrayList<AssociationRuleITRare>();
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		int suppS = getSupportOfItemset(s);
		HashTableITRare allItemsets = getFrequentItemsetSubsuming(s);
		for(List<Itemset> list : allItemsets.table){
			if(list != null){
				for(Itemset c : list){
					if(c.size() == s.length){ 
						continue;
					}
					if(!isRareItem(c.support)) {
						continue;
					}
					int[] l = new int[c.itemset.length - s.length];
					int pos =0;
					for(Integer item : c.itemset){
						if(!seti.contains(item)){
							l[pos++] = item;
						}
					}
					int suppC = getSupportOfItemset(c.itemset);
					double conf = (double)suppC / suppS;  
					if(conf >= minconf){
						AssociationRuleITRare rule = new AssociationRuleITRare();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						rule.ruleType = determineRuleType(s, l);
						rules.add(rule);
					}
				}
			}
		}
		return rules;
	}
	private String determineRuleType(int[] antecedent, int[] consequent) {
		if (ruleTypeMode == RuleTypeMode.INDIVIDUAL_ITEMS) {
			return determineRuleTypeByIndividualItems(antecedent, consequent);
		} else {
			return determineRuleTypeByCompositeItemset(antecedent, consequent);
		}
	}
	private String determineRuleTypeByIndividualItems(int[] antecedent, int[] consequent) {
		boolean hasRareAnt = false, hasFrequentAnt = false, hasNoiseAnt = false;
		for(int item : antecedent) {
			int[] singleItem = {item};
			int itemSupport = getSupportOfItemset(singleItem);
			if(isRareItem(itemSupport)) hasRareAnt = true;
			if(isFrequentItem(itemSupport)) hasFrequentAnt = true;
			if(isNoiseItem(itemSupport)) hasNoiseAnt = true;
		}
		boolean hasRareCons = false, hasFrequentCons = false, hasNoiseCons = false;
		for(int item : consequent) {
			int[] singleItem = {item};
			int itemSupport = getSupportOfItemset(singleItem);
			if(isRareItem(itemSupport)) hasRareCons = true;
			if(isFrequentItem(itemSupport)) hasFrequentCons = true;
			if(isNoiseItem(itemSupport)) hasNoiseCons = true;
		}
		String antType = "Mixed";
		if(hasRareAnt && !hasFrequentAnt && !hasNoiseAnt) antType = "Rare";
		else if(!hasRareAnt && hasFrequentAnt && !hasNoiseAnt) antType = "Frequent";
		else if(!hasRareAnt && !hasFrequentAnt && hasNoiseAnt) antType = "Noise";
		else if(hasRareAnt && hasFrequentAnt) antType = "Mixed";
		String consType = "Mixed";
		if(hasRareCons && !hasFrequentCons && !hasNoiseCons) consType = "Rare";
		else if(!hasRareCons && hasFrequentCons && !hasNoiseCons) consType = "Frequent";
		else if(!hasRareCons && !hasFrequentCons && hasNoiseCons) consType = "Noise";
		else if(hasRareCons && hasFrequentCons) consType = "Mixed";
		return antType + "-to-" + consType;
	}
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
		boolean antNoise = isNoiseItem(suppAnt);
		boolean consNoise = isNoiseItem(suppCons);
		if(antNoise && consRare) return "Noise-to-Rare";
		if(antNoise && consFrequent) return "Noise-to-Frequent";
		if(antRare && consNoise) return "Rare-to-Noise";
		if(antFrequent && consNoise) return "Frequent-to-Noise";
		if(antNoise && consNoise) return "Noise-to-Noise";
		return "Other"; // fallback
	}
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
	public abstract int getSupportOfItemset(int[] s);
}
