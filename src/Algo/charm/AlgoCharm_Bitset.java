package Algo.charm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import Algo.ArraysAlgos;
import datastructures.triangularmatrix.TriangularMatrix;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids_bitset.Itemset;
import patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import tools.MemoryLogger;
public class AlgoCharm_Bitset {
	private int minsupRelative;  
	protected TransactionDatabase database; 
	protected long startTimestamp;
	protected long endTime; 
	protected Itemsets closedItemsets;
	BufferedWriter writer = null; 
	protected int itemsetCount; 
	private TriangularMatrix matrix; // the triangular matrix
	protected HashTable hash;
	final int BUFFERS_SIZE = 2000;
	private int[] itemsetBuffer = null;
	boolean showTransactionIdentifiers = false;
	public AlgoCharm_Bitset() {
	}
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsup,
			boolean useTriangularMatrixOptimization, int hashTableSize) throws IOException {
		MemoryLogger.getInstance().reset();
		if(output == null){
			writer = null;
			closedItemsets =  new Itemsets("FREQUENT CLOSED ITEMSETS");
	    }else{ // if the user wants to save the result to a file
	    	closedItemsets = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		this.hash = new HashTable(hashTableSize);
		itemsetCount = 0;
		this.database = database;
		startTimestamp = System.currentTimeMillis();
		this.minsupRelative = (int) Math.ceil(minsup * database.size());
		final Map<Integer, BitSetSupport> mapItemTIDS = new HashMap<Integer, BitSetSupport>();
		int maxItemId = 0;
		maxItemId = calculateSupportSingleItems(database, mapItemTIDS);
		if (useTriangularMatrixOptimization) {
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
			if(support >= minsupRelative) {
				frequentItems.add(item);
			}
		}
		Collections.sort(frequentItems, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				return mapItemTIDS.get(arg0).support - mapItemTIDS.get(arg1).support;  
			}}); 
		for(int i=0; i < frequentItems.size(); i++) {
			Integer itemX = frequentItems.get(i);
			if(itemX == null) {
				continue;
			}
			BitSetSupport tidsetX = mapItemTIDS.get(itemX);
			int[] itemsetX = new int[] {itemX};
			List<int[]> equivalenceClassIitemsets = new ArrayList<int[]>();
			List<BitSetSupport> equivalenceClassItidsets = new ArrayList<BitSetSupport>();
loopJ:		for(int j=i+1; j < frequentItems.size(); j++) {
				Integer itemJ = frequentItems.get(j);
				if(itemJ == null) {
					continue;
				}
				int supportIJ = -1;
				if(itemsetX.length == 1 && useTriangularMatrixOptimization) {
					supportIJ = matrix.getSupportForItems(itemX, itemJ);
					if (supportIJ < minsupRelative) {
						continue loopJ;
					}
				}
				BitSetSupport tidsetJ = mapItemTIDS.get(itemJ);
				BitSetSupport bitsetSupportUnion = new BitSetSupport();
				if(itemsetX.length == 1 && useTriangularMatrixOptimization) {
					bitsetSupportUnion = performANDFirstTime(tidsetX, tidsetJ, supportIJ);
				}else {
					bitsetSupportUnion = performAND(tidsetX, tidsetJ);
				}
				if(bitsetSupportUnion.support < minsupRelative) {
					continue;
				}
				if(tidsetX.support == tidsetJ.support && 
					bitsetSupportUnion.support == tidsetX.support) {
					frequentItems.set(j, null);
					int[] realUnion = new int[itemsetX.length + 1];
					System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
					realUnion[itemsetX.length] = itemJ;
					itemsetX = realUnion;
				}else if(tidsetX.support < tidsetJ.support
						&& bitsetSupportUnion.support == tidsetX.support) {
					int[] realUnion = new int[itemsetX.length + 1];
					System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
					realUnion[itemsetX.length] = itemJ;
					itemsetX = realUnion;
				}else if(tidsetX.support > tidsetJ.support
						&& bitsetSupportUnion.support == tidsetJ.support) {
					frequentItems.set(j, null);
					equivalenceClassIitemsets.add(new int[] {itemJ});
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}else {  
					equivalenceClassIitemsets.add(new int[] {itemJ});
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}
			}
			if(equivalenceClassIitemsets.size() > 0) {
				processEquivalenceClass(itemsetX, equivalenceClassIitemsets, equivalenceClassItidsets);
			}
			save(null, itemsetX, tidsetX);
		}
		if(writer != null){
			writer.close();
		}
		MemoryLogger.getInstance().checkMemory();
		endTime = System.currentTimeMillis();
		return closedItemsets; 
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
	BitSetSupport performANDFirstTime(BitSetSupport tidsetI,
			BitSetSupport tidsetJ, int supportIJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		bitsetSupportIJ.support = supportIJ;
		return bitsetSupportIJ;
	}
	BitSetSupport performAND(BitSetSupport tidsetI,	BitSetSupport tidsetJ) {
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		bitsetSupportIJ.support = bitsetSupportIJ.bitset.cardinality();
		return bitsetSupportIJ;
	}
	void processEquivalenceClass(int[] prefix, List<int[]> equivalenceClassItemsets,
			List<BitSetSupport> equivalenceClassTidsets) throws IOException {
		if(equivalenceClassItemsets.size() == 1) {
			int[] itemsetI = equivalenceClassItemsets.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);
			save(prefix, itemsetI, tidsetI); 
			return;
		}
		if(equivalenceClassItemsets.size() == 2) {
			int[] itemsetI = equivalenceClassItemsets.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);
			int[] itemsetJ = equivalenceClassItemsets.get(1);
			BitSetSupport tidsetJ = equivalenceClassTidsets.get(1);
			BitSetSupport bitsetSupportIJ = performAND(tidsetI, tidsetJ);
			if(bitsetSupportIJ.support >= minsupRelative) {
				int[] suffixIJ = ArraysAlgos.concatenate(itemsetI, itemsetJ);
				save(prefix, suffixIJ, bitsetSupportIJ);
			}
			if(bitsetSupportIJ.support != tidsetI.support) {
				save(prefix, itemsetI, tidsetI);
			}
			if(bitsetSupportIJ.support != tidsetJ.support) {
				save(prefix, itemsetJ, tidsetJ);
			}
			return;
		}
		for(int i=0; i < equivalenceClassItemsets.size(); i++) {
			int[] itemsetX = equivalenceClassItemsets.get(i);
			if(itemsetX == null) {
				continue;
			}
			BitSetSupport tidsetX = equivalenceClassTidsets.get(i);
			List<int[]> equivalenceClassIitemsets = new ArrayList<int[]>();
			List<BitSetSupport> equivalenceClassItidsets = new ArrayList<BitSetSupport>();
			for(int j=i+1; j < equivalenceClassItemsets.size(); j++) {
				int[] itemsetJ = equivalenceClassItemsets.get(j);
				if(itemsetJ == null) {
					continue;
				}
				BitSetSupport tidsetJ = equivalenceClassTidsets.get(j);
				BitSetSupport bitsetSupportUnion = new BitSetSupport();
				bitsetSupportUnion = performAND(tidsetX, tidsetJ);
				if(bitsetSupportUnion.support < minsupRelative) {
					continue;
				}
				if(tidsetX.support == tidsetJ.support && 
					bitsetSupportUnion.support == tidsetX.support) {
					equivalenceClassItemsets.set(j, null);
					equivalenceClassTidsets.set(j, null);
					int[] realUnion = ArraysAlgos.concatenate(itemsetX, itemsetJ);
					itemsetX = realUnion;
				}else if(tidsetX.support < tidsetJ.support
						&& bitsetSupportUnion.support == tidsetX.support) {
					int[] realUnion = ArraysAlgos.concatenate(itemsetX, itemsetJ);
					itemsetX = realUnion;
				}else if(tidsetX.support > tidsetJ.support
						&& bitsetSupportUnion.support == tidsetJ.support) {
					equivalenceClassItemsets.set(j, null);
					equivalenceClassTidsets.set(j, null);
					equivalenceClassIitemsets.add(itemsetJ);
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}else {  
					equivalenceClassIitemsets.add(itemsetJ);
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}
			}
			if(equivalenceClassIitemsets.size()>0) {
				int[] newPrefix = ArraysAlgos.concatenate(prefix, itemsetX);
				processEquivalenceClass(newPrefix, equivalenceClassIitemsets, equivalenceClassItidsets);
			}
			save(prefix, itemsetX, tidsetX);
		}
		MemoryLogger.getInstance().checkMemory();
	}
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	public void printStats() {
		System.out.println("=============  CHARM v96r6 Bitset - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent closed itemsets count : "
				+ itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out
				.println("===================================================");
	}
	public Itemsets getClosedItemsets() {
		return closedItemsets;
	}
	public class BitSetSupport{
		BitSet bitset = new BitSet();
		int support;
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
				Itemset itemsetWithTidset = new Itemset(prefixSuffix, tidset.bitset, tidset.support);
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

