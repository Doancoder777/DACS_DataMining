package Algorithrms.frequentpatterns.itemsettree;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
public class ItemsetTreeNode implements Serializable{
	int[] itemset;
	int support;
	Collection<ItemsetTreeNode> childs = new HashSet<ItemsetTreeNode>();
	public ItemsetTreeNode(int[] itemset, int support){
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
		buffer.append("\n");
		for(ItemsetTreeNode node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	public String toString(){
		return toString(new StringBuilder(), "  ");
	}
}
