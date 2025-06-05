package Algorithrms.Rarepartem.eclat_rare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

public class AlgoEclatRareBitset extends AbstractRareItemsetAlgorithm {

    private int minPatternLength = 1;
    private int maxPatternLength = Integer.MAX_VALUE;

    private Set<Integer> rareItemsSet = null;
    private Set<Integer> allValidItemsSet = null;

    public class BitSetSupport {
        BitSet bitset = new BitSet();
        int support;

        public BitSetSupport() {}

        public BitSetSupport(BitSet bitset, int support) {
            this.bitset = bitset;
            this.support = support;
        }
    }

    public AlgoEclatRareBitset() {
        super();
    }

    @Override
    public Itemsets runAlgorithm(String output, TransactionDatabase database,
                                double minRareSupport, double maxFrequentSupport) throws IOException {
        return runAlgorithm(output, database, minRareSupport, maxFrequentSupport, 1, Integer.MAX_VALUE);
    }

    public Itemsets runAlgorithm(String output, TransactionDatabase database,
                                double minRareSupport, double maxFrequentSupport,
                                int minSize, int maxSize) throws IOException {

        this.minPatternLength = Math.max(1, minSize);
        this.maxPatternLength = Math.max(minSize, maxSize);

        System.out.println("=== ECLAT RARE MIXED MODE PARAMETERS ===");
        System.out.println("MinSize: " + this.minPatternLength);
        System.out.println("MaxSize: " + this.maxPatternLength);
        System.out.println("MinRareSupport: " + minRareSupport);
        System.out.println("MaxSupport: " + maxFrequentSupport);
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
        System.out.println("========================================");

        initializeParameters(database, minRareSupport, maxFrequentSupport);

        if (output == null) {
            writer = null;
            rareItemsets = new Itemsets("MIXED RARE ITEMSETS");
        } else {
            rareItemsets = null;
            writer = new BufferedWriter(new FileWriter(output));
        }

        final Map<Integer, BitSetSupport> mapItemBitsets = new HashMap<Integer, BitSetSupport>();
        calculateSupportSingleItemsBitset(database, mapItemBitsets);

        classifyItemsMixed(mapItemBitsets);

        List<Integer> validItems = new ArrayList<Integer>();

        for (Entry<Integer, BitSetSupport> entry : mapItemBitsets.entrySet()) {
            int support = entry.getValue().support;
            int item = entry.getKey();

            if (support > minRareSupportRelative && allValidItemsSet.contains(item)) {
                validItems.add(item);

                if (isValidMixedRarePattern(new int[]{item}, 1, support)) {
                    saveRareSingleItemBitset(item, entry.getValue());
                }
            }
        }

        Collections.sort(validItems);
        System.out.println("Valid items for mining: " + validItems.size());
        System.out.println("Rare items: " + rareItemsSet.size());

        if (maxPatternLength >= 2 && validItems.size() > 1) {
            generateMixedRareCombinationsBitset(validItems, mapItemBitsets);
        }

        finalizeExecution();

        if (writer != null) {
            writer.close();
        }

        return rareItemsets;
    }

    private void classifyItemsMixed(Map<Integer, BitSetSupport> mapItemBitsets) {
        this.rareItemsSet = new HashSet<>();
        this.allValidItemsSet = new HashSet<>();

        int rareCount = 0, frequentCount = 0, infrequentCount = 0;

        for (Entry<Integer, BitSetSupport> entry : mapItemBitsets.entrySet()) {
            int item = entry.getKey();
            int support = entry.getValue().support;

            if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {

                rareItemsSet.add(item);
                allValidItemsSet.add(item);
                rareCount++;
            } else if (support > maxFrequentSupportRelative) {

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

        if (support <= minRareSupportRelative || support > maxFrequentSupportRelative) {
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

    private void calculateSupportSingleItemsBitset(TransactionDatabase database,
                                                  Map<Integer, BitSetSupport> mapItemBitsets) {

        for (int i = 0; i < database.size(); i++) {
            for (Integer item : database.getTransactions().get(i)) {
                BitSetSupport bitsetSupport = mapItemBitsets.get(item);
                if (bitsetSupport == null) {
                    bitsetSupport = new BitSetSupport();
                    mapItemBitsets.put(item, bitsetSupport);
                }

                bitsetSupport.bitset.set(i);
                bitsetSupport.support++;
            }
        }
    }

    private void generateMixedRareCombinationsBitset(List<Integer> validItems,
                                                    Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {

        List<Itemset> level = new ArrayList<Itemset>();

        System.out.println("DEBUG: Valid items for 2-itemset generation: " + validItems);

        for (int i = 0; i < validItems.size(); i++) {
            for (int j = i + 1; j < validItems.size(); j++) {
                Integer itemI = validItems.get(i);
                Integer itemJ = validItems.get(j);

                BitSetSupport bitsetI = mapItemBitsets.get(itemI);
                BitSetSupport bitsetJ = mapItemBitsets.get(itemJ);

                BitSetSupport intersectionBitset = performBitsetIntersection(bitsetI, bitsetJ);
                int support = intersectionBitset.support;

                System.out.println("2-itemset: " + itemI + " " + itemJ + " SUP: " + support +
                                  " (threshold=" + minRareSupportRelative + ")");

                if (support > minRareSupportRelative) {
                    Itemset itemset = new Itemset(new int[]{itemI, itemJ});
                    itemset.setAbsoluteSupport(support);
                    level.add(itemset);

                    if (isValidMixedRarePattern(new int[]{itemI, itemJ}, 2, support)) {
                        saveRareItemsetBitset(new int[]{itemI}, 1, itemJ, support);
                        System.out.println("  -> SAVED as rare pattern");
                    } else {
                        System.out.println("  -> Added to level but not saved (no rare item or support out of range)");
                    }
                } else {
                    System.out.println("  -> REJECTED (support too low)");
                }
            }
        }

        System.out.println("Generated " + level.size() + " 2-itemsets for next level");

        int k = 3;
        while (!level.isEmpty() && k <= maxPatternLength) {
            System.out.println("Generating " + k + "-itemsets from " + level.size() + " candidates...");
            List<Itemset> newLevel = generateCandidatesLevelKBitset(level, mapItemBitsets);
            System.out.println("Found " + newLevel.size() + " " + k + "-itemsets");
            level = newLevel;
            k++;
        }
    }

    private BitSetSupport performBitsetIntersection(BitSetSupport bitsetI, BitSetSupport bitsetJ) {
        BitSet intersection = (BitSet) bitsetI.bitset.clone();
        intersection.and(bitsetJ.bitset);
        int support = intersection.cardinality();
        return new BitSetSupport(intersection, support);
    }

    private List<Itemset> generateCandidatesLevelKBitset(List<Itemset> levelKMinus1,
                                                        Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        List<Itemset> candidates = new ArrayList<Itemset>();

        if (levelKMinus1.isEmpty()) return candidates;

        int currentSize = levelKMinus1.get(0).size();
        int nextSize = currentSize + 1;

        System.out.println("Generating " + nextSize + "-itemsets using EXHAUSTIVE method (no Apriori pruning)");

        Set<Integer> allItemsSet = new HashSet<>();
        for (Itemset itemset : levelKMinus1) {
            for (int item : itemset.getItems()) {
                allItemsSet.add(item);
            }
        }

        List<Integer> allItems = new ArrayList<>(allItemsSet);
        allItems.sort(Integer::compareTo);

        System.out.println("Items available for " + nextSize + "-itemsets: " + allItems.size());

        generateAllCombinations(allItems, nextSize, new int[nextSize], 0, 0,
                               candidates, mapItemBitsets);

        System.out.println("Generated " + candidates.size() + " valid " + nextSize + "-itemsets");
        return candidates;
    }

    private void generateAllCombinations(List<Integer> items, int targetSize, int[] current,
                                        int currentIndex, int startPos, List<Itemset> candidates,
                                        Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {

        if (currentIndex == targetSize) {

            BitSet combinedBitset = calculateCombinedBitset(current, mapItemBitsets);
            if (combinedBitset != null) {
                int support = combinedBitset.cardinality();

                if (support > minRareSupportRelative) {

                    if (isValidMixedRarePattern(current, targetSize, support)) {
                        Itemset candidate = new Itemset(current.clone());
                        candidate.setAbsoluteSupport(support);
                        candidates.add(candidate);

                        if (targetSize == 2) {
                            saveRareItemsetBitset(new int[]{current[0]}, 1, current[1], support);
                        } else {
                            int[] prefix = new int[targetSize - 1];
                            System.arraycopy(current, 0, prefix, 0, targetSize - 1);
                            saveRareItemsetBitset(prefix, targetSize - 1, current[targetSize - 1], support);
                        }
                    }
                }
            }
            return;
        }

        for (int i = startPos; i < items.size(); i++) {
            current[currentIndex] = items.get(i);
            generateAllCombinations(items, targetSize, current, currentIndex + 1, i + 1,
                                   candidates, mapItemBitsets);
        }
    }

    private BitSet calculateCombinedBitset(int[] itemset, Map<Integer, BitSetSupport> mapItemBitsets) {
        BitSet combinedBitset = null;
        for (int item : itemset) {
            BitSetSupport itemBitset = mapItemBitsets.get(item);
            if (itemBitset == null) {
                System.err.println("ERROR: No bitset for item " + item);
                return null;
            }
            if (combinedBitset == null) {
                combinedBitset = (BitSet) itemBitset.bitset.clone();
            } else {
                combinedBitset.and(itemBitset.bitset);
            }
        }
        return combinedBitset;
    }

    private void saveRareSingleItemBitset(int item, BitSetSupport bitsetSupport) throws IOException {
        rareItemsetCount++;

        if (writer == null) {
            Itemset itemset = new Itemset(new int[]{item});
            itemset.setAbsoluteSupport(bitsetSupport.support);
            rareItemsets.addItemset(itemset, itemset.size());
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append(item);
            buffer.append(" #SUP: ");
            buffer.append(bitsetSupport.support);

            if (showTransactionIdentifiers) {
                buffer.append(" #TID:");
                for (int tid = bitsetSupport.bitset.nextSetBit(0); tid != -1;
                     tid = bitsetSupport.bitset.nextSetBit(tid + 1)) {
                    buffer.append(" " + tid);
                }
            }

            writer.write(buffer.toString());
            writer.newLine();
        }
    }

    private void saveRareItemsetBitset(int[] prefix, int prefixLength, int suffixItem, int support) throws IOException {
        rareItemsetCount++;

        if (writer == null) {
            int[] itemsetArray = new int[prefixLength + 1];
            System.arraycopy(prefix, 0, itemsetArray, 0, prefixLength);
            itemsetArray[prefixLength] = suffixItem;

            Itemset itemset = new Itemset(itemsetArray);
            itemset.setAbsoluteSupport(support);
            rareItemsets.addItemset(itemset, itemset.size());
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < prefixLength; i++) {
                buffer.append(prefix[i]);
                buffer.append(" ");
            }
            buffer.append(suffixItem);
            buffer.append(" #SUP: ");
            buffer.append(support);

            writer.write(buffer.toString());
            writer.newLine();
        }
    }

    @Override
    public void printStats() {
        System.out.println("=============  ECLAT RARE MIXED BITSET - STATS =============");
        System.out.println(" Transactions count from database : " + database.size());
        System.out.println(" Pattern size range: " + minPatternLength + " - " + maxPatternLength);
        System.out.println(" Rare items identified: " + (rareItemsSet != null ? rareItemsSet.size() : 0));
        System.out.println(" All valid items: " + (allValidItemsSet != null ? allValidItemsSet.size() : 0));
        System.out.println(" Mixed rare itemsets count : " + rareItemsetCount);
        System.out.println(" Mode: MIXED (Frequent + Rare Items)");
        System.out.println(" Total time ~ " + getExecutionTime() + " ms");
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" BitSet optimization: ENABLED");
        System.out.println(" Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("=============================================================");
    }

    public void setMinimumPatternLength(int minLength) {
        this.minPatternLength = Math.max(1, minLength);
    }

    public void setMaximumPatternLength(int maxLength) {
        this.maxPatternLength = Math.max(1, maxLength);
    }

    public int getMinimumPatternLength() {
        return minPatternLength;
    }

    public int getMaximumPatternLength() {
        return maxPatternLength;
    }
}