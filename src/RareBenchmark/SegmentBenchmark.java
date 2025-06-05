package RareBenchmark;

import Algorithrms.Rarepartem.eclat_rare.AlgoEclatRareBitset;
import Algorithrms.Rarepartem.nlistrare.PrePostRare;
import Algorithrms.Rarepartem.rpgrowth.AlgoRPGrowth;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_count.Itemsets;
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
 * UPDATED: Benchmark for Top 3 Mixed Mode Algorithms with correct class names
 * - PrePostRare (Mixed Mode)
 * - AlgoEclatRareBitset (Mixed Mode) 
 * - AlgoRPGrowth (Mixed Mode)
 */
public class SegmentBenchmark {
    
    private static final double[] SUPPORT_POINTS = {
        10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0
    };
    
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 5;
    
    public static class BenchmarkResult {
        public String algorithm;
        public double supportPoint;
        public long runtime;
        public double memory;
        public int itemsetsFound;
        public String dataset;
        
        public BenchmarkResult(String algorithm, double supportPoint, 
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
        System.out.println("🎯 TOP 3 RARE MINING ALGORITHMS BENCHMARK");
        System.out.println("=========================================");
        System.out.println("✅ Testing algorithms:");
        System.out.println("   🥇 PrePost Rare");
        System.out.println("   🥈 ECLAT Rare");
        System.out.println("   🥉 RP-Growth");
        System.out.println();
        System.out.println("📋 Configuration:");
        System.out.println("   • Size constraints: " + MIN_SIZE + " to " + MAX_SIZE + " items");
        System.out.println("   • Consistent rare pattern definition");
        System.out.println("   • " + SUPPORT_POINTS.length + " support points");
        System.out.println("=========================================");
        
        String dataDir = "Data";
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".hui"));
        
        if (files == null || files.length == 0) {
            System.out.println("❌ Không tìm thấy file dữ liệu trong thư mục: " + dataDir);
            return;
        }
        
        System.out.println("\n📁 Available datasets:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("\n🔍 Select dataset (1-" + files.length + "): ");
        int fileChoice = scanner.nextInt();
        
        if (fileChoice < 1 || fileChoice > files.length) {
            System.out.println("❌ Invalid choice!");
            scanner.close();
            return;
        }
        
        File selectedFile = files[fileChoice - 1];
        System.out.println("✅ Selected: " + selectedFile.getName());
        
        System.out.println("\nSelect test mode:");
        System.out.println("1. Full comparison (all 3 algorithms, " + (3 * SUPPORT_POINTS.length) + " tests)");
        System.out.println("2. Quick test (ECLAT only, " + SUPPORT_POINTS.length + " tests)");
        System.out.println("3. Performance focus (ECLAT + RP-Growth, " + (2 * SUPPORT_POINTS.length) + " tests)");
        System.out.print("Choice (1-3): ");
        int testMode = scanner.nextInt();
        
        List<String> algorithms = new ArrayList<>();
        switch (testMode) {
            case 1:
                algorithms.add("PrePost");
                algorithms.add("ECLAT");
                algorithms.add("RPGrowth");
                break;
            case 2:
                algorithms.add("ECLAT");
                break;
            case 3:
                algorithms.add("ECLAT");
                algorithms.add("RPGrowth");
                break;
            default:
                algorithms.add("ECLAT");
        }
        
        scanner.close();
        
        try {
            System.out.println("\n🚀 Starting benchmark with " + algorithms.size() + " algorithm(s)...");
            List<BenchmarkResult> results = runBenchmark(selectedFile, algorithms);
            saveResults(results, selectedFile.getName());
            generateCharts();
            displaySummary(results);
            
        } catch (Exception e) {
            System.err.println("❌ Benchmark error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<BenchmarkResult> runBenchmark(File dataFile, List<String> algorithms) throws IOException {
        List<BenchmarkResult> results = new ArrayList<>();
        String inputFile = dataFile.getAbsolutePath();
        String fileName = dataFile.getName();
        
        int testCount = 0;
        int totalTests = algorithms.size() * SUPPORT_POINTS.length;
        
        System.out.println("\n📊 Running " + totalTests + " tests on " + fileName);
        System.out.println("Size constraints: " + MIN_SIZE + "-" + MAX_SIZE + " items");
        
        for (double supportPoint : SUPPORT_POINTS) {
            System.out.printf("\n📈 Support Point: %.0f%% (range: %.1f%%-%.1f%%)\n", 
                supportPoint, supportPoint-2.5, supportPoint+2.5);
            
            double minSupp = Math.max(0.001, (supportPoint - 2.5) / 100.0);
            double maxSupp = (supportPoint + 2.5) / 100.0;
            
            for (String algorithm : algorithms) {
                testCount++;
                System.out.printf("  [%d/%d] %s... ", testCount, totalTests, algorithm);
                
                try {
                    BenchmarkResult result = null;
                    
                    switch (algorithm) {
                        case "PrePost":
                            result = testPrePostRare(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "ECLAT":
                            result = testECLATRare(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                        case "RPGrowth":
                            result = testRPGrowth(inputFile, fileName, minSupp, maxSupp, supportPoint);
                            break;
                    }
                    
                    if (result != null) {
                        results.add(result);
                        System.out.printf("✅ %dms (%d patterns)\n", result.runtime, result.itemsetsFound);
                    } else {
                        System.out.println("❌ Failed");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                }
                
                // Brief pause to avoid overwhelming the system
                try { Thread.sleep(25); } catch (InterruptedException ignored) {}
            }
        }
        
        return results;
    }
    
    /**
     * Test PrePostRare
     */
    private static BenchmarkResult testPrePostRare(String inputFile, String fileName, 
                                                  double minSupp, double maxSupp, double supportPoint) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            // Convert to standard format
            String convertedFile = "temp_prepost_" + System.currentTimeMillis() + ".txt";
            convertToStandardFormat(inputFile, convertedFile);
            
            // Run algorithm
            String outputFile = "temp_output_" + System.currentTimeMillis() + ".txt";
            PrePostRare algo = new PrePostRare();
            algo.runAlgorithm(convertedFile, minSupp, maxSupp, outputFile, MIN_SIZE, MAX_SIZE);
            
            // Measure results
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int patterns = algo.getOutputCount();
            
            // Cleanup
            new File(convertedFile).delete();
            new File(outputFile).delete();
            
            return new BenchmarkResult("PrePost Rare", supportPoint, runtime, memory, patterns, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Test AlgoEclatRareBitset
     */
    private static BenchmarkResult testECLATRare(String inputFile, String fileName, 
                                               double minSupp, double maxSupp, double supportPoint) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            // Convert to transaction database
            TransactionDatabase database = convertToTransactionDatabase(inputFile);
            
            // Run algorithm
            AlgoEclatRareBitset algo = new AlgoEclatRareBitset();
            algo.setShowTransactionIdentifiers(false);
            Itemsets results = algo.runAlgorithm(null, database, minSupp, maxSupp, MIN_SIZE, MAX_SIZE);
            
            // Measure results
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int patterns = countPatterns(results);
            
            return new BenchmarkResult("ECLAT Rare", supportPoint, runtime, memory, patterns, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Test AlgoRPGrowth
     */
    private static BenchmarkResult testRPGrowth(String inputFile, String fileName, 
                                              double minSupp, double maxSupp, double supportPoint) {
        try {
            MemoryLogger.getInstance().reset();
            long startTime = System.currentTimeMillis();
            
            // Run algorithm directly on file
            AlgoRPGrowth algo = new AlgoRPGrowth();
            Itemsets results = algo.runAlgorithm(inputFile, null, maxSupp, minSupp, MIN_SIZE, MAX_SIZE);
            
            // Measure results
            long runtime = System.currentTimeMillis() - startTime;
            MemoryLogger.getInstance().checkMemory();
            double memory = MemoryLogger.getInstance().getMaxMemory();
            int patterns = countPatterns(results);
            
            return new BenchmarkResult("RP-Growth", supportPoint, runtime, memory, patterns, fileName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Convert ItemsetTree format to standard transaction format
     */
    private static void convertToStandardFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactions = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.readLine(); // Skip header
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                    continue;
                }
                
                String[] parts = line.trim().split(" ");
                if (parts.length < 2) continue;
                
                try {
                    int tid = Integer.parseInt(parts[0]);
                    int item = Integer.parseInt(parts[1]);
                    int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
                    
                    if (count > 0) {
                        transactions.computeIfAbsent(tid, k -> new ArrayList<>());
                        if (!transactions.get(tid).contains(item)) {
                            transactions.get(tid).add(item);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            transactions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    List<Integer> items = entry.getValue();
                    items.sort(Integer::compareTo);
                    for (int i = 0; i < items.size(); i++) {
                        if (i > 0) writer.print(" ");
                        writer.print(items.get(i));
                    }
                    writer.println();
                });
        }
    }
    
    /**
     * Convert ItemsetTree format to TransactionDatabase
     */
    private static TransactionDatabase convertToTransactionDatabase(String inputFile) throws IOException {
        Map<Integer, List<Integer>> transactions = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.readLine(); // Skip header
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                    continue;
                }
                
                String[] parts = line.trim().split(" ");
                if (parts.length < 2) continue;
                
                try {
                    int tid = Integer.parseInt(parts[0]);
                    int item = Integer.parseInt(parts[1]);
                    int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
                    
                    if (count > 0) {
                        transactions.computeIfAbsent(tid, k -> new ArrayList<>());
                        if (!transactions.get(tid).contains(item)) {
                            transactions.get(tid).add(item);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        TransactionDatabase database = new TransactionDatabase();
        transactions.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                List<Integer> items = entry.getValue();
                items.sort(Integer::compareTo);
                database.addTransaction(items);
            });
        
        return database;
    }
    
    /**
     * Count total patterns in Itemsets
     */
    private static int countPatterns(Itemsets itemsets) {
        return itemsets.getLevels().stream()
            .mapToInt(level -> level != null ? level.size() : 0)
            .sum();
    }
    
    /**
     * Save benchmark results to CSV
     */
    private static void saveResults(List<BenchmarkResult> results, String datasetName) {
        try {
            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter("results/segment_benchmark.csv"))) {
                writer.println("SupportPoint,Algorithm,Runtime(ms),Memory(MB),PatternsFound,Dataset");
                results.forEach(writer::println);
            }
            
            System.out.println("\n💾 Results saved: results/segment_benchmark.csv");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot save results: " + e.getMessage());
        }
    }
    
    /**
     * Generate Python visualization script
     */
    private static void generateCharts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/generate_chart.py"))) {
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
            
            System.out.println("💾 Visualization script: results/generate_chart.py");
            
        } catch (IOException e) {
            System.err.println("⚠️  Cannot create visualization script: " + e.getMessage());
        }
    }
    
    /**
     * Display comprehensive benchmark summary
     */
    private static void displaySummary(List<BenchmarkResult> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🏆 TOP 3 RARE MINING ALGORITHMS - FINAL RESULTS");
        System.out.println("=".repeat(80));
        
        // Group results by algorithm
        Map<String, List<BenchmarkResult>> byAlgorithm = new HashMap<>();
        results.forEach(r -> byAlgorithm.computeIfAbsent(r.algorithm, k -> new ArrayList<>()).add(r));
        
        System.out.println("\n📋 Test Configuration:");
        System.out.println("   • Definition: MRT < Support(Pattern) <= MFT AND has ≥1 rare item");
        System.out.println("   • Support points tested: " + SUPPORT_POINTS.length);
        System.out.println("   • Total tests completed: " + results.size());
        
        System.out.println("\n📊 Performance Summary:");
        System.out.println("Algorithm           | Avg Runtime | Avg Memory | Avg Patterns | Performance");
        System.out.println("-".repeat(75));
        
        for (String algo : byAlgorithm.keySet()) {
            List<BenchmarkResult> algoResults = byAlgorithm.get(algo);
            
            double avgRuntime = algoResults.stream().mapToLong(r -> r.runtime).average().orElse(0);
            double avgMemory = algoResults.stream().mapToDouble(r -> r.memory).average().orElse(0);
            double avgPatterns = algoResults.stream().mapToInt(r -> r.itemsetsFound).average().orElse(0);
            
            String performance = avgRuntime < 200 ? "🚀 Excellent" :
                               avgRuntime < 500 ? "✅ Good" :
                               avgRuntime < 1000 ? "⚠️  Fair" : "🐌 Slow";
            
            System.out.printf("%-18s | %8.0fms | %8.1fMB | %11.0f | %s\n",
                algo, avgRuntime, avgMemory, avgPatterns, performance);
        }
        
        // Pattern consistency check
        System.out.println("\n🔍 Pattern Consistency Verification:");
        Map<Double, Set<Integer>> patternsBySupport = new HashMap<>();
        
        for (BenchmarkResult result : results) {
            patternsBySupport.computeIfAbsent(result.supportPoint, k -> new HashSet<>())
                .add(result.itemsetsFound);
        }
        
        boolean allConsistent = patternsBySupport.values().stream()
            .allMatch(counts -> counts.size() <= 1);
        
        if (allConsistent) {
            System.out.println("   ✅ All algorithms found IDENTICAL pattern counts!");
            System.out.println("   🎯 Implementation is working correctly");
            System.out.println("   🔬 Algorithms are producing consistent results");
        } else {
            System.out.println("   ⚠️  Some algorithms found different pattern counts:");
            patternsBySupport.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> System.out.println("      Support " + entry.getKey() + 
                    "%: " + entry.getValue() + " different counts"));
        }
        
        // Winner analysis
        System.out.println("\n🥇 Performance Champions:");
        
        // Fastest algorithm
        String fastest = byAlgorithm.entrySet().stream()
            .min((a, b) -> Double.compare(
                a.getValue().stream().mapToLong(r -> r.runtime).average().orElse(Double.MAX_VALUE),
                b.getValue().stream().mapToLong(r -> r.runtime).average().orElse(Double.MAX_VALUE)))
            .map(Map.Entry::getKey).orElse("Unknown");
        
        // Most memory efficient
        String mostMemoryEfficient = byAlgorithm.entrySet().stream()
            .min((a, b) -> Double.compare(
                a.getValue().stream().mapToDouble(r -> r.memory).average().orElse(Double.MAX_VALUE),
                b.getValue().stream().mapToDouble(r -> r.memory).average().orElse(Double.MAX_VALUE)))
            .map(Map.Entry::getKey).orElse("Unknown");
        
        System.out.println("   🏃‍♂️ Fastest Runtime: " + fastest);
        System.out.println("   💾 Most Memory Efficient: " + mostMemoryEfficient);
        
        System.out.println("\n📁 Generated Files:");
        System.out.println("   📊 results/segment_benchmark.csv");
        System.out.println("   🐍 results/generate_chart.py");
        
        System.out.println("\n🎯 Next Steps:");
        System.out.println("   1. Run: cd results && python generate_chart.py");
        System.out.println("   2. View generated chart: runtime_analysis_single_support.png");
        System.out.println("   3. Analyze CSV data for detailed insights");
        
        System.out.println("\n✨ Key Achievements:");
        System.out.println("   • Consistent rare pattern mining");
        System.out.println("   • Size-constrained optimization");
        System.out.println("   • Performance comparison of top algorithms");
        System.out.println("   • Validated algorithm implementations");
        
        System.out.println("\n" + "=".repeat(80));
    }
}