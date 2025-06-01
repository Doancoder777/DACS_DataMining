package Algo.eclat;
import java.util.BitSet;
import java.util.Map;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids_bitset.Itemset;
import patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import tools.MemoryLogger;
public class AlgoDEclat_Bitset extends AlgoEclat_Bitset{
	public void printStats() {
		System.out.println("=============  DECLAT vALTERNATE-Bitset v0.96r18- STATS =============");
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
}

