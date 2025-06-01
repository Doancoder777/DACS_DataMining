package Algorithrms.frequentpatterns.eclat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;
public class AlgoEclat_Bitset {
	private int minsupRelative;  
	protected TransactionDatabase database; 
	protected long startTimestamp;
	protected long endTime; 
	protected Itemsets frequentItemsets;
	BufferedWriter writer = null; 
	protected int itemsetCount; 
	private TriangularMatrix matrix; // the triangular matrix
	final int BUFFERS_SIZE = 2000;
	private int[] itemsetBuffer = null;
	boolean showTransactionIdentifiers = false;
	int maxItemsetSize = Integer.MAX_VALUE;
	public AlgoEclat_Bitset() {
	}
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsupp,
			boolean useTriangularMatrixOptimization) throws IOException {
		MemoryLogger.getInstance().reset();
		itemsetBuffer = new int[BUFFERS_SIZE];
		if(output == null){
			writer = null;
			frequentItemsets =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
	    	frequentItemsets = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		itemsetCount = 0;
		this.database = database;
		startTimestamp = System.currentTimeMillis();
		this.minsupRelative = (int) Math.ceil(minsupp * database.size());
		final Map<Integer, BitSetSupport> mapItemTIDS = new HashMap<Integer, BitSetSupport>();
		int maxItemId = calculateSupportSingleItems(database,  mapItemTIDS);
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
		for(Entry<Integer, BitSetSupport> entry : mapItemTIDS.entrySet()) {
			BitSetSupport tidset = entry.getValue();
			int support = tidset.support;
			int item = entry.getKey();
			if(support >= minsupRelative && maxItemsetSize >= 1) {
				frequentItems.add(item);
				saveSingleItem(item, support, tidset.bitset);
			}
		}
		Collections.sort(frequentItems, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				return mapItemTIDS.get(arg0).support - mapItemTIDS.get(arg1).support; 
			}}); 
		if(maxItemsetSize >=2){
			for(int i=0; i < frequentItems.size(); i++) {
				Integer itemI = frequentItems.get(i);
				BitSetSupport tidsetI = mapItemTIDS.get(itemI);
				List<Integer> equivalenceClassIitems = new ArrayList<Integer>();
				List<BitSetSupport> equivalenceClassItidsets = new ArrayList<BitSetSupport>();
	loopJ:		for(int j=i+1; j < frequentItems.size(); j++) {
					int itemJ = frequentItems.get(j);
					int supportIJ = -1;
					if(useTriangularMatrixOptimization) {
						supportIJ = matrix.getSupportForItems(itemI, itemJ);
						if (supportIJ < minsupRelative) {
							continue loopJ;
						}
					}
					BitSetSupport tidsetJ = mapItemTIDS.get(itemJ);
					BitSetSupport bitsetSupportIJ;
					if(useTriangularMatrixOptimization) {
						bitsetSupportIJ = performANDFirstTime(tidsetI, tidsetJ, supportIJ);
					}else {
						bitsetSupportIJ = performAND(tidsetI, tidsetJ);
					}
					if(useTriangularMatrixOptimization || bitsetSupportIJ.support >= minsupRelative){
					    equivalenceClassIitems.add(itemJ);
					    equivalenceClassItidsets.add(bitsetSupportIJ);
					}
				}
				if(equivalenceClassIitems.size()>0) {
					itemsetBuffer[0] = itemI;
					processEquivalenceClass(itemsetBuffer, 1, equivalenceClassIitems, equivalenceClassItidsets);
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
	int calculateSupportSingleItems(TransactionDatabase database,
			final Map<Integer, BitSetSupport> mapItemTIDS) {
		int maxItemId = 0;
		for (int i = 0; i < database.size(); i++) {
			for (Integer item : database.getTransactions().get(i)) {
				BitSetSupport tids = mapItemTIDS.get(item);
				if(tids == null){
					tids = new BitSetSupport();
					mapItemTIDS.put(item, tids);
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				tids.bitset.set(i);
				tids.support++;
			}
		}
		return maxItemId;
	}
	 BitSetSupport performAND(BitSetSupport tidsetI,
			BitSetSupport tidsetJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		bitsetSupportIJ.support = bitsetSupportIJ.bitset.cardinality();
		return bitsetSupportIJ;
	}
	BitSetSupport performANDFirstTime(BitSetSupport tidsetI,
			BitSetSupport tidsetJ, int supportIJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		bitsetSupportIJ.support = supportIJ;
		return bitsetSupportIJ;
	}
	private void processEquivalenceClass(int[] prefix, int prefixLength, List<Integer> equivalenceClassItems,
			List<BitSetSupport> equivalenceClassTidsets) throws IOException {
		if(equivalenceClassItems.size() == 1) {
			int itemI = equivalenceClassItems.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);
			save(prefix, prefixLength, itemI, tidsetI);
			return;
		}
		if(equivalenceClassItems.size() == 2) {
			int itemI = equivalenceClassItems.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);
			save(prefix, prefixLength, itemI, tidsetI);
			int itemJ = equivalenceClassItems.get(1);
			BitSetSupport tidsetJ = equivalenceClassTidsets.get(1);
			save(prefix, prefixLength, itemJ, tidsetJ);
			if(prefixLength+2 <= maxItemsetSize){
				BitSetSupport bitsetSupportIJ = performAND(tidsetI, tidsetJ);
				if(bitsetSupportIJ.support >= minsupRelative) {
					int newPrefixLength = prefixLength+1;
					prefix[prefixLength] = itemI;
					save(prefix, newPrefixLength, itemJ, bitsetSupportIJ);
				}
			}
			return;
		}
		for(int i=0; i< equivalenceClassItems.size(); i++) {
			int itemI = equivalenceClassItems.get(i);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(i);
			save(prefix, prefixLength, itemI, tidsetI);
			if(prefixLength+2 <= maxItemsetSize){
				List<Integer> equivalenceClassISuffixItems= new ArrayList<Integer>();
				List<BitSetSupport> equivalenceITidsets = new ArrayList<BitSetSupport>();
				for(int j=i+1; j < equivalenceClassItems.size(); j++) {
					int itemJ = equivalenceClassItems.get(j);
					BitSetSupport tidsetJ = equivalenceClassTidsets.get(j);
					BitSetSupport bitsetSupportIJ = performAND(tidsetI, tidsetJ);
					if(bitsetSupportIJ.support >= minsupRelative) {
						equivalenceClassISuffixItems.add(itemJ);
						equivalenceITidsets.add(bitsetSupportIJ);
					}
				}
				if(equivalenceClassISuffixItems.size() >0) {
					prefix[prefixLength] = itemI;
					int newPrefixLength = prefixLength+1;
					processEquivalenceClass(prefix, newPrefixLength, equivalenceClassISuffixItems, equivalenceITidsets);
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	private void save(int[] prefix, int prefixLength, int suffixItem, BitSetSupport tidset) throws IOException {
		itemsetCount++;
		if(writer == null){
			int[] itemsetArray = new int[prefixLength+1];
			System.arraycopy(prefix, 0, itemsetArray, 0, prefixLength);
			itemsetArray[prefixLength] = suffixItem;
			Itemset itemset = new Itemset(itemsetArray);
			itemset.setAbsoluteSupport(tidset.support);
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
			buffer.append(tidset.support);
			if(showTransactionIdentifiers) {
				BitSet bitset = tidset.bitset;
				buffer.append(" #TID:");
	        	for (int tid = bitset.nextSetBit(0); tid != -1; tid = bitset.nextSetBit(tid + 1)) {
	        		buffer.append(" " + tid); 
	        	}
			}
			writer.write(buffer.toString());
			writer.newLine();
		}
	}
	private void saveSingleItem(int item, int support, BitSet tidset) throws IOException {
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
	        	writer.append(" #TID:");
	        	for (int tid = tidset.nextSetBit(0); tid != -1; tid = tidset.nextSetBit(tid + 1)) {
	        		writer.append(" " + tid); 
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
		System.out.println("=============  ECLAT vALTERNATE-Bitset_96r18 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + database.size());
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}
	public Itemsets getItemsets() {
		return frequentItemsets;
	}
	public class BitSetSupport{
		BitSet bitset = new BitSet();
		int support;
	}
	public void setMaximumPatternLength(int length) {
		this.maxItemsetSize = length;
	}
}

