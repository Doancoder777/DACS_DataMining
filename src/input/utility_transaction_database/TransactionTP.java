package input.utility_transaction_database;
import java.util.List;
import Algo.two_phase.AlgoTwoPhase;
public class TransactionTP{
	protected final List<ItemUtility> itemsUtilities; 
	protected final int transactionUtility;
	public TransactionTP(List<ItemUtility> itemsUtilities, int transactionUtility){
		this.itemsUtilities =  itemsUtilities;
		this.transactionUtility = transactionUtility;
	}
	public List<ItemUtility> getItems(){
		return itemsUtilities;
	}
	public ItemUtility get(int index){
		return itemsUtilities.get(index);
	}
	public void print(){
		System.out.print(toString());
	}
	public String toString(){
		StringBuilder r = new StringBuilder ();
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
			if(i == itemsUtilities.size() -1){
				r.append(":");
			}
		}
		r.append(transactionUtility + ": ");
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
		}
		return r.toString();
	}
	public boolean contains(Integer item) {
		for(ItemUtility itemI : itemsUtilities){
			if(itemI.item == item){
				return true;
			}else if(itemI.item > item){
				return false;
			}
		}
		return false;
	}
	public boolean contains(int item) {
		for(int i=0; i<itemsUtilities.size(); i++){
			if(itemsUtilities.get(i).item == item){
				return true;
			}else if(itemsUtilities.get(i).item > item){
				return false;
			}
		}
		return false;
	}
	public int size(){
		return itemsUtilities.size();
	}
	public List<ItemUtility> getItemsUtilities() {
		return itemsUtilities;
	}
	public int getTransactionUtility() {
		return transactionUtility;
	}
}

