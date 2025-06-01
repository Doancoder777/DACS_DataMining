package input.sequence_database_list_strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import input.sequence_database_array_integers.SequenceDatabase;
public class Sequence{
	private final List<List<String>> itemsets = new ArrayList<List<String>>();
	private int id; 
	public Sequence(int id){
		this.id = id;
	}
	public void addItemset(List<String> itemset) {
		itemsets.add(itemset);
	}
	public void print() {
		System.out.print(toString());
	}
	public String toString() {
		StringBuilder r = new StringBuilder("");
		for(List<String> itemset : itemsets){
			r.append('(');
			for(String item : itemset){
				r.append( item);
				r.append(' ');
			}
			r.append(')');
		}
		return r.append("    ").toString();
	}
	public int getId() {
		return id;
	}
	public List<List<String>> getItemsets() {
		return itemsets;
	}
	public List<String> get(int index) {
		return itemsets.get(index);
	}
	public int size(){
		return itemsets.size();
	}
	public Sequence cloneSequenceMinusItems(Map<String, Set<Integer>> mapSequenceID, double relativeMinSup) {
		Sequence sequence = new Sequence(getId());
		for(List<String> itemset : itemsets){
			List<String> newItemset = cloneItemsetMinusItems(itemset, mapSequenceID, relativeMinSup);
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			} 
		}
		return sequence; // return the new sequence
	}
	public List<String> cloneItemsetMinusItems(List<String> itemset,Map<String, Set<Integer>> mapSequenceID, double relativeMinsup) {
		List<String> newItemset = new ArrayList<String>();
		for(String item : itemset){
			if(mapSequenceID.get(item).size() >= relativeMinsup){
				newItemset.add(item); // add it to the new itemset
			}
		}
		return newItemset; // return the new itemset.
	} 
}

