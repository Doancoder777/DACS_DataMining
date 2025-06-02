package Algorithrms.Rarepartem.eclat_rare;

import java.io.BufferedWriter;
import java.io.IOException;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

public abstract class AbstractRareItemsetAlgorithm {
    
    protected int minRareSupportRelative;
    protected int maxFrequentSupportRelative;
    protected TransactionDatabase database;
    protected long startTimestamp;
    protected long endTime;
    protected Itemsets rareItemsets;
    protected BufferedWriter writer;
    protected int rareItemsetCount;
    protected boolean showTransactionIdentifiers = false;
    protected int maxItemsetSize = Integer.MAX_VALUE;
    
    public AbstractRareItemsetAlgorithm() {
        MemoryLogger.getInstance().reset();
    }
    
    public abstract Itemsets runAlgorithm(String output, TransactionDatabase database, 
                                         double minRareSupport, double maxFrequentSupport) throws IOException;
    
    protected void initializeParameters(TransactionDatabase database, double minRareSupport, double maxFrequentSupport) {
        this.database = database;
        this.startTimestamp = System.currentTimeMillis();
        this.rareItemsetCount = 0;
        this.minRareSupportRelative = (int) Math.ceil(minRareSupport * database.size()) - 1;
        this.maxFrequentSupportRelative = (int) Math.ceil(maxFrequentSupport * database.size());
    }
    
    protected boolean isRareSupport(int support) {
        return support > minRareSupportRelative && support <= maxFrequentSupportRelative;
    }
    
    protected void finalizeExecution() {
        MemoryLogger.getInstance().checkMemory();
        this.endTime = System.currentTimeMillis();
    }
    
    public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
        this.showTransactionIdentifiers = showTransactionIdentifiers;
    }
    
    public void setMaximumPatternLength(int length) {
        this.maxItemsetSize = length;
    }
    
    public abstract void printStats();
    
    public Itemsets getRareItemsets() {
        return rareItemsets;
    }
    
    public int getRareItemsetCount() {
        return rareItemsetCount;
    }
    
    public long getExecutionTime() {
        return endTime - startTimestamp;
    }
}