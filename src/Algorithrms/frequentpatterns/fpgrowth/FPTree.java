package Algorithrms.frequentpatterns.fpgrowth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import patterns.itemset_array_integers_with_count.Itemset;
public class FPTree {
	List<Integer> headerList = null;
	Map<Integer, FPNode> mapItemNodes = new HashMap<Integer, FPNode>();
	Map<Integer, FPNode> mapItemLastNode = new HashMap<Integer, FPNode>();
	FPNode root = new FPNode(); // null node
	public FPTree(){	
	}
	public void addTransaction(List<Integer> transaction) {
		FPNode currentNode = root;
		for(Integer item : transaction){
			FPNode child = currentNode.getChildWithID(item);
			if(child == null){ 
				FPNode newNode = new FPNode();
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
	private void fixNodeLinks(Integer item, FPNode newNode) {
		FPNode lastNode = mapItemLastNode.get(item);
		if(lastNode != null) {
			lastNode.nodeLink = newNode;
		}
		mapItemLastNode.put(item, newNode); 
		FPNode headernode = mapItemNodes.get(item);
		if(headernode == null){  // there is not
			mapItemNodes.put(item, newNode);
		}
	}
	void addPrefixPath(List<FPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, int relativeMinsupp) {
		int pathCount = prefixPath.get(0).counter;  
		FPNode currentNode = root;
		for(int i = prefixPath.size() -1; i >=1; i--){ 
			FPNode pathItem = prefixPath.get(i);
			if(mapSupportBeta.get(pathItem.itemID) >= relativeMinsupp){
				FPNode child = currentNode.getChildWithID(pathItem.itemID);
				if(child == null){ 
					FPNode newNode = new FPNode();
					newNode.itemID = pathItem.itemID;
					newNode.parent = currentNode;
					newNode.counter = pathCount;  // set its support
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
	void addPrefixPathGRGrowth(List<FPNode> prefixPath, Map<Integer, Integer> mapSupportBeta, int relativeMinsupp) {
		int pathCount = prefixPath.get(0).counter;  
		FPNode currentNode = root;
		for(int i = prefixPath.size() -1; i >=1; i--){ 
			FPNode pathItem = prefixPath.get(i);
			int support = mapSupportBeta.get(pathItem.itemID);
			if(support >= relativeMinsupp
					&& support < prefixPath.size()){
				FPNode child = currentNode.getChildWithID(pathItem.itemID);
				if(child == null){ 
					FPNode newNode = new FPNode();
					newNode.itemID = pathItem.itemID;
					newNode.parent = currentNode;
					newNode.counter = pathCount;  // set its support
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
		headerList =  new ArrayList<Integer>(mapItemNodes.keySet());
		Collections.sort(headerList, new Comparator<Integer>(){
			public int compare(Integer id1, Integer id2){
				int compare = mapSupport.get(id2) - mapSupport.get(id1);
				return (compare == 0) ? (id1 - id2) : compare;
			}
		});
	}
	@Override
	public String toString() {
		String temp = "F";
		temp += " HeaderList: "+ headerList + "\n";
		temp += root.toString("");
		return temp;
	}
}

