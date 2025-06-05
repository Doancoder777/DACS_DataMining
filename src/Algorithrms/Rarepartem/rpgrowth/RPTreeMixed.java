package Algorithrms.Rarepartem.rpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPTreeMixed {

    List<Integer> headerList = null;

    Map<Integer, RPNode> mapItemNodes = new HashMap<Integer, RPNode>();

    Map<Integer, RPNode> mapItemLastNode = new HashMap<Integer, RPNode>();

    RPNode root = new RPNode();

    public RPTreeMixed(){
    }

    public void addTransaction(List<Integer> transaction) {
        RPNode currentNode = root;

        for(Integer item : transaction){

            RPNode child = currentNode.getChildWithID(item);
            if(child == null){

                RPNode newNode = new RPNode();
                newNode.itemID = item;
                newNode.parent = currentNode;

                currentNode.childs.add(newNode);

                currentNode = newNode;

                fixNodeLinks(item, newNode);
            }else{

                child.counter++;
                currentNode = child;
            }
        }
    }

    private void fixNodeLinks(Integer item, RPNode newNode) {

        RPNode lastNode = mapItemLastNode.get(item);
        if(lastNode != null) {

            lastNode.nodeLink = newNode;
        }

        mapItemLastNode.put(item, newNode);

        RPNode headernode = mapItemNodes.get(item);
        if(headernode == null){
            mapItemNodes.put(item, newNode);
        }
    }

    void addPrefixPathMixed(List<RPNode> prefixPath, Map<Integer, Integer> mapSupportBeta,
                           int relativeMinThreshold) {

        int pathCount = prefixPath.get(0).counter;

        RPNode currentNode = root;

        for(int i = prefixPath.size() - 1; i >= 1; i--){
            RPNode pathItem = prefixPath.get(i);

            if(mapSupportBeta.get(pathItem.itemID) >= relativeMinThreshold){

                RPNode child = currentNode.getChildWithID(pathItem.itemID);
                if(child == null){

                    RPNode newNode = new RPNode();
                    newNode.itemID = pathItem.itemID;
                    newNode.parent = currentNode;
                    newNode.counter = pathCount;
                    currentNode.childs.add(newNode);
                    currentNode = newNode;

                    fixNodeLinks(pathItem.itemID, newNode);
                }else{

                    child.counter += pathCount;
                    currentNode = child;
                }
            }
        }
    }

    void createHeaderList(final Map<Integer, Integer> mapSupport) {

        headerList = new ArrayList<Integer>(mapItemNodes.keySet());

        Collections.sort(headerList, new Comparator<Integer>(){
            public int compare(Integer id1, Integer id2){

                int compare = mapSupport.get(id2) - mapSupport.get(id1);

                return (compare == 0) ? (id1 - id2) : compare;
            }
        });
    }

    @Override

    public String toString() {
        String temp = "Mixed-RP-Tree";

        temp += " HeaderList: "+ headerList + "\n";

        temp += root.toString("");
        return temp;
    }

    public void printTreeStats() {
        System.out.println("=== Mixed RP-Tree Statistics ===");
        System.out.println("Header list size: " + (headerList != null ? headerList.size() : 0));
        System.out.println("Distinct items in tree: " + mapItemNodes.size());
        System.out.println("Root children count: " + root.childs.size());

        int totalNodes = countNodes(root);
        System.out.println("Total nodes in tree: " + totalNodes);
    }

    private int countNodes(RPNode node) {
        int count = 1;
        for (RPNode child : node.childs) {
            count += countNodes(child);
        }
        return count;
    }

    public List<Integer> getAllItems() {
        return new ArrayList<>(mapItemNodes.keySet());
    }

    public boolean containsItem(Integer item) {
        return mapItemNodes.containsKey(item);
    }
}