package Algorithrms.frequentpatterns.fin_prepost;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import tools.MemoryLogger;
public class PrePost {
	long startTimestamp;
	long endTimestamp;
	public int outputCount;
	BufferedWriter writer = null;
	public int[][] bf;
	public int bf_cursor;
	public int bf_size;
	public int bf_col;
	public int bf_currentSize;
	public int numOfFItem; // Number of items
	public int minSupport; // minimum support
	public Item[] item; // list of items sorted by support
	public int[] result; // the current itemset
	public int resultLen; // the size of the current itemset
	public int resultCount;
	public int nlLenSum; // node list length of the current itemset
	public PPCTreeNode ppcRoot;
	public NodeListTreeNode nlRoot;
	public PPCTreeNode[] headTable;
	public int[] headTableLen;
	public int[] itemsetCount;
	public int[] sameItems;
	public int nlNodeCount;
	public boolean usePrePostPlus = false;
	public void setUsePrePostPlus(boolean usePrePostPlus) {
		this.usePrePostPlus = usePrePostPlus;
	}
	static Comparator<Item> comp = new Comparator<Item>() {
		public int compare(Item a, Item b) {
			return ((Item) b).num - ((Item) a).num;
		}
	};
	private int numOfTrans;
	public void runAlgorithm(String filename, double minsup, String output)
			throws IOException {
		outputCount = 0;
		nlNodeCount = 0;
		ppcRoot = new PPCTreeNode();
		nlRoot = new NodeListTreeNode();
		resultLen = 0;
		resultCount = 0;
		nlLenSum = 0;
		MemoryLogger.getInstance().reset();
		writer = new BufferedWriter(new FileWriter(output));
		startTimestamp = System.currentTimeMillis();
		bf_size = 1000000;
		bf = new int[100000][];
		bf_currentSize = bf_size * 10;
		bf[0] = new int[bf_currentSize];
		bf_cursor = 0;
		bf_col = 0;
		getData(filename, minsup);
		resultLen = 0;
		result = new int[numOfFItem];
		buildTree(filename);
		nlRoot.label = numOfFItem;
		nlRoot.firstChild = null;
		nlRoot.next = null;
		initializeTree();
		sameItems = new int[numOfFItem];
		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;
		NodeListTreeNode curNode = nlRoot.firstChild;
		NodeListTreeNode next = null;
		while (curNode != null) {
			next = curNode.next;
			traverse(curNode, nlRoot, 1, 0);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			curNode = next;
		}
		writer.close();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
	}
	void buildTree(String filename) throws IOException {
		ppcRoot.label = -1;
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		Item[] transaction = new Item[1000];
		while (((line = reader.readLine()) != null)) {
			if (line.isEmpty() == true || line.charAt(0) == '#'
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}
			String[] lineSplited = line.split(" ");
			int tLen = 0; // tLen
			for (String itemString : lineSplited) {
				int itemX = Integer.parseInt(itemString);
				for (int j = 0; j < numOfFItem; j++) {
					if (itemX == item[j].index) {
						transaction[tLen] = new Item();
						transaction[tLen].index = itemX; // the item
						transaction[tLen].num = 0 - j;
						tLen++;
						break;
					}
				}
			}
			Arrays.sort(transaction, 0, tLen, comp);
			int curPos = 0;
			PPCTreeNode curRoot = (ppcRoot);
			PPCTreeNode rightSibling = null;
			while (curPos != tLen) {
				PPCTreeNode child = curRoot.firstChild;
				while (child != null) {
					if (child.label == 0 - transaction[curPos].num) {
						curPos++;
						child.count++;
						curRoot = child;
						break;
					}
					if (child.rightSibling == null) {
						rightSibling = child;
						child = null;
						break;
					}
					child = child.rightSibling;
				}
				if (child == null)
					break;
			}
			for (int j = curPos; j < tLen; j++) {
				PPCTreeNode ppcNode = new PPCTreeNode();
				ppcNode.label = 0 - transaction[j].num;
				if (rightSibling != null) {
					rightSibling.rightSibling = ppcNode;
					rightSibling = null;
				} else {
					curRoot.firstChild = ppcNode;
				}
				ppcNode.rightSibling = null;
				ppcNode.firstChild = null;
				ppcNode.father = curRoot;
				ppcNode.labelSibling = null;
				ppcNode.count = 1;
				curRoot = ppcNode;
			}
		}
		reader.close();
		headTable = new PPCTreeNode[numOfFItem];
		headTableLen = new int[numOfFItem];
		PPCTreeNode[] tempHead = new PPCTreeNode[numOfFItem];
		itemsetCount = new int[(numOfFItem - 1) * numOfFItem / 2];
		PPCTreeNode root = ppcRoot.firstChild;
		int pre = 0;
		int last = 0;
		while (root != null) {
			root.foreIndex = pre;
			pre++;
			if (headTable[root.label] == null) {
				headTable[root.label] = root;
				tempHead[root.label] = root;
			} else {
				tempHead[root.label].labelSibling = root;
				tempHead[root.label] = root;
			}
			headTableLen[root.label]++;
			PPCTreeNode temp = root.father;
			while (temp.label != -1) {
				itemsetCount[root.label * (root.label - 1) / 2 + temp.label] += root.count;
				temp = temp.father;
			}
			if (root.firstChild != null) {
				root = root.firstChild;
			} else {
				root.backIndex = last;
				last++;
				if (root.rightSibling != null) {
					root = root.rightSibling;
				} else {
					root = root.father;
					while (root != null) {
						root.backIndex = last;
						last++;
						if (root.rightSibling != null) {
							root = root.rightSibling;
							break;
						}
						root = root.father;
					}
				}
			}
		}
	}
	void initializeTree() {
		NodeListTreeNode lastChild = null;
		for (int t = numOfFItem - 1; t >= 0; t--) {
			if (bf_cursor > bf_currentSize - headTableLen[t] * 3) {
				bf_col++;
				bf_cursor = 0;
				bf_currentSize = 10 * bf_size;
				bf[bf_col] = new int[bf_currentSize];
			}
			NodeListTreeNode nlNode = new NodeListTreeNode();
			nlNode.label = t;
			nlNode.support = 0;
			nlNode.NLStartinBf = bf_cursor;
			nlNode.NLLength = 0;
			nlNode.NLCol = bf_col;
			nlNode.firstChild = null;
			nlNode.next = null;
			PPCTreeNode ni = headTable[t];
			while (ni != null) {
				nlNode.support += ni.count;
				bf[bf_col][bf_cursor++] = ni.foreIndex;
				bf[bf_col][bf_cursor++] = ni.backIndex;
				bf[bf_col][bf_cursor++] = ni.count;
				nlNode.NLLength++;
				ni = ni.labelSibling;
			}
			if (nlRoot.firstChild == null) {
				nlRoot.firstChild = nlNode;
				lastChild = nlNode;
			} else {
				lastChild.next = nlNode;
				lastChild = nlNode;
			}
		}
	}
	void getData(String filename, double support) throws IOException {
		numOfTrans = 0;
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while (((line = reader.readLine()) != null)) {
			if (line.isEmpty() == true || line.charAt(0) == '#'
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}
			numOfTrans++;
			String[] lineSplited = line.split(" ");
			for (String itemString : lineSplited) {
				Integer item = Integer.parseInt(itemString);
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
		}
		reader.close();
		minSupport = (int) Math.ceil(support * numOfTrans);
		numOfFItem = mapItemCount.size();
		Item[] tempItems = new Item[numOfFItem];
		int i = 0;
		for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= minSupport) {
				tempItems[i] = new Item();
				tempItems[i].index = entry.getKey();
				tempItems[i].num = entry.getValue();
				i++;
			}
		}
		item = new Item[i];
		System.arraycopy(tempItems, 0, item, 0, i);
		numOfFItem = item.length;
		Arrays.sort(item, comp);
	}
	NodeListTreeNode iskItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj,
			int level, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {
		if (bf_cursor + ni.NLLength * 3 > bf_currentSize) {
			bf_col++;
			bf_cursor = 0;
			bf_currentSize = bf_size > ni.NLLength * 1000 ? bf_size
					: ni.NLLength * 1000;
			bf[bf_col] = new int[bf_currentSize];
		}
		NodeListTreeNode nlNode = new NodeListTreeNode();
		nlNode.support = 0;
		nlNode.NLStartinBf = bf_cursor;
		nlNode.NLCol = bf_col;
		nlNode.NLLength = 0;
		int cursor_i = ni.NLStartinBf;
		int cursor_j = nj.NLStartinBf;
		int col_i = ni.NLCol;
		int col_j = nj.NLCol;
		int last_cur = -1;
		while (cursor_i < ni.NLStartinBf + ni.NLLength * 3
				&& cursor_j < nj.NLStartinBf + nj.NLLength * 3) {
			if (bf[col_i][cursor_i] > bf[col_j][cursor_j]
					&& bf[col_i][cursor_i + 1] < bf[col_j][cursor_j + 1]) {
				if (last_cur == cursor_j) {
					bf[bf_col][bf_cursor - 1] += bf[col_i][cursor_i + 2];
				} else {
					bf[bf_col][bf_cursor++] = bf[col_j][cursor_j];
					bf[bf_col][bf_cursor++] = bf[col_j][cursor_j + 1];
					bf[bf_col][bf_cursor++] = bf[col_i][cursor_i + 2];
					nlNode.NLLength++;
				}
				nlNode.support += bf[col_i][cursor_i + 2];
				last_cur = cursor_j;
				cursor_i += 3;
			} else if (bf[col_i][cursor_i] < bf[col_j][cursor_j]) {
				cursor_i += 3;
			} else if (bf[col_i][cursor_i + 1] > bf[col_j][cursor_j + 1]) {
				cursor_j += 3;
			}
		}
		if (nlNode.support >= minSupport) {
			if (ni.support == nlNode.support && (usePrePostPlus || nlNode.NLLength == 1)) {
				sameItems[sameCountRef.count++] = nj.label;
				bf_cursor = nlNode.NLStartinBf;
				if (nlNode != null) {
						nlNode = null;
				}
			} else {
				nlNode.label = nj.label;
				nlNode.firstChild = null;
				nlNode.next = null;
				if (ni.firstChild == null) {
					ni.firstChild = nlNode;
					lastChild = nlNode;
				} else {
					lastChild.next = nlNode;
					lastChild = nlNode;
				}
			}
			return lastChild;
		} else {
			bf_cursor = nlNode.NLStartinBf;
			if (nlNode != null)
				nlNode = null;
		}
		return lastChild;
	}
	public void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot,
			int level, int sameCount) throws IOException {
		MemoryLogger.getInstance().checkMemory();
		NodeListTreeNode sibling = curNode.next;
		NodeListTreeNode lastChild = null;
		while (sibling != null) {
			if (level > 1
					|| (level == 1 && itemsetCount[(curNode.label - 1)
							* curNode.label / 2 + sibling.label] >= minSupport)) {
				IntegerByRef sameCountTemp = new IntegerByRef();
				sameCountTemp.count = sameCount;
				lastChild = iskItemSetFreq(curNode, sibling, level, lastChild,
						sameCountTemp);
				sameCount = sameCountTemp.count;
			}
			sibling = sibling.next;
		}
		resultCount += Math.pow(2.0, sameCount);
		nlLenSum += Math.pow(2.0, sameCount) * curNode.NLLength;
		result[resultLen++] = curNode.label;
		writeItemsetsToFile(curNode, sameCount);
		nlNodeCount++;
		int from_cursor = bf_cursor;
		int from_col = bf_col;
		int from_size = bf_currentSize;
		NodeListTreeNode child = curNode.firstChild;
		NodeListTreeNode next = null;
		while (child != null) {
			next = child.next;
			traverse(child, curNode, level + 1, sameCount);
			for (int c = bf_col; c > from_col; c--) {
				bf[c] = null;
			}
			bf_col = from_col;
			bf_cursor = from_cursor;
			bf_currentSize = from_size;
			child = next;
		}
		resultLen--;
	}
	private void writeItemsetsToFile(NodeListTreeNode curNode, int sameCount)
			throws IOException {
		StringBuilder buffer = new StringBuilder();
		if(curNode.support >= minSupport) {
			outputCount++;
			for (int i = 0; i < resultLen; i++) {
				buffer.append(item[result[i]].index);
				buffer.append(' ');
			}
			buffer.append("#SUP: ");
			buffer.append(curNode.support);
			buffer.append("\n");
		}
		if (sameCount > 0) {
			for (long i = 1, max = 1 << sameCount; i < max; i++) {
				for (int k = 0; k < resultLen; k++) {
					buffer.append(item[result[k]].index);
					buffer.append(' ');
				}
				for (int j = 0; j < sameCount; j++) {
					int isSet = (int) i & (1 << j);
					if (isSet > 0) {
						buffer.append(item[sameItems[j]].index);
						buffer.append(' ');
					}
				}
				buffer.append("#SUP: ");
				buffer.append(curNode.support);
				buffer.append("\n");
				outputCount++;
			}
		}
		writer.write(buffer.toString());
	}
	public void printStats() {
		String prePost = usePrePostPlus ? "PrePost+" : "PrePost";
		System.out.println("========== " + prePost + " - STATS ============");
		System.out.println(" Minsup = " + minSupport
				+ "\n Number of transactions: " + numOfTrans);
		System.out.println(" Number of frequent  itemsets: " + outputCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:"
				+ MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================");
	}
	class IntegerByRef {
		int count;
	}
	class Item {
		public int index;
		public int num;
	}
	class NodeListTreeNode {
		public int label;
		public NodeListTreeNode firstChild;
		public NodeListTreeNode next;
		public int support;
		public int NLStartinBf;
		public int NLLength;
		public int NLCol;
	}
	class PPCTreeNode {
		public int label;
		public PPCTreeNode firstChild;
		public PPCTreeNode rightSibling;
		public PPCTreeNode labelSibling;
		public PPCTreeNode father;
		public int count;
		public int foreIndex;
		public int backIndex;
	}
}

