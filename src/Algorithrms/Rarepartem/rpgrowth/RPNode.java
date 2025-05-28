package Rarepartem.rpgrowth;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a RP-Tree node as used by the RP-Growth algorithm.
 * Based on FP-Tree node but optimized for rare pattern mining.
 * 
 * @author Based on FPNode by Philippe Fournier-Viger, Modified for RP-Growth
 */
public class RPNode {
    /** item id (-1 for root node) */
    int itemID = -1;  
    
    /** frequency counter (support) */
    int counter = 1;  
    
    /** the parent node of this node or null if it is the root */
    RPNode parent = null; 
    
    /** the child nodes of this node */
    List<RPNode> childs = new ArrayList<RPNode>();
    
    /** link to next node with the same item id (for the header table) */
    RPNode nodeLink = null; 
    
    /**
     * Constructor
     */
    public RPNode(){
        
    }

    /**
     * Return the immediate child of this node having a given ID.
     * If there is no such child, return null.
     * 
     * @param id the item id we are looking for
     * @return the child node with the given id or null if not found
     */
    public RPNode getChildWithID(int id) {
        // for each child node
        for(RPNode child : childs){
            // if the id matches what we are looking for
            if(child.itemID == id){
                // return that node
                return child;
            }
        }
        // if not found, return null
        return null;
    }

    /**
     * Method for getting a string representation of this tree 
     * (for debugging purposes).
     * 
     * @param indent the indentation string
     * @return a string representation of the subtree rooted at this node
     */
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
    
    /**
     * Simple string representation of this node
     * @return the item ID as a string
     */
    public String toString() {
        return "" + itemID;
    }
}