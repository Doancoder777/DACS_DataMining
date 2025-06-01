package Algo.charm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids_bitset.Itemset;
import patterns.itemset_array_integers_with_tids_bitset.Itemsets;
public class AlgoCharmMFI {
	private long startTimestamp;
	private long endTimestamp; 
	protected Itemsets maximalItemsets;
	BufferedWriter writer = null; 
	boolean showTransactionIdentifiers = false;
	public AlgoCharmMFI() {
	}
	public Itemsets runAlgorithm(String output, Itemsets frequentClosed) throws IOException {
		if(output == null){
			writer = null;
	    }else{ // if the user wants to save the result to a file
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		maximalItemsets =  frequentClosed;
		maximalItemsets.setName("FREQUENT MAXIMAL ITEMSETS");
		startTimestamp = System.currentTimeMillis();
		int maxItemsetLength = frequentClosed.getLevels().size();
		for (int i = 1; i < maxItemsetLength - 1; i++) {
			List<Itemset> ti = frequentClosed.getLevels().get(i);
			for (int j = i+1; j < maxItemsetLength; j++) {
				List<Itemset> tip1 = frequentClosed.getLevels().get(j);
				findMaximal(ti, tip1, frequentClosed);
			}
		}
		if(writer != null){
			for(List<Itemset> level : maximalItemsets.getLevels()){
				for(int i=0; i < level.size(); i++){
					Itemset itemset = level.get(i);
					writer.write(itemset.toString() + " #SUP: "	+ itemset.getAbsoluteSupport());
					if(showTransactionIdentifiers) {
						BitSet bitset = itemset.getTransactionsIds();
			        	writer.append(" #TID:");
			        	for (int tid = bitset.nextSetBit(0); tid != -1; tid = bitset.nextSetBit(tid + 1)) {
			        		writer.append(" " + tid); 
			        	}
					}
					writer.newLine();
				}
			}
			writer.close();
		}
		endTimestamp = System.currentTimeMillis();
		return maximalItemsets; 
	}
	private void findMaximal(List<Itemset> ti, List<Itemset> tip1, Itemsets maximalItemsets) {
		for (Itemset itemsetJ : tip1) {
			Iterator<Itemset> iter = ti.iterator();
			while (iter.hasNext()) {
				Itemset itemsetI = (Itemset) iter.next();
				if (itemsetJ.containsAll(itemsetI) ) {
					iter.remove();
					maximalItemsets.decreaseItemsetCount();
				}	
			}
		}
	}
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	public void printStats(int transactionCount) {
		System.out.println("=============  CHARM-MFI - STATS =============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ transactionCount);
		System.out.println(" Frequent maximal itemsets count : "
				+ maximalItemsets.getItemsetsCount());
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}
	public Itemsets getItemsets() {
		return maximalItemsets;
	}
}

