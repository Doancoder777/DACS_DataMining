package input.sequence_database_list_integers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import input.sequence_database_array_integers.SequenceDatabase;
public class Sequence {
	private final List<List<Integer>> itemsets = new ArrayList<List<Integer>>();
	private int id; 
	public Sequence(int id) {
		this.id = id;
	}
	public void addItemset(List<Integer> itemset) {
		itemsets.add(itemset);
	}
	public void print() {
		System.out.print(toString());
	}
	public String toString() {
		StringBuilder r = new StringBuilder("");
		for (List<Integer> itemset : itemsets) {
			r.append('(');
			for (Integer item : itemset) {
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}
		return r.append("    ").toString();
	}
	public int getId() {
		return id;
	}
	public List<List<Integer>> getItemsets() {
		return itemsets;
	}
	public List<Integer> get(int index) {
		return itemsets.get(index);
	}
	public int size() {
		return itemsets.size();
	}
	public Sequence cloneSequenceMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinSup) {
		Sequence sequence = new Sequence(getId());
		for(List<Integer> itemset : itemsets){
			List<Integer> newItemset = cloneItemsetMinusItems(itemset, mapSequenceID, relativeMinSup);
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			} 
		}
		return sequence; // return the new sequence
	}
	public Sequence cloneSequenceMinusItems(double relativeMinSup, Map<Integer, Set<Sequence>> mapSequenceID) {
		Sequence sequence = new Sequence(getId());
		for(List<Integer> itemset : itemsets){
			List<Integer> newItemset = cloneItemsetMinusItems(relativeMinSup, itemset, mapSequenceID);
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			} 
		}
		return sequence; // return the new sequence
	}
	public List<Integer> cloneItemsetMinusItems(double relativeMinsup, List<Integer> itemset,Map<Integer, Set<Sequence>> mapSequenceID) {
		List<Integer> newItemset = new ArrayList<Integer>();
		for(Integer item : itemset){
			if(mapSequenceID.get(item).size() >= relativeMinsup){
				newItemset.add(item); // add it to the new itemset
			}
		}
		return newItemset; // return the new itemset.
	} 
	public List<Integer> cloneItemsetMinusItems(List<Integer> itemset,Map<Integer, Set<Integer>> mapSequenceID, double minSupportAbsolute) {
		List<Integer> newItemset = new ArrayList<Integer>();
		for(Integer item : itemset){
			Set<Integer> sidSet = mapSequenceID.get(item);
			if(sidSet  !=null && sidSet.size() >= minSupportAbsolute){
				newItemset.add(item); // add it to the new itemset
			}
		}
		return newItemset; // return the new itemset.
	} 
}

