package patterns.itemset_list_integers_without_support;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class Itemset{
	private final List<Integer> items = new ArrayList<Integer>(); 
	public Itemset(Integer item){
		addItem(item);
	}
	public Itemset(){
	}
	public void addItem(Integer value){
			items.add(value);
	}
	public List<Integer> getItems(){
		return items;
	}
	public Integer get(int index){
		return items.get(index);
	}
	public String toString(){
		StringBuilder r = new StringBuilder ();
		for(Integer item : items){
			r.append(item.toString());
			r.append(' ');
		}
		return r.toString();
	}
	public int size(){
		return items.size();
	}
	public Itemset cloneItemSetMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinsup) {
		Itemset itemset = new Itemset();
		for(Integer item : items){
			if(mapSequenceID.get(item).size() >= relativeMinsup){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	public Itemset cloneItemSet(){
		Itemset itemset = new Itemset();
		itemset.getItems().addAll(items);
		return itemset;
	}
	public boolean containsAll(Itemset itemset2){
		int i = 0;
		for(Integer item : itemset2.getItems()){
			boolean found = false; // flag to remember if we have find the item
			while(found == false && i < size()){
				if(get(i).equals(item)){
					found = true;
				}// if the current item in this itemset is larger than 
				else if(get(i) > item){
					return false;
				}
				i++; // continue searching from position  i++
			}
			if(!found){
				return false;
			}
		}
		return true; // if all items were found, return true
	}
}

