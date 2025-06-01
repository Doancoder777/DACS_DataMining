package input.cost_utility_transaction_database;
public class ItemCost{
	public ItemCost(int item, int cost) {
		this.item = item;
		this.cost = cost;
	}
	public int item;
	public  int cost;
	public String toString() {
		return "[" + item + "," + cost + "]";
	}
}
