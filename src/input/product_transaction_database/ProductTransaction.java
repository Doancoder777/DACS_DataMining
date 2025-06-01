package input.product_transaction_database;
import java.util.List;
public class ProductTransaction {
	private int profit;
	private List<Integer> items;
	public ProductTransaction(int profit, List<Integer> items) {
		this.profit = profit;
		this.items = items;
	}
	public int getProfit() {
		return profit;
	}
	public List<Integer> getItems() {
		return items;
	}
	public boolean contains(int item) {
		return items.contains(item);
	}
	public int size() {
		return items.size();
	}
	public String toString() {
		return "Profit: " + profit + " | Items: " + items;
	}
}

