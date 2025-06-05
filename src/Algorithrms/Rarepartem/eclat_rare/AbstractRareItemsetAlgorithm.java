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

        System.out.println("Database size: " + database.size());
        System.out.println("MRT (absolute): " + minRareSupportRelative);
        System.out.println("MFT (absolute): " + maxFrequentSupportRelative);
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

    protected int getDatabaseSize() {
        return database != null ? database.size() : 0;
    }

    protected int getMinRareSupportAbsolute() {
        return minRareSupportRelative;
    }

    protected int getMaxFrequentSupportAbsolute() {
        return maxFrequentSupportRelative;
    }

    protected boolean isValidPatternSize(int size) {
        return size <= maxItemsetSize;
    }

    protected void printConfiguration() {
        System.out.println("=== ALGORITHM CONFIGURATION ===");
        System.out.println("Database size: " + getDatabaseSize());
        System.out.println("MRT (absolute): " + minRareSupportRelative);
        System.out.println("MFT (absolute): " + maxFrequentSupportRelative);
        System.out.println("Max itemset size: " + maxItemsetSize);
        System.out.println("Show TIDs: " + showTransactionIdentifiers);
        System.out.println("Output mode: " + (writer != null ? "File" : "Memory"));
        System.out.println("==============================");
    }

    protected void validateThresholds() {
        if (minRareSupportRelative >= maxFrequentSupportRelative) {
            throw new IllegalArgumentException(
                "Invalid thresholds: MRT (" + minRareSupportRelative +
                ") must be < MFT (" + maxFrequentSupportRelative + ")");
        }

        if (minRareSupportRelative < 0) {
            throw new IllegalArgumentException("MRT cannot be negative: " + minRareSupportRelative);
        }

        if (maxFrequentSupportRelative > getDatabaseSize()) {
            throw new IllegalArgumentException(
                "MFT (" + maxFrequentSupportRelative +
                ") cannot exceed database size (" + getDatabaseSize() + ")");
        }
    }
}