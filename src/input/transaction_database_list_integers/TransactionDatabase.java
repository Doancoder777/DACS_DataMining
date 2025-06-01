package input.transaction_database_list_integers;
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
public class TransactionDatabase {
	private final Set<Integer> items = new HashSet<Integer>();
	private final List<List<Integer>> transactions = new ArrayList<List<Integer>>();
	private int maxItemID;
	Map<Integer, String> mapItemIDtoStringValue = null;
	public void addTransaction(List<Integer> transaction) {
		transactions.add(transaction);
		items.addAll(transaction);
	}
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line
		try (BufferedReader myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))))) {
			maxItemID = 0;
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == false) {
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
						addTransaction(thisLine.split(" "));
					}
				}
			}
		}
	}
	private void addTransaction(String itemsString[]) {
		List<Integer> itemset = new ArrayList<Integer>();
		for (String attribute : itemsString) {
			int item = Integer.parseInt(attribute);
			itemset.add(item);
			items.add(item);
			if (item > maxItemID) {
				maxItemID = item;
			}
		}
		transactions.add(itemset);
	}
	public void printDatabase() {
		System.out.println("===================  TRANSACTION DATABASE ===================");
		int count = 0;
		for (List<Integer> itemset : transactions) { // pour chaque objet
			System.out.print(count + ":  ");
			print(itemset); // print the transaction
			count++;
		}
	}
	private void print(List<Integer> itemset) {
		StringBuilder r = new StringBuilder();
		for (Integer item : itemset) {
			r.append(item.toString());
			r.append(' ');
		}
		System.out.println(r); // print to System.out
	}
	public int size() {
		return transactions.size();
	}
	public List<List<Integer>> getTransactions() {
		return transactions;
	}
	public Set<Integer> getItems() {
		return items;
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
	public int getMaxItemID() {
		return maxItemID;
	}
}

