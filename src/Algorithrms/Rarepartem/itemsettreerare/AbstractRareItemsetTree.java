package Algorithrms.Rarepartem.itemsettreerare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import patterns.itemset_array_integers_with_count.Itemset;

abstract class AbstractRareItemsetTree {
	
	RareItemsetTreeNode root = null;
	int nodeCount; 
	long totalItemCountInNodes;  
	long startTimestamp;  
	long endTimestamp;   

	protected int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return null;
		}
	
		int minI = itemset1.length < itemset2.length ? itemset1.length : itemset2.length;
		int count = 0;  
		
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
	
	public RareHashTableIT getRareItemsetSubsuming(int[] is, int minRareSupport, int maxRareSupport) {
		RareHashTableIT hashTable = getRareItemsetSubsuming(is);
		
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(itemset.support <= minRareSupport || itemset.support > maxRareSupport){
						it.remove();
					}
				}
			}
		}
		return hashTable;
	}
	
	abstract protected RareHashTableIT getRareItemsetSubsuming(int[] s);
	
	public abstract int getSupportOfItemset(int[] s);
	
	public List<RareAssociationRuleIT> generateRareRules(int[] s, int minRareSupport, int maxRareSupport, double minconf) {
		List<RareAssociationRuleIT> rules = new ArrayList<RareAssociationRuleIT>();
		
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		int suppS = getSupportOfItemset(s);
		
		RareHashTableIT rareItemsets = getRareItemsetSubsuming(s, minRareSupport, maxRareSupport);
		for(List<Itemset> list : rareItemsets.table){
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
						RareAssociationRuleIT rule = new RareAssociationRuleIT();
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
}