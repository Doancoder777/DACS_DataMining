package TEST;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import Algorithrms.Rarepartem.nlistrare.PrePostRare;
import tools.MemoryLogger;

public class MainTestEclatRareMixed {

    public static void main(String[] args) throws IOException {

        MemoryLogger.getInstance().reset();
        long startTime = System.currentTimeMillis();

        String dataDir = "Data";
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) ->
            name.endsWith(".txt") || name.endsWith(".hui"));

        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy tệp dữ liệu trong thư mục: " + dataDir);
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== PREPOST RARE - RARE PATTERN MINING ===");
        System.out.println("ĐỊNH NGHĨA:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("- Rare ItemSet: MRT < Support(Pattern) <= MFT AND có ít nhất 1 rare item");
        System.out.println("=======================================================");

        System.out.println("\nDanh sách tệp dữ liệu:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Chọn file (1-" + files.length + "): ");
        int choice = scanner.nextInt();
        while (choice < 1 || choice > files.length) {
            System.out.print("Lựa chọn không hợp lệ. Chọn lại (1-" + files.length + "): ");
            choice = scanner.nextInt();
        }

        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn: " + fileName);

        System.out.print("MinRareSupport - minsup (%): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("Không hợp lệ. Nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }

        System.out.print("MinFrequentSupport - maxsup (%): ");
        double maxsupPercent = scanner.nextDouble();
        while (maxsupPercent < minsupPercent || maxsupPercent > 100) {
            System.out.print("Không hợp lệ (>= minsup). Nhập lại: ");
            maxsupPercent = scanner.nextDouble();
        }

        System.out.print("MinSize (>=1): ");
        int minSize = scanner.nextInt();
        while (minSize < 1) {
            System.out.print("Không hợp lệ (>= 1). Nhập lại: ");
            minSize = scanner.nextInt();
        }

        System.out.print("MaxSize (>= MinSize): ");
        int maxSize = scanner.nextInt();
        while (maxSize < minSize) {
            System.out.print("Không hợp lệ (>= " + minSize + "). Nhập lại: ");
            maxSize = scanner.nextInt();
        }

        File releaseDir = new File("release");
        if (!releaseDir.exists()) releaseDir.mkdirs();

        File prepostDir = new File("release/PrePostRare");
        if (!prepostDir.exists()) prepostDir.mkdirs();

        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        String outputPath = prepostDir.getAbsolutePath() + "/" + outputBaseName +
                           "_PrePostRare_" + (int)minsupPercent + "_" + (int)maxsupPercent +
                           "_size" + minSize + "to" + maxSize + ".txt";

        try {
            runPrePostRare(input, outputPath, minsupPercent/100.0, maxsupPercent/100.0,
                          minSize, maxSize, fileName, startTime, minsupPercent, maxsupPercent);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void runPrePostRare(String input, String outputPath, double minsup, double maxsup,
                                      int minSize, int maxSize, String fileName, long startTime,
                                      double minsupPercent, double maxsupPercent) throws IOException {

        System.out.println("\n=== EXECUTING PREPOST RARE ===");
        System.out.println("Output: " + outputPath);

        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));

        writer.println("=== PREPOST RARE - RARE ITEMSETS MINING ===");
        writer.println("File: " + fileName);
        writer.println("MinRareSupport: " + minsupPercent + "%");
        writer.println("MaxSupport: " + maxsupPercent + "%");
        writer.println("Size range: [" + minSize + ", " + maxSize + "]");
        writer.println("Start time: " + new java.util.Date(startTime));
        writer.println();

        System.out.println("Converting data format...");
        String convertedFile = "temp_converted_" + System.currentTimeMillis() + ".txt";
        int transactionCount = convertItemsetTreeToStandardFormat(input, convertedFile);

        writer.println("========== DATABASE INFO ==========");
        writer.println("Transactions: " + transactionCount);
        writer.println();

        String tempOutput = "temp_prepost_" + System.currentTimeMillis() + ".txt";
        PrePostRare algo = new PrePostRare();

        System.out.println("Running PrePost Rare...");
        algo.runAlgorithm(convertedFile, minsup, maxsup, tempOutput, minSize, maxSize);
        algo.printStats();

        writeFormattedResults(writer, tempOutput, transactionCount, minsup, maxsup,
                             minSize, maxSize, algo.getOutputCount());

        writePerformanceStats(writer, startTime);

        writer.close();
        new File(convertedFile).delete();
        new File(tempOutput).delete();

        System.out.println("=== COMPLETED ===");
        System.out.println("Results saved to: " + outputPath);
    }

    private static int convertItemsetTreeToStandardFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;

        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("Empty input file");
        }

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' ||
                line.charAt(0) == '%' || line.charAt(0) == '@') {
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

        System.out.println("Converted " + transactionMap.size() + " transactions");
        return transactionMap.size();
    }

    private static void writeFormattedResults(PrintWriter writer, String tempOutputPath,
                                            int transactionCount, double minsup, double maxsup,
                                            int minSize, int maxSize, int outputCount) throws IOException {

        writer.println("========== RARE ITEMSETS ==========");
        writer.println("Support range: (" + (minsup*100) + "%, " + (maxsup*100) + "%]");
        writer.println("Size range: [" + minSize + ", " + maxSize + "]");
        writer.println();

        BufferedReader reader = new BufferedReader(new FileReader(tempOutputPath));
        String line;
        Map<Integer, List<String>> itemsetsBySize = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            if (line.contains("#SUP:")) {
                String[] parts = line.split("#SUP:");
                String itemsPart = parts[0].trim();
                int support = Integer.parseInt(parts[1].trim());

                double supportPercent = (support * 100.0) / transactionCount;
                int itemsetSize = itemsPart.isEmpty() ? 0 : itemsPart.split(" ").length;

                if (itemsetSize >= minSize && itemsetSize <= maxSize) {
                    String formattedLine = String.format("%s #SUP: %d (%.2f%%)",
                                                   itemsPart, support, supportPercent);
                    itemsetsBySize.computeIfAbsent(itemsetSize, k -> new ArrayList<>()).add(formattedLine);
                }
            }
        }
        reader.close();

        int totalCount = 0;
        for (int size = minSize; size <= maxSize; size++) {
            List<String> itemsets = itemsetsBySize.get(size);
            if (itemsets != null && !itemsets.isEmpty()) {
                writer.println("--- " + size + "-itemsets ---");
                System.out.println("--- " + size + "-itemsets ---");

                for (String itemset : itemsets) {
                    writer.println(itemset);
                    System.out.println(itemset);
                    totalCount++;
                }
                writer.println();
                System.out.println();
            }
        }

        writer.println("========== SUMMARY ==========");
        writer.println("Total rare itemsets: " + totalCount);
        writer.println("Size range: " + minSize + " - " + maxSize);
        writer.println("Algorithm: PrePost Rare");

        System.out.println("========== SUMMARY ==========");
        System.out.println("Total rare itemsets: " + totalCount);

        writer.println();
    }

    private static void writePerformanceStats(PrintWriter writer, long startTime) {
        MemoryLogger.getInstance().checkMemory();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;
        long milliseconds = executionTime % 1000;
        String formattedTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);

        double maxMemory = MemoryLogger.getInstance().getMaxMemory();

        writer.println("========== PERFORMANCE ==========");
        writer.println("Execution time: " + formattedTime + " (" + executionTime + " ms)");
        writer.println("Memory usage: " + String.format("%.2f", maxMemory) + " MB");
        writer.println("End time: " + new java.util.Date(endTime));

        System.out.println("========== PERFORMANCE ==========");
        System.out.println("Execution time: " + formattedTime);
        System.out.println("Memory usage: " + String.format("%.2f", maxMemory) + " MB");
    }
}