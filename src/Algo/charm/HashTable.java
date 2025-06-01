package Algo.charm;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
class HashTable {
	private List<Itemset>[] table;
	public HashTable(int size) {
		table = new ArrayList[size];
	}
	public boolean containsSupersetOf(Itemset itemset, int hashcode) {
		if (table[hashcode] == null) {
			return false;
		}
		for (Object object : table[hashcode]) {
			Itemset itemsetX = (Itemset) object;
			if (itemsetX.getAbsoluteSupport() == itemset.getAbsoluteSupport()
					&& itemsetX.containsAll(itemset)) {
				return true;
			}
		}
		return false;
	}
	public void put(Itemset itemset, int hashcode) {
		if (table[hashcode] == null) {
			table[hashcode] = new ArrayList<Itemset>();
		}
		table[hashcode].add(itemset);
	}
	public int hashCode(BitSet tidset) {
		int hashcode = 0;
		for (int tid = tidset.nextSetBit(0); tid >= 0; tid = tidset.nextSetBit(tid+1)) {
			hashcode += tid;
		}
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		return (hashcode % table.length);
	}
	public int hashCode(Set<Integer> tidset) {
		int hashcode = 0;
		for (int tid : tidset) {
			hashcode += tid;
		}
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		return (hashcode % table.length);
	}
}

