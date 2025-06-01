package patterns.itemset_array_integers_with_count;
import java.util.Arrays;
import java.util.List;
import Algo.ArraysAlgos;
import patterns.AbstractOrderedItemset;
public class Itemset extends AbstractOrderedItemset{
	public int[] itemset; 
	public int support = 0; 
	public int[] getItems() {
		return itemset;
	}
	public Itemset(){
		itemset = new int[]{};
	}
	public Itemset(int item){
		itemset = new int[]{item};
	}
	public Itemset(int item, int support){
		itemset = new int[]{item};
	    this.support = support;
	}
	public Itemset(int [] items){
		this.itemset = items;
	}
	public Itemset(List<Integer> itemset, int support){
		this.itemset = new int[itemset.size()];
	    int i = 0;
	    for (Integer item : itemset) { 
	    	this.itemset[i++] = item.intValue();
	    }
	    this.support = support;
	}
	public int getAbsoluteSupport(){
		return support;
	}
	public int size() {
		return itemset.length;
	}
	public Integer get(int position) {
		return itemset[position];
	}
	public void setAbsoluteSupport(Integer support) {
		this.support = support;
	}
	public void increaseTransactionCount() {
		this.support++;
	}
	public Itemset cloneItemSetMinusOneItem(Integer itemToRemove) {
		int[] newItemset = new int[itemset.length -1];
		int i=0;
		for(int j =0; j < itemset.length; j++){
			if(itemset[j] != itemToRemove){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
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
	public Itemset intersection(Itemset itemset2) {
		int [] intersection = ArraysAlgos.intersectTwoSortedArrays(this.getItems(), itemset2.getItems());
		return new Itemset(intersection);
	}
	@Override
	public int hashCode() {
		return Arrays.hashCode(itemset);
	}
}

