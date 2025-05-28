package Rarepartem.rpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a RP-Tree (Rare Pattern Tree) as used by the RP-Growth algorithm.
 * Based on FP-Tree but optimized for rare pattern mining.
 * 
 * @author Based on FPTree by Philippe Fournier-Viger, Modified for RP-Growth
 */
public class RPTree {
    /** List of items in the header table (rare items only) */
    List<Integer> headerList = null;
    
    /** Map of item to first occurrence node in tree */
    Map<Integer, RPNode> mapItemNodes = new HashMap<Integer, RPNode>();
    
    /** Map of item to last occurrence node (for node links) */
    Map<Integer, RPNode> mapItemLastNode = new HashMap<Integer, RPNode>();
    
    /** root of the tree */
    RPNode root = new RPNode(); // null node with itemID = -1

    /**
     * Constructor
     */
    public RPTree(){    
        
    }

    /**
     * Method for adding a transaction to the rp-tree (for the initial construction)
     * @param transaction transaction containing rare items only, sorted by descending support
     */
    public void addTransaction(List<Integer> transaction) {
        RPNode currentNode = root;
        
        // For each rare item in the transaction
        for(Integer item : transaction){
            // look if there is a node already in the RP-Tree
            RPNode child = currentNode.getChildWithID(item);
            if(child == null){ 
                // there is no node, we create a new one
                RPNode newNode = new RPNode();
                newNode.itemID = item;
                newNode.parent = currentNode;
                // link the new node to its parent
                currentNode.childs.add(newNode);
                
                // set as current node for next iteration 
                currentNode = newNode;
                
                // Update the header table and node links
                fixNodeLinks(item, newNode);    
            }else{ 
                // there is a node already, we update its counter
                child.counter++;
                currentNode = child;
            }
        }
    }

    /**
     * Method to fix the node link for an item after inserting a new node.
     * This maintains the linked list of nodes with the same item ID.
     * 
     * @param item the item of the new node
     * @param newNode the new node that has been inserted
     */
    private void fixNodeLinks(Integer item, RPNode newNode) {
        // get the latest node in the tree with this item
        RPNode lastNode = mapItemLastNode.get(item);
        if(lastNode != null) {
            // add the new node to the node link of the last node
            lastNode.nodeLink = newNode;
        }
        // set the new node as the last node 
        mapItemLastNode.put(item, newNode); 
        
        // if this is the first node with this item, add it to the header table
        RPNode headernode = mapItemNodes.get(item);
        if(headernode == null){
            mapItemNodes.put(item, newNode);
        }
    }
    
    /**
     * Method for adding a prefixpath to the rp-tree.
     * This is used when constructing conditional RP-trees.
     * 
     * @param prefixPath the prefix path to add
     * @param mapSupportBeta the support counts for items in the conditional tree
     * @param relativeMinsupp the minimum support threshold for this conditional tree
     */
    void addPrefixPath(List<RPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, int relativeMinsupp) {
        // the first element of the prefix path contains the path support
        int pathCount = prefixPath.get(0).counter;  
        
        RPNode currentNode = root;
        // For each item in the prefix path (in backward order)
        // (ignore the first element which just contains support count)
        for(int i = prefixPath.size() - 1; i >= 1; i--){ 
            RPNode pathItem = prefixPath.get(i);
            // if the item is frequent enough, include it
            if(mapSupportBeta.get(pathItem.itemID) >= relativeMinsupp){
    
                // look if there is a node already in the RP-Tree
                RPNode child = currentNode.getChildWithID(pathItem.itemID);
                if(child == null){ 
                    // create a new node
                    RPNode newNode = new RPNode();
                    newNode.itemID = pathItem.itemID;
                    newNode.parent = currentNode;
                    newNode.counter = pathCount;  // set its support
                    currentNode.childs.add(newNode);
                    currentNode = newNode;
                    // Update header table and node links
                    fixNodeLinks(pathItem.itemID, newNode);        
                }else{ 
                    // update existing node
                    child.counter += pathCount;
                    currentNode = child;
                }
            }
        }
    }

    /**
     * Method for creating the list of items in the header table, 
     * in descending order of support.
     * 
     * @param mapSupport the frequencies of each item (key: item, value: support)
     */
    void createHeaderList(final Map<Integer, Integer> mapSupport) {
        // create header list with all items that have nodes in the tree
        headerList = new ArrayList<Integer>(mapItemNodes.keySet());
        
        // sort the header table by decreasing order of support
        Collections.sort(headerList, new Comparator<Integer>(){
            public int compare(Integer id1, Integer id2){
                // compare the support
                int compare = mapSupport.get(id2) - mapSupport.get(id1);
                // if same frequency, use lexical ordering
                return (compare == 0) ? (id1 - id2) : compare;
            }
        });
    }
    
    @Override
    /**
     * Method for getting a string representation of the RP-tree 
     * (for debugging purposes).
     * 
     * @return a string representation of the tree
     */
    public String toString() {
        String temp = "RP-Tree";
        // append header list
        temp += " HeaderList: "+ headerList + "\n";
        // append child nodes
        temp += root.toString("");
        return temp;
    }
}