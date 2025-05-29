package Rarepartem.rpgrowth;

import java.util.ArrayList;
import java.util.List;

public class RPNode {
    int itemID = -1;  
    
    int counter = 1;  
    
    RPNode parent = null; 
    
    List<RPNode> childs = new ArrayList<RPNode>();
    
    RPNode nodeLink = null; 
    
    public RPNode(){
        
    }

    public RPNode getChildWithID(int id) {
        for(RPNode child : childs){
            if(child.itemID == id){
                return child;
            }
        }
        return null;
    }

    public String toString(String indent) {
        StringBuilder output = new StringBuilder();
        output.append("" + itemID);
        output.append(" (count=" + counter);
        output.append(")\n");
        
        String newIndent = indent + "   ";
        for (RPNode child : childs) {
            output.append(newIndent + child.toString(newIndent));
        }
        return output.toString();
    }
    
    public String toString() {
        return "" + itemID;
    }
}