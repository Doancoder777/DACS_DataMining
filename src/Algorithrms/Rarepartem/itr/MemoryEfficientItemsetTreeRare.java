package Algorithrms.Rarepartem.itr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import Algo.ArraysAlgos;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;
public class MemoryEfficientItemsetTreeRare extends AbstractItemsetTreeRare implements Serializable {
	private static final long serialVersionUID = 1L;
	long sumBranchesLength; // tổng độ dài các nhánh
	int totalNumberOfBranches; // tổng số nhánh
	public MemoryEfficientItemsetTreeRare() {	
		super();
	}
	public void buildTree(String input)
			throws IOException {
		startTimestamp = System.currentTimeMillis();
		MemoryLogger.getInstance().reset();
		root = new ItemsetTreeNodeRare(null, 0);
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) {
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			String[] lineSplited = line.split(" ");
			int[] itemset = new int[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) {
				itemset[i] = Integer.parseInt(lineSplited[i]);
			}
			construct(null, root, itemset, null);
		}
		reader.close();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
	}
	public void addTransaction(int[] transaction){
		construct(null, root, transaction, null);
	}
	private void construct(ItemsetTreeNodeRare parentOfR, ItemsetTreeNodeRare r, int[] s, int[] prefix) {
 		if(same(s, prefix, r.itemset)){
			r.support++;
			return;
		}
 		int[] rprefix = append(prefix, r.itemset);
		if(ancestorOf(s, rprefix)){
			int[] sprime = copyItemsetWithoutItemsFrom(s, prefix);
			int[] rprime = copyItemsetWithoutItemsFrom(rprefix, sprime);
			ItemsetTreeNodeRare newNodeS = new ItemsetTreeNodeRare(sprime, r.support +1);
			newNodeS.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNodeS);
			r.itemset = rprime;
			return;  // trả về
		}
		int[] l = getLargestCommonAncestor(s, rprefix);
		if(l != null){ // nếu có một tổ tiên chung lớn nhất
			int[] sprime = copyItemsetWithoutItemsFrom(s, l);
			int[] rprime = copyItemsetWithoutItemsFrom(r.itemset, l);
			ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(l, r.support +1);
			newNode.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNode);
			r.itemset = rprime;
			ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(sprime, 1);
			newNode.childs.add(newNode2);
			return;
		}
		int indexLastItemOfR = (rprefix == null)? 0 : rprefix.length;
		r.support++;
		for(ItemsetTreeNodeRare ci : r.childs){
			int[] ciprefix = append(rprefix, ci.itemset);
			if(same(s, ciprefix)){ // trường hợp 2
				ci.support++;
				return;
			}
			if(ancestorOf(s, ciprefix)){ // trường hợp 3
				int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix); 
				int[] ciprime = copyItemsetWithoutItemsFrom(ci.itemset, s); 
				ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(sprime, ci.support+ 1);
				newNode.childs.add(ci);
				r.childs.remove(ci);
				r.childs.add(newNode);
				ci.itemset = ciprime;
				return;
			}
			if(ancestorOf(ciprefix, s)){ // trường hợp 4
				construct(r, ci, s, rprefix);
				return;
			}
			if(ciprefix[indexLastItemOfR] == s[indexLastItemOfR]){
				int[] ancestor = getLargestCommonAncestor(s, ciprefix);
				int[] ancestorprime = copyItemsetWithoutItemsFrom(ancestor, rprefix);
				ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(ancestorprime, ci.support+ 1);
				r.childs.add(newNode);
				ci.itemset = copyItemsetWithoutItemsFrom(ci.itemset, ancestorprime);
				newNode.childs.add(ci);
				r.childs.remove(ci);
				int[] sprime = copyItemsetWithoutItemsFromArrays(s, ancestorprime, rprefix);
				ItemsetTreeNodeRare newNode2 = new ItemsetTreeNodeRare(sprime, 1);
				newNode.childs.add(newNode2);
				return;
			}
		}
		int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix);
		ItemsetTreeNodeRare newNode = new ItemsetTreeNodeRare(sprime, 1);
		r.childs.add(newNode);
	}
	private int[] copyItemsetWithoutItemsFromArrays(int[] r,
			int[] prefix, int[] s) {
		List<Integer> rprime = new ArrayList<Integer>(r.length);
loop1:	for(Integer rvalue : r){
			if(prefix != null){
				for(int pvalue : prefix){
					if(pvalue == rvalue){
						continue loop1;
					}else if(pvalue > rvalue){
						break;
					}
				}
			}
			if(s != null){
				for(int svalue : s){
					if(rvalue == svalue){
						continue loop1;
					}else if(svalue > rvalue){
						break;
					}
				}
			}
			rprime.add(rvalue);
		}
		int[] rprimeArray = new int[rprime.size()];
		for(int i=0; i< rprime.size(); i++){
			rprimeArray[i] = rprime.get(i);
		}
		return rprimeArray;
	}
	private int[] copyItemsetWithoutItemsFrom(int[] itemset1, int[] itemset2) {
		if(itemset2 == null){
			return itemset1;
		}
		List<Integer> itemset1prime = new ArrayList<Integer>(itemset1.length);
loop1:	for(int i1value : itemset1){
			for(int i2value : itemset2){
				if(i2value == i1value){
					continue loop1;
				}else if(i2value > i1value){
					break;
				}
			}
			itemset1prime.add(i1value);
		}
		int[] itemset1primeArray = new int[itemset1prime.size()];
		for(int i=0; i< itemset1prime.size(); i++){
			itemset1primeArray[i] = itemset1prime.get(i);
		}
		return itemset1primeArray;
	}
	private boolean same(int[] itemset1, int[] prefix, int[] itemset2) {
		if(prefix == null) {
			return same(itemset1, itemset2);
		}
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		if(itemset1.length != itemset2.length + prefix.length){
			return false;
		}
		int i = 0;
		while(i < prefix.length){
			if(itemset1[i] != prefix[i]){
				return false;
			}
			i++;
		}
		int j = 0;
		while(j< itemset2.length){
			if(itemset1[j++] != itemset2[i++]){
				return false;
			}
		}
		return true;
	}
	public int[] append(int[] a1, int[] a2){
		if(a1 == null){
			return a2;
		}
		if(a2 == null){
			return a1;
		}
		int[] newArray = new int[a1.length + a2.length];
		int i=0;
		for(; i< a1.length; i++){
			newArray[i] = a1[i];
		}
		for(int j =0; j< a2.length; j++){
			newArray[i++] = a2[j];
		}
		return newArray;
	}
	public void printStatistics() {
		System.gc();
		System.out.println("========== MEMORY EFFICIENT ITEMSET TREE CONSTRUCTION - STATS ============");
		System.out.println(" Thời gian xây dựng cây ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Bộ nhớ tối đa:" + MemoryLogger.getInstance().getMaxMemory());
		nodeCount = 0;
		totalItemCountInNodes = 0;
		sumBranchesLength = 0;
		totalNumberOfBranches = 0;
		recursiveStats(root, 1);
		System.out.println(" Số lượng nút: " + nodeCount);
		System.out.println(" Tổng các mục trong tất cả các nút: " + totalItemCountInNodes + " trung bình mỗi nút :" + totalItemCountInNodes / ((double)nodeCount));
		System.out.println("=====================================");
	}
	private void recursiveStats(ItemsetTreeNodeRare root, int length) {
		if(root != null && root.itemset!=null){
			nodeCount++;
			totalItemCountInNodes += root.itemset.length;
		}
		for(ItemsetTreeNodeRare node : root.childs){
			recursiveStats(node, ++length);
		}
		if(root.childs.size() == 0) {
			sumBranchesLength += length;
			totalNumberOfBranches += 1;
		}
	}
	public void printTree() {
		System.out.println(root.toString(new StringBuilder(),""));
	}
	public String toString() {
		return root.toString(new StringBuilder(), "");
	}
	public int getSupportOfItemset(int[] s) {
		return count(s, root, new int[0]);  // gọi phương thức count.
	}
	private int count(int[] s, ItemsetTreeNodeRare root, int[] prefix) {
		int count = 0;
		for(ItemsetTreeNodeRare ci : root.childs){
			int[] ciprefix = append(prefix, ci.itemset);
			if(ciprefix[0]  <= s[0]){
				if(ArraysAlgos.includedIn(s, ciprefix)){
					count += ci.support;
				}else if(ciprefix[ciprefix.length -1] < s[s.length -1]){  
					count += count(s, ci, ciprefix);
				}
			}
		}
		return count;
	}
	public HashTableITRare getFrequentItemsetSubsuming(int[] is, int minsup) {
		HashTableITRare hashTable = getFrequentItemsetSubsuming(is);
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		return hashTable;
	}
	public HashTableITRare getFrequentItemsetSubsuming(int[] s){
		HashTableITRare hash = new HashTableITRare(1000);
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		selectiveMining(s, seti, root, hash, null);
		return hash;
	}
	private int selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNodeRare t, HashTableITRare hash, int[] prefix) {
		int childrenSup = 0;
		for(ItemsetTreeNodeRare ci : t.childs){
			childrenSup += ci.support;
			int[] ciprefix = append(prefix, ci.itemset);
			if(ciprefix[0]  <= s[0]){
				if(ArraysAlgos.includedIn(s, ciprefix)){
					if(ci.childs.size() ==0){
						hash.put(s, ci.support);
						recursiveAdd(s, seti, ciprefix, ci.support, hash, 0);
					}else{
						int remainingSup = ci.support - selectiveMining(s, seti, ci, hash, ciprefix);
						if (remainingSup > 0)
						{
							hash.put(s, remainingSup);
							recursiveAdd(s, seti, ciprefix, remainingSup, hash, 0);
						} 
					}
				}
				else if(ciprefix[ciprefix.length -1] < s[s.length -1]){ 
					selectiveMining(s, seti, ci, hash, ciprefix);
				}
			}
		}
		return childrenSup;
	}
	private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableITRare hash, int pos) {
		if(pos >= ci.length){
			return;
		}
		if(!seti.contains(ci[pos])){
			int[] newS = new int[s.length+1]; // tạo itemset mới
			int j=0;  // vị trí hiện tại
			boolean added = false;  // chỉ ra nếu chúng ta đã thêm mục ở pos rồi
			for(Integer item : s){
				if(added || item < ci[pos]){
					newS[j++] = item;
				}else{
					newS[j++] = ci[pos];
					newS[j++] = item;
					added = true;  // chúng ta đặt biến đó thành true để không chèn nó hai lần!
				}
			}
			if(j < s.length+1){
				newS[j++] = ci[pos];
			}
			hash.put(newS, cisupport);
			recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
		}
		recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
	}
}
