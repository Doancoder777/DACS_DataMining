package Algorithrms.Rarepartem.itr;
import java.util.ArrayList;
import java.util.List;
import patterns.itemset_array_integers_with_count.Itemset;
public class HashTableITRare {
	public List<Itemset>[] table;
	public HashTableITRare(int size){
		table = new ArrayList[size];
	}
	public void put(int[] items, int support) {
		int hashcode = hashCode(items);
		if(table[hashcode] ==  null){
			table[hashcode] = new ArrayList<Itemset>();
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			table[hashcode].add(itemset);
		}else{
			for(Itemset existingItemset : table[hashcode]){
				if(same(items, existingItemset.itemset)){
					existingItemset.support += support; 
					return;
				}
			}
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			table[hashcode].add(itemset);
		}
	}
	public int hashCode(int[] items){
		int hashcode = 0;
		for (int i=0; i< items.length; i++) {
			hashcode += (items[i] + (i*10));
	    }
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		return (hashcode % table.length);
	}
	private boolean same(int[] itemset1, int[] itemset2) {
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
}

