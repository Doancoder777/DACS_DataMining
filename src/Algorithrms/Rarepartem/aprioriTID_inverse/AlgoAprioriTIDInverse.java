package Algorithrms.Rarepartem.aprioriTID_inverse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids.Itemset;
import patterns.itemset_array_integers_with_tids.Itemsets;
import tools.MemoryLogger;

public class AlgoAprioriTIDInverse {

    protected int k;
    Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();
    int minSuppRelative;
    int maxSuppRelative;

    private int minPatternLength = 1;
    private int maxPatternLength = Integer.MAX_VALUE;

    private Set<Integer> rareItemsSet = null;
    private Set<Integer> allValidItemsSet = null;

    long startTimestamp = 0;
    long endTimeStamp = 0;
    BufferedWriter writer = null;
    protected Itemsets patterns = null;
    private int itemsetCount = 0;
    private int databaseSize = 0;
    private TransactionDatabase database = null;
    private boolean emptySetIsRequired = false;
    boolean showTransactionIdentifiers = false;

    public AlgoAprioriTIDInverse() {
    }

    public Itemsets runAlgorithm(TransactionDatabase database, double minsup, double maxsup)
            throws NumberFormatException, IOException {
        return runAlgorithm(database, minsup, maxsup, 1, Integer.MAX_VALUE);
    }

    public Itemsets runAlgorithm(TransactionDatabase database, double minsup, double maxsup,
                                int minSize, int maxSize) throws NumberFormatException, IOException {
        this.database = database;

        this.minPatternLength = Math.max(1, minSize);
        this.maxPatternLength = Math.max(minSize, maxSize);

        System.out.println("=== APRIORI-TID MIXED MODE PARAMETERS ===");
        System.out.println("MinSize: " + this.minPatternLength);
        System.out.println("MaxSize: " + this.maxPatternLength);
        System.out.println("MinRareSupport: " + minsup);
        System.out.println("MaxSupport: " + maxsup);
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
        System.out.println("=========================================");

        Itemsets result = runAlgorithmMixed(null, null, minsup, maxsup);
        this.database = null;
        return result;
    }

    public Itemsets runAlgorithmMixed(String input, String output, double minsup, double maxsup)
            throws NumberFormatException, IOException {

        startTimestamp = System.currentTimeMillis();
        itemsetCount = 0;

        if(output == null){
            writer = null;
            patterns = new Itemsets("MIXED RARE ITEMSETS");
        } else {
            patterns = null;
            writer = new BufferedWriter(new FileWriter(output));
        }

        mapItemTIDS = new HashMap<Integer, Set<Integer>>();
        databaseSize = 0;

        if(database != null){
            for(List<Integer> transaction : database.getTransactions()){
                for (int item : transaction) {
                    Set<Integer> tids = mapItemTIDS.get(item);
                    if (tids == null) {
                        tids = new HashSet<Integer>();
                        mapItemTIDS.put(item, tids);
                    }
                    tids.add(databaseSize);
                }
                databaseSize++;
            }
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            String line;
            while (((line = reader.readLine()) != null)) {
                if (line.isEmpty() == true ||
                        line.charAt(0) == '#' || line.charAt(0) == '%'
                                || line.charAt(0) == '@') {
                    continue;
                }
                String[] lineSplited = line.split(" ");
                for (String token : lineSplited) {
                    int item = Integer.parseInt(token);
                    Set<Integer> tids = mapItemTIDS.get(item);
                    if (tids == null) {
                        tids = new HashSet<Integer>();
                        mapItemTIDS.put(item, tids);
                    }
                    tids.add(databaseSize);
                }
                databaseSize++;
            }
            reader.close();
        }

        if(emptySetIsRequired ){
            patterns.addItemset(new Itemset(new int[]{}), 0);
        }

        this.minSuppRelative = (int) Math.ceil(minsup * databaseSize) - 1;
        this.maxSuppRelative = (int) Math.ceil(maxsup * databaseSize);

        System.out.println("=== APRIORI-TID MIXED THRESHOLD CALCULATION ===");
        System.out.println("Database size: " + databaseSize);
        System.out.println("MinRareSupport threshold (MRT): " + minSuppRelative);
        System.out.println("MaxSupport threshold (MFT): " + maxSuppRelative);
        System.out.println("Definition: " + minSuppRelative + " < Support(X) <= " + maxSuppRelative);
        System.out.println("===============================================");

        classifyItemsMixed();

        if (databaseSize > 50000) {
            System.out.println("========== SCALABILITY WARNING ==========");
            System.out.println("Database size: " + databaseSize + " transactions");
            System.out.println("AprioriTID requires significant memory for large datasets due to TID sets");
            System.out.println("Estimated memory needed: ~" + (databaseSize * allValidItemsSet.size() / 1000) + " MB");
            System.out.println();
            System.out.println("RECOMMENDATIONS:");
            System.out.println("- For best performance: Use ECLAT Rare Mixed or PrePost Rare");
            System.out.println("- For full accuracy: Use RP-Growth Mixed");
            System.out.println();
            System.out.println("PROCEEDING with AprioriTID (may encounter memory issues)...");
            System.out.println("If OutOfMemoryError occurs, please use other algorithms");
            System.out.println("=========================================");
        }

        k = 1;
        List<Itemset> level = new ArrayList<Itemset>();
        Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();

        while (iterator.hasNext()) {
            MemoryLogger.getInstance().checkMemory();
            Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();

            if (entry.getValue().size() > minSuppRelative && allValidItemsSet.contains(entry.getKey())) {
                Integer item = entry.getKey();
                Itemset itemset = new Itemset(item);
                itemset.setTIDs(mapItemTIDS.get(item));
                level.add(itemset);

                if (isValidMixedRarePattern(new int[]{item}, 1, entry.getValue().size())) {
                    saveItemset(itemset);
                }
            } else {
                iterator.remove();
            }
        }

        Collections.sort(level, new Comparator<Itemset>() {
            public int compare(Itemset o1, Itemset o2) {
                return o1.get(0) - o2.get(0);
            }
        });

        k = 2;
        while (!level.isEmpty() && k <= maxPatternLength) {
            System.out.println("Processing level " + k + " with " + level.size() + " candidates");

            int maxCandidates = databaseSize > 100000 ? 500 : (databaseSize > 50000 ? 2000 : 50000);

            if (level.size() > maxCandidates) {
                System.out.println("MEMORY PROTECTION: Limiting candidates from " + level.size() +
                                  " to " + maxCandidates + " for memory safety");
                level = level.subList(0, maxCandidates);
            }

            List<Itemset> newLevel = generateCandidateSizeKMixed(level);

            if (newLevel.isEmpty()) {
                System.out.println("No more candidates generated, stopping");
                break;
            }

            level = newLevel;
            k++;

            if (level.size() > maxCandidates * 5) {
                System.out.println("MEMORY PRESSURE: Too many candidates at level " + k + ", stopping");
                break;
            }
        }

        if(writer != null){
            writer.close();
        }
        endTimeStamp = System.currentTimeMillis();
        return patterns;
    }

    private void classifyItemsMixed() {
        this.rareItemsSet = new HashSet<>();
        this.allValidItemsSet = new HashSet<>();

        int rareCount = 0, frequentCount = 0, infrequentCount = 0;

        for (Entry<Integer, Set<Integer>> entry : mapItemTIDS.entrySet()) {
            int item = entry.getKey();
            int support = entry.getValue().size();

            if (support > minSuppRelative && support <= maxSuppRelative) {

                rareItemsSet.add(item);
                allValidItemsSet.add(item);
                rareCount++;
            } else if (support > maxSuppRelative) {

                allValidItemsSet.add(item);
                frequentCount++;
            } else {

                infrequentCount++;
            }
        }

        System.out.println("Item classification:");
        System.out.println("- Rare items (MRT < sup <= MFT): " + rareCount);
        System.out.println("- Frequent items (sup > MFT): " + frequentCount);
        System.out.println("- Infrequent items (sup <= MRT): " + infrequentCount);
        System.out.println("- Total valid items: " + allValidItemsSet.size());
    }

    private boolean isValidMixedRarePattern(int[] itemset, int size, int support) {

        if (size < minPatternLength || size > maxPatternLength) {
            return false;
        }

        if (support <= minSuppRelative || support > maxSuppRelative) {
            return false;
        }

        boolean hasRareItem = false;
        for (int item : itemset) {
            if (rareItemsSet.contains(item)) {
                hasRareItem = true;
                break;
            }
        }

        return hasRareItem;
    }

    protected List<Itemset> generateCandidateSizeKMixed(List<Itemset> levelK_1) throws IOException {
        List<Itemset> candidates = new ArrayList<Itemset>();

        int maxPairs = Math.min(levelK_1.size() * (levelK_1.size() - 1) / 2, 100000);
        int processedPairs = 0;

        loop1: for (int i = 0; i < levelK_1.size(); i++) {
            Itemset itemset1 = levelK_1.get(i);
            loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
                Itemset itemset2 = levelK_1.get(j);

                processedPairs++;
                if (processedPairs > maxPairs) {
                    System.out.println("MEMORY PROTECTION: Processed " + maxPairs + " pairs, stopping");
                    break loop1;
                }

                for (int k = 0; k < itemset1.size(); k++) {
                    if (k == itemset1.size() - 1) {
                        if (itemset1.getItems()[k] >= itemset2.get(k)) {
                            continue loop1;
                        }
                    }
                    else if (itemset1.getItems()[k] < itemset2.getItems()[k]) {
                        continue loop2;
                    } else if (itemset1.getItems()[k] > itemset2.getItems()[k]) {
                        continue loop1;
                    }
                }

                Set<Integer> intersectionTIDs = new HashSet<Integer>();
                for (Integer tid : itemset1.getTransactionsIds()) {
                    if (itemset2.getTransactionsIds().contains(tid)) {
                        intersectionTIDs.add(tid);
                    }
                }

                int newItemset[] = new int[itemset1.size()+1];
                System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
                newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];

                boolean hasRareItem = false;
                for (int item : newItemset) {
                    if (rareItemsSet.contains(item)) {
                        hasRareItem = true;
                        break;
                    }
                }

                if (intersectionTIDs.size() > minSuppRelative &&
                    (hasRareItem || intersectionTIDs.size() <= maxSuppRelative * 2)) {

                    Itemset candidate = new Itemset(newItemset);
                    candidate.setTIDs(intersectionTIDs);
                    candidates.add(candidate);

                    if (isValidMixedRarePattern(newItemset, newItemset.length, intersectionTIDs.size())) {
                        saveItemset(candidate);
                    }
                }
            }
        }

        return candidates;
    }

    public void setMaxItemsetSize(int maxItemsetSize) {
        this.maxPatternLength = maxItemsetSize;
    }

    public void setMinimumPatternLength(int minLength) {
        this.minPatternLength = Math.max(1, minLength);
    }

    public void setMaximumPatternLength(int maxLength) {
        this.maxPatternLength = Math.max(1, maxLength);
    }

    void saveItemset(Itemset itemset) throws IOException {
        itemsetCount++;
        if(writer != null){
            writer.write(itemset.toString() + " #SUP: " + itemset.getTransactionsIds().size());
            if(showTransactionIdentifiers) {
                writer.append(" #TID:");
                for (Integer tid: itemset.getTransactionsIds()) {
                    writer.append(" " + tid);
                }
            }
            writer.newLine();
        } else {
            patterns.addItemset(itemset, itemset.size());
        }
    }

    public void setEmptySetIsRequired(boolean emptySetIsRequired) {
        this.emptySetIsRequired = emptySetIsRequired;
    }

    public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
        this.showTransactionIdentifiers = showTransactionIdentifiers;
    }

    public void printStats() {
        System.out.println("=============  APRIORI-TID MIXED - STATS =============");
        System.out.println(" Transactions count from database : " + databaseSize);
        System.out.println(" Pattern size range: " + minPatternLength + " - " + maxPatternLength);
        System.out.println(" Rare items identified: " + (rareItemsSet != null ? rareItemsSet.size() : 0));
        System.out.println(" All valid items: " + (allValidItemsSet != null ? allValidItemsSet.size() : 0));
        System.out.println(" Mixed rare itemsets count : " + itemsetCount);
        System.out.println(" Mode: MIXED (Frequent + Rare Items)");
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp) + " ms");
        System.out.println(" Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("======================================================");
    }

    public int getDatabaseSize() {
        return databaseSize;
    }

    public int getItemsetCount() {
        return itemsetCount;
    }

    public long getExecutionTime() {
        return endTimeStamp - startTimestamp;
    }

    public int getMinimumPatternLength() {
        return minPatternLength;
    }

    public int getMaximumPatternLength() {
        return maxPatternLength;
    }
}