package TEST;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import Algorithrms.Rarepartem.rpgrowth.AlgoRPGrowth;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

public class MainTestRPGrowth {
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
        System.out.println("=== RP-GROWTH MIXED - RARE PATTERN MINING ===");
        System.out.println("ĐỊNH NGHĨA MIXED MODE:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("- Rare ItemSet: MRT < Support(Pattern) <= MFT AND có ít nhất 1 rare item");
        System.out.println("- Chấp nhận patterns chứa cả frequent và rare items");
        System.out.println("================================================================");

        // Chọn file
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

        // Nhập MinRareSupport
        System.out.print("Nhập MinRareSupport - minsup (phần trăm, từ 0 đến 100): ");
        double minRareSuppPercent = scanner.nextDouble();
        while (minRareSuppPercent < 0 || minRareSuppPercent > 100) {
            System.out.print("MinRareSupport không hợp lệ. Vui lòng nhập lại (0-100): ");
            minRareSuppPercent = scanner.nextDouble();
        }

        // Nhập MinFrequentSupport
        System.out.print("Nhập MinFrequentSupport - maxsup (phần trăm, từ 0 đến 100): ");
        double minFreqSuppPercent = scanner.nextDouble();
        while (minFreqSuppPercent < minRareSuppPercent || minFreqSuppPercent > 100) {
            System.out.print("MinFrequentSupport không hợp lệ (phải >= minsup). Vui lòng nhập lại: ");
            minFreqSuppPercent = scanner.nextDouble();
        }

        // Nhập MinSize - THAM SỐ MỚI
        System.out.print("Nhập MinSize - kích thước tối thiểu của itemset (>=1): ");
        int minSize = scanner.nextInt();
        while (minSize < 1) {
            System.out.print("MinSize không hợp lệ (phải >= 1). Vui lòng nhập lại: ");
            minSize = scanner.nextInt();
        }

        // Nhập MaxSize - THAM SỐ MỚI
        System.out.print("Nhập MaxSize - kích thước tối đa của itemset (>= MinSize): ");
        int maxSize = scanner.nextInt();
        while (maxSize < minSize) {
            System.out.print("MaxSize không hợp lệ (phải >= MinSize=" + minSize + "). Vui lòng nhập lại: ");
            maxSize = scanner.nextInt();
        }

        // Tạo thư mục output
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        File rpGrowthDir = new File("release" + File.separator + "RPGrowth");
        if (!rpGrowthDir.exists()) {
            rpGrowthDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + rpGrowthDir.getAbsolutePath());
        }

        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        double minRareSupp = minRareSuppPercent / 100.0;
        double minFreqSupp = minFreqSuppPercent / 100.0;
        
        // FIX: Tạo output path từng phần để tránh lỗi syntax
        StringBuilder outputPathBuilder = new StringBuilder();
        outputPathBuilder.append(rpGrowthDir.getAbsolutePath());
        outputPathBuilder.append(File.separator);
        outputPathBuilder.append(outputBaseName);
        outputPathBuilder.append("_RPGrowth_Mixed_");
        outputPathBuilder.append((int)minRareSuppPercent);
        outputPathBuilder.append("_");
        outputPathBuilder.append((int)minFreqSuppPercent);
        outputPathBuilder.append("_size");
        outputPathBuilder.append(minSize);
        outputPathBuilder.append("to");
        outputPathBuilder.append(maxSize);
        outputPathBuilder.append(".txt");
        String outputPath = outputPathBuilder.toString();

        try {
            runRPGrowth(input, outputPath, minFreqSupp, minRareSupp, minSize, maxSize, 
                       fileName, startTime, minRareSuppPercent, minFreqSuppPercent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void runRPGrowth(String input, String outputPath, double minFreqSupp, double minRareSupp, 
                                   int minSize, int maxSize, String fileName, long startTime, 
                                   double minRareSuppPercent, double minFreqSuppPercent) throws IOException {
        System.out.println("\n=== CHẠY RP-GROWTH MIXED MODE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);

        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== RP-GROWTH MIXED - RARE PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + minRareSuppPercent + "%");
        writer.println("MinFrequentSupport (maxsup): " + minFreqSuppPercent + "%");
        writer.println("MinSize: " + minSize + " items");
        writer.println("MaxSize: " + maxSize + " items");
        writer.println("Mode: MIXED (Frequent + Rare Items)");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/RPGrowth/");
        writer.println();
        writer.println("========== ĐỊNH NGHĨA MIXED MODE ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println("- Rare ItemSet: MRT < Support(Pattern) <= MFT AND có ít nhất 1 rare item");
        writer.println("- Kích thước itemset: " + minSize + " <= size <= " + maxSize);
        writer.println("- Chấp nhận patterns chứa cả frequent và rare items");
        writer.println();

        // Sử dụng phương thức mới với 6 tham số
        AlgoRPGrowth algo = new AlgoRPGrowth();
        Itemsets patterns = algo.runAlgorithm(input, null, minFreqSupp, minRareSupp, minSize, maxSize);
        
        algo.printStats();

        writeRareResults(writer, patterns, "MIXED RARE ITEMSETS", algo.getDatabaseSize(), 
                        minRareSupp, minFreqSupp, minSize, maxSize);
        writePerformanceStats(writer, startTime, minSize, maxSize);

        writer.close();
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/RPGrowth/");
    }

    private static void writeRareResults(PrintWriter writer, Itemsets patterns, String title, 
                                        int transactionCount, double minRareSupp, double minFreqSupp,
                                        int minSize, int maxSize) {
        writer.println("========== " + title + " ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + (minRareSupp*100) + "%, " + (minFreqSupp*100) + "%]");
        writer.println("Kích thước itemset: " + minSize + " <= size <= " + maxSize);
        writer.println("Điều kiện: Pattern có ít nhất 1 rare item");
        writer.println("Mode: MIXED (chấp nhận cả frequent và rare items)");
        writer.println();
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + transactionCount);
        writer.println();

        int totalCount = 0;
        java.util.List<java.util.List<Itemset>> levels = patterns.getLevels();
        
        for (int level = 0; level < levels.size(); level++) {
            java.util.List<Itemset> itemsetsAtLevel = levels.get(level);
            if (itemsetsAtLevel != null && !itemsetsAtLevel.isEmpty()) {
                int actualSize = itemsetsAtLevel.get(0).size();
                
                // Chỉ hiển thị level nằm trong khoảng [minSize, maxSize]
                if (actualSize >= minSize && actualSize <= maxSize) {
                    writer.println("--- " + actualSize + "-itemsets HIẾM (MIXED) ---");
                    System.out.println("--- " + actualSize + "-itemsets HIẾM (MIXED) ---");
                    
                    for (Itemset itemset : itemsetsAtLevel) {
                        int support = itemset.getAbsoluteSupport();
                        double supportPercent = (support * 100.0) / transactionCount;
                        String line = itemset.toString() + " #SUP: " + support + " (" + 
                                     String.format("%.2f", supportPercent) + "%)";
                        writer.println(line);
                        System.out.println(line);
                        totalCount++;
                    }
                    writer.println();
                    System.out.println();
                }
            }
        }

        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số mixed rare itemsets tìm được: " + totalCount);
        writer.println("Kích thước: " + minSize + " đến " + maxSize + " items");
        writer.println("Thuật toán: RP-Growth Mixed Mode");
        writer.println("Định nghĩa: MRT < Support(Pattern) <= MFT AND có ít nhất 1 rare item");
        writer.println("Output folder: release/RPGrowth/");
        
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số mixed rare itemsets tìm được: " + totalCount);
        System.out.println("Kích thước: " + minSize + " đến " + maxSize + " items");
        System.out.println("Thuật toán: RP-Growth Mixed Mode");
        writer.println();
    }

    private static void writePerformanceStats(PrintWriter writer, long startTime, int minSize, int maxSize) {
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
        writer.println("Thuật toán: RP-Growth Mixed Mode");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Kích thước itemset: " + minSize + " đến " + maxSize + " items");
        writer.println("Mode: MIXED (Frequent + Rare Items)");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/RPGrowth/");
        writer.println("Định nghĩa: MRT < Support(Pattern) <= MFT AND có ít nhất 1 rare item");

        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: RP-Growth Mixed Mode");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        System.out.println("Kích thước itemset: " + minSize + " đến " + maxSize + " items");
        System.out.println("Mode: MIXED (Frequent + Rare Items)");
    }
}