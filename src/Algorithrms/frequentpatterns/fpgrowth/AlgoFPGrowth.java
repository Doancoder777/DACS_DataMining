package Algorithrms.frequentpatterns.fpgrowth;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;
public class AlgoFPGrowth {
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	public int minSupportRelative;// the relative minimum support
	BufferedWriter writer = null; // object to write the output file
	protected Itemsets patterns = null;
	final int BUFFERS_SIZE = 2000;
	private int[] itemsetBuffer = null;
	private FPNode[] fpNodeTempBuffer = null;
	private int[] itemsetOutputBuffer = null;
	private int maxPatternLength = 1000;
	private int minPatternLength = 0;
	public AlgoFPGrowth() {
	}
	public Itemsets runAlgorithm(String input, String output, double minsupp) throws FileNotFoundException, IOException {
		startTimestamp = System.currentTimeMillis();
		itemsetCount = 0;
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
		if(output == null){
			writer = null;
			patterns = new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
			itemsetOutputBuffer = new int[BUFFERS_SIZE];
		}
		final Map<Integer, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 
		this.minSupportRelative = (int) Math.ceil(minsupp * transactionCount);
		System.out.println("FP-Growth parameters:");
		System.out.println("- MinSupport: " + minSupportRelative + " (" + (minsupp * 100) + "%)");
		System.out.println("- Total transactions: " + transactionCount);
		FPTree tree = new FPTree();
		buildFPTreeFromTransactionMap(input, tree, mapSupport);
		tree.createHeaderList(mapSupport);
		if(tree.headerList.size() > 0) {
			itemsetBuffer = new int[BUFFERS_SIZE];
			fpNodeTempBuffer = new FPNode[BUFFERS_SIZE];
			fpgrowth(tree, itemsetBuffer, 0, transactionCount, mapSupport);
		}
		if(writer != null){
			writer.close();
		}
		endTime = System.currentTimeMillis();
		MemoryLogger.getInstance().checkMemory();
		return patterns;
	}
	private void buildFPTreeFromTransactionMap(String input, FPTree tree, Map<Integer, Integer> mapSupport) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		line = reader.readLine();
		if (line == null || line.trim().isEmpty()) {
			reader.close();
			throw new IOException("Input file is empty or invalid");
		}
		String[] header = line.trim().split(" ");
		if (header.length != 2) {
			reader.close();
			throw new IOException("First line must contain number of transactions and items");
		}
		Map<Integer, List<Integer>> transactionMap = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}
			String[] parts = line.trim().split(" ");
			if (parts.length < 2) {
				continue;
			}
			int transactionId = Integer.parseInt(parts[0]);
			int itemId = Integer.parseInt(parts[1]);
			int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
			if (count > 0) {
				transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
				if (!transactionMap.get(transactionId).contains(itemId)) {
					transactionMap.get(transactionId).add(itemId);
				}
			}
		}
		reader.close();
		int validTransactions = 0;
		for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
			List<Integer> transaction = entry.getValue();
			List<Integer> filteredTransaction = new ArrayList<>();
			for (Integer item : transaction) {
				if (mapSupport.get(item) >= minSupportRelative) {
					filteredTransaction.add(item);
				}
			}
			if (!filteredTransaction.isEmpty()) {
				Collections.sort(filteredTransaction, new Comparator<Integer>() {
					public int compare(Integer item1, Integer item2) {
						int compare = mapSupport.get(item2) - mapSupport.get(item1);
						if (compare == 0) {
							return (item1 - item2);
						}
						return compare;
					}
				});
				tree.addTransaction(filteredTransaction);
				validTransactions++;
			}
		}
		System.out.println("FP-Tree built successfully:");
		System.out.println("- Total transactions read: " + transactionMap.size());
		System.out.println("- Valid transactions with frequent items: " + validTransactions);
		System.out.println("Frequent items (support >= " + minSupportRelative + "):");
		mapSupport.entrySet().stream()
			.filter(entry -> entry.getValue() >= minSupportRelative)
			.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
			.forEach(entry -> {
				double supportPercent = (entry.getValue() * 100.0) / transactionCount;
				System.out.println("  Item " + entry.getKey() + ": " + entry.getValue() + 
								 " (" + String.format("%.1f", supportPercent) + "%)");
			});
	}
	private void fpgrowth(FPTree tree, int[] prefix, int prefixLength, int prefixSupport, Map<Integer, Integer> mapSupport) throws IOException {
		if(prefixLength == maxPatternLength){
			return;
		}
		boolean singlePath = true;
		int position = 0;
		if(tree.root.childs.size() > 1) {
			singlePath = false;
		}else {
			if(tree.root.childs.size() == 1) {
				FPNode currentNode = tree.root.childs.get(0);
				while(true){
					if(currentNode.childs.size() > 1) {
						singlePath = false;
						break;
					}
					fpNodeTempBuffer[position] = currentNode;
					position++;
					if(currentNode.childs.size() == 0) {
						break;
					}
					currentNode = currentNode.childs.get(0);
				}
			}
		}
		if(singlePath && position > 0){	
			saveAllCombinationsOfPrefixPath(fpNodeTempBuffer, position, prefix, prefixLength);
		}else {
			for(int i = tree.headerList.size()-1; i >= 0; i--){
				Integer item = tree.headerList.get(i);
				int support = mapSupport.get(item);
				prefix[prefixLength] = item;
				int betaSupport = (prefixSupport < support) ? prefixSupport: support;
				saveItemset(prefix, prefixLength+1, betaSupport);
				if(prefixLength+1 < maxPatternLength){
					List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
					FPNode path = tree.mapItemNodes.get(item);
					Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
					while(path != null){
						if(path.parent.itemID != -1){
							List<FPNode> prefixPath = new ArrayList<FPNode>();
							prefixPath.add(path);   // NOTE: we add it just to keep its support,
							int pathCount = path.counter;
							FPNode parent = path.parent;
							while(parent.itemID != -1){
								prefixPath.add(parent);
								if(mapSupportBeta.get(parent.itemID) == null){
									mapSupportBeta.put(parent.itemID, pathCount);
								}else{
									mapSupportBeta.put(parent.itemID, mapSupportBeta.get(parent.itemID) + pathCount);
								}
								parent = parent.parent;
							}
							prefixPaths.add(prefixPath);
						}
						path = path.nodeLink;
					}
					FPTree treeBeta = new FPTree();
					for(List<FPNode> prefixPath : prefixPaths){
						treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minSupportRelative); 
					}  
					if(treeBeta.root.childs.size() > 0){
						treeBeta.createHeaderList(mapSupportBeta); 
						fpgrowth(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
					}
				}
			}
		}
	}
	private void saveAllCombinationsOfPrefixPath(FPNode[] fpNodeTempBuffer, int position, 
			int[] prefix, int prefixLength) throws IOException {
		int support = 0;
loop1:	for (long i = 1, max = 1 << position; i < max; i++) {
			int newPrefixLength = prefixLength;
			for (int j = 0; j < position; j++) {
				int isSet = (int) i & (1 << j);
				if (isSet > 0) {
					if(newPrefixLength == maxPatternLength){
						continue loop1;
					}
					prefix[newPrefixLength++] = fpNodeTempBuffer[j].itemID;
					support = fpNodeTempBuffer[j].counter;
				}
			}
			saveItemset(prefix, newPrefixLength, support);
		}
	}
	private Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
			throws FileNotFoundException, IOException {
		Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		line = reader.readLine();
		if (line == null || line.trim().isEmpty()) {
			reader.close();
			throw new IOException("Input file is empty or invalid");
		}
		String[] header = line.trim().split(" ");
		if (header.length != 2) {
			reader.close();
			throw new IOException("First line must contain number of transactions and items");
		}
		System.out.println("Reading ItemsetTree format - Header: " + line);
		Map<Integer, List<Integer>> transactionMap = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}
			String[] parts = line.trim().split(" ");
			if (parts.length < 2) {
				continue;
			}
			int transactionId = Integer.parseInt(parts[0]);
			int itemId = Integer.parseInt(parts[1]);
			int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
			if (count > 0) {
				transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
				if (!transactionMap.get(transactionId).contains(itemId)) {
					transactionMap.get(transactionId).add(itemId);
					mapSupport.put(itemId, mapSupport.getOrDefault(itemId, 0) + 1);
				}
			}
		}
		transactionCount = transactionMap.size();
		reader.close();
		System.out.println("Database scan completed:");
		System.out.println("- Scanned " + transactionCount + " unique transactions");
		System.out.println("- Found " + mapSupport.size() + " unique items");
		return mapSupport;
	}
	private void saveItemset(int[] itemset, int itemsetLength, int support) throws IOException {
		if(itemsetLength < minPatternLength) {
			return;
		}
		itemsetCount++;
		if(writer != null){
			System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
			Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
			StringBuilder buffer = new StringBuilder();
			for(int i = 0; i < itemsetLength; i++){
				buffer.append(itemsetOutputBuffer[i]);
				if(i != itemsetLength-1){
					buffer.append(' ');
				}
			}
			buffer.append(" #SUP: ");
			buffer.append(support);
			writer.write(buffer.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			int[] itemsetArray = new int[itemsetLength];
			System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);
			Arrays.sort(itemsetArray);
			Itemset itemsetObj = new Itemset(itemsetArray);
			itemsetObj.setAbsoluteSupport(support);
			patterns.addItemset(itemsetObj, itemsetLength);
		}
	}
	public void printStats() {
		System.out.println("=============  FP-GROWTH 2.42 - STATS (ENHANCED) =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
		System.out.println(" Frequent itemsets count : " + itemsetCount); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===========================================================");
	}
	public int getDatabaseSize() {
		return transactionCount;
	}
	public void setMaximumPatternLength(int length) {
		maxPatternLength = length;
	}
	public void setMinimumPatternLength(int minPatternLength) {
		this.minPatternLength = minPatternLength;
	}
}
