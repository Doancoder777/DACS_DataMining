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

/**
 * ECLAT Rare Debug Version - Trace missing 3-itemsets
 * Focus on debugging why {53, 56, 80} is missing
 */
public class AlgoEclatRareDebug3Items {

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

    public AlgoEclatRareDebug3Items() {
    }

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
        
        this.minRareSupportRelative = (int) Math.ceil(minRareSupport * database.size()) - 1;
        this.maxFrequentSupportRelative = (int) Math.ceil(maxFrequentSupport * database.size());
        
        System.out.println("=== ECLAT RARE DEBUG - 3-ITEMSETS TRACING ===");
        System.out.println("Target missing: {53, 56, 80} with support ~52518");
        System.out.println("Thresholds: " + minRareSupportRelative + " < Support <= " + maxFrequentSupportRelative);
        System.out.println("===============================================");
        
        // Calculate BitSet tidsets
        final Map<Integer, BitSetSupport> mapItemBitsets = new HashMap<Integer, BitSetSupport>();
        calculateSupportSingleItemsBitset(database, mapItemBitsets);

        // Find rare single items
        List<Integer> rareItems = new ArrayList<Integer>();
        
        for (Entry<Integer, BitSetSupport> entry : mapItemBitsets.entrySet()) {
            int support = entry.getValue().support;
            int item = entry.getKey();
            
            if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {
                rareItems.add(item);
                saveRareSingleItemBitset(item, entry.getValue());
            }
        }

        Collections.sort(rareItems, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return mapItemBitsets.get(arg0).support - mapItemBitsets.get(arg1).support;
            }
        });

        System.out.println("Rare items found: " + rareItems.size());
        
        // Check if target items are in rare list
        boolean has53 = rareItems.contains(53);
        boolean has56 = rareItems.contains(56);
        boolean has80 = rareItems.contains(80);
        System.out.println("Target items check:");
        System.out.println("- Item 53 rare? " + has53 + " (support: " + (mapItemBitsets.containsKey(53) ? mapItemBitsets.get(53).support : "N/A") + ")");
        System.out.println("- Item 56 rare? " + has56 + " (support: " + (mapItemBitsets.containsKey(56) ? mapItemBitsets.get(56).support : "N/A") + ")");
        System.out.println("- Item 80 rare? " + has80 + " (support: " + (mapItemBitsets.containsKey(80) ? mapItemBitsets.get(80).support : "N/A") + ")");

        // Generate combinations with detailed tracing
        if (maxItemsetSize >= 2 && rareItems.size() > 1) {
            generateRareCombinationsWithDebug(rareItems, mapItemBitsets);
        }
        
        MemoryLogger.getInstance().checkMemory();
        
        if (writer != null) {
            writer.close();
        }
        
        endTime = System.currentTimeMillis();
        
        return rareItemsets;
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

    private void generateRareCombinationsWithDebug(List<Integer> rareItems, 
                                                  Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        
        System.out.println("\n=== GENERATING 2-ITEMSETS ===");
        List<Itemset> level2 = new ArrayList<Itemset>();
        
        // Track specific combinations we're interested in
        boolean found53_56 = false;
        boolean found56_80 = false;
        boolean found53_80 = false;
        
        for (int i = 0; i < rareItems.size(); i++) {
            for (int j = i + 1; j < rareItems.size(); j++) {
                Integer itemI = rareItems.get(i);
                Integer itemJ = rareItems.get(j);
                
                BitSetSupport bitsetI = mapItemBitsets.get(itemI);
                BitSetSupport bitsetJ = mapItemBitsets.get(itemJ);
                
                BitSetSupport intersectionBitset = performBitsetIntersection(bitsetI, bitsetJ);
                int support = intersectionBitset.support;
                
                // Check for target combinations
                if ((itemI == 53 && itemJ == 56) || (itemI == 56 && itemJ == 53)) {
                    found53_56 = true;
                    System.out.println("Found {53, 56}: support=" + support + " (rare: " + (support > minRareSupportRelative && support <= maxFrequentSupportRelative) + ")");
                }
                if ((itemI == 56 && itemJ == 80) || (itemI == 80 && itemJ == 56)) {
                    found56_80 = true;
                    System.out.println("Found {56, 80}: support=" + support + " (rare: " + (support > minRareSupportRelative && support <= maxFrequentSupportRelative) + ")");
                }
                if ((itemI == 53 && itemJ == 80) || (itemI == 80 && itemJ == 53)) {
                    found53_80 = true;
                    System.out.println("Found {53, 80}: support=" + support + " (rare: " + (support > minRareSupportRelative && support <= maxFrequentSupportRelative) + ")");
                }
                
                if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {
                    Itemset itemset = new Itemset(new int[]{itemI, itemJ});
                    itemset.setAbsoluteSupport(support);
                    level2.add(itemset);
                    
                    saveRareItemsetBitset(new int[]{itemI}, 1, itemJ, support);
                }
            }
        }
        
        System.out.println("2-itemsets generated: " + level2.size());
        System.out.println("Prerequisites for {53, 56, 80}:");
        System.out.println("- {53, 56} found: " + found53_56);
        System.out.println("- {56, 80} found: " + found56_80);
        System.out.println("- {53, 80} found: " + found53_80);
        
        // Generate 3-itemsets with detailed tracing
        if (maxItemsetSize >= 3 && level2.size() > 1) {
            System.out.println("\n=== GENERATING 3-ITEMSETS ===");
            generateCandidatesLevel3WithDebug(level2, mapItemBitsets);
        }
    }

    private void generateCandidatesLevel3WithDebug(List<Itemset> level2, 
                                                  Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        
        System.out.println("Level 2 itemsets: " + level2.size());
        
        int candidatesGenerated = 0;
        int targetChecked = 0;
        
        for (int i = 0; i < level2.size(); i++) {
            Itemset itemset1 = level2.get(i);
            for (int j = i + 1; j < level2.size(); j++) {
                Itemset itemset2 = level2.get(j);
                
                // Check join condition
                boolean canJoin = true;
                for (int k = 0; k < itemset1.size() - 1; k++) {
                    if (itemset1.getItems()[k] != itemset2.getItems()[k]) {
                        canJoin = false;
                        break;
                    }
                }
                
                if (canJoin && itemset1.getItems()[itemset1.size() - 1] < itemset2.getItems()[itemset2.size() - 1]) {
                    // Create candidate
                    int[] newItemset = new int[itemset1.size() + 1];
                    System.arraycopy(itemset1.getItems(), 0, newItemset, 0, itemset1.size());
                    newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() - 1];
                    
                    candidatesGenerated++;
                    
                    // Check if this is our target combination
                    boolean isTarget = (newItemset.length == 3 && 
                                       ((newItemset[0] == 53 && newItemset[1] == 56 && newItemset[2] == 80) ||
                                        (newItemset[0] == 53 && newItemset[1] == 80 && newItemset[2] == 56) ||
                                        (newItemset[0] == 56 && newItemset[1] == 53 && newItemset[2] == 80) ||
                                        (newItemset[0] == 56 && newItemset[1] == 80 && newItemset[2] == 53) ||
                                        (newItemset[0] == 80 && newItemset[1] == 53 && newItemset[2] == 56) ||
                                        (newItemset[0] == 80 && newItemset[1] == 56 && newItemset[2] == 53)));
                    
                    if (isTarget) {
                        targetChecked++;
                        System.out.println("TARGET CANDIDATE FOUND: {" + newItemset[0] + ", " + newItemset[1] + ", " + newItemset[2] + "}");
                        System.out.println("  Generated from: {" + itemset1.getItems()[0] + ", " + itemset1.getItems()[1] + "} + {" + itemset2.getItems()[0] + ", " + itemset2.getItems()[1] + "}");
                    }
                    
                    // Calculate support
                    BitSet combinedBitset = null;
                    for (int item : newItemset) {
                        BitSetSupport itemBitset = mapItemBitsets.get(item);
                        if (combinedBitset == null) {
                            combinedBitset = (BitSet) itemBitset.bitset.clone();
                        } else {
                            combinedBitset.and(itemBitset.bitset);
                        }
                    }
                    
                    int support = combinedBitset.cardinality();
                    
                    if (isTarget) {
                        System.out.println("  Support calculated: " + support);
                        System.out.println("  Threshold check: " + support + " > " + minRareSupportRelative + " && " + support + " <= " + maxFrequentSupportRelative);
                        System.out.println("  Is rare: " + (support > minRareSupportRelative && support <= maxFrequentSupportRelative));
                    }
                    
                    if (support > minRareSupportRelative && support <= maxFrequentSupportRelative) {
                        Itemset candidate = new Itemset(newItemset);
                        candidate.setAbsoluteSupport(support);
                        
                        int[] prefix = new int[newItemset.length - 1];
                        System.arraycopy(newItemset, 0, prefix, 0, newItemset.length - 1);
                        saveRareItemsetBitset(prefix, newItemset.length - 1, newItemset[newItemset.length - 1], support);
                        
                        if (isTarget) {
                            System.out.println("  TARGET SAVED!");
                        }
                    } else if (isTarget) {
                        System.out.println("  TARGET REJECTED - not rare!");
                    }
                }
            }
        }
        
        System.out.println("3-itemset candidates generated: " + candidatesGenerated);
        System.out.println("Target {53, 56, 80} checked: " + targetChecked + " times");
    }

    private BitSetSupport performBitsetIntersection(BitSetSupport bitsetI, BitSetSupport bitsetJ) {
        BitSet intersection = (BitSet) bitsetI.bitset.clone();
        intersection.and(bitsetJ.bitset);
        int support = intersection.cardinality();
        return new BitSetSupport(intersection, support);
    }

    private void saveRareSingleItemBitset(int item, BitSetSupport bitsetSupport) throws IOException {
        rareItemsetCount++;
        
        if (writer == null) {
            Itemset itemset = new Itemset(new int[]{item});
            itemset.setAbsoluteSupport(bitsetSupport.support);
            rareItemsets.addItemset(itemset, itemset.size());
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
        }
    }

    public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
        this.showTransactionIdentifiers = showTransactionIdentifiers;
    }

    public void setMaximumPatternLength(int length) {
        this.maxItemsetSize = length;
    }

    public void printStats() {
        System.out.println("=============  ECLAT RARE DEBUG - STATS =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Transactions count from database : " + database.size());
        System.out.println(" Rare itemsets count : " + rareItemsetCount);
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println("======================================================");
    }

    public Itemsets getRareItemsets() {
        return rareItemsets;
    }
}