package Algorithrms.frequentpatterns.fpgrowth;
import java.util.ArrayList;
import java.util.List;
import patterns.itemset_array_integers_with_count.Itemset;
public class FPNode {
	int itemID = -1;  // item id
	int counter = 1;  // frequency counter  (a.k.a. support)
	FPNode parent = null; 
	List<FPNode> childs = new ArrayList<FPNode>();
	FPNode nodeLink = null; // link to next node with the same item id (for the header table).
	FPNode(){
	}
	FPNode getChildWithID(int id) {
		for(FPNode child : childs){
			if(child.itemID == id){
				return child;
			}
		}
		return null;
	}
	public String toString(String indent) {
		StringBuilder output = new StringBuilder();
		output.append(""+ itemID);
		output.append(" (count="+ counter);
		output.append(")\n");
		String newIndent = indent + "   ";
		for (FPNode child : childs) {
			output.append(newIndent+ child.toString(newIndent));
		}
		return output.toString();
	}
	public String toString() {
		return ""+itemID;
	}
}

