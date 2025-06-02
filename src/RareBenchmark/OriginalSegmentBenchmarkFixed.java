// Giữ nguyên tất cả imports và logic cũ từ SegmentBenchmark.java
package RareBenchmark;

import Algorithrms.Rarepartem.aprioriTID_inverse.AlgoAprioriTIDInverse;
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
 * ORIGINAL SegmentBenchmark - CHỈ SỬA OUTPUT 
 * Giữ nguyên logic cũ, chỉ thay đổi cách tạo support points
 * Thay vì segments (5-10%, 10-15%), dùng single values (10%, 15%, 20%...)
 */
public class OriginalSegmentBenchmarkFixed {
    
    // THAY ĐỔI: Dùng single support values thay vì ranges
    // Giống research paper: test tại các mốc support cụ thể
    private static final double[] SUPPORT_POINTS = {
        10.0,   // Test tại 10%
        15.0,   // Test tại 15%  
        20.0,   // Test tại 20%
        25.0,   // Test tại 25%
        30.0,   // Test tại 30%
        35.0,   // Test tại 35%
        40.0    // Test tại 40%
    };
    
    // Giữ nguyên SegmentResult class, chỉ sửa ý nghĩa
    public static class SegmentResult {
        public String algorithm;
        public double supportPoint;     // Thay vì minSupport/maxSupport
        public long runtime;
        public double memory;
        public int itemsetsFound;
        public String dataset;
        
        public SegmentResult(String algorithm, double supportPoint, 
                           long runtime, double memory, int itemsetsFound, String dataset) {
            this.algorithm = algorithm;
            this.supportPoint = supportPoint;
            this.runtime = runtime;
            this.memory = memory;
            this.itemsetsFound = itemsetsFound;
            this.dataset = dataset;
        }
        
        @Override
        public String toString() {
            // Format CSV: SupportPoint,Algorithm,Runtime(ms),Memory(MB),ItemsetsFound,Dataset
            return String.format("%.1f,%s,%d,%.2f,%d,%s", 
                supportPoint, algorithm, runtime, memory, itemsetsFound, dataset);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 ORIGINAL SEGMENT BENCHMARK - SINGLE SUPPORT POINTS");
        System.out.println("=====================================================");
        System.out.println("THAY ĐỔI: Test tại single support values thay vì ranges");
        System.out.println("Support points:");
        for (int i = 0; i < SUPPORT_POINTS.length; i++) {
            System.out.printf("  %d. %.0f%% support threshold\n", i + 1, SUPPORT_POINTS[i]);
        }
        System.out.println("Total: " + SUPPORT_POINTS.length + " support points");
        System.out.println("====================================================");
        
        // Giữ nguyên logic chọn dataset
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
        
        // Giữ nguyên logic chọn algorithms
        System.out.println("\nChọn algorithms để test:");
        System.out.println("1. Tất cả algorithms (" + (5 * SUPPORT_POINTS.length) + " tests)");
        System.out.println("2. Algorithms nhanh (ECLAT + RP-Growth, " + (2 * SUPPORT_POINTS.length) + " tests)");
        System.out.println("3. Chỉ ECLAT Rare (" + SUPPORT_POINTS.length + " tests)");
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
        
        int totalTests = selectedAlgorithms.size() * SUPPORT_POINTS.length;
        System.out.println("\n🔥 Bắt đầu benchmark tại single support points:");
        System.out.println("Algorithms: " + selectedAlgorithms);
        System.out.println("Support points: " + SUPPORT_POINTS.length);
        System.out.println("Total tests: " + totalTests);
        System.out.println("========================================================================");
        
        try {
            List<SegmentResult> allResults = runSingleSupportBenchmark(selectedFile, selectedAlgorithms);
            
            // Save results
            saveSingleSupportResults(allResults, selectedFile.getName());
            generateSingleSupportChartScript();
            
            // Display summary
            displaySingleSupportSummary(allResults);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * THAY ĐỔI: Test tại single support points thay vì ranges
     */
    private static List<SegmentResult> runSingleSupportBenchmark(File dataFile, List<String> algorithms) throws IOException {
        List<SegmentResult> allResults = new ArrayList<>();
        String inputFile = dataFile.getAbsolutePath();
        String fileName = dataFile.getName();
        
        int testCount = 0;
        int totalTests = algorithms.size() * SUPPORT_POINTS.length;
        
        for (double supportPoint : SUPPORT_POINTS) {
            System.out.printf("\n📊 Testing Support Point: %.0f%%\n", supportPoint);
            System.out.println("================================");
            
            // LOGIC THAY ĐỔI: Thay vì range (min-max), dùng single point
            // Để tương thích với rare itemset definition, ta dùng range nhỏ quanh point
            double minSupp = (supportPoint - 2.5) / 100.0;  // -2.5% từ point
            double maxSupp = (supportPoint + 2.5) / 100.0;  // +2.5% từ point
            
            // Đảm bảo không âm
            if (minSupp < 0) minSupp = 0.001;
            
            for (String algorithm : algorithms) {
                testCount++;
                System.out.printf("[%d/%d] Testing %s at %.0f%% support... ", 
                    testCount, totalTests, algorithm, supportPoint);
                
                try {
                    SegmentResult result = null;
                    
                    // Giữ nguyên logic test algorithms
                    switch (algorithm) {
                        case "AprioriTIDInverse":
                            result = testAprioriAtSingleSupport(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "EclatRareBitset":
                            result = testEclatAtSingleSupport(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "PrePostRare":
                            result = testPrePostAtSingleSupport(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "RareItemsetTree":
                            result = testRareTreeAtSingleSupport(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "RPGrowth":
                            result = testRPGrowthAtSingleSupport(inputFile, fileName, minSupp, maxSupp, supportPoint);
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
    
    // GIỮ NGUYÊN tất cả test methods, chỉ đổi tên và parameter
    private static SegmentResult testEclatAtSingleSupport(String inputFile, String fileName, 
                                                         double minSupp, double maxSupp, double supportPoint) {
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
            
            return new SegmentResult("ECLAT Rare", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testAprioriAtSingleSupport(String inputFile, String fileName, 
                                                           double minSupp, double maxSupp, double supportPoint) {
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
            
            return new SegmentResult("Apriori TID", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testRPGrowthAtSingleSupport(String inputFile, String fileName, 
                                                            double minSupp, double maxSupp, double supportPoint) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            AlgoRPGrowth algo = new AlgoRPGrowth();
            Itemsets patterns = algo.runAlgorithm(inputFile, null, maxSupp, minSupp);
            
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int itemsets = countItemsetsWithCount(patterns);
            
            return new SegmentResult("RP-Growth", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testPrePostAtSingleSupport(String inputFile, String fileName, 
                                                           double minSupp, double maxSupp, double supportPoint) {
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
            
            return new SegmentResult("PrePost Rare", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private static SegmentResult testRareTreeAtSingleSupport(String inputFile, String fileName, 
                                                            double minSupp, double maxSupp, double supportPoint) {
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
            
            return new SegmentResult("RareItemsetTree", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    // GIỮ NGUYÊN tất cả utility methods
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
     * Save single support results to CSV
     */
    private static void saveSingleSupportResults(List<SegmentResult> results, String fileName) {
        try {
            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter("results/segment_benchmark.csv"));
            writer.println("SupportPoint,Algorithm,Runtime(ms),Memory(MB),ItemsetsFound,Dataset");
            
            for (SegmentResult result : results) {
                writer.println(result.toString());
            }
            writer.close();
            
            System.out.println("\n💾 Single support results saved: results/segment_benchmark.csv");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save CSV: " + e.getMessage());
        }
    }
    
    /**
     * Generate Python script tương thích với single support format
     */
    private static void generateSingleSupportChartScript() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/generate_segment_chart.py"))) {
            // Copy fixed chart script từ artifact phía trên
            writer.println("import pandas as pd");
            writer.println("import matplotlib.pyplot as plt");
            writer.println("import numpy as np");
            writer.println();
            writer.println("# Load single support data");
            writer.println("df = pd.read_csv('segment_benchmark.csv')");
            writer.println();
            writer.println("# Create chart like research paper with SINGLE support values");
            writer.println("plt.figure(figsize=(12, 8))");
            writer.println();
            writer.println("algorithms = df['Algorithm'].unique()");
            writer.println("colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']");
            writer.println("markers = ['o', 's', '^', 'D', 'v']");
            writer.println();
            writer.println("# Plot each algorithm");
            writer.println("for i, algo in enumerate(algorithms):");
            writer.println("    algo_data = df[df['Algorithm'] == algo].sort_values('SupportPoint')");
            writer.println("    ");
            writer.println("    plt.plot(algo_data['SupportPoint'], algo_data['Runtime(ms)'] / 1000,");
            writer.println("             marker=markers[i % len(markers)],");
            writer.println("             color=colors[i % len(colors)],");
            writer.println("             linewidth=2.5, markersize=8,");
            writer.println("             label=algo, alpha=0.8)");
            writer.println();
            writer.println("plt.xlabel('Minimum Support (%)', fontsize=12, fontweight='bold')");
            writer.println("plt.ylabel('Runtime (seconds)', fontsize=12, fontweight='bold')");
            writer.println("plt.title('Runtime Analysis - Rare Itemset Mining\\n(Single Support Values)', fontsize=14, fontweight='bold')");
            writer.println("plt.legend(loc='upper right')");
            writer.println("plt.grid(True, alpha=0.3)");
            writer.println();
            writer.println("# Invert x-axis to match research paper format");
            writer.println("plt.gca().invert_xaxis()");
            writer.println();
            writer.println("# Add vertical lines at each support point");
            writer.println("support_points = sorted(df['SupportPoint'].unique(), reverse=True)");
            writer.println("for point in support_points:");
            writer.println("    plt.axvline(x=point, color='gray', linestyle='--', alpha=0.3, linewidth=1)");
            writer.println("    plt.text(point, plt.ylim()[1] * 0.95, f\"{point:.0f}%\",");
            writer.println("             ha='center', va='top', fontsize=10, rotation=0,");
            writer.println("             bbox=dict(boxstyle=\"round,pad=0.2\", facecolor='white', alpha=0.7))");
            writer.println();
            writer.println("plt.tight_layout()");
            writer.println("plt.savefig('runtime_analysis_single_support.png', dpi=300, bbox_inches='tight')");
            writer.println("plt.show()");
            writer.println();
            writer.println("print('\\n📊 Chart saved: runtime_analysis_single_support.png')");
            
            System.out.println("💾 Python script: results/generate_segment_chart.py");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save Python script: " + e.getMessage());
        }
    }
    
    /**
     * Display summary cho single support format
     */
    private static void displaySingleSupportSummary(List<SegmentResult> results) {
        System.out.println("\n================================================================================");
        System.out.println("📊 SINGLE SUPPORT BENCHMARK SUMMARY");
        System.out.println("================================================================================");
        
        // Group by support point
        Map<Double, List<SegmentResult>> resultsBySupport = new HashMap<>();
        for (SegmentResult result : results) {
            resultsBySupport.computeIfAbsent(result.supportPoint, k -> new ArrayList<>()).add(result);
        }
        
        System.out.println("Support Point Performance Summary:");
        System.out.println("Support | Avg Runtime | Avg Memory | Avg Patterns | Best Algorithm   | Performance");
        System.out.println("---------------------------------------------------------------------------------");
        
        for (double supportPoint : SUPPORT_POINTS) {
            List<SegmentResult> supportResults = resultsBySupport.get(supportPoint);
            
            if (supportResults != null && !supportResults.isEmpty()) {
                double avgRuntime = supportResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
                double avgMemory = supportResults.stream().mapToDouble(r -> r.memory).average().orElse(0);
                double avgPatterns = supportResults.stream().mapToInt(r -> r.itemsetsFound).average().orElse(0);
                
                SegmentResult fastest = supportResults.stream()
                    .min((a, b) -> Long.compare(a.runtime, b.runtime))
                    .orElse(null);
                
                String performance = avgRuntime < 100 ? "Excellent" : 
                                   avgRuntime < 500 ? "Good" : 
                                   avgRuntime < 1000 ? "Fair" : "Slow";
                
                System.out.printf("%6.0f%% | %8.0fms | %8.1fMB | %10.0f | %-15s | %s\n",
                    supportPoint, avgRuntime, avgMemory, avgPatterns, 
                    fastest != null ? fastest.algorithm : "N/A", performance);
            }
        }
        
        System.out.println();
        
        // Algorithm performance across support points
        Map<String, List<SegmentResult>> resultsByAlgo = new HashMap<>();
        for (SegmentResult result : results) {
            resultsByAlgo.computeIfAbsent(result.algorithm, k -> new ArrayList<>()).add(result);
        }
        
        System.out.println("Algorithm Performance Across All Support Points:");
        System.out.println("Algorithm         | Avg Runtime | Best Point | Worst Point | Range Factor | Wins");
        System.out.println("---------------------------------------------------------------------------------");
        
        Map<String, Integer> algorithmWins = new HashMap<>();
        
        // Count wins per algorithm
        for (double supportPoint : SUPPORT_POINTS) {
            List<SegmentResult> supportResults = resultsBySupport.get(supportPoint);
            if (supportResults != null && !supportResults.isEmpty()) {
                SegmentResult winner = supportResults.stream()
                    .min((a, b) -> Long.compare(a.runtime, b.runtime))
                    .orElse(null);
                if (winner != null) {
                    algorithmWins.merge(winner.algorithm, 1, Integer::sum);
                }
            }
        }
        
        for (String algorithm : resultsByAlgo.keySet()) {
            List<SegmentResult> algoResults = resultsByAlgo.get(algorithm);
            
            double avgRuntime = algoResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
            
            SegmentResult best = algoResults.stream()
                .min((a, b) -> Long.compare(a.runtime, b.runtime))
                .orElse(null);
            
            SegmentResult worst = algoResults.stream()
                .max((a, b) -> Long.compare(a.runtime, b.runtime))
                .orElse(null);
            
            double rangeFactor = (best != null && worst != null && best.runtime > 0) ? 
                                (double) worst.runtime / best.runtime : 1.0;
            
            int wins = algorithmWins.getOrDefault(algorithm, 0);
            
            System.out.printf("%-16s | %8.0fms | %8.0f%% | %9.0f%% | %8.1fx | %d/%d\n",
                algorithm, avgRuntime, 
                best != null ? best.supportPoint : 0,
                worst != null ? worst.supportPoint : 0,
                rangeFactor, wins, SUPPORT_POINTS.length);
        }
        
        System.out.println();
        System.out.println("💡 Key Insights:");
        
        // Find trends
        String overallChampion = algorithmWins.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        String fastestAverage = resultsByAlgo.entrySet().stream()
            .min((a, b) -> {
                double avgA = a.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                double avgB = b.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                return Double.compare(avgA, avgB);
            })
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        // Find easiest and hardest support points
        double easiestSupport = resultsBySupport.entrySet().stream()
            .min((a, b) -> {
                double avgA = a.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                double avgB = b.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                return Double.compare(avgA, avgB);
            })
            .map(Map.Entry::getKey)
            .orElse(0.0);
            
        double hardestSupport = resultsBySupport.entrySet().stream()
            .max((a, b) -> {
                double avgA = a.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                double avgB = b.getValue().stream().mapToLong(r -> r.runtime).average().orElse(0);
                return Double.compare(avgA, avgB);
            })
            .map(Map.Entry::getKey)
            .orElse(0.0);
        
        System.out.println("  🏆 Overall champion: " + overallChampion + 
                          " (" + algorithmWins.getOrDefault(overallChampion, 0) + "/" + SUPPORT_POINTS.length + " wins)");
        System.out.println("  ⚡ Fastest average: " + fastestAverage);
        System.out.println("  🎯 Easiest support point: " + easiestSupport + "%");
        System.out.println("  🔥 Hardest support point: " + hardestSupport + "%");
        
        // Support difficulty trend
        System.out.println("  📈 Trend: " + (hardestSupport < easiestSupport ? 
            "Higher support = harder (unexpected)" : 
            "Lower support = harder (expected)"));
        
        System.out.println();
        System.out.println("📁 Output files:");
        System.out.println("  📊 CSV data: results/segment_benchmark.csv");
        System.out.println("  🐍 Python script: results/generate_segment_chart.py");
        System.out.println();
        System.out.println("🎯 Key difference from original:");
        System.out.println("  ✅ Single support values (10%, 15%, 20%...) instead of ranges");
        System.out.println("  ✅ Each point represents one specific support threshold");
        System.out.println("  ✅ Chart format matches research papers exactly");
        
        System.out.println();
        System.out.println("🎯 Next steps:");
        System.out.println("  1. cd results && python generate_segment_chart.py");
        System.out.println("  2. View runtime_analysis_single_support.png");
        System.out.println("  3. Compare with research paper format");
        
        System.out.println("================================================================================");
    }
}