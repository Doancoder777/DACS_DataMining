package Algorithrms.Rarepartem.eclat_rare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Algorithrms.Rarepartem.eclat_rare.AbstractRareItemsetAlgorithm;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

public class AlgoEclatRareBitset extends AbstractRareItemsetAlgorithm {

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
        
        initializeParameters(database, minRareSupport, maxFrequentSupport);
        
        if (output == null) {
            writer = null;
            rareItemsets = new Itemsets("RARE ITEMSETS");
        } else {
            rareItemsets = null;
            writer = new BufferedWriter(new FileWriter(output));
        }
        
        final Map<Integer, BitSetSupport> mapItemBitsets = new HashMap<Integer, BitSetSupport>();
        calculateSupportSingleItemsBitset(database, mapItemBitsets);

        List<Integer> rareItems = new ArrayList<Integer>();
        
        for (Entry<Integer, BitSetSupport> entry : mapItemBitsets.entrySet()) {
            int support = entry.getValue().support;
            int item = entry.getKey();
            
            if (isRareSupport(support) && maxItemsetSize >= 1) {
                rareItems.add(item);
                saveRareSingleItemBitset(item, entry.getValue());
            }
        }

        Collections.sort(rareItems);

        if (maxItemsetSize >= 2 && rareItems.size() > 1) {
            generateRareCombinationsBitset(rareItems, mapItemBitsets);
        }
        
        finalizeExecution();
        
        if (writer != null) {
            writer.close();
        }
        
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

    private void generateRareCombinationsBitset(List<Integer> rareItems, 
                                               Map<Integer, BitSetSupport> mapItemBitsets) throws IOException {
        
        List<Itemset> level = new ArrayList<Itemset>();
        
        for (int i = 0; i < rareItems.size(); i++) {
            for (int j = i + 1; j < rareItems.size(); j++) {
                Integer itemI = rareItems.get(i);
                Integer itemJ = rareItems.get(j);
                
                if (itemI > itemJ) {
                    Integer temp = itemI;
                    itemI = itemJ;
                    itemJ = temp;
                }
                
                BitSetSupport bitsetI = mapItemBitsets.get(itemI);
                BitSetSupport bitsetJ = mapItemBitsets.get(itemJ);
                
                BitSetSupport intersectionBitset = performBitsetIntersection(bitsetI, bitsetJ);
                int support = intersectionBitset.support;
                
                if (isRareSupport(support)) {
                    Itemset itemset = new Itemset(new int[]{itemI, itemJ});
                    itemset.setAbsoluteSupport(support);
                    level.add(itemset);
                    
                    saveRareItemsetBitset(new int[]{itemI}, 1, itemJ, support);
                }
            }
        }
        
        int k = 3;
        while (!level.isEmpty() && k <= maxItemsetSize) {
            level = generateCandidatesLevelKBitset(level, mapItemBitsets);
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
        
        for (int i = 0; i < levelKMinus1.size(); i++) {
            Itemset itemset1 = levelKMinus1.get(i);
            for (int j = i + 1; j < levelKMinus1.size(); j++) {
                Itemset itemset2 = levelKMinus1.get(j);
                
                boolean canJoin = true;
                for (int k = 0; k < itemset1.size() - 1; k++) {
                    if (itemset1.getItems()[k] != itemset2.getItems()[k]) {
                        canJoin = false;
                        break;
                    }
                }
                
                if (canJoin && itemset1.getItems()[itemset1.size() - 1] < itemset2.getItems()[itemset2.size() - 1]) {
                    int[] newItemset = new int[itemset1.size() + 1];
                    System.arraycopy(itemset1.getItems(), 0, newItemset, 0, itemset1.size());
                    newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() - 1];
                    
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
                    
                    if (isRareSupport(support)) {
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
        System.out.println("=============  ECLAT RARE BITSET v1.0 - STATS =============");
        System.out.println(" Transactions count from database : " + database.size());
        System.out.println(" Rare itemsets count : " + rareItemsetCount);
        System.out.println(" Total time ~ " + getExecutionTime() + " ms");
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" BitSet optimization: ENABLED");
        System.out.println(" Definition: MRT < Support(X) <= MFT");
        System.out.println("============================================================");
    }
}