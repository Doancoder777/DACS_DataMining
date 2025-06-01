package Algorithrms.frequentpatterns.aprioriTID_inverse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids.Itemset;
import patterns.itemset_array_integers_with_tids.Itemsets;
import tools.MemoryLogger;
public class AlgoAprioriTIDInverse {
	protected int k; 
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();
	int minSuppRelative;
	int maxSuppRelative;
	int maxItemsetSize = Integer.MAX_VALUE;
	long startTimestamp = 0; 
	long endTimeStamp = 0; 
	BufferedWriter writer = null;
	protected Itemsets patterns = null;
	private int itemsetCount = 0;
	private int databaseSize = 0;
	private TransactionDatabase database = null;
	private boolean emptySetIsRequired = false;
	boolean showTransactionIdentifiers = false;
	public AlgoAprioriTIDInverse() {
	}
	public Itemsets runAlgorithm(TransactionDatabase database, double minsup, double maxsup)
			throws NumberFormatException, IOException {
		this.database = database;
		Itemsets result = runAlgorithm(null, null, minsup, maxsup);
		this.database = null;
		return result;
	}
	public Itemsets runAlgorithm(String input, String output, double minsup, double maxsup)
			throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		itemsetCount = 0;
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user wants to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count
		databaseSize = 0; 
		if(database != null){
			for(List<Integer> transaction : database.getTransactions()){ // for each transaction
				for (int item : transaction) {
					Set<Integer> tids = mapItemTIDS.get(item);
					if (tids == null) {
						tids = new HashSet<Integer>();
						mapItemTIDS.put(item, tids);
					}
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			String line;
			while (((line = reader.readLine()) != null)) { // for each transaction
				if (line.isEmpty() == true ||
						line.charAt(0) == '#' || line.charAt(0) == '%'
								|| line.charAt(0) == '@') {
					continue;
				}
				String[] lineSplited = line.split(" ");
				for (String token : lineSplited) {
					int item = Integer.parseInt(token);
					Set<Integer> tids = mapItemTIDS.get(item);
					if (tids == null) {
						tids = new HashSet<Integer>();
						mapItemTIDS.put(item, tids);
					}
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
			reader.close(); // close the input file
		}
		if(emptySetIsRequired ){
			patterns.addItemset(new Itemset(new int[]{}), 0);
		}
		double rawMinSupp = minsup * databaseSize;
		double rawMaxSupp = maxsup * databaseSize;
		int oldMinSuppRelative = (int) Math.ceil(minsup * databaseSize);
		int oldMaxSuppRelative = (int) Math.ceil(maxsup * databaseSize);
		this.minSuppRelative = (int) Math.ceil(minsup * databaseSize) - 1;
		this.maxSuppRelative = (int) Math.ceil(maxsup * databaseSize);
		System.out.println("=== APRIORI-TID INVERSE THRESHOLD CALCULATION (FINAL FIXED) ===");
		System.out.println("Input parameters:");
		System.out.println("- minsup: " + (minsup*100) + "%");  
		System.out.println("- maxsup: " + (maxsup*100) + "%");
		System.out.println("- Database size: " + databaseSize);
		System.out.println();
		System.out.println("Raw calculations:");  
		System.out.println("- minsup * databaseSize = " + rawMinSupp);
		System.out.println("- maxsup * databaseSize = " + rawMaxSupp);
		System.out.println();
		System.out.println("OLD (BUGGY) thresholds:");
		System.out.println("- minSuppRelative = ceil(" + rawMinSupp + ") = " + oldMinSuppRelative);
		System.out.println("- maxSuppRelative = ceil(" + rawMaxSupp + ") = " + oldMaxSuppRelative);
		System.out.println("- Definition would be: " + oldMinSuppRelative + " < Support(X) <= " + oldMaxSuppRelative);
		System.out.println("- Problem: Items with support=" + oldMinSuppRelative + " would NOT be rare!");
		System.out.println();
		System.out.println("NEW (FIXED) thresholds:");
		System.out.println("- minSuppRelative = ceil(" + rawMinSupp + ") - 1 = " + minSuppRelative + " (FIXED)");
		System.out.println("- maxSuppRelative = ceil(" + rawMaxSupp + ") = " + maxSuppRelative);
		System.out.println("- Definition: " + minSuppRelative + " < Support(X) <= " + maxSuppRelative);
		System.out.println("- Now items with support=" + oldMinSuppRelative + " WILL be rare! ✓");
		System.out.println("================================================================");
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			MemoryLogger.getInstance().checkMemory();
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator
					.next();
			if (entry.getValue().size() > minSuppRelative
					&& entry.getValue().size() <= maxSuppRelative){
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item));
				level.add(itemset);
				saveItemset(itemset);
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
			k++;
		}
		if(writer != null){
			writer.close();
		}
		endTimeStamp = System.currentTimeMillis();
		return patterns;
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
					else if (itemset1.getItems()[k] < itemset2.getItems()[k]) {
						continue loop2; // we continue searching
					} else if (itemset1.getItems()[k] > itemset2.getItems()[k]) {
						continue loop1; // we stop searching: because of lexical
					}
				}
				Set<Integer> list = new HashSet<Integer>();
				for (Integer val1 : itemset1.getTransactionsIds()) {
					if (itemset2.getTransactionsIds().contains(val1)) {
						list.add(val1);
					}
				}
				if (list.size() > minSuppRelative && list.size() <= maxSuppRelative) {
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list);
					candidates.add(candidate);
					saveItemset(candidate);
				}
			}
		}
		return candidates;
	}
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
	void saveItemset(Itemset itemset) throws IOException {
		itemsetCount++;
		if(writer != null){
			writer.write(itemset.toString() + " #SUP: "
					+ itemset.getTransactionsIds().size() );
			if(showTransactionIdentifiers) {
	        	writer.append(" #TID:");
	        	for (Integer tid: itemset.getTransactionsIds()) {
	        		writer.append(" " + tid); 
	        	}
			}
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			patterns.addItemset(itemset, itemset.size());
		}
	}
	public void setEmptySetIsRequired(boolean emptySetIsRequired) {
		this.emptySetIsRequired = emptySetIsRequired;
	}
	public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
		this.showTransactionIdentifiers = showTransactionIdentifiers;
	}
	public void printStats() {
		System.out.println("=============  APRIORI INVERSE TID v2.18 - STATS (FINAL FIXED) =============");
		System.out.println(" Transactions count from database : " + databaseSize);
		System.out.println(" Rare itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out.println(" Definition: MRT < Support(X) <= MFT (FINAL FIXED)");
		System.out
				.println("===================================================================");
	}
	public int getDatabaseSize() {
		return databaseSize;
	}
}
