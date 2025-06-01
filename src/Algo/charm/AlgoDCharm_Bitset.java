package Algo.charm;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import Algo.ArraysAlgos;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids_bitset.Itemset;
import patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import tools.MemoryLogger;
public class AlgoDCharm_Bitset extends AlgoCharm_Bitset{
	public void printStats() {
		System.out.println("=============  dCharm vALTERNATE-Bitset v96r6 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + database.size());
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}
	int calculateSupportSingleItems(TransactionDatabase database,
			final Map<Integer, BitSetSupport> mapItemTIDS) {
		int maxItemId = 0;
		for (int i = 0; i < database.size(); i++) {
			for (Integer item : database.getTransactions().get(i)) {
				BitSetSupport tids = mapItemTIDS.get(item);
				if(tids == null){
					tids = new BitSetSupport();
					tids.bitset.set(0, database.size(), true);
					mapItemTIDS.put(item, tids);
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				tids.bitset.set(i, false);
				tids.support++;
			}
		}	
		return maxItemId;
	}
	 BitSetSupport performAND(BitSetSupport tidsetI, BitSetSupport tidsetJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetJ.bitset.clone();
		bitsetSupportIJ.bitset.andNot(tidsetI.bitset);
		bitsetSupportIJ.support = tidsetI.support - bitsetSupportIJ.bitset.cardinality();
		return bitsetSupportIJ;
	}
	BitSetSupport performANDFirstTime(BitSetSupport tidsetI,
			BitSetSupport tidsetJ, int supportIJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetJ.bitset.clone();
		bitsetSupportIJ.bitset.andNot(tidsetI.bitset);
		bitsetSupportIJ.support = tidsetI.support - bitsetSupportIJ.bitset.cardinality();
		return bitsetSupportIJ;
	}
	void save(int[] prefix, int[] suffix, BitSetSupport tidset) throws IOException {
		int[] prefixSuffix;
		if(prefix == null) {
			prefixSuffix = suffix;
		}else {
			prefixSuffix = ArraysAlgos.concatenate(prefix, suffix);
		}
		Arrays.sort(prefixSuffix);
		patterns.itemset_array_integers_with_count.Itemset itemset = new patterns.itemset_array_integers_with_count.Itemset(prefixSuffix);
		itemset.setAbsoluteSupport(tidset.support);
		int hashcode = hash.hashCode(tidset.bitset);
		if (!hash.containsSupersetOf(itemset, hashcode)) {
			itemsetCount++;
			if (writer == null) { 
				Itemset itemsetWithTidset = new Itemset(prefixSuffix, null, tidset.support);
				closedItemsets.addItemset(itemsetWithTidset, itemset.size()); 
			} else {
				writer.write(itemset.toString() + " #SUP: " + itemset.support);
				if(showTransactionIdentifiers) {
					BitSet bitset = tidset.bitset;
		        	writer.append(" #TID:");
		        	for (int tid = bitset.nextSetBit(0); tid != -1; tid = bitset.nextSetBit(tid + 1)) {
		        		writer.append(" " + tid); 
		        	}
				}
				writer.newLine();
			}
			hash.put(itemset, hashcode);
		}
	}
}

