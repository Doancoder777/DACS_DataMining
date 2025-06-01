package input.utility_transaction_database;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import Algo.two_phase.AlgoTwoPhase;
public class UtilityTransactionDatabaseTP {
	private final Set<Integer> allItems = new HashSet<Integer>();
	private final List<TransactionTP> transactions = new ArrayList<TransactionTP>();
	private long totalUtility;
	private int maxItemID;
	Map<Integer, String> mapItemIDtoStringValue = null;
	public void loadFile(String path) throws IOException {
		maxItemID = 0;
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.startsWith("@ITEM")) {
					thisLine = thisLine.substring(6);
					int index = thisLine.indexOf("=");
					int itemID = Integer.parseInt(thisLine.substring(0, index));
					String stringValue = thisLine.substring(index + 1);
					if (mapItemIDtoStringValue == null) {
						mapItemIDtoStringValue = new HashMap<Integer, String>();
					}
					mapItemIDtoStringValue.put(itemID, stringValue);
				} else if (thisLine.isEmpty() == false && thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					processTransaction(thisLine.split(":"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	private void processTransaction(String[] line) {
		int transactionUtility = Integer.parseInt(line[1]);
		String[] items = line[0].split(" ");
		String[] utilities = line[2].split(" ");
		List<ItemUtility> itemUtilityObjects = new ArrayList<ItemUtility>();
		for (int i = 0; i < items.length; i++) {
			int item = Integer.parseInt(items[i]);
			itemUtilityObjects.add(new ItemUtility(item, Integer.parseInt(utilities[i])));
			allItems.add(item);
			if(item >maxItemID) {
				maxItemID = item;
			}
		}
		totalUtility += transactionUtility;
		transactions.add(new TransactionTP(itemUtilityObjects, transactionUtility));
	}
	public void printDatabase() {
		System.out.println("===================  Database ===================");
		int count = 0;
		for (TransactionTP itemset : transactions) {
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	public int size() {
		return transactions.size();
	}
	public List<TransactionTP> getTransactions() {
		return transactions;
	}
	public Set<Integer> getAllItems() {
		return allItems;
	}
	public String getNameForItem(Integer item) {
		if (mapItemIDtoStringValue == null) {
			return null;
		}
		String name = mapItemIDtoStringValue.get(item);
		if (name == null) {
			return null;
		}
		return name;
	}
	public Map<Integer, String> getMapItemToStringValues() {
		return mapItemIDtoStringValue;
	}
	public long getTotalUtility() {
		return totalUtility;
	}
	public int getMaxItemID() {
		return maxItemID;
	}
}

