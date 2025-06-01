package Algo.eclat;
import java.util.HashSet;
import java.util.Set;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;
public class AlgoDEclat extends AlgoEclat{
	public void printStats() {
		System.out.println("=============  dECLAT v0.96r18 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent itemsets count : "
				+ itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}
	 Set<Integer> performANDFirstTime(Set<Integer> tidsetI, int supportI,
			Set<Integer> tidsetJ, int supportJ) {
		Set<Integer> diffsetIJ = new HashSet<Integer>();
		for(Integer tid : tidsetI) {
			if(tidsetJ.contains(tid) == false) {
				diffsetIJ.add(tid);
			}			
		}
		return diffsetIJ;
	}
	 Set<Integer> performAND(Set<Integer> tidsetI, int supportI,
			Set<Integer> tidsetJ, int supportJ) {
		Set<Integer> diffsetIJ = new HashSet<Integer>();
		for(Integer tid : tidsetJ) {
			if(tidsetI.contains(tid) == false) {
				diffsetIJ.add(tid);
			}			
		}
		return diffsetIJ;
	}
	int calculateSupport(int lengthOfX, int supportPrefix,  Set<Integer> tidsetX) {
		if(lengthOfX == 1) {
			return tidsetX.size();
		}else {
			return supportPrefix - tidsetX.size();
		}
	}
}

