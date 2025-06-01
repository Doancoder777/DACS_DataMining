package patterns;
public abstract class AbstractOrderedItemset extends AbstractItemset{
	public AbstractOrderedItemset() {
		super();
	}
	public abstract int getAbsoluteSupport();
	public abstract int size();
	public abstract Integer get(int position);
	public Integer getLastItem() {
		return get(size() - 1);
	}
	public String toString(){
		if(size() == 0) {
			return "EMPTYSET";
		}
		StringBuilder r = new StringBuilder ();
		for(int i=0; i< size(); i++){
			r.append(get(i));
			r.append(' ');
		}
		return r.toString(); // return the tring
	}
	public double getRelativeSupport(int nbObject) {
		return ((double)getAbsoluteSupport()) / ((double) nbObject);
	}
	public boolean contains(Integer item) {
		for (int i=0; i< size(); i++) {
			if (get(i).equals(item)) {
				return true;
			} else if (get(i) > item) {
				return false;
			}
		}
		return false;
	}
	public boolean containsAll(AbstractOrderedItemset itemset2){
		if(size() < itemset2.size()){
			return false;
		}
		int i = 0;
		for(int j =0; j < itemset2.size(); j++){
			boolean found = false; // flag to remember if we have find the item at position j
			while(found == false && i< size()){
				if(get(i).equals(itemset2.get(j))){
					found = true;
				}// if the current item in this itemset is larger than 
				else if(get(i) > itemset2.get(j)){
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
	public boolean isEqualTo(AbstractOrderedItemset itemset2) {
		if (this.size() != itemset2.size()) {
			return false;
		}
		for (int i = 0; i < itemset2.size(); i++) {
			if (!itemset2.get(i).equals(this.get(i))) {
				return false;
			}
		}
		return true;
	}
	public boolean isEqualTo(int[] itemset) {
		if (this.size() != itemset.length) {
			return false;
		}
		for (int i = 0; i < itemset.length; i++) {
			if (itemset[i] != this.get(i)) {
				return false;
			}
		}
		return true;
	}
	public boolean allTheSameExceptLastItemV2(AbstractOrderedItemset itemset2) {
		if (itemset2.size() != this.size()) {
			return false;
		}
		for (int i = 0; i < this.size() - 1; i++) {
			if (!this.get(i).equals(itemset2.get(i))) {
				return false;
			}
		}
		return true;
	}
	public Integer allTheSameExceptLastItem(AbstractOrderedItemset itemset2) {
		if(itemset2.size() != this.size()){
			return null;
		}
		for(int i=0; i< this.size(); i++){
			if(i == this.size()-1){ 
				if(this.get(i) >= itemset2.get(i)){  
					return null;
				}
			}
			else if(!this.get(i).equals(itemset2.get(i))){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
	}
}
