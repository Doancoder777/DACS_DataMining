package Algorithrms.frequentpatterns.aprioriTID;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import patterns.itemset_array_integers_with_tids_bitset.Itemset;
import tools.MemoryLogger;
public class AlgoAprioriTID_Bitset {
	protected int k; 
	Map<Integer, BitSet> mapItemTIDS = new HashMap<Integer, BitSet>();
	int minSuppRelative;
	int maxItemsetSize = Integer.MAX_VALUE;
	long startTimestamp = 0; 
	long endTimeStamp = 0; 
	BufferedWriter writer = null;
	private int itemsetCount;
	private int tidcount = 0;
	boolean showTransactionIdentifiers = false;
	public AlgoAprioriTID_Bitset() {
	}
	public void runAlgorithm(String input, String output, double minsup)
			throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		itemsetCount = 0;
		writer = new BufferedWriter(new FileWriter(output));
		tidcount = 0;
		mapItemTIDS = new HashMap<Integer, BitSet>(); 
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { 
			MemoryLogger.getInstance().checkMemory();
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			String[] lineSplited = line.split(" ");
			for (String stringItem : lineSplited) {
				int item = Integer.parseInt(stringItem);
				BitSet tids = mapItemTIDS.get(item);
				if (tids == null) {
					tids = new BitSet();
					mapItemTIDS.put(item, tids);
				}
				tids.set(tidcount);
			}
			tidcount++;
		}
		reader.close(); // close the input file
		this.minSuppRelative = (int) Math.ceil(minsup * tidcount);
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		Iterator<Entry<Integer, BitSet>> iterator = mapItemTIDS.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, BitSet> entry = (Map.Entry<Integer, BitSet>) iterator
					.next();
			int cardinality = entry.getValue().cardinality();
			if (cardinality >= minSuppRelative && maxItemsetSize >= 1) { 
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item), cardinality);
				level.add(itemset);
				saveItemsetToFile(itemset);
			} else {
				iterator.remove(); // if the item is not frequent we don't
			}
		}
		Collections.sort(level, new Comparator<Itemset>() {
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		k = 2;
		while (!level.isEmpty() && k <= maxItemsetSize) {
			level = generateCandidateSizeK(level);
			; // We keep only the last level...
			k++;
		}
		writer.close();
		endTimeStamp = System.currentTimeMillis();
	}
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1)
			throws IOException {
		List<Itemset> candidates = new ArrayList<Itemset>();
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			Itemset itemset1 = levelK_1.get(i);
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				Itemset itemset2 = levelK_1.get(j);
				for (int k = 0; k < itemset1.size(); k++) {
					if (k == itemset1.size() - 1) {
						if (itemset1.getItems()[k] >= itemset2.get(k)) {
							continue loop1;
						}
					}
					else if (itemset1.getItems()[k] < itemset2.get(k)) {
						continue loop2; // we continue searching
					} else if (itemset1.getItems()[k] > itemset2.get(k)) {
						continue loop1; // we stop searching: because of lexical
					}
				}
				Integer missing = itemset2.get(itemset2.size() - 1);
				BitSet list = (BitSet) itemset1.getTransactionsIds().clone();
				list.and(itemset2.getTransactionsIds());
				int cardinality = list.cardinality();
				if (cardinality >= minSuppRelative) {
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list, cardinality);
					candidates.add(candidate);
					saveItemsetToFile(candidate);
				}
			}
		}
		return candidates;
	}
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
	void saveItemsetToFile(Itemset itemset) throws IOException {
		writer.write(itemset.toString() + " #SUP: " + itemset.cardinality);
		if(showTransactionIdentifiers) {
        	writer.append(" #TID:");
        	BitSet transactionIDs = itemset.getTransactionsIds();
        	for (int tid = transactionIDs.nextSetBit(0); tid != -1; tid = transactionIDs.nextSetBit(tid + 1)) {
        		writer.append(" " + tid); 
        	}
		}
		writer.newLine();
		itemsetCount++; // increase frequent itemset count
	}
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	public void printStats() {
		System.out.println("=============  APRIORI TID BITSET v2.12 - STATS =============");
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)	+ " ms");
		System.out.println("===================================================");
	}
	public void setMaximumPatternLength(int length) {
		this.maxItemsetSize = length;
	}
}

