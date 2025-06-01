package Algorithrms.Rarepartem.eclat_rare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;


public class AlgoEclatRareBitset {

    private int minRareSupportRelative;
    private int maxFrequentSupportRelative;
    protected TransactionDatabase database;
    protected long startTimestamp;
    protected long endTime;
    protected Itemsets rareItemsets;
    BufferedWriter writer = null;
    protected int rareItemsetCount;
    boolean showTransactionIdentifiers = false;
    int maxItemsetSize = Integer.MAX_VALUE;


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
    }

    /**
     * Run algorithm với BitSet optimization
     */
    public Itemsets runAlgorithm(String output, TransactionDatabase database, 
                                double minRareSupport, double maxFrequentSupport) throws IOException {
        
        MemoryLogger.getInstance().reset();
        
        if (output == null) {
            writer = null;
            rareItemsets = new Itemsets("RARE ITEMSETS");
        } else {
            rareItemsets = null;
            writer = new BufferedWriter(new FileWriter(output));
        }

        rareItemsetCount = 0;
        this.database = database;
        startTimestamp = System.currentTimeMillis();
        
        // Same threshold calculation as Apriori Rare Fixed
        this.minRareSupportRelative = (int) Math.ceil(minRareSupport * database.size()) - 1;
        this.maxFrequentSupportRelative = (int) Math.ceil(maxFrequentSupport * database.size());
        
        // Calculate BitSet tidsets for all items (OPTIMIZED)
        final Map<Integer, BitSetSupport> mapItemBitsets = new HashMap<Integer, BitSetSupport>();
        calculateSupportSingleItemsBitset(database, mapItemBitsets);

        // Find ONLY rare single items (FIXED logic)
        List<Integer> rareItems = new ArrayList<Integer>();
        
        for (Entry<Integer, BitSetSupport> entry : mapItemBitsets.entrySet()) {
            int support = entry.getValue().support;
            int item = entry.getKey();
            
            if (support > minRareSupportRelative && support <= maxFrequentSupportRelative 
                && maxItemsetSize >= 1) {
                rareItems.add(item);
                saveRareSingleItemBitset(item, entry.getValue());
            }
        }

        // Sort rare items by ITEM ID (not support) for proper join conditions
        Collections.sort(rareItems);

        // Generate combinations ONLY from rare items using BitSet operations (FIXED)
        if (maxItemsetSize >= 2 && rareItems.size() > 1) {
            generateRareCombinationsBitset(rareItems, mapItemBitsets);
        }
        
        MemoryLogger.getInstance().checkMemory();
        
        if (writer != null) {
            writer.close();
        }
        
        endTime = System.currentTimeMillis();
        
        return rareItemsets;
    }

    /**
     * Calculate tidsets using BitSet (MUCH faster than HashSet)
     * Memory usage: 1 bit per transaction instead of 32+ bytes per Integer
     */
    private void calculateSupportSingleItemsBitset(TransactionDatabase database,
                                                  Map<Integer, BitSetSupport> mapItemBitsets) {
        
        for (int i = 0; i < database.size(); i++) {
            for (Integer item : database.getTransactions().get(i)) {
                BitSetSupport bitsetSupport = mapItemBitsets.get(item);
                if (bitsetSupport == null) {
                    bitsetSupport = new BitSetSupport();
                    mapItemBitsets.put(item, bitsetSupport);
                }
                
                // Set bit for this transaction (FAST operation)
                bitsetSupport.bitset.set(i);
                bitsetSupport.support++; // Increment support counter
            }
        }
    }

    /**
     * Generate rare combinations using BitSet operations (MUCH faster)
     * FIXED: Ensure proper item ordering for correct joins
     */
    private void generateRareCombinationsBitset(List<Integer> rareItems, 
                                               Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        
        // Start with 2-itemsets using BitSet intersection
        List<Itemset> level = new ArrayList<Itemset>();
        
        // FIXED: Ensure items are properly ordered for joins
        for (int i = 0; i < rareItems.size(); i++) {
            for (int j = i + 1; j < rareItems.size(); j++) {
                Integer itemI = rareItems.get(i);
                Integer itemJ = rareItems.get(j);
                
                // FIXED: Ensure ascending order (itemI < itemJ)
                if (itemI > itemJ) {
                    Integer temp = itemI;
                    itemI = itemJ;
                    itemJ = temp;
                }
                
                BitSetSupport bitsetI = mapItemBitsets.get(itemI);
                BitSetSupport bitsetJ = mapItemBitsets.get(itemJ);
                
                // FAST BitSet intersection using AND operation
                BitSetSupport intersectionBitset = performBitsetIntersection(bitsetI, bitsetJ);
                int support = intersectionBitset.support;
                
                if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {
                    // FIXED: Create itemset with proper ordering
                    Itemset itemset = new Itemset(new int[]{itemI, itemJ});
                    itemset.setAbsoluteSupport(support);
                    level.add(itemset);
                    
                    saveRareItemsetBitset(new int[]{itemI}, 1, itemJ, support);
                }
            }
        }
        
        // Continue with larger itemsets using BitSet operations
        int k = 3;
        while (!level.isEmpty() && k <= maxItemsetSize) {
            level = generateCandidatesLevelKBitset(level, mapItemBitsets);
            k++;
        }
    }

    /**
     * FAST BitSet intersection using bitwise AND
     * Performance: O(bitset_size/64) vs O(min(setA, setB)) for HashSet
     */
    private BitSetSupport performBitsetIntersection(BitSetSupport bitsetI, BitSetSupport bitsetJ) {
        // Clone first bitset to avoid modifying original
        BitSet intersection = (BitSet) bitsetI.bitset.clone();
        
        // Perform bitwise AND - VERY FAST operation
        intersection.and(bitsetJ.bitset);
        
        // Count set bits - this is the support
        int support = intersection.cardinality();
        
        return new BitSetSupport(intersection, support);
    }

    /**
     * Generate candidates for level k using BitSet operations
     * FIXED: Correct Apriori join condition
     */
    private List<Itemset> generateCandidatesLevelKBitset(List<Itemset> levelKMinus1, 
                                                        Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        List<Itemset> candidates = new ArrayList<Itemset>();
        
        for (int i = 0; i < levelKMinus1.size(); i++) {
            Itemset itemset1 = levelKMinus1.get(i);
            for (int j = i + 1; j < levelKMinus1.size(); j++) {
                Itemset itemset2 = levelKMinus1.get(j);
                
                // FIXED: Correct Apriori join condition
                // Check if first k-2 items are the same
                boolean canJoin = true;
                for (int k = 0; k < itemset1.size() - 1; k++) {
                    if (itemset1.getItems()[k] != itemset2.getItems()[k]) {
                        canJoin = false;
                        break;
                    }
                }
                
                // FIXED: Also check that last items are in ascending order
                if (canJoin && itemset1.getItems()[itemset1.size() - 1] < itemset2.getItems()[itemset2.size() - 1]) {
                    // Create new candidate by merging
                    int[] newItemset = new int[itemset1.size() + 1];
                    System.arraycopy(itemset1.getItems(), 0, newItemset, 0, itemset1.size());
                    newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() - 1];
                    
                    // Calculate support using FAST BitSet operations
                    BitSet combinedBitset = null;
                    for (int item : newItemset) {
                        BitSetSupport itemBitset = mapItemBitsets.get(item);
                        if (combinedBitset == null) {
                            combinedBitset = (BitSet) itemBitset.bitset.clone();
                        } else {
                            combinedBitset.and(itemBitset.bitset); // FAST AND operation
                        }
                    }
                    
                    int support = combinedBitset.cardinality();
                    
                    if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {
                        Itemset candidate = new Itemset(newItemset);
                        candidate.setAbsoluteSupport(support);
                        candidates.add(candidate);
                        
                        int[] prefix = new int[newItemset.length - 1];
                        System.arraycopy(newItemset, 0, prefix, 0, newItemset.length - 1);
                        saveRareItemsetBitset(prefix, newItemset.length - 1, newItemset[newItemset.length - 1], support);
                    }
                }
            }
        }
        
        return candidates;
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
                // Extract transaction IDs from BitSet efficiently
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

    public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
        this.showTransactionIdentifiers = showTransactionIdentifiers;
    }

    public void setMaximumPatternLength(int length) {
        this.maxItemsetSize = length;
    }

    public void printStats() {
        System.out.println("=============  ECLAT RARE BITSET v1.0 - STATS =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Transactions count from database : " + database.size());
        System.out.println(" Rare itemsets count : " + rareItemsetCount);
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" BitSet optimization: ENABLED");
        System.out.println(" Definition: MRT < Support(X) <= MFT");
        System.out.println("============================================================");
    }

    public Itemsets getRareItemsets() {
        return rareItemsets;
    }
}