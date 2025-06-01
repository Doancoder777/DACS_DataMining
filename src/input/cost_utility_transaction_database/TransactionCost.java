package input.cost_utility_transaction_database;
import java.util.List;
public class TransactionCost{
	protected final List<ItemCost> itemsCosts; 
	protected final int transactionUtility;
	public TransactionCost(List<ItemCost> itemsCosts, int transactionUtility){
		this.itemsCosts =  itemsCosts;
		this.transactionUtility = transactionUtility;
	}
	public List<ItemCost> getItems(){
		return itemsCosts;
	}
	public ItemCost get(int index){
		return itemsCosts.get(index);
	}
	public void print(){
		System.out.print(toString());
	}
	public String toString(){
		StringBuilder r = new StringBuilder ();
		for(int i=0; i< itemsCosts.size(); i++){
			r.append(itemsCosts.get(i) + " ");
			if(i == itemsCosts.size() -1){
				r.append(":");
			}
		}
		r.append(transactionUtility + ": ");
		for(int i=0; i< itemsCosts.size(); i++){
			r.append(itemsCosts.get(i) + " ");
		}
		return r.toString();
	}
	public boolean contains(Integer item) {
		for(ItemCost itemI : itemsCosts){
			if(itemI.item == item){
				return true;
			}else if(itemI.item > item){
				return false;
			}
		}
		return false;
	}
	public boolean contains(int item) {
		for(int i=0; i<itemsCosts.size(); i++){
			if(itemsCosts.get(i).item == item){
				return true;
			}else if(itemsCosts.get(i).item > item){
				return false;
			}
		}
		return false;
	}
	public int size(){
		return itemsCosts.size();
	}
	public List<ItemCost> getItemsCosts() {
		return itemsCosts;
	}
	public int getTransactionUtility() {
		return transactionUtility;
	}
}

