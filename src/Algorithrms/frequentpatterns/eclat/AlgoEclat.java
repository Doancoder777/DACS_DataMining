package Algorithrms.frequentpatterns.eclat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;
public class AlgoEclat {
	private int minsupRelative;  
	protected TransactionDatabase database; 
	protected long startTimestamp;
	protected long endTime; 
	protected Itemsets frequentItemsets;
	BufferedWriter writer = null; 
	protected int itemsetCount; 
	private TriangularMatrix matrix; 
	final int BUFFERS_SIZE = 2000;
	private int[] itemsetBuffer = null;
	boolean showTransactionIdentifiers = false;
	int maxItemsetSize = Integer.MAX_VALUE;
	public AlgoEclat() {
	}
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsupp,
			boolean useTriangularMatrixOptimization) throws IOException {
		MemoryLogger.getInstance().reset();
		itemsetBuffer = new int[BUFFERS_SIZE];
		if(output == null){
			writer = null;
			frequentItemsets =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user wants to save the result to a file
	    	frequentItemsets = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		itemsetCount =0;
		this.database = database;
		startTimestamp = System.currentTimeMillis();
		this.minsupRelative = (int) Math.ceil(minsupp * database.size());
		final Map<Integer, Set<Integer>> mapItemCount = new HashMap<Integer, Set<Integer>>();
		int maxItemId = calculateSupportSingleItems(database, mapItemCount);
		if (useTriangularMatrixOptimization && maxItemsetSize >=1) {
			matrix = new TriangularMatrix(maxItemId + 1);
			for (List<Integer> itemset : database.getTransactions()) {
				Object[] array = itemset.toArray();
				for (int i = 0; i < itemset.size(); i++) {
					Integer itemI = (Integer) array[i];
					for (int j = i + 1; j < itemset.size(); j++) {
						Integer itemJ = (Integer) array[j];
						matrix.incrementCount(itemI, itemJ);
					}
				}
			}
		}
		List<Integer> frequentItems = new ArrayList<Integer>();
		for(Entry<Integer, Set<Integer>> entry : mapItemCount.entrySet()) {
			Set<Integer> tidset = entry.getValue();
			int support = tidset.size();
			int item = entry.getKey();
			if(support >= minsupRelative && maxItemsetSize >= 1) {
				frequentItems.add(item);
				saveSingleItem(item, tidset, tidset.size());
			}
		}
		Collections.sort(frequentItems, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				return mapItemCount.get(arg0).size() - mapItemCount.get(arg1).size();
			}}); 
		if(maxItemsetSize >=2){
			for(int i=0; i < frequentItems.size(); i++) {
				Integer itemI = frequentItems.get(i);
				Set<Integer> tidsetI = mapItemCount.get(itemI);
				int supportI = tidsetI.size();
				List<Integer> equivalenceClassIitems = new ArrayList<Integer>();
				List<Set<Integer>> equivalenceClassItidsets = new ArrayList<Set<Integer>>();
	loopJ:		for(int j=i+1; j < frequentItems.size(); j++) {
					int itemJ = frequentItems.get(j);
					if(useTriangularMatrixOptimization) {
						int support = matrix.getSupportForItems(itemI, itemJ);
						if (support < minsupRelative) {
							continue loopJ;
						}
					}
					Set<Integer> tidsetJ = mapItemCount.get(itemJ);
					int supportJ = tidsetJ.size();
					Set<Integer> tidsetIJ = performANDFirstTime(tidsetI, supportI, tidsetJ, supportJ);
					if(useTriangularMatrixOptimization || calculateSupport(2, supportI, tidsetIJ) >= minsupRelative){
					    equivalenceClassIitems.add(itemJ);
					    equivalenceClassItidsets.add(tidsetIJ);
					}
				}
				if(equivalenceClassIitems.size() > 0) {
					itemsetBuffer[0] = itemI;
					processEquivalenceClass(itemsetBuffer, 1, supportI, equivalenceClassIitems, equivalenceClassItidsets);
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
		if(writer != null){
			writer.close();
		}
		endTime = System.currentTimeMillis();
		return frequentItemsets; 
	}
	private int calculateSupportSingleItems(TransactionDatabase database,
			final Map<Integer, Set<Integer>> mapItemCount) {
		int maxItemId = 0;
		for (int i = 0; i < database.size(); i++) {
			for (Integer item : database.getTransactions().get(i)) {
				Set<Integer> set = mapItemCount.get(item);
				if (set == null) {
					set = new HashSet<Integer>();
					mapItemCount.put(item, set);
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				set.add(i); 
			}
		}
		return maxItemId;
	}
	private void processEquivalenceClass(int[] prefix, int prefixLength, int supportPrefix, List<Integer> equivalenceClassItems,
			List<Set<Integer>> equivalenceClassTidsets) throws IOException {
		int length = prefixLength+1;
		if(equivalenceClassItems.size() == 1) {
			int itemI = equivalenceClassItems.get(0);
			Set<Integer> tidsetItemset = equivalenceClassTidsets.get(0);
			int support = calculateSupport(length, supportPrefix, tidsetItemset);
			save(prefix, prefixLength, itemI, tidsetItemset, support);
			return;
		}
		if(equivalenceClassItems.size() == 2) {
			int itemI = equivalenceClassItems.get(0);
			Set<Integer> tidsetI = equivalenceClassTidsets.get(0);
			int supportI = calculateSupport(length, supportPrefix, tidsetI);
			save(prefix, prefixLength, itemI, tidsetI, supportI);
			int itemJ = equivalenceClassItems.get(1);
			Set<Integer> tidsetJ = equivalenceClassTidsets.get(1);
			int supportJ = calculateSupport(length, supportPrefix, tidsetJ);
			save(prefix, prefixLength, itemJ, tidsetJ, supportJ);
			if(prefixLength+2 <= maxItemsetSize){
				Set<Integer> tidsetIJ = this.performAND(tidsetI, tidsetI.size(), tidsetJ, tidsetJ.size());
				int supportIJ = calculateSupport(length, supportI, tidsetIJ);
				if(supportIJ >= minsupRelative) {
					int newPrefixLength = prefixLength+1;
					prefix[prefixLength] = itemI;
					save(prefix, newPrefixLength, itemJ, tidsetIJ, supportIJ);
				}
			}
			return;
		}
		for(int i=0; i< equivalenceClassItems.size(); i++) {
			int suffixI = equivalenceClassItems.get(i);
			Set<Integer> tidsetI = equivalenceClassTidsets.get(i);
			int supportI = calculateSupport(length, supportPrefix, tidsetI);
			save(prefix, prefixLength, suffixI, tidsetI, supportI);
			if(prefixLength+2 <= maxItemsetSize){
				List<Integer> equivalenceClassISuffixItems= new ArrayList<Integer>();
				List<Set<Integer>> equivalenceITidsets = new ArrayList<Set<Integer>>();
				for(int j=i+1; j < equivalenceClassItems.size(); j++) {
					int suffixJ = equivalenceClassItems.get(j);
					Set<Integer> tidsetJ = equivalenceClassTidsets.get(j);
					int supportJ = calculateSupport(length, supportPrefix, tidsetJ);
					Set<Integer> tidsetIJ = performAND(tidsetI, supportI, tidsetJ, 
							supportJ);
					int supportIJ = calculateSupport(length, supportI, tidsetIJ);
					if(supportIJ >= minsupRelative) {
						equivalenceClassISuffixItems.add(suffixJ);
						equivalenceITidsets.add(tidsetIJ);
					}
				}
				if(equivalenceClassISuffixItems.size() >0) {
					prefix[prefixLength] = suffixI;
					int newPrefixLength = prefixLength+1;
					processEquivalenceClass(prefix, newPrefixLength, supportI, equivalenceClassISuffixItems, equivalenceITidsets);
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	int calculateSupport(int lengthOfX, int supportPrefix, Set<Integer> tidsetI) {
		return tidsetI.size();
	}
	 Set<Integer> performAND(Set<Integer> tidsetI, int supportI,
			Set<Integer> tidsetJ, int supportJ) {
		Set<Integer> tidsetIJ = new HashSet<Integer>();
		if(supportI > supportJ) {
			for(Integer tid : tidsetJ) {
				if(tidsetI.contains(tid)) {
					tidsetIJ.add(tid);
				}			
			}
		}else {
			for(Integer tid : tidsetI) {
				if(tidsetJ.contains(tid)) {
					tidsetIJ.add(tid);
				}
			}
		}
		return tidsetIJ;
	}
		 Set<Integer> performANDFirstTime(Set<Integer> tidsetI, int supportI,
				Set<Integer> tidsetJ, int supportJ) {
			return performAND(tidsetI, supportI, tidsetJ, supportJ);
		}
	private void save(int[] prefix, int prefixLength, int suffixItem, Set<Integer> tidset, int support) throws IOException {
		itemsetCount++;
		if(writer == null){
			int[] itemsetArray = new int[prefixLength+1];
			System.arraycopy(prefix, 0, itemsetArray, 0, prefixLength);
			itemsetArray[prefixLength] = suffixItem;
			Itemset itemset = new Itemset(itemsetArray);
			itemset.setAbsoluteSupport(support);
			frequentItemsets.addItemset(itemset, itemset.size());
		}else{
			StringBuilder buffer = new StringBuilder();
			for(int i=0; i < prefixLength; i++) {
				int item = prefix[i];
				buffer.append(item);
				buffer.append(" ");
			}
			buffer.append(suffixItem);
			buffer.append(" #SUP: ");
			buffer.append(support);
			if(showTransactionIdentifiers) {
				buffer.append(" #TID:");
	        	for (Integer tid: tidset) {
	        		buffer.append(" " + tid); 
	        	}
			}
			writer.write(buffer.toString());
			writer.newLine();
		}
	}
	private void saveSingleItem(int item, Set<Integer> tidset, int support) throws IOException {
		itemsetCount++;
		if(writer == null){
			Itemset itemset = new Itemset(new int[] {item});
			itemset.setAbsoluteSupport(support);
			frequentItemsets.addItemset(itemset, itemset.size());
		}else{
			StringBuilder buffer = new StringBuilder();
			buffer.append(item);
			buffer.append(" #SUP: ");
			buffer.append(support);
			if(showTransactionIdentifiers) {
				buffer.append(" #TID:");
	        	for (Integer tid: tidset) {
	        		buffer.append(" " + tid); 
	        	}
			}
			writer.write(buffer.toString());
			writer.newLine();
		}
	}
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	public void printStats() {
		System.out.println("=============  ECLAT v0.96r18 - STATS =============");
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
	public Itemsets getItemsets() {
		return frequentItemsets;
	}
	public void setMaximumPatternLength(int length) {
		this.maxItemsetSize = length;
	}
}

