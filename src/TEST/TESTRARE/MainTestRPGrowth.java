package TEST.TESTRARE;
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
        System.out.println("=== RP-GROWTH - RARE PATTERN MINING ===");
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
        double minRareSuppPercent = scanner.nextDouble();
        while (minRareSuppPercent < 0 || minRareSuppPercent > 100) {
            System.out.print("MinRareSupport không hợp lệ. Vui lòng nhập lại (0-100): ");
            minRareSuppPercent = scanner.nextDouble();
        }
        System.out.print("Nhập MinFrequentSupport - maxsup (phần trăm, từ 0 đến 100): ");
        double minFreqSuppPercent = scanner.nextDouble();
        while (minFreqSuppPercent < minRareSuppPercent || minFreqSuppPercent > 100) {
            System.out.print("MinFrequentSupport không hợp lệ (phải >= minsup). Vui lòng nhập lại: ");
            minFreqSuppPercent = scanner.nextDouble();
        }
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
        String outputPath = rpGrowthDir.getAbsolutePath() + File.separator + outputBaseName + 
                           "_RPGrowth_" + (int)minRareSuppPercent + "_" + (int)minFreqSuppPercent + ".txt";
        try {
            runRPGrowth(input, outputPath, minFreqSupp, minRareSupp, fileName, startTime, 
                       minRareSuppPercent, minFreqSuppPercent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner.close();
    }
    private static void runRPGrowth(String input, String outputPath, double minFreqSupp, double minRareSupp, 
                                   String fileName, long startTime, double minRareSuppPercent, 
                                   double minFreqSuppPercent) throws IOException {
        System.out.println("\n=== CHẠY RP-GROWTH ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== RP-GROWTH - RARE PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + minRareSuppPercent + "%");
        writer.println("MinFrequentSupport (maxsup): " + minFreqSuppPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/RPGrowth/");
        writer.println();
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println();
        AlgoRPGrowth algo = new AlgoRPGrowth();
        Itemsets patterns = algo.runAlgorithm(input, null, minFreqSupp, minRareSupp);
        algo.printStats();
        writeRareResults(writer, patterns, "RARE ITEMSETS", algo.getDatabaseSize(), minRareSupp, minFreqSupp);
        writePerformanceStats(writer, startTime);
        writer.close();
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/RPGrowth/");
    }
    private static void writeRareResults(PrintWriter writer, Itemsets patterns, String title, 
                                        int transactionCount, double minRareSupp, double minFreqSupp) {
        writer.println("========== " + title + " ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + (minRareSupp*100) + "%, " + (minFreqSupp*100) + "%]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
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
                writer.println("--- " + actualSize + "-itemsets HIẾM ---");
                System.out.println("--- " + actualSize + "-itemsets HIẾM ---");
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
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số rare itemsets tìm được: " + totalCount);
        writer.println("Thuật toán: RP-Growth");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/RPGrowth/");
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: RP-Growth");
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
        writer.println("Thuật toán: RP-Growth");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/RPGrowth/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: RP-Growth");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}
