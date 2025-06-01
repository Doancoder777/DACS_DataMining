package Algorithrms.frequentpatterns.itemsettree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import patterns.itemset_array_integers_with_count.Itemset;
abstract class AbstractItemsetTree {
	ItemsetTreeNode root = null;
	int nodeCount; // số lượng nút trong cây (được tính lại bởi printStatistics())
	long totalItemCountInNodes;  // tổng số mục được lưu trữ trong các nút (được tính lại bởi printStatistics())
	long startTimestamp;  // thời gian bắt đầu xây dựng cây (buildTree())
	long endTimestamp;   // thời gian kết thúc xây dựng cây (buildTree())
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
	public HashTableIT getFrequentItemsetSubsuming(int[] is, int minsup) {
		HashTableIT hashTable = getFrequentItemsetSubsuming(is);
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		return hashTable;
	}
	abstract protected HashTableIT getFrequentItemsetSubsuming(int[] s);
	public List<AssociationRuleIT> generateRules(int[] s, int minsup, double minconf) {
		List<AssociationRuleIT> rules = new ArrayList<AssociationRuleIT>();
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		int suppS = getSupportOfItemset(s);
		HashTableIT frequentItemsets = getFrequentItemsetSubsuming(s, minsup);
		for(List<Itemset> list : frequentItemsets.table){
			if(list != null){
				for(Itemset c : list){
					if(c.size() == s.length){ 
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
						AssociationRuleIT rule = new AssociationRuleIT();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						rules.add(rule);
					}
				}
			}
		}
		return rules;
	}
	public abstract int getSupportOfItemset(int[] s);
}
