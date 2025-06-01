package input.product_transaction_database;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
public class ProductTransactionDatabase {
	private List<ProductTransaction> transactions;
	private Set<Integer> distinctItems;
	private ArrayList<Integer> distinctItemsList;
	private int maxItemID;
	public ProductTransactionDatabase() {
		transactions = new ArrayList<ProductTransaction>();
		distinctItems = new HashSet<Integer>();
	}
	public void loadFile(String filepath) throws Exception {
		Scanner scanner = new Scanner(new File(filepath));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.isEmpty() == false && line.charAt(0) != '#' && line.charAt(0) != '%' && line.charAt(0) != '@') {
				String[] parts = line.split(" ");
				int profit = Integer.parseInt(parts[0]);
				List<Integer> items = new ArrayList<Integer>();
				for (int i = 1; i < parts.length; i++) {
					int item = Integer.parseInt(parts[i]);
					items.add(item);
					this.distinctItems.add(item);
					if (item > maxItemID) {
						maxItemID = item;
					}
				}
				ProductTransaction transaction = new ProductTransaction(profit, items);
				transactions.add(transaction);
			}
		}
		scanner.close();
		distinctItemsList = new ArrayList<Integer>(distinctItems);
	}
	public int getMaxItemID() {
		return maxItemID;
	}
	public List<ProductTransaction> getTransactions() {
		return transactions;
	}
	public List<Integer> getItems() {
		return distinctItemsList;
	}
	public int size() {
		return transactions.size();
	}
}
