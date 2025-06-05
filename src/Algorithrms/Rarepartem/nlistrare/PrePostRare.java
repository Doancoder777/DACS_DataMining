package Algorithrms.Rarepartem.nlistrare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import tools.MemoryLogger;

public class PrePostRare extends AbstractPrePostAlgorithm {

    private int minPatternLength = 1;
    private int maxPatternLength = 1000;

    private Set<Integer> rareItemsSet = null;
    private Set<Integer> allValidItemsSet = null;

    @Override
    public void runAlgorithm(String filename, double minsup, double maxsup, String output)
            throws IOException {
        runAlgorithm(filename, minsup, maxsup, output, 1, 1000);
    }

    public void runAlgorithm(String filename, double minsup, double maxsup, String output,
                            int minSize, int maxSize) throws IOException {

        this.minPatternLength = Math.max(1, minSize);
        this.maxPatternLength = Math.max(minSize, maxSize);

        System.out.println("=== PREPOST RARE MIXED MODE PARAMETERS ===");
        System.out.println("MinSize: " + this.minPatternLength);
        System.out.println("MaxSize: " + this.maxPatternLength);
        System.out.println("MinRareSupport: " + minsup);
        System.out.println("MaxSupport: " + maxsup);
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
        System.out.println("=========================================");

        initializeAlgorithm();
        writer = new BufferedWriter(new FileWriter(output));
        initializeBuffer();

        findTargetItemsMixed(filename, minsup, maxsup);

        resultLen = 0;
        result = new int[allValidItemsSet.size()];
        buildTreeMixed(filename);

        nlRoot.label = allValidItemsSet.size();
        nlRoot.firstChild = null;
        nlRoot.next = null;
        initializeTree();

        sameItems = new int[allValidItemsSet.size()];

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;

        NodeListTreeNode curNode = nlRoot.firstChild;
        NodeListTreeNode next = null;

        while (curNode != null) {
            next = curNode.next;
            traverse(curNode, nlRoot, 1, 0);

            cleanupBuffer(from_col);
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;

            curNode = next;
        }

        finalizeExecution();
    }

    protected void findTargetItemsMixed(String filename, double minsup, double maxsup) throws IOException {
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

        computeAbsoluteThresholds(minsup, maxsup);

        this.rareItemsSet = new HashSet<>();
        this.allValidItemsSet = new HashSet<>();

        for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
            int itemId = entry.getKey();
            int support = entry.getValue();

            if (support > minSuppRelative && support <= maxSuppRelative) {
                rareItemsSet.add(itemId);
            }

            if (support > minSuppRelative) {
                allValidItemsSet.add(itemId);
            }
        }

        System.out.println("Rare items identified: " + rareItemsSet.size());
        System.out.println("All valid items: " + allValidItemsSet.size());

        Item[] tempItems = new Item[allValidItemsSet.size()];
        int i = 0;

        for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
            if (allValidItemsSet.contains(entry.getKey())) {
                tempItems[i] = new Item();
                tempItems[i].index = entry.getKey();
                tempItems[i].num = entry.getValue();
                i++;
            }
        }

        item = new Item[i];
        System.arraycopy(tempItems, 0, item, 0, i);
        numOfRareItem = item.length;
        Arrays.sort(item, comp);
    }

    protected void buildTreeMixed(String filename) throws IOException {
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
            int tLen = 0;

            for (String itemString : lineSplited) {
                int itemX = Integer.parseInt(itemString);

                for (int j = 0; j < numOfRareItem; j++) {
                    if (itemX == item[j].index) {
                        transaction[tLen] = new Item();
                        transaction[tLen].index = itemX;
                        transaction[tLen].num = 0 - j;
                        tLen++;
                        break;
                    }
                }
            }

            Arrays.sort(transaction, 0, tLen, comp);
            insertTransactionIntoTree(transaction, tLen);
        }
        reader.close();

        buildHeaderTableAndIndexing();
    }

    private boolean containsAtLeastOneRareItem(int[] itemIndices, int itemsetLength) {
        for (int i = 0; i < itemsetLength; i++) {
            int itemId = item[itemIndices[i]].index;
            if (rareItemsSet.contains(itemId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRareItemsetMixed(int[] itemIndices, int itemsetLength, int support) {

        if (itemsetLength < minPatternLength || itemsetLength > maxPatternLength) {
            return false;
        }

        boolean supportInRange = (support > minSuppRelative && support <= maxSuppRelative);

        boolean hasRareItem = containsAtLeastOneRareItem(itemIndices, itemsetLength);

        return supportInRange && hasRareItem;
    }

    @Override
    protected void findTargetItems(String filename, double minsup, double maxsup) throws IOException {

        findTargetItemsMixed(filename, minsup, maxsup);
    }

    @Override
    protected void buildTree(String filename) throws IOException {
        buildTreeMixed(filename);
    }

    private void insertTransactionIntoTree(Item[] transaction, int tLen) {
        int curPos = 0;
        PPCTreeNode curRoot = ppcRoot;
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

            if (child == null) break;
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

    private void buildHeaderTableAndIndexing() {
        headTable = new PPCTreeNode[numOfRareItem];
        headTableLen = new int[numOfRareItem];
        PPCTreeNode[] tempHead = new PPCTreeNode[numOfRareItem];
        itemsetCount = new int[(numOfRareItem - 1) * numOfRareItem / 2];

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

   @Override
protected void initializeTree() {
    NodeListTreeNode lastChild = null;

    for (int t = numOfRareItem - 1; t >= 0; t--) {

        ensureBufferCapacity(headTableLen[t] * 3);

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

    private NodeListTreeNode isRareItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj,
            int level, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {

        ensureBufferCapacity(ni.NLLength * 3);

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

        if (nlNode.support > minSuppRelative) {
            if (ni.support == nlNode.support && nlNode.NLLength == 1) {
                    sameItems[sameCountRef.count++] = nj.label;
                    bf_cursor = nlNode.NLStartinBf;
                    nlNode = null;
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
            nlNode = null;
        }
        return lastChild;
    }

    @Override
    protected void traverse(NodeListTreeNode curNode, NodeListTreeNode curRoot,
            int level, int sameCount) throws IOException {

        MemoryLogger.getInstance().checkMemory();

        NodeListTreeNode sibling = curNode.next;
        NodeListTreeNode lastChild = null;

        while (sibling != null) {
            if (level > 1 || (level == 1 &&
                isInSupportRange(itemsetCount[(curNode.label - 1) * curNode.label / 2 + sibling.label]))) {

                IntegerByRef sameCountTemp = new IntegerByRef();
                sameCountTemp.count = sameCount;
                lastChild = isRareItemSetFreq(curNode, sibling, level, lastChild, sameCountTemp);
                sameCount = sameCountTemp.count;
            }
            sibling = sibling.next;
        }

        resultCount += Math.pow(2.0, sameCount);
        nlLenSum += Math.pow(2.0, sameCount) * curNode.NLLength;

        result[resultLen++] = curNode.label;
        writeRareItemsetsToFileMixed(curNode, sameCount);
        nlNodeCount++;

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;

        NodeListTreeNode child = curNode.firstChild;
        NodeListTreeNode next = null;

        while (child != null) {
            next = child.next;
            traverse(child, curNode, level + 1, sameCount);

            cleanupBuffer(from_col);
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;

            child = next;
        }

        resultLen--;
    }

    private void writeRareItemsetsToFileMixed(NodeListTreeNode curNode, int sameCount)
            throws IOException {
        StringBuilder buffer = new StringBuilder();

        if (isRareItemsetMixed(result, resultLen, curNode.support)) {
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
                int[] tempItemset = new int[resultLen + sameCount];
                System.arraycopy(result, 0, tempItemset, 0, resultLen);

                int addedCount = 0;
                for (int j = 0; j < sameCount; j++) {
                    int isSet = (int) i & (1 << j);
                    if (isSet > 0) {
                        tempItemset[resultLen + addedCount] = sameItems[j];
                        addedCount++;
                    }
                }

                if (isRareItemsetMixed(tempItemset, resultLen + addedCount, curNode.support)) {
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
        }

        writer.write(buffer.toString());
    }

    @Override
    public void printStats() {
        System.out.println("========== PREPOST RARE MIXED - STATS ============");
        System.out.println(" Transactions count from database: " + numOfTrans);
        System.out.println(" MinRareSupport (MRT): " + minSuppRelative);
        System.out.println(" MaxSupport (MFT): " + maxSuppRelative);
        System.out.println(" Pattern size range: " + minPatternLength + " - " + maxPatternLength);
        System.out.println(" Rare items identified: " + (rareItemsSet != null ? rareItemsSet.size() : 0));
        System.out.println(" All valid items: " + (allValidItemsSet != null ? allValidItemsSet.size() : 0));
        System.out.println(" Number of rare itemsets: " + outputCount);
        System.out.println(" Mode: MIXED (Frequent + Rare Items)");
        System.out.println(" Maximum memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Total time: " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("=======================================================");
    }

    public int getMinPatternLength() { return minPatternLength; }
    public int getMaxPatternLength() { return maxPatternLength; }
    public void setMinPatternLength(int minLength) { this.minPatternLength = Math.max(1, minLength); }
    public void setMaxPatternLength(int maxLength) { this.maxPatternLength = Math.max(1, maxLength); }
}