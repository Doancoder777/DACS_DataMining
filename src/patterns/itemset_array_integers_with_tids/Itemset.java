package patterns.itemset_array_integers_with_tids;
import java.util.HashSet;
import java.util.Set;
import patterns.AbstractOrderedItemset;
public class Itemset extends AbstractOrderedItemset{
	public int[] itemset; 
	public Set<Integer> transactionsIds = new HashSet<Integer>();
	public Itemset() {
	}
	public Itemset(int item){
		itemset = new int[]{item};
	}
	public Itemset(int [] items){
		this.itemset = items;
	}
	public int getAbsoluteSupport() {
		return transactionsIds.size();
	}
	public int[] getItems() {
		return itemset;
	}
	public Integer get(int index) {
		return itemset[index];
	}
	public void setTIDs(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	public int size() {
		return itemset.length;
	}
	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
	public Itemset cloneItemSetMinusAnItemset(Itemset itemsetToNotKeep) {
		int[] newItemset = new int[itemset.length - itemsetToNotKeep.size()];
		int i=0;
		for(int j =0; j < itemset.length; j++){
			if(itemsetToNotKeep.contains(itemset[j]) == false){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
	}
	public Itemset cloneItemSetMinusOneItem(Integer itemsetToRemove) {
		int[] newItemset = new int[itemset.length -1];
		int i=0;
		for(int j =0; j < itemset.length; j++){
			if(itemset[j] != itemsetToRemove){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
	}
}

