package Algo.two_phase;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class ItemsetTP{
	private final List<Integer> items = new ArrayList<Integer>(); 
	private int utility =0;
	private Set<Integer> transactionsIds = null;
	public ItemsetTP(){
	}
	public double getRelativeSupport(int nbObject) {
		return ((double)transactionsIds.size()) / ((double) nbObject);
	}
	public String getRelativeSupportAsString(int nbObject) {
		double frequence = ((double)transactionsIds.size()) / ((double) nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(4); 
		return format.format(frequence);
	}
	public int getAbsoluteSupport(){
		return transactionsIds.size();
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
	public void print(){
		System.out.print(toString());
	}
	public String toString(){
		StringBuilder r = new StringBuilder ();
		for(Integer attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}
	public void setTIDset(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	public int size(){
		return items.size();
	}
	public Set<Integer> getTIDset() {
		return transactionsIds;
	}
	public int getUtility() {
		return utility;
	}
	public void incrementUtility(int increment){
		utility += increment;
	}
}

