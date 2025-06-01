package Algo.two_phase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import input.utility_transaction_database.ItemUtility;
import input.utility_transaction_database.TransactionTP;
import input.utility_transaction_database.UtilityTransactionDatabaseTP;
import tools.MemoryLogger;
public class AlgoTwoPhase {
	private ItemsetsTP highUtilityItemsets = null;
	protected UtilityTransactionDatabaseTP database;
	int minUtility;
	long startTimestamp = 0;  // start time
	long endTimestamp = 0; // end time
	private int candidatesCount; // the number of candidates generated
	public AlgoTwoPhase() {
	}
	public ItemsetsTP runAlgorithm(UtilityTransactionDatabaseTP database, int minUtility) {
		this.database = database;
		this.minUtility = minUtility;
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		highUtilityItemsets = new ItemsetsTP("HIGH UTILITY ITEMSETS");
		candidatesCount =0;
		List<ItemsetTP> candidatesSize1 = new ArrayList<ItemsetTP>();
		Map<Integer, Set<Integer>> mapItemTidsets = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapItemTWU = new HashMap<Integer, Integer>();
		int maxItem = Integer.MIN_VALUE;
		for(int i=0; i< database.size(); i++){
			TransactionTP transaction = database.getTransactions().get(i);
			for(int j=0; j< transaction.getItems().size(); j++) {
				ItemUtility itemUtilityObj = transaction.getItems().get(j);
				int item = itemUtilityObj.item;
				if(item > maxItem){
					maxItem = item;
				}
				Set<Integer> tidset = mapItemTidsets.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					mapItemTidsets.put(item, tidset);
				}
				tidset.add(i);
				Integer sumUtility = mapItemTWU.get(item);
				if(sumUtility == null){  // if no utility yet
					sumUtility = 0;
				}
				sumUtility += transaction.getTransactionUtility(); // add the utility
				mapItemTWU.put(item, sumUtility);
			}
		}
		for(int item=0; item<= maxItem; item++){
			Integer estimatedUtility = mapItemTWU.get(item);
			if(estimatedUtility != null && estimatedUtility >= minUtility){
				ItemsetTP itemset = new ItemsetTP();
				itemset.addItem(item);
				itemset.setTIDset(mapItemTidsets.get(item));
				candidatesSize1.add(itemset);
				highUtilityItemsets.addItemset(itemset, itemset.size());
			}
		}
		List<ItemsetTP> currentLevel = candidatesSize1;
		while (true) {
			int candidateCount = highUtilityItemsets.getItemsetsCount();
			currentLevel = generateCandidateSizeK(currentLevel, highUtilityItemsets);
			if(candidateCount == highUtilityItemsets.getItemsetsCount()){
				break;
			}
		}
		MemoryLogger.getInstance().checkMemory();
		candidatesCount = highUtilityItemsets.getItemsetsCount();
		for(List<ItemsetTP> level : highUtilityItemsets.getLevels()){
			Iterator<ItemsetTP> iterItemset = level.iterator();
			while(iterItemset.hasNext()){
				ItemsetTP candidate = iterItemset.next();
				for(TransactionTP transaction : database.getTransactions()){
					int transactionUtility =0;
					int matchesCount =0; 
					for(int i=0; i< transaction.size(); i++){
						if(candidate.getItems().contains(transaction.get(i).item)){
							transactionUtility += transaction.getItemsUtilities().get(i).utility;
							matchesCount++; // increase the number of matches
						}
					}
					if(matchesCount == candidate.size()){
						candidate.incrementUtility(transactionUtility);
					}
				}
				if(candidate.getUtility() < minUtility){
					iterItemset.remove(); // delete it
					highUtilityItemsets.decreaseCount();  // decrease number of itemsets found
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return highUtilityItemsets; 
	}
	protected List<ItemsetTP> generateCandidateSizeK(List<ItemsetTP> levelK_1, ItemsetsTP candidatesHTWUI) {
	loop1:	for(int i=0; i< levelK_1.size(); i++){
				ItemsetTP itemset1 = levelK_1.get(i);
	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					ItemsetTP itemset2 = levelK_1.get(j);
				for(int k=0; k< itemset1.size(); k++){
					if(k == itemset1.size()-1){ 
						if(itemset1.getItems().get(k) >= itemset2.get(k)){  
							continue loop1;
						}
					}
					else if(itemset1.getItems().get(k) < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k) > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				Integer missing = itemset2.get(itemset2.size()-1);
				Set<Integer> tidset = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTIDset()){
					if(itemset2.getTIDset().contains(val1)){
						tidset.add(val1);
					}
				}
				int twu =0;
				for(Integer tid : tidset){
					twu += database.getTransactions().get(tid).getTransactionUtility();
				}
				if(twu >= minUtility){
					ItemsetTP candidate = new ItemsetTP();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTIDset(tidset);
					candidatesHTWUI.addItemset(candidate, candidate.size());
				}
			}
		}
		return candidatesHTWUI.getLevels().get(candidatesHTWUI.getLevels().size()-1);
	}
	public void printStats() {
		System.out
				.println("=============  TWO-PHASE ALGORITHM - STATS =============");
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Candidates count : " + candidatesCount); 
		System.out.println(" High-utility itemsets count : " + highUtilityItemsets.getItemsetsCount()); 
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out
				.println("===================================================");
	}
}
