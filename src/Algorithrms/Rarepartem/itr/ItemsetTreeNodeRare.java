package Algorithrms.Rarepartem.itr;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
public class ItemsetTreeNodeRare implements Serializable{
	public int[] itemset;
	public int support;
	public Collection<ItemsetTreeNodeRare> childs = new HashSet<ItemsetTreeNodeRare>();
	public ItemsetTreeNodeRare(int[] itemset, int support){
		this.itemset = itemset;
		this.support = support;
	}
	public String toString(StringBuilder buffer, String space){
		buffer.append(space);
		if(itemset == null){
			buffer.append("{}");
		}else{
			buffer.append("[");
			for(Integer item : itemset){
				buffer.append(item);
				buffer.append(" ");
			}
			buffer.append("]");
		}
		buffer.append("   sup=");
		buffer.append(support);
		String patternType = getPatternType(support);
		buffer.append(" [").append(patternType).append("]");
		buffer.append("\n");
		for(ItemsetTreeNodeRare node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	private String getPatternType(int support) {
		int tempMRT = 1;  
		int tempMFT = 5;  
		if(support <= tempMRT) {
			return "NOISE";
		} else if(support > tempMRT && support <= tempMFT) {
			return "RARE";
		} else {
			return "FREQUENT";
		}
	}
	public String toString(){
		return toString(new StringBuilder(), "  ");
	}
}
