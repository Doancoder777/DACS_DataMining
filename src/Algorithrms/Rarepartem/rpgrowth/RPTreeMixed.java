package Algorithrms.Rarepartem.rpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modified RPTree that supports mixed frequent-rare patterns
 * Key difference: Allows both frequent and rare items in the tree
 * Final filtering is done based on pattern support and rare item presence
 */
public class RPTreeMixed {
    // List of items in the header table
    List<Integer> headerList = null;
    
    // Map of item to first node with that item
    Map<Integer, RPNode> mapItemNodes = new HashMap<Integer, RPNode>();
    
    // Map of item to last node with that item (for node links)
    Map<Integer, RPNode> mapItemLastNode = new HashMap<Integer, RPNode>();
    
    // Root of the tree
    RPNode root = new RPNode(); // null node

    /**
     * Constructor
     */
    public RPTreeMixed(){    
    }

    /**
     * Method for adding a transaction to the RP-tree
     * MODIFIED: Accepts all items (both frequent and rare)
     * @param transaction
     */
    public void addTransaction(List<Integer> transaction) {
        RPNode currentNode = root;
        // For each item in the transaction
        for(Integer item : transaction){
            // Look if there is a node already in the RP-Tree
            RPNode child = currentNode.getChildWithID(item);
            if(child == null){ 
                // There is no node, we create a new one
                RPNode newNode = new RPNode();
                newNode.itemID = item;
                newNode.parent = currentNode;
                // Link the new node to its parent
                currentNode.childs.add(newNode);
                
                // Take this node as the current node for the next iteration 
                currentNode = newNode;
                
                // Update the header table and node links
                fixNodeLinks(item, newNode);    
            }else{ 
                // There is a node already, we update it
                child.counter++;
                currentNode = child;
            }
        }
    }

    /**
     * Method to fix the node link for an item after inserting a new node.
     * @param item  the item of the new node
     * @param newNode the new node that has been inserted.
     */
    private void fixNodeLinks(Integer item, RPNode newNode) {
        // Get the latest node in the tree with this item
        RPNode lastNode = mapItemLastNode.get(item);
        if(lastNode != null) {
            // If not null, then we add the new node to the node link of the last node
            lastNode.nodeLink = newNode;
        }
        // Finally, we set the new node as the last node 
        mapItemLastNode.put(item, newNode); 
        
        RPNode headernode = mapItemNodes.get(item);
        if(headernode == null){  // there is not
            mapItemNodes.put(item, newNode);
        }
    }
    
    /**
     * MODIFIED: Method for adding a prefixpath to the mixed RP-tree
     * Key change: Accepts items with support > minThreshold (includes frequent items)
     * @param prefixPath  The prefix path
     * @param mapSupportBeta  The frequencies of items in the prefixpaths
     * @param relativeMinThreshold Minimum threshold for including items
     */
    void addPrefixPathMixed(List<RPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, 
                           int relativeMinThreshold) {
        // The first element of the prefix path contains the path support
        int pathCount = prefixPath.get(0).counter;  
        
        RPNode currentNode = root;
        // For each item in the transaction (in backward order)
        // (and we ignore the first element of the prefix path)
        for(int i = prefixPath.size() - 1; i >= 1; i--){ 
            RPNode pathItem = prefixPath.get(i);
            
            // MODIFIED: Accept item if support >= minThreshold (both frequent and rare)
            // Original only accepted rare items: support < maxSupp && support >= minRareSupp
            // New: Accept if support >= minThreshold (allows frequent items)
            if(mapSupportBeta.get(pathItem.itemID) >= relativeMinThreshold){
                
                // Look if there is a node already in the RP-Tree
                RPNode child = currentNode.getChildWithID(pathItem.itemID);
                if(child == null){ 
                    // There is no node, we create a new one
                    RPNode newNode = new RPNode();
                    newNode.itemID = pathItem.itemID;
                    newNode.parent = currentNode;
                    newNode.counter = pathCount;  // set its support
                    currentNode.childs.add(newNode);
                    currentNode = newNode;
                    // Update the header table and node links
                    fixNodeLinks(pathItem.itemID, newNode);        
                }else{ 
                    // There is a node already, we update it
                    child.counter += pathCount;
                    currentNode = child;
                }
            }
        }
    }

    /**
     * Method for creating the list of items in the header table, 
     * in descending order of support.
     * @param mapSupport the frequencies of each item (key: item  value: support)
     */
    void createHeaderList(final Map<Integer, Integer> mapSupport) {
        // Create an array to store the header list with
        // all the items stored in the map received as parameter
        headerList = new ArrayList<Integer>(mapItemNodes.keySet());
        
        // Sort the header table by decreasing order of support
        Collections.sort(headerList, new Comparator<Integer>(){
            public int compare(Integer id1, Integer id2){
                // Compare the support
                int compare = mapSupport.get(id2) - mapSupport.get(id1);
                // If the same frequency, we check the lexical ordering!
                // Otherwise we use the support
                return (compare == 0) ? (id1 - id2) : compare;
            }
        });
    }
    
    @Override
    /**
     * Method for getting a string representation of the Mixed RP-Tree 
     * (to be used for debugging purposes).
     * @return a string
     */
    public String toString() {
        String temp = "Mixed-RP-Tree";
        // Append header list
        temp += " HeaderList: "+ headerList + "\n";
        // Append child nodes
        temp += root.toString("");
        return temp;
    }

    /**
     * UTILITY: Get statistics about the tree
     */
    public void printTreeStats() {
        System.out.println("=== Mixed RP-Tree Statistics ===");
        System.out.println("Header list size: " + (headerList != null ? headerList.size() : 0));
        System.out.println("Distinct items in tree: " + mapItemNodes.size());
        System.out.println("Root children count: " + root.childs.size());
        
        // Count total nodes
        int totalNodes = countNodes(root);
        System.out.println("Total nodes in tree: " + totalNodes);
    }
    
    /**
     * UTILITY: Count total nodes in the tree recursively
     */
    private int countNodes(RPNode node) {
        int count = 1; // Count current node
        for (RPNode child : node.childs) {
            count += countNodes(child);
        }
        return count;
    }
    
    /**
     * UTILITY: Get all items in the tree
     */
    public List<Integer> getAllItems() {
        return new ArrayList<>(mapItemNodes.keySet());
    }
    
    /**
     * UTILITY: Check if tree contains a specific item
     */
    public boolean containsItem(Integer item) {
        return mapItemNodes.containsKey(item);
    }
}