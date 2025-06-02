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

public class OriginalSegmentBenchmarkFixed {
    
    private static final double[] SUPPORT_POINTS = {
        10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0
    };
    
    public static class SegmentResult {
        public String algorithm;
        public double supportPoint;
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
            return String.format("%.1f,%s,%d,%.2f,%d,%s", 
                supportPoint, algorithm, runtime, memory, itemsetsFound, dataset);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 FIXED SEGMENT BENCHMARK - SINGLE SUPPORT POINTS");
        System.out.println("=====================================================");
        
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
        
        try {
            List<SegmentResult> allResults = runSingleSupportBenchmark(selectedFile, selectedAlgorithms);
            saveSingleSupportResults(allResults, selectedFile.getName());
            generateSingleSupportChartScript();
            displaySingleSupportSummary(allResults);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<SegmentResult> runSingleSupportBenchmark(File dataFile, List<String> algorithms) throws IOException {
        List<SegmentResult> allResults = new ArrayList<>();
        String inputFile = dataFile.getAbsolutePath();
        String fileName = dataFile.getName();
        
        int testCount = 0;
        int totalTests = algorithms.size() * SUPPORT_POINTS.length;
        
        for (double supportPoint : SUPPORT_POINTS) {
            System.out.printf("\n📊 Testing Support Point: %.0f%%\n", supportPoint);
            
            double minSupp = (supportPoint - 2.5) / 100.0;
            double maxSupp = (supportPoint + 2.5) / 100.0;
            if (minSupp < 0) minSupp = 0.001;
            
            for (String algorithm : algorithms) {
                testCount++;
                System.out.printf("[%d/%d] Testing %s at %.0f%% support... ", 
                    testCount, totalTests, algorithm, supportPoint);
                
                try {
                    SegmentResult result = null;
                    
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
                
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
        
        return allResults;
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
            
            
            int itemsets = algo.getOutputCount();
            
            new File(convertedFile).delete();
            new File(tempOutputPath).delete();
            
            return new SegmentResult("PrePost Rare", supportPoint, runtime, memory, itemsets, fileName);
            
        } catch (Exception e) {
            System.err.println("PrePost error: " + e.getMessage());
            return null;
        }
    }
    
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
            
            System.out.println("\n💾 Results saved: results/segment_benchmark.csv");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save CSV: " + e.getMessage());
        }
    }
    
    private static void generateSingleSupportChartScript() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/generate_chart.py"))) {
            writer.println("import pandas as pd");
            writer.println("import matplotlib.pyplot as plt");
            writer.println("import numpy as np");
            writer.println();
            writer.println("df = pd.read_csv('segment_benchmark.csv')");
            writer.println();
            writer.println("plt.figure(figsize=(12, 8))");
            writer.println();
            writer.println("algorithms = df['Algorithm'].unique()");
            writer.println("colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']");
            writer.println("markers = ['o', 's', '^', 'D', 'v']");
            writer.println();
            writer.println("for i, algo in enumerate(algorithms):");
            writer.println("    algo_data = df[df['Algorithm'] == algo].sort_values('SupportPoint')");
            writer.println("    plt.plot(algo_data['SupportPoint'], algo_data['Runtime(ms)'] / 1000,");
            writer.println("             marker=markers[i % len(markers)], color=colors[i % len(colors)],");
            writer.println("             linewidth=2.5, markersize=8, label=algo, alpha=0.8)");
            writer.println();
            writer.println("plt.xlabel('Minimum Support (%)', fontsize=12, fontweight='bold')");
            writer.println("plt.ylabel('Runtime (seconds)', fontsize=12, fontweight='bold')");
            writer.println("plt.title('Runtime Analysis - Rare Itemset Mining', fontsize=14, fontweight='bold')");
            writer.println("plt.legend(loc='upper right')");
            writer.println("plt.grid(True, alpha=0.3)");
            writer.println("plt.gca().invert_xaxis()");
            writer.println("plt.tight_layout()");
            writer.println("plt.savefig('runtime_analysis.png', dpi=300, bbox_inches='tight')");
            writer.println("plt.show()");
            writer.println("print('Chart saved: runtime_analysis.png')");
            
            System.out.println("💾 Python script: results/generate_chart.py");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save Python script: " + e.getMessage());
        }
    }
    
    private static void displaySingleSupportSummary(List<SegmentResult> results) {
        System.out.println("\n================================================================================");
        System.out.println("📊 BENCHMARK SUMMARY");
        System.out.println("================================================================================");
        
        Map<String, List<SegmentResult>> resultsByAlgo = new HashMap<>();
        for (SegmentResult result : results) {
            resultsByAlgo.computeIfAbsent(result.algorithm, k -> new ArrayList<>()).add(result);
        }
        
        System.out.println("Algorithm Performance Summary:");
        System.out.println("Algorithm         | Avg Runtime | Tests | Best Point | Status");
        System.out.println("----------------------------------------------------------");
        
        for (String algorithm : resultsByAlgo.keySet()) {
            List<SegmentResult> algoResults = resultsByAlgo.get(algorithm);
            
            double avgRuntime = algoResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
            int testCount = algoResults.size();
            
            SegmentResult best = algoResults.stream()
                .min((a, b) -> Long.compare(a.runtime, b.runtime))
                .orElse(null);
            
            String status = avgRuntime < 100 ? "Excellent" : 
                           avgRuntime < 500 ? "Good" : 
                           avgRuntime < 1000 ? "Fair" : "Slow";
            
            System.out.printf("%-16s | %8.0fms | %5d | %8.0f%% | %s\n",
                algorithm, avgRuntime, testCount,
                best != null ? best.supportPoint : 0, status);
        }
        
        System.out.println("\n📁 Output files:");
        System.out.println("  📊 results/segment_benchmark.csv");
        System.out.println("  🐍 results/generate_chart.py");
        System.out.println("\n🎯 Run: cd results && python generate_chart.py");
    }
}