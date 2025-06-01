package Algorithrms.Rarepartem.itemsettreerare;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class RareItemsetTreeNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
	int[] itemset;
	int support;
	Collection<RareItemsetTreeNode> childs = new HashSet<RareItemsetTreeNode>();
	
	public RareItemsetTreeNode(int[] itemset, int support){
		this.itemset = itemset;
		this.support = support;
	}

	public String toString(StringBuilder buffer, String space){
		buffer.append(space);
		if(itemset == null){
			buffer.append("{}");
		}else{
			buffer.append("[");
			for(int i = 0; i < itemset.length; i++){
				buffer.append(itemset[i]);
				if(i < itemset.length - 1){
					buffer.append(" ");
				}
			}
			buffer.append("]");
		}
		buffer.append("   sup=");
		buffer.append(support);
		buffer.append("\n");
		
		for(RareItemsetTreeNode node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	
	public String toString(){
		return toString(new StringBuilder(), "  ");
	}
	
	public void addChild(RareItemsetTreeNode child){
		this.childs.add(child);
	}
	
	public boolean removeChild(RareItemsetTreeNode child){
		return this.childs.remove(child);
	}
	
	public boolean hasChildren(){
		return !childs.isEmpty();
	}
	
	public int getChildrenCount(){
		return childs.size();
	}
	
	public int[] getItemset(){
		return itemset;
	}
	
	public int getSupport(){
		return support;
	}
	
	public void setSupport(int support){
		this.support = support;
	}
	
	public void incrementSupport(){
		this.support++;
	}
	
	public Collection<RareItemsetTreeNode> getChildren(){
		return childs;
	}
}
