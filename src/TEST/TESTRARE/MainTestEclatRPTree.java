package TEST.TESTRARE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import Algorithrms.frequentpatterns.eclatrptree.EclatRPTree;
import Algorithrms.frequentpatterns.eclatrptree.RarePattern;
import tools.MemoryLogger;

/**
 * Chương trình test thuật toán EclatRPTree để khai thác rare patterns
 * Output được lưu trong folder riêng: release/EclatRPTree/
 * 
 * ĐỊNH NGHĨA: Rare Item có MRT < Support(X) <= MFT
 */
public class MainTestEclatRPTree {

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
        
        System.out.println("=== ECLAT RPTREE - RARE PATTERN MINING ===");
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
        
        File eclatDir = new File("release" + File.separator + "EclatRPTree");
        if (!eclatDir.exists()) {
            eclatDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + eclatDir.getAbsolutePath());
        }

        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        String outputPath = eclatDir.getAbsolutePath() + File.separator + outputBaseName + 
                           "_EclatRPTree_" + (int)minsupPercent + "_" + (int)maxsupPercent + ".txt";

        try {
            runEclatRPTree(input, outputPath, minsupPercent, maxsupPercent, fileName, startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void runEclatRPTree(String input, String outputPath, double minsupPercent, double maxsupPercent,
                                      String fileName, long startTime) throws IOException {
        System.out.println("\n=== CHẠY ECLAT RPTREE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== ECLAT RPTREE - RARE PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + minsupPercent + "%");
        writer.println("MinFrequentSupport (maxsup): " + maxsupPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/EclatRPTree/");
        writer.println();
        
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println();

        System.out.println("Đang xây dựng Eclat RPTree...");
        EclatRPTree eclatTree = new EclatRPTree(minsupPercent, maxsupPercent);
        
        long buildStart = System.currentTimeMillis();
        eclatTree.buildTree(input);
        long buildTime = System.currentTimeMillis() - buildStart;
        
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + eclatTree.getTransactionCount());
        writer.println("Thời gian xây dựng: " + buildTime + " ms");
        writer.println("Định nghĩa: " + eclatTree.getMrtAbsolute() + " < Support(X) <= " + eclatTree.getMftAbsolute());
        writer.println();
        
        eclatTree.printStatistics();

        System.out.println("Đang lấy kết quả rare patterns...");
        List<RarePattern> rarePatterns = eclatTree.getRarePatterns();
        
        writeRareResults(writer, rarePatterns, "RARE ITEMSETS", eclatTree.getTransactionCount(), 
                        eclatTree.getMrtAbsolute(), eclatTree.getMftAbsolute(), buildTime);
        
        writePerformanceStats(writer, startTime, buildTime);
        writer.close();
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/EclatRPTree/");
    }

    private static void writeRareResults(PrintWriter writer, List<RarePattern> rarePatterns, String title, 
                                       int transactionCount, int minRareSupport, int maxRareSupport,
                                       long buildTime) {
        writer.println("========== " + title + " ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + minRareSupport + ", " + maxRareSupport + "]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Thời gian xây dựng: " + buildTime + " ms");
        writer.println();
        
        int totalCount = 0;
        int currentSize = 0;
        
        for (RarePattern pattern : rarePatterns) {
            int size = pattern.getItemset().length;
            
            if (size != currentSize) {
                if (currentSize > 0) {
                    writer.println();
                }
                currentSize = size;
                writer.println("--- " + currentSize + "-itemsets HIẾM ---");
                System.out.println("--- " + currentSize + "-itemsets HIẾM ---");
            }
            
            double supportPercent = (pattern.getSupport() * 100.0) / transactionCount;
            
            StringBuilder itemsetStr = new StringBuilder();
            itemsetStr.append("{");
            for (int i = 0; i < pattern.getItemset().length; i++) {
                itemsetStr.append(pattern.getItemset()[i]);
                if (i < pattern.getItemset().length - 1) {
                    itemsetStr.append(" ");
                }
            }
            itemsetStr.append("}");
            
            String line = String.format("%s #SUP: %d (%.2f%%)", 
                                       itemsetStr.toString(), 
                                       pattern.getSupport(), 
                                       supportPercent);
            writer.println(line);
            System.out.println(line);
            totalCount++;
        }
        
        writer.println();
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số rare itemsets tìm được: " + totalCount);
        writer.println("Thuật toán: Eclat RPTree");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/EclatRPTree/");
        
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: Eclat RPTree");
        writer.println();
    }

    private static void writePerformanceStats(PrintWriter writer, long startTime, long buildTime) {
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
        writer.println("Thuật toán: Eclat RPTree");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("- Thời gian xây dựng: " + buildTime + " ms");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/EclatRPTree/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: Eclat RPTree");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("- Thời gian xây dựng: " + buildTime + " ms");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}