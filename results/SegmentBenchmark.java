// Copy all imports from previous versions

package RareBenchmark;

import Algorithrms.frequentpatterns.aprioriTID_inverse.AlgoAprioriTIDInverse;
import Algorithrms.Rarepartem.eclat_rare.AlgoEclatRareBitset;
import Algorithrms.Rarepartem.nlistrare.PrePostRare;
import Algorithrms.Rarepartem.itemsettreerare.RareItemsetTree;
import Algorithrms.Rarepartem.rpgrowth.AlgoRPGrowth;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemsets;
import patterns.itemset_array_integers_with_tids.Itemset;
import tools.MemoryLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Segment Benchmark - Test từng đoạn support riêng biệt
 * 5→10, 10→15, 15→20, 20→25, 25→30, 30→35, 35→40
 */
public class SegmentBenchmark {
    
    // Định nghĩa các đoạn support để test
    private static final double[][] SUPPORT_SEGMENTS = {
        {5.0, 10.0},   // Segment 1: 5% → 10%
        {10.0, 15.0},  // Segment 2: 10% → 15%
        {15.0, 20.0},  // Segment 3: 15% → 20%
        {20.0, 25.0},  // Segment 4: 20% → 25%
        {25.0, 30.0},  // Segment 5: 25% → 30%
        {30.0, 35.0},  // Segment 6: 30% → 35%
        {35.0, 40.0}   // Segment 7: 35% → 40%
    };
    
    public static class SegmentResult {
        public String algorithm;
        public double minSupport;  // Start of segment
        public double maxSupport;  // End of segment
        public long runtime;
        public double memory;
        public int itemsetsFound;
        public String dataset;
        public String segmentName;
        
        public SegmentResult(String algorithm, double minSupport, double maxSupport, 
                           long runtime, double memory, int itemsetsFound, String dataset) {
            this.algorithm = algorithm;
            this.minSupport = minSupport;
            this.maxSupport = maxSupport;
            this.runtime = runtime;
            this.memory = memory;
            this.itemsetsFound = itemsetsFound;
            this.dataset = dataset;
            this.segmentName = String.format("%.0f-%.0f%%", minSupport, maxSupport);
        }
        
        @Override
        public String toString() {
            return String.format("%.1f,%.1f,%s,%d,%.2f,%d,%s,%s", 
                minSupport, maxSupport, algorithm, runtime, memory, itemsetsFound, dataset, segmentName);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 SEGMENT BENCHMARK - RARE ITEMSET MINING");
        System.out.println("=========================================");
        System.out.println("Test segments:");
        for (int i = 0; i < SUPPORT_SEGMENTS.length; i++) {
            System.out.printf("  %d. %.0f%% → %.0f%% (range: %.0f%%)\n", 
                i + 1, SUPPORT_SEGMENTS[i][0], SUPPORT_SEGMENTS[i][1], 
                SUPPORT_SEGMENTS[i][1] - SUPPORT_SEGMENTS[i][0]);
        }
        System.out.println("Total: " + SUPPORT_SEGMENTS.length + " segments");
        System.out.println("========================================");
        
        // Chọn dataset
        String dataDir = "Data";
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".hui"));
        
        if (files == null || files.length == 0) {
            System.out.println("❌ Không tìm thấy file dữ liệu trong thư mục: " + dataDir);
            return;
        }
        
        System.out.println("📁 Danh sách file dữ liệu:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n🔍 Chọn file để test (1-" + files.length + "): ");
        int fileChoice = scanner.nextInt();
        
        if (fileChoice < 1 || fileChoice > files.length) {
            System.out.println("❌ Lựa chọn không hợp lệ!");
            scanner.close();
            return;
        }
        
        File selectedFile = files[fileChoice - 1];
        System.out.println("✅ Đã chọn: " + selectedFile.getName());
        
        System.out.println("\nChọn algorithms để test:");
        System.out.println("1. Tất cả algorithms (" + (5 * SUPPORT_SEGMENTS.length) + " tests)");
        System.out.println("2. Algorithms nhanh (ECLAT + RP-Growth, " + (2 * SUPPORT_SEGMENTS.length) + " tests)");
        System.out.println("3. Chỉ ECLAT Rare (" + SUPPORT_SEGMENTS.length + " tests)");
        System.out.print("Nhập lựa chọn (1-3): ");
        int algoChoice = scanner.nextInt();
        
        List<String> selectedAlgorithms = new ArrayList<>();
        switch (algoChoice) {
            case 1:
                selectedAlgorithms.add("AprioriTIDInverse");
                selectedAlgorithms.add("EclatRareBitset");
                selectedAlgorithms.add("PrePostRare");
                selectedAlgorithms.add("RareItemsetTree");
                selectedAlgorithms.add("RPGrowth");
                break;
            case 2:
                selectedAlgorithms.add("EclatRareBitset");
                selectedAlgorithms.add("RPGrowth");
                break;
            case 3:
                selectedAlgorithms.add("EclatRareBitset");
                break;
        }
        
        scanner.close();
        
        int totalTests = selectedAlgorithms.size() * SUPPORT_SEGMENTS.length;
        System.out.println("\n🔥 Bắt đầu segment benchmark:");
        System.out.println("Algorithms: " + selectedAlgorithms);
        System.out.println("Segments: " + SUPPORT_SEGMENTS.length);
        System.out.println("Total tests: " + totalTests);
        System.out.println("Estimated time: " + (totalTests * 1) + "-" + (totalTests * 5) + " phút");
        System.out.println("========================================================================");
        
        try {
            List<SegmentResult> allResults = runSegmentBenchmark(selectedFile, selectedAlgorithms);
            
            // Save results
            saveSegmentResults(allResults, selectedFile.getName());
            generateSegmentChartScript(allResults);
            
            // Display summary
            displaySegmentSummary(allResults);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Chạy benchmark cho từng segment
     */
    private static List<SegmentResult> runSegmentBenchmark(File dataFile, List<String> algorithms) throws IOException {
        List<SegmentResult> allResults = new ArrayList<>();
        String inputFile = dataFile.getAbsolutePath();
        String fileName = dataFile.getName();
        
        int testCount = 0;
        int totalTests = algorithms.size() * SUPPORT_SEGMENTS.length;
        
        for (int segmentIndex = 0; segmentIndex < SUPPORT_SEGMENTS.length; segmentIndex++) {
            double[] segment = SUPPORT_SEGMENTS[segmentIndex];
            double minSupport = segment[0];
            double maxSupport = segment[1];
            
            System.out.printf("\n📊 Testing Segment %d: %.0f%% → %.0f%%\n", 
                segmentIndex + 1, minSupport, maxSupport);
            System.out.println("==================================================");
            
            double minSupp = minSupport / 100.0;
            double maxSupp = maxSupport / 100.0;
            
            for (String algorithm : algorithms) {
                testCount++;
                System.out.printf("[%d/%d] Testing %s (%.0f%% → %.0f%%)... ", 
                    testCount, totalTests, algorithm, minSupport, maxSupport);
                
                try {
                    SegmentResult result = null;
                    
                    switch (algorithm) {
                        case "AprioriTIDInverse":
                            result = testAprioriTIDInverseSegment(inputFile, fileName, minSupp, maxSupp, minSupport, maxSupport);
                            break;
                        case "EclatRareBitset":
                            result = testEclatRareBitsetSegment(inputFile, fileName, minSupp, maxSupp, minSupport, maxSupport);
                            break;
                        case "PrePostRare":
                            result = testPrePostRareSegment(inputFile, fileName, minSupp, maxSupp, minSupport, maxSupport);
                            break;
                        case "RareItemsetTree":
                            result = testRareItemsetTreeSegment(inputFile, fileName, minSupp, maxSupp, minSupport, maxSupport);
                            break;
                        case "RPGrowth":
                            result = testRPGrowthSegment(inputFile, fileName, minSupp, maxSupp, minSupport, maxSupport);
                            break;
                    }
                    
                    if (result != null && result.runtime > 0) {
                        allResults.add(result);
                        System.out.printf("✅ %dms (%d patterns)\n", result.runtime, result.itemsetsFound);
                    } else {
                        System.out.println("❌ Failed");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                }
                
                // Brief pause
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        
        return allResults;
    }
    
    // Test methods for each algorithm (each segment)
    private static SegmentResult testEclatRareBitsetSegment(String inputFile, String fileName, 
                                                           double minSupp, double maxSupp, 
                                                           double minSuppPercent, double maxSuppPercent) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            TransactionDatabase database = convertItemsetTreeToTransactionDatabase(inputFile);
            AlgoEclatRareBitset algo = new AlgoEclatRareBitset();
            algo.setShowTransactionIdentifiers(false);
            
            Itemsets rareItemsets = algo.runAlgorithm(null, database, minSupp, maxSupp);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = countItemsetsWithCount(rareItemsets);
            
            return new SegmentResult("ECLAT Rare", minSuppPercent, maxSuppPercent, 
                                    runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testAprioriTIDInverseSegment(String inputFile, String fileName, 
                                                            double minSupp, double maxSupp,
                                                            double minSuppPercent, double maxSuppPercent) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            TransactionDatabase database = convertItemsetTreeToTransactionDatabase(inputFile);
            AlgoAprioriTIDInverse algo = new AlgoAprioriTIDInverse();
            algo.setShowTransactionIdentifiers(false);
            
            patterns.itemset_array_integers_with_tids.Itemsets rareItemsets = 
                algo.runAlgorithm(database, minSupp, maxSupp);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = countItemsetsWithTids(rareItemsets);
            
            return new SegmentResult("Apriori TID", minSuppPercent, maxSuppPercent, 
                                    runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testRPGrowthSegment(String inputFile, String fileName, 
                                                    double minSupp, double maxSupp,
                                                    double minSuppPercent, double maxSuppPercent) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            AlgoRPGrowth algo = new AlgoRPGrowth();
            Itemsets patterns = algo.runAlgorithm(inputFile, null, maxSupp, minSupp);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = countItemsetsWithCount(patterns);
            
            return new SegmentResult("RP-Growth", minSuppPercent, maxSuppPercent, 
                                    runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testPrePostRareSegment(String inputFile, String fileName, 
                                                       double minSupp, double maxSupp,
                                                       double minSuppPercent, double maxSuppPercent) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            String convertedFile = "temp_prepost_" + System.currentTimeMillis() + ".txt";
            convertItemsetTreeToStandardFormat(inputFile, convertedFile);
            
            String tempOutputPath = "temp_prepost_output_" + System.currentTimeMillis() + ".txt";
            PrePostRare algo = new PrePostRare();
            algo.runAlgorithm(convertedFile, minSupp, maxSupp, tempOutputPath);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = algo.outputCount;
            
            // Cleanup
            new File(convertedFile).delete();
            new File(tempOutputPath).delete();
            
            return new SegmentResult("PrePost Rare", minSuppPercent, maxSuppPercent, 
                                    runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testRareItemsetTreeSegment(String inputFile, String fileName, 
                                                           double minSupp, double maxSupp,
                                                           double minSuppPercent, double maxSuppPercent) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            int transactionCount = countTransactions(inputFile);
            int minRareSupport = (int) Math.ceil((minSupp * transactionCount)) - 1;
            int maxRareSupport = (int) Math.ceil((maxSupp * transactionCount));
            
            RareItemsetTree rareTree = new RareItemsetTree(minRareSupport, maxRareSupport);
            rareTree.buildTree(inputFile);
            
            List<patterns.itemset_array_integers_with_count.Itemset> allRareItemsets = 
                rareTree.mineAllRareItemsetsWithPruning(inputFile, minRareSupport, maxRareSupport);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = allRareItemsets.size();
            
            return new SegmentResult("RareItemsetTree", minSuppPercent, maxSuppPercent, 
                                    runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    // Utility methods (copy from previous versions)
    private static TransactionDatabase convertItemsetTreeToTransactionDatabase(String inputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            try {
                int transactionId = Integer.parseInt(parts[0]);
                int itemId = Integer.parseInt(parts[1]);
                int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
                
                if (count > 0) {
                    transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                    if (!transactionMap.get(transactionId).contains(itemId)) {
                        transactionMap.get(transactionId).add(itemId);
                    }
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        reader.close();
        
        TransactionDatabase database = new TransactionDatabase();
        transactionMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                List<Integer> transaction = entry.getValue();
                transaction.sort(Integer::compareTo);
                database.addTransaction(transaction);
            });
        
        return database;
    }
    
    private static void convertItemsetTreeToStandardFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            try {
                int transactionId = Integer.parseInt(parts[0]);
                int itemId = Integer.parseInt(parts[1]);
                int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
                
                if (count > 0) {
                    transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                    if (!transactionMap.get(transactionId).contains(itemId)) {
                        transactionMap.get(transactionId).add(itemId);
                    }
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        reader.close();
        
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        transactionMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                List<Integer> transaction = entry.getValue();
                transaction.sort(Integer::compareTo);
                for (int i = 0; i < transaction.size(); i++) {
                    if (i > 0) writer.print(" ");
                    writer.print(transaction.get(i));
                }
                writer.println();
            });
        writer.close();
    }
    
    private static int countTransactions(String inputFile) throws IOException {
        Set<Integer> transactionIds = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) continue;
            
            try {
                int transactionId = Integer.parseInt(parts[0]);
                transactionIds.add(transactionId);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        reader.close();
        return transactionIds.size();
    }
    
    private static int countItemsetsWithTids(patterns.itemset_array_integers_with_tids.Itemsets itemsets) {
        int count = 0;
        List<List<patterns.itemset_array_integers_with_tids.Itemset>> levels = itemsets.getLevels();
        for (List<patterns.itemset_array_integers_with_tids.Itemset> level : levels) {
            if (level != null) count += level.size();
        }
        return count;
    }
    
    private static int countItemsetsWithCount(Itemsets itemsets) {
        int count = 0;
        List<List<patterns.itemset_array_integers_with_count.Itemset>> levels = itemsets.getLevels();
        for (List<patterns.itemset_array_integers_with_count.Itemset> level : levels) {
            if (level != null) count += level.size();
        }
        return count;
    }
    
    /**
     * Save segment results to CSV
     */
    private static void saveSegmentResults(List<SegmentResult> results, String fileName) {
        try {
            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter("results/segment_benchmark.csv"));
            writer.println("MinSupport,MaxSupport,Algorithm,Runtime(ms),Memory(MB),ItemsetsFound,Dataset,Segment");
            
            for (SegmentResult result : results) {
                writer.println(result.toString());
            }
            writer.close();
            
            System.out.println("\n💾 Segment results saved: results/segment_benchmark.csv");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save CSV: " + e.getMessage());
        }
    }
    
    /**
     * Generate Python script cho segment chart
     */
    private static void generateSegmentChartScript(List<SegmentResult> results) {
        try {
            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter("results/generate_segment_chart.py"));
            writer.println("import pandas as pd");
            writer.println("import matplotlib.pyplot as plt");
            writer.println("import numpy as np");
            writer.println();
            writer.println("# Load segment data");
            writer.println("df = pd.read_csv('segment_benchmark.csv')");
            writer.println();
            writer.println("# Create chart like research paper");
            writer.println("plt.figure(figsize=(12, 8))");
            writer.println();
            writer.println("# Use midpoint of each segment for x-axis");
            writer.println("df['MidSupport'] = (df['MinSupport'] + df['MaxSupport']) / 2");
            writer.println();
            writer.println("algorithms = df['Algorithm'].unique()");
            writer.println("colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']");
            writer.println("markers = ['o', 's', '^', 'D', 'v']");
            writer.println();
            writer.println("for i, algo in enumerate(algorithms):");
            writer.println("    algo_data = df[df['Algorithm'] == algo].sort_values('MidSupport')");
            writer.println("    plt.plot(algo_data['MidSupport'], algo_data['Runtime(ms)'] / 1000,");
            writer.println("             marker=markers[i % len(markers)],");
            writer.println("             color=colors[i % len(colors)],");
            writer.println("             linewidth=2.5, markersize=8,");
            writer.println("             label=algo)");
            writer.println();
            writer.println("plt.xlabel('Support Range Midpoint (%)', fontsize=12, fontweight='bold')");
            writer.println("plt.ylabel('Runtime (seconds)', fontsize=12, fontweight='bold')");
            writer.println("plt.title('Segment-wise Runtime Analysis - Rare Itemset Mining', fontsize=14, fontweight='bold')");
            writer.println("plt.legend(loc='upper right')");
            writer.println("plt.grid(True, alpha=0.3)");
            writer.println("plt.gca().invert_xaxis()");
            writer.println();
            writer.println("# Add segment labels");
            writer.println("segments = df[['MinSupport', 'MaxSupport']].drop_duplicates().sort_values('MinSupport')");
            writer.println("for _, seg in segments.iterrows():");
            writer.println("    mid = (seg['MinSupport'] + seg['MaxSupport']) / 2");
            writer.println("    plt.axvline(x=mid, color='gray', linestyle='--', alpha=0.3)");
            writer.println("    plt.text(mid, plt.ylim()[1] * 0.95, f\"{seg['MinSupport']:.0f}-{seg['MaxSupport']:.0f}%\",");
            writer.println("             ha='center', va='top', fontsize=9, rotation=90)");
            writer.println();
            writer.println("plt.tight_layout()");
            writer.println("plt.savefig('segment_runtime_analysis.png', dpi=300, bbox_inches='tight')");
            writer.println("plt.show()");
            writer.println();
            writer.println("# Print segment summary");
            writer.println("print('\\n📊 SEGMENT SUMMARY:')");
            writer.println("for _, seg in segments.iterrows():");
            writer.println("    seg_data = df[(df['MinSupport'] == seg['MinSupport']) & (df['MaxSupport'] == seg['MaxSupport'])]");
            writer.println("    avg_runtime = seg_data['Runtime(ms)'].mean()");
            writer.println("    avg_patterns = seg_data['ItemsetsFound'].mean()");
            writer.println("    print(f\"{seg['MinSupport']:.0f}%-{seg['MaxSupport']:.0f}%: {avg_runtime:.0f}ms avg, {avg_patterns:.0f} patterns avg\")");
            
            writer.close();
            
            System.out.println("💾 Python segment script: results/generate_segment_chart.py");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save Python script: " + e.getMessage());
        }
    }
    
    /**
     * Display segment summary
     */
    private static void displaySegmentSummary(List<SegmentResult> results) {
        System.out.println("\n================================================================================");
        System.out.println("📊 SEGMENT BENCHMARK SUMMARY");
        System.out.println("================================================================================");
        
        // Group by segment
        Map<String, List<SegmentResult>> resultsBySegment = new HashMap<>();
        for (SegmentResult result : results) {
            resultsBySegment.computeIfAbsent(result.segmentName, k -> new ArrayList<>()).add(result);
        }
        
        System.out.println("Segment Performance Summary:");
        System.out.println("Segment    | Avg Runtime | Avg Memory | Avg Patterns | Best Algorithm");
        System.out.println("------------------------------------------------------------------------");
        
        for (double[] segment : SUPPORT_SEGMENTS) {
            String segmentName = String.format("%.0f-%.0f%%", segment[0], segment[1]);
            List<SegmentResult> segmentResults = resultsBySegment.get(segmentName);
            
            if (segmentResults != null && !segmentResults.isEmpty()) {
                double avgRuntime = segmentResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
                double avgMemory = segmentResults.stream().mapToDouble(r -> r.memory).average().orElse(0);
                double avgPatterns = segmentResults.stream().mapToInt(r -> r.itemsetsFound).average().orElse(0);
                
                SegmentResult fastest = segmentResults.stream()
                    .min((a, b) -> Long.compare(a.runtime, b.runtime))
                    .orElse(null);
                
                System.out.printf("%-10s | %8.0fms | %8.1fMB | %10.0f | %s\n",
                    segmentName, avgRuntime, avgMemory, avgPatterns, 
                    fastest != null ? fastest.algorithm : "N/A");
            }
        }
        
        System.out.println();
        
        // Algorithm performance across segments
        Map<String, List<SegmentResult>> resultsByAlgo = new HashMap<>();
        for (SegmentResult result : results) {
            resultsByAlgo.computeIfAbsent(result.algorithm, k -> new ArrayList<>()).add(result);
        }
        
        System.out.println("Algorithm Performance Across All Segments:");
        System.out.println("Algorithm         | Avg Runtime | Best Segment | Worst Segment | Consistency");
        System.out.println("--------------------------------------------------------------------------");
        
        for (String algorithm : resultsByAlgo.keySet()) {
            List<SegmentResult> algoResults = resultsByAlgo.get(algorithm);
            
            double avgRuntime = algoResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
            
            SegmentResult best = algoResults.stream()
                .min((a, b) -> Long.compare(a.runtime, b.runtime))
                .orElse(null);
            
            SegmentResult worst = algoResults.stream()
                .max((a, b) -> Long.compare(a.runtime, b.runtime))
                .orElse(null);
            
            // Calculate consistency (lower std deviation = more consistent)
            double stdDev = 0;
            if (algoResults.size() > 1) {
                double mean = avgRuntime;
                double variance = algoResults.stream()
                    .mapToDouble(r -> Math.pow(r.runtime - mean, 2))
                    .average().orElse(0);
                stdDev = Math.sqrt(variance);
            }
            double consistency = stdDev / avgRuntime * 100; // CV percentage
            
            System.out.printf("%-16s | %8.0fms | %9s | %10s | %8.1f%%\n",
                algorithm, avgRuntime, 
                best != null ? best.segmentName : "N/A",
                worst != null ? worst.segmentName : "N/A",
                consistency);
        }
        
        System.out.println();
        System.out.println("💡 Key Insights:");
        
        // Find which segment is generally fastest
        Map<String, Double> segmentAvgRuntimes = new HashMap<>();
        for (double[] segment : SUPPORT_SEGMENTS) {
            String segmentName = String.format("%.0f-%.0f%%", segment[0], segment[1]);
            List<SegmentResult> segmentResults = resultsBySegment.get(segmentName);
            if (segmentResults != null && !segmentResults.isEmpty()) {
                double avgRuntime = segmentResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
                segmentAvgRuntimes.put(segmentName, avgRuntime);
            }
        }
        
        String fastestSegment = segmentAvgRuntimes.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
            
        String slowestSegment = segmentAvgRuntimes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        System.out.println("  ⚡ Fastest segment overall: " + fastestSegment);
        System.out.println("  🐌 Slowest segment overall: " + slowestSegment);
        
        // Find most consistent algorithm
        String mostConsistent = resultsByAlgo.entrySet().stream()
            .min((a, b) -> {
                double cvA = calculateCV(a.getValue());
                double cvB = calculateCV(b.getValue());
                return Double.compare(cvA, cvB);
            })
            .map(Map.Entry::getKey)
            .orElse("N/A");
            
        System.out.println("  📊 Most consistent algorithm: " + mostConsistent);
        
        System.out.println();
        System.out.println("📁 Output files:");
        System.out.println("  📊 CSV data: results/segment_benchmark.csv");
        System.out.println("  🐍 Python script: results/generate_segment_chart.py");
        System.out.println();
        System.out.println("🎯 Next steps:");
        System.out.println("  1. cd results && python generate_segment_chart.py");
        System.out.println("  2. View segment_runtime_analysis.png");
        System.out.println("  3. Compare segment performance patterns");
        
        System.out.println("================================================================================");
    }
    
    /**
     * Calculate coefficient of variation for consistency measurement
     */
    private static double calculateCV(List<SegmentResult> results) {
        if (results.size() < 2) return 0;
        
        double mean = results.stream().mapToLong(r -> r.runtime).average().orElse(0);
        double variance = results.stream()
            .mapToDouble(r -> Math.pow(r.runtime - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        return mean > 0 ? (stdDev / mean * 100) : 0;
    }
}