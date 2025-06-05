package Algorithrms.Rarepartem.eclat_rare;

import java.io.BufferedWriter;
import java.io.IOException;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

/**
 * =================================================================
 * ABSTRACT BASE CLASS cho tất cả Rare Itemset Mining Algorithms
 * =================================================================
 * 
 * DESIGN PATTERN: Template Method Pattern
 * - Abstract class định nghĩa skeleton của rare itemset mining algorithm
 * - Concrete subclasses implement specific mining strategies
 * - Common functionality được share giữa các algorithms
 * 
 * CORE RESPONSIBILITIES:
 * 1. PARAMETER MANAGEMENT: Quản lý thresholds và constraints
 * 2. LIFECYCLE MANAGEMENT: Initialize → Execute → Finalize 
 * 3. STATISTICS TRACKING: Memory usage, execution time, result count
 * 4. OUTPUT HANDLING: File output hoặc in-memory storage
 * 
 * SUPPORTED ALGORITHMS:
 * - ECLAT Rare (vertical mining với BitSet)
 * - PrePost Rare (pre/post order indexing)
 * - RP-Growth (FP-Growth variant cho rare patterns)
 * 
 * COMMON PARAMETERS:
 * - minRareSupport: Minimum Rare Threshold (MRT)
 * - maxFrequentSupport: Maximum Frequent Threshold (MFT) 
 * - Size constraints: minItemsetSize, maxItemsetSize
 * - Output options: showTransactionIdentifiers
 */
public abstract class AbstractRareItemsetAlgorithm {
    
    // ===== CORE THRESHOLDS =====
    // Absolute support thresholds (converted từ relative percentages)
    protected int minRareSupportRelative;      // MRT: Minimum threshold cho rare items
    protected int maxFrequentSupportRelative;  // MFT: Maximum threshold cho frequent items
    
    // ===== DATABASE REFERENCE =====
    protected TransactionDatabase database;    // Input transaction database
    
    // ===== TIMING TRACKING =====
    protected long startTimestamp;            // Algorithm start time
    protected long endTime;                   // Algorithm end time  
    
    // ===== RESULT STORAGE =====
    protected Itemsets rareItemsets;         // In-memory storage cho rare itemsets
    protected BufferedWriter writer;         // File output writer (null nếu in-memory)
    protected int rareItemsetCount;          // Counter cho số rare itemsets found
    
    // ===== OUTPUT OPTIONS =====
    protected boolean showTransactionIdentifiers = false;  // Show TIDs trong output
    protected int maxItemsetSize = Integer.MAX_VALUE;      // Maximum itemset size constraint
    
    /**
     * =================================================================
     * CONSTRUCTOR - Initialize Memory Tracking
     * =================================================================
     * 
     * Reset MemoryLogger để tracking memory usage chính xác
     * Mỗi algorithm run sẽ có fresh memory measurement
     */
    public AbstractRareItemsetAlgorithm() {
        MemoryLogger.getInstance().reset();
    }
    
    /**
     * =================================================================
     * ABSTRACT METHOD: Main Algorithm Entry Point
     * =================================================================
     * 
     * Template method pattern - mỗi subclass phải implement mining logic
     * 
     * @param output Output file path (null cho in-memory storage)
     * @param database Input transaction database
     * @param minRareSupport MRT threshold (0.0-1.0)
     * @param maxFrequentSupport MFT threshold (0.0-1.0) 
     * @return Itemsets chứa tất cả rare itemsets found
     * @throws IOException Nếu có lỗi I/O
     */
    public abstract Itemsets runAlgorithm(String output, TransactionDatabase database, 
                                        double minRareSupport, double maxFrequentSupport) throws IOException;
    
    /**
     * =================================================================
     * PARAMETER INITIALIZATION - Common Setup cho mọi algorithms
     * =================================================================
     * 
     * Convert relative thresholds thành absolute counts:
     * - minRareSupportRelative = ceil(minRareSupport × |D|) - 1
     * - maxFrequentSupportRelative = ceil(maxFrequentSupport × |D|)
     * 
     * WHY -1 cho minRareSupportRelative?
     * - Để đảm bảo strict inequality: support > MRT
     * - VD: MRT=0.1, |D|=10 → threshold=0, cần support ≥ 1
     * 
     * @param database Input database để tính |D|
     * @param minRareSupport Relative MRT (0.0-1.0)
     * @param maxFrequentSupport Relative MFT (0.0-1.0)
     */
    protected void initializeParameters(TransactionDatabase database, double minRareSupport, double maxFrequentSupport) {
        this.database = database;
        this.startTimestamp = System.currentTimeMillis();
        this.rareItemsetCount = 0;
        
        // ===== CONVERT RELATIVE → ABSOLUTE THRESHOLDS =====
        // MRT: Minimum Rare Threshold - support phải > threshold này
        this.minRareSupportRelative = (int) Math.ceil(minRareSupport * database.size()) - 1;
        
        // MFT: Maximum Frequent Threshold - support phải ≤ threshold này cho rare
        this.maxFrequentSupportRelative = (int) Math.ceil(maxFrequentSupport * database.size());
        
        System.out.println("Database size: " + database.size());
        System.out.println("MRT (absolute): " + minRareSupportRelative);
        System.out.println("MFT (absolute): " + maxFrequentSupportRelative);
    }
    
    /**
     * =================================================================
     * SUPPORT RANGE VALIDATION
     * =================================================================
     * 
     * Check xem support có nằm trong rare range không:
     * - Rare range: MRT < support ≤ MFT
     * - Frequent: support > MFT  
     * - Infrequent: support ≤ MRT
     * 
     * @param support Absolute support count
     * @return true nếu support nằm trong rare range
     */
    protected boolean isRareSupport(int support) {
        return support > minRareSupportRelative && support <= maxFrequentSupportRelative;
    }
    
    /**
     * =================================================================
     * ALGORITHM FINALIZATION - Cleanup và Statistics
     * =================================================================
     * 
     * Được gọi khi algorithm hoàn thành:
     * 1. Check memory usage peak
     * 2. Record end timestamp
     * 3. Cleanup resources nếu cần
     */
    protected void finalizeExecution() {
        MemoryLogger.getInstance().checkMemory();  // Record peak memory usage
        this.endTime = System.currentTimeMillis();  // Record end time
    }
    
    /**
     * =================================================================
     * OUTPUT CONFIGURATION METHODS
     * =================================================================
     */
    
    /**
     * Enable/disable transaction identifier display trong output
     * Khi enabled, output sẽ show: itemset #SUP: count #TID: tid1 tid2 tid3...
     * 
     * @param showTransactionIdentifiers true để show TIDs
     */
    public void setShowTransactionIdentifiers(boolean showTransactionIdentifiers) {
        this.showTransactionIdentifiers = showTransactionIdentifiers;
    }
    
    /**
     * Set maximum pattern length constraint
     * Patterns có size > length sẽ không được mine
     * 
     * @param length Maximum itemset size
     */
    public void setMaximumPatternLength(int length) {
        this.maxItemsetSize = length;
    }
    
    /**
     * =================================================================
     * ABSTRACT METHOD: Statistics Display
     * =================================================================
     * 
     * Mỗi algorithm phải implement method này để show:
     * - Database statistics
     * - Threshold values  
     * - Result counts
     * - Performance metrics (time, memory)
     * - Algorithm-specific optimizations
     */
    public abstract void printStats();
    
    /**
     * =================================================================
     * GETTER METHODS - Access to Results và Statistics
     * =================================================================
     */
    
    /**
     * Get tất cả rare itemsets found (in-memory storage)
     * @return Itemsets object chứa results, null nếu file output
     */
    public Itemsets getRareItemsets() {
        return rareItemsets;
    }
    
    /**
     * Get total number of rare itemsets found
     * @return Count of rare itemsets
     */
    public int getRareItemsetCount() {
        return rareItemsetCount;
    }
    
    /**
     * Get total execution time của algorithm
     * @return Execution time in milliseconds
     */
    public long getExecutionTime() {
        return endTime - startTimestamp;
    }
    
    /**
     * =================================================================
     * UTILITY METHODS - Helper functions cho subclasses
     * =================================================================
     */
    
    /**
     * Get database size (number of transactions)
     * @return Number of transactions trong database
     */
    protected int getDatabaseSize() {
        return database != null ? database.size() : 0;
    }
    
    /**
     * Get absolute MRT threshold
     * @return Minimum rare support (absolute count)
     */
    protected int getMinRareSupportAbsolute() {
        return minRareSupportRelative;
    }
    
    /**
     * Get absolute MFT threshold  
     * @return Maximum frequent support (absolute count)
     */
    protected int getMaxFrequentSupportAbsolute() {
        return maxFrequentSupportRelative;
    }
    
    /**
     * Check if pattern size is within constraints
     * @param size Pattern size to check
     * @return true nếu size valid
     */
    protected boolean isValidPatternSize(int size) {
        return size <= maxItemsetSize;
    }
    
    /**
     * =================================================================
     * DEBUG METHODS - Để troubleshooting
     * =================================================================
     */
    
    /**
     * Print basic algorithm configuration
     * Useful cho debugging parameter setup
     */
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
    
    /**
     * Validate thresholds để catch configuration errors
     * @throws IllegalArgumentException nếu thresholds invalid
     */
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