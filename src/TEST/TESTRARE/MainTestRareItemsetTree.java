package TEST.TESTRARE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import Algorithrms.Rarepartem.itemsettreerare.RareItemsetTree;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;

/**
 * Chương trình test thuật toán Rare Itemset Tree với tối ưu cắt tỉa
 * Output được lưu trong folder riêng: release/RareItemsetTree/
 * 
 * ĐỊNH NGHĨA: Rare Item có MRT < Support(X) <= MFT
 */
public class MainTestRareItemsetTree {

    public static void main(String[] arg) throws IOException {
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
        
        System.out.println("=== RARE ITEMSET TREE - RARE PATTERN MINING ===");
        System.out.println("ĐỊNH NGHĨA:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("- Tối ưu: Cắt tỉa transactions và candidates");
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
        
        File rareTreeDir = new File("release" + File.separator + "RareItemsetTree");
        if (!rareTreeDir.exists()) {
            rareTreeDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + rareTreeDir.getAbsolutePath());
        }

        // Tính toán ngưỡng
        int transactionCount = countTransactions(input);
        double rawMinSupp = (minsupPercent / 100.0) * transactionCount;
        double rawMaxSupp = (maxsupPercent / 100.0) * transactionCount;
        int minRareSupport = (int) Math.ceil(rawMinSupp) - 1;
        int maxRareSupport = (int) Math.ceil(rawMaxSupp);

        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        String outputPath = rareTreeDir.getAbsolutePath() + File.separator + outputBaseName + 
                           "_RareTree_" + (int)minsupPercent + "_" + (int)maxsupPercent + ".txt";

        try {
            runRareItemsetTree(input, outputPath, minRareSupport, maxRareSupport, 
                             transactionCount, fileName, startTime, minsupPercent, maxsupPercent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void runRareItemsetTree(String input, String outputPath, int minRareSupport, int maxRareSupport,
                                         int transactionCount, String fileName, long startTime, 
                                         double minsupPercent, double maxsupPercent) throws IOException {
        System.out.println("\n=== CHẠY RARE ITEMSET TREE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== RARE ITEMSET TREE - RARE PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + minsupPercent + "%");
        writer.println("MinFrequentSupport (maxsup): " + maxsupPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/RareItemsetTree/");
        writer.println("Tối ưu: Transaction pruning + Candidate pruning + Support caching");
        writer.println();
        
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println("- Định nghĩa: " + minRareSupport + " < Support(X) <= " + maxRareSupport);
        writer.println();

        System.out.println("Đang xây dựng Rare Itemset Tree...");
        RareItemsetTree rareTree = new RareItemsetTree(minRareSupport, maxRareSupport);
        
        long buildStart = System.currentTimeMillis();
        rareTree.buildTree(input);
        long buildTime = System.currentTimeMillis() - buildStart;
        
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + rareTree.getTransactionCount());
        writer.println("Thời gian xây dựng cây: " + buildTime + " ms");
        writer.println();
        
        rareTree.printStatistics();

        System.out.println("Đang khai thác tất cả rare itemsets...");
        long miningStart = System.currentTimeMillis();
        
        // SỬ DỤNG PHƯƠNG THỨC KHAI THÁC VỚI CẮT TỈA
        List<Itemset> allRareItemsets = rareTree.mineAllRareItemsetsWithPruning(input, minRareSupport, maxRareSupport);
        
        long miningTime = System.currentTimeMillis() - miningStart;
        System.out.println("Thời gian khai thác: " + miningTime + " ms");

        Collections.sort(allRareItemsets, new Comparator<Itemset>() {
            @Override
            public int compare(Itemset o1, Itemset o2) {
                return Integer.compare(o1.support, o2.support);
            }
        });

        writeRareResults(writer, allRareItemsets, "RARE ITEMSETS", transactionCount, 
                        minRareSupport, maxRareSupport, buildTime, miningTime);
        
        writePerformanceStats(writer, startTime, buildTime, miningTime);
        writer.close();
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/RareItemsetTree/");
    }
    
    private static int countTransactions(String inputFilePath) throws IOException {
        Set<Integer> transactionIds = new HashSet<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        
        reader.readLine(); // Skip header
        
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;
            }
            
            int transactionId = Integer.parseInt(parts[0]);
            transactionIds.add(transactionId);
        }
        
        reader.close();
        return transactionIds.size();
    }

    private static void writeRareResults(PrintWriter writer, List<Itemset> rareItemsets, String title, 
                                       int transactionCount, int minRareSupport, int maxRareSupport,
                                       long buildTime, long miningTime) {
        writer.println("========== " + title + " ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + minRareSupport + ", " + maxRareSupport + "]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Thời gian xây dựng cây: " + buildTime + " ms");
        writer.println("Thời gian khai thác: " + miningTime + " ms");
        writer.println("Tối ưu: Transaction pruning + Candidate pruning + Support caching");
        writer.println();
        
        int totalCount = 0;
        
        int currentSize = 0;
        for (Itemset itemset : rareItemsets) {
            if (itemset.size() != currentSize) {
                if (currentSize > 0) {
                    writer.println();
                }
                currentSize = itemset.size();
                writer.println("--- " + currentSize + "-itemsets HIẾM ---");
                System.out.println("--- " + currentSize + "-itemsets HIẾM ---");
            }
            
            double supportPercent = (itemset.support * 100.0) / transactionCount;
            
            String line = String.format("%s #SUP: %d (%.2f%%)", 
                                       itemset.toString(), 
                                       itemset.support, 
                                       supportPercent);
            writer.println(line);
            System.out.println(line);
            totalCount++;
        }
        
        writer.println();
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số rare itemsets tìm được: " + totalCount);
        writer.println("Thuật toán: Rare Itemset Tree");
        writer.println("Tối ưu áp dụng:");
        writer.println("- Transaction pruning: Loại bỏ items không rare");
        writer.println("- Candidate pruning: Kiểm tra subset conditions");
        writer.println("- Support upper bound estimation");
        writer.println("- Support value caching");
        writer.println("- Quick join conditions checking");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/RareItemsetTree/");
        
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: Rare Itemset Tree");
        writer.println();
    }

    private static void writePerformanceStats(PrintWriter writer, long startTime, long buildTime, long miningTime) {
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
        writer.println("Thuật toán: Rare Itemset Tree");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("- Thời gian xây dựng cây: " + buildTime + " ms");
        writer.println("- Thời gian khai thác: " + miningTime + " ms");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/RareItemsetTree/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println();
        writer.println("========== TỐI ƯU ÁP DỤNG ==========");
        writer.println("1. Transaction Pruning: Loại bỏ transactions chỉ chứa items không rare");
        writer.println("2. Candidate Pruning: Kiểm tra subset conditions trước khi tính support");
        writer.println("3. Support Upper Bound: Ước tính upper bound để skip candidates");
        writer.println("4. Support Caching: Cache kết quả tính support để tái sử dụng");
        writer.println("5. Quick Join Check: Kiểm tra điều kiện join nhanh");
        writer.println("6. Early Termination: Dừng sớm khi không còn candidates");
        
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: Rare Itemset Tree");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("- Thời gian xây dựng cây: " + buildTime + " ms");  
        System.out.println("- Thời gian khai thác: " + miningTime + " ms");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}