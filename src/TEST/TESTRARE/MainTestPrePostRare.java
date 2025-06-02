package TEST.TESTRARE;

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

public class MainTestPrePostRare {
    
    public static void main(String[] args) throws IOException {
        
        MemoryLogger.getInstance().reset();
        long startTime = System.currentTimeMillis();
        
        String dataDir = "Data";
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".hui"));
        
        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy tệp dữ liệu trong thư mục: " + dataDir);
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== PREPOST RARE - RARE ITEMSETS MINING ===");
        System.out.println("ĐỊNH NGHĨA:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("==========================================================");
        
        System.out.println("\nDanh sách tệp dữ liệu trong thư mục:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        
        System.out.print("Vui lòng chọn số thứ tự của tệp (1-" + files.length + "): ");
        int choice = scanner.nextInt();
        while (choice < 1 || choice > files.length) {
            System.out.print("Lựa chọn không hợp lệ. Vui lòng chọn lại (1-" + files.length + "): ");
            choice = scanner.nextInt();
        }
        
        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);
        
        System.out.print("Nhập MinRareSupport - minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("MinRareSupport không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }
        
        System.out.print("Nhập MinFrequentSupport - maxsup (phần trăm, từ 0 đến 100): ");
        double maxsupPercent = scanner.nextDouble();
        while (maxsupPercent < minsupPercent || maxsupPercent > 100) {
            System.out.print("MinFrequentSupport không hợp lệ (phải >= minsup). Vui lòng nhập lại: ");
            maxsupPercent = scanner.nextDouble();
        }
        
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        
        File prepostRareDir = new File("release" + File.separator + "PrePostRare");
        if (!prepostRareDir.exists()) {
            prepostRareDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + prepostRareDir.getAbsolutePath());
        }
        
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        double minsup = minsupPercent / 100.0;
        double maxsup = maxsupPercent / 100.0;
        String outputPath = prepostRareDir.getAbsolutePath() + File.separator + outputBaseName + 
                        "_PrePostRare_" + (int)minsupPercent + "_" + (int)maxsupPercent + ".txt";
        
        try {
            runPrePostRare(input, outputPath, minsup, maxsup, fileName, startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }
    
    private static void runPrePostRare(String input, String outputPath, double minsup, double maxsup, 
                                    String fileName, long startTime) throws IOException {
        
        System.out.println("\n=== CHẠY PREPOST RARE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        
        writer.println("=== PREPOST RARE - RARE ITEMSETS MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + (minsup * 100) + "%");
        writer.println("MinFrequentSupport (maxsup): " + (maxsup * 100) + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/PrePostRare/");
        writer.println();
        
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println();
        
        System.out.println("Đang convert dữ liệu từ ItemsetTree format...");
        String convertedFile = "temp_converted_" + System.currentTimeMillis() + ".txt";
        int transactionCount = convertItemsetTreeToStandardFormat(input, convertedFile);
        
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + transactionCount);
        writer.println();
        
        String tempOutputPath = "temp_prepost_rare_" + System.currentTimeMillis() + ".txt";
        PrePostRare algo = new PrePostRare();
        algo.runAlgorithm(convertedFile, minsup, maxsup, tempOutputPath);
        algo.printStats();
        
        writeFormattedResults(writer, tempOutputPath, transactionCount, minsup, maxsup, algo.getOutputCount());
        writePerformanceStats(writer, startTime);
        
        writer.close();
        new File(convertedFile).delete();
        new File(tempOutputPath).delete();
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/PrePostRare/");
    }
    
    private static int convertItemsetTreeToStandardFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("File đầu vào rỗng hoặc không hợp lệ");
        }
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;
            }
            
            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            
            if (count > 0) {
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                }
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
        
        System.out.println("Đã convert thành công " + transactionMap.size() + " transactions");
        return transactionMap.size();
    }
    
    private static void writeFormattedResults(PrintWriter writer, String tempOutputPath, 
                                            int transactionCount, double minsup, double maxsup, 
                                            int outputCount) throws IOException {
        
        writer.println("========== RARE ITEMSETS ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + (minsup*100) + "%, " + (maxsup*100) + "%]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
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
                String formattedLine = String.format("%s #SUP: %d (%.2f%%)", 
                                                itemsPart, support, supportPercent);
                itemsetsBySize.computeIfAbsent(itemsetSize, k -> new ArrayList<>()).add(formattedLine);
            }
        }
        reader.close();
        
        int totalCount = 0;
        for (int size = 1; size <= 10; size++) {
            List<String> itemsets = itemsetsBySize.get(size);
            if (itemsets != null && !itemsets.isEmpty()) {
                writer.println("--- " + size + "-itemsets HIẾM ---");
                System.out.println("--- " + size + "-itemsets HIẾM ---");
                for (String itemset : itemsets) {
                    writer.println(itemset);
                    System.out.println(itemset);
                    totalCount++;
                }
                writer.println();
                System.out.println();
            }
        }
        
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số rare itemsets tìm được: " + totalCount);
        writer.println("Thuật toán: PrePost Rare");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/PrePostRare/");
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: PrePost Rare");
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
        String formattedMemory = String.format("%.2f", maxMemory);
        
        writer.println("========== THÔNG TIN HIỆU SUẤT ==========");
        writer.println("Thuật toán: PrePost Rare");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/PrePostRare/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: PrePost Rare");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}