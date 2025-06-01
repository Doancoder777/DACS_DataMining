package patterns.itemset_array_integers_with_tids_bitset;
import java.util.BitSet;
import patterns.AbstractOrderedItemset;
public class Itemset extends AbstractOrderedItemset{
	public int[] itemset; // the array of items
	private BitSet transactionsIds;
	public int cardinality =0;  // the cardinality of the above bitset
	public Itemset(){
		transactionsIds = new BitSet();
		itemset = new int[0];
	}
	public Itemset(int[] itemset, BitSet bitset, int support){
		this.transactionsIds = bitset;
		this.itemset = itemset;
		this.cardinality = support;
	}
	public Itemset(int item){
		itemset = new int[]{item};
	}
	public Itemset(int [] items){
		this.itemset = items;
	}
	public int getAbsoluteSupport(){
		return cardinality;
	}
	public int[] getItems(){
		return itemset;
	}
	public Integer get(int index){
		return itemset[index];
	}
	public void setTIDs(BitSet listTransactionIds, int cardinality) {
		this.transactionsIds = listTransactionIds;
		this.cardinality = cardinality;
	}
	public int size(){
		return itemset.length;
	}
	public BitSet getTransactionsIds() {
		return transactionsIds;
	}
}

