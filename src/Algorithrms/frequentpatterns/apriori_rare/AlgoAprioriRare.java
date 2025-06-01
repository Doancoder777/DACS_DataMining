package Algorithrms.frequentpatterns.apriori_rare;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import Algo.ArraysAlgos;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;
public class AlgoAprioriRare {
	protected int k; 
	protected int totalCandidateCount = 0; // number of candidates generated during last execution
	protected long startTimestamp; // start time of last execution
	protected long endTimestamp; // end time of last execution
	private int itemsetCount;  // itemset count, found during the last execution
	private int databaseSize;
	private int minsupRelative;
	private List<int[]> database = null;
	protected Itemsets patterns = null;
	BufferedWriter writer = null; 
	public AlgoAprioriRare() {
	}
	public Itemsets runAlgorithm(double minsup, String input, String output) throws IOException {
		if(output == null){
			writer = null;
			patterns =  new Itemsets("MINIMAL RARE ITEMSETS");
	    }else{ // if the user wants to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		startTimestamp = System.currentTimeMillis();
		itemsetCount = 0;
		totalCandidateCount = 0;
		MemoryLogger.getInstance().reset();
		databaseSize = 0;
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>(); // to count the support of each item
		database = new ArrayList<int[]>(); // the database in memory (intially empty)
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { 
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			String[] lineSplited = line.split(" ");
			int transaction[] = new int[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) { 
				Integer item = Integer.parseInt(lineSplited[i]);
				transaction[i] = item;
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
			database.add(transaction);
			databaseSize++;
		}
		reader.close();
		this.minsupRelative = (int) Math.ceil(minsup * databaseSize);
		k = 1;
		List<Integer> frequent1 = new ArrayList<Integer>();
		for(Entry<Integer, Integer> entry : mapItemCount.entrySet()){
			if(entry.getValue() >= minsupRelative){
				frequent1.add(entry.getKey());
			}else{
				saveItemsetToFile(entry.getKey(), entry.getValue());
			}
		}
		mapItemCount = null;
		Collections.sort(frequent1, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		if(frequent1.size() == 0){
			if(writer != null){
				writer.close();
			}
			return patterns; 
		}
		totalCandidateCount += frequent1.size();
		List<Itemset> level = null;
		k = 2;
		do{
			MemoryLogger.getInstance().checkMemory();
			List<Itemset> candidatesK;
			if(k ==2){
				candidatesK = generateCandidate2(frequent1);
			}else{
				candidatesK = generateCandidateSizeK(level);
			}
			totalCandidateCount += candidatesK.size();
			for(int[] transaction: database){
	 loopCand:	for(Itemset candidate : candidatesK){
					int pos = 0;
					for(int item: transaction){
						if(item == candidate.itemset[pos]){
							pos++;
							if(pos == candidate.itemset.length){
								candidate.support++;
								continue loopCand;
							}
						}else if(item > candidate.itemset[pos]){
							continue loopCand;
						}
					}
				}
			}
			level = new ArrayList<Itemset>();
			for (Itemset candidate : candidatesK) {
				if (candidate.getAbsoluteSupport() >= minsupRelative) {
					level.add(candidate);
				}else{
					saveItemset(candidate);
				}
			}
			k++;
		}while(level.isEmpty() == false);
		endTimestamp = System.currentTimeMillis();
		MemoryLogger.getInstance().checkMemory();
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	public int getDatabaseSize() {
		return databaseSize;
	}
	private List<Itemset> generateCandidate2(List<Integer> frequent1) {
		List<Itemset> candidates = new ArrayList<Itemset>();
		for (int i = 0; i < frequent1.size(); i++) {
			Integer item1 = frequent1.get(i);
			for (int j = i + 1; j < frequent1.size(); j++) {
				Integer item2 = frequent1.get(j);
				candidates.add(new Itemset(new int []{item1, item2}));
			}
		}
		return candidates;
	}
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) {
		List<Itemset> candidates = new ArrayList<Itemset>();
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			int[] itemset1 = levelK_1.get(i).itemset;
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				int[] itemset2 = levelK_1.get(j).itemset;
				for (int k = 0; k < itemset1.length; k++) {
					if (k == itemset1.length - 1) {
						if (itemset1[k] >= itemset2[k]) {
							continue loop1;
						}
					}
					else if (itemset1[k] < itemset2[k]) {
						continue loop2; // we continue searching
					} else if (itemset1[k] > itemset2[k]) {
						continue loop1; // we stop searching: because of lexical
					}
				}
				int newItemset[] = new int[itemset1.length+1];
				System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
				newItemset[itemset1.length] = itemset2[itemset2.length -1];
				if (allSubsetsOfSizeK_1AreFrequent(newItemset, levelK_1)) {
					candidates.add(new Itemset(newItemset));
				}
			}
		}
		return candidates; // return the set of candidates
	}
	protected boolean allSubsetsOfSizeK_1AreFrequent(int[] candidate, List<Itemset> levelK_1) {
		for(int posRemoved=0; posRemoved< candidate.length; posRemoved++){
	        int first = 0;
	        int last = levelK_1.size() - 1;
	        boolean found = false;
	        while( first <= last )
	        {
	        	int middle = ( first + last ) >>> 1; // divide by 2
		    	int comparison = ArraysAlgos.sameAs(levelK_1.get(middle).getItems(), candidate, posRemoved);
		        if(comparison < 0 ){
		        	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
		        }
		        else if(comparison  > 0 ){
		        	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
		        }
	            else{
	            	found = true; //  we have found it so we stop
	                break;
	            }
	        }
			if(found == false){  // if we did not find it, that means that candidate is not a frequent itemset because
				return false;
			}
		}
		return true;
	}
	void saveItemset(Itemset itemset) throws IOException {
		itemsetCount++; 
		if(writer != null){
			writer.write(itemset.toString() + " #SUP: "
					+ itemset.getAbsoluteSupport());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			patterns.addItemset(itemset, itemset.size());
		}
	}
	void saveItemsetToFile(Integer item, Integer support) throws IOException {
		itemsetCount++; // increase frequent itemset count
		if(writer != null){
			writer.write(item + " #SUP: " + support);
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			Itemset itemset = new Itemset(item);
			itemset.setAbsoluteSupport(support);
			patterns.addItemset(itemset, 1);
		}
	}
	public void printStats() {
		System.out.println("=============  APRIORI-RARE - STATS =============");
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Minimal rare itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
}

