package TEST.TESTFREEQUENT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import frequentpatterns.fpgrowth.AlgoFPGrowth;
import patterns.itemset_array_integers_with_count.Itemset;
import patterns.itemset_array_integers_with_count.Itemsets;
import tools.MemoryLogger;

/**
 * Chương trình test thuật toán FP-Growth để tìm tất cả frequent itemsets
 */
public class MainTestFPGrowthSimple {

    public static void main(String[] args) throws IOException {
        // Đặt lại bộ đếm bộ nhớ
        MemoryLogger.getInstance().reset();
        
        // Đánh dấu thời gian bắt đầu thuật toán
        long startTime = System.currentTimeMillis();
        
        // Đường dẫn thư mục chứa dữ liệu
        String dataDir = "Data";

        // Quét thư mục để lấy danh sách tệp
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".hui"));
        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy tệp dữ liệu trong thư mục: " + dataDir);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
        // Hiển thị danh sách tệp và yêu cầu người dùng chọn
        System.out.println("Danh sách tệp dữ liệu trong thư mục:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        System.out.print("Vui lòng chọn số thứ tự của tệp (1-" + files.length + "): ");

        int choice = scanner.nextInt();
        while (choice < 1 || choice > files.length) {
            System.out.print("Lựa chọn không hợp lệ. Vui lòng chọn lại (1-" + files.length + "): ");
            choice = scanner.nextInt();
        }

        // Lấy đường dẫn tệp được chọn
        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);

        // Nhập minsup dưới dạng phần trăm
        System.out.print("Nhập minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("Minsup không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }

        // Tạo thư mục release nếu chưa tồn tại
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }

        // Tạo tên file output
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", ""); // Loại bỏ extension
        double minsup = minsupPercent / 100.0;
        String outputPath = "release" + File.separator + outputBaseName + "_FPGrowth_" + (int)minsupPercent + ".txt";

        try {
            runFPGrowth(input, outputPath, minsup, fileName, startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    /**
     * Chạy thuật toán FP-Growth
     */
    private static void runFPGrowth(String input, String outputPath, double minsup, String fileName, long startTime) throws IOException {
        System.out.println("\n=== CHẠY FP-GROWTH ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        // Tạo file output
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== FP-GROWTH - FREQUENT PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("Minsup tương đối: " + (minsup * 100) + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println();

        // Chạy thuật toán
        AlgoFPGrowth algo = new AlgoFPGrowth();
        Itemsets patterns = algo.runAlgorithm(input, null, minsup);
        
        // In thống kê
        algo.printStats();
        
        // Ghi kết quả ra file
        writeResults(writer, patterns, "FREQUENT ITEMSETS", algo.getDatabaseSize());
        
        // Ghi thống kê hiệu suất
        writePerformanceStats(writer, startTime);
        writer.close();
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
    }

    /**
     * Ghi kết quả itemsets ra file
     */
    private static void writeResults(PrintWriter writer, Itemsets patterns, String title, int transactionCount) {
        writer.println("========== " + title + " ==========");
        writer.println("Số lượng giao dịch: " + transactionCount);
        writer.println();
        
        int totalCount = 0;
        // Duyệt qua từng level (kích thước itemset)
        java.util.List<java.util.List<Itemset>> levels = patterns.getLevels();
        for (int level = 0; level < levels.size(); level++) {
            java.util.List<Itemset> itemsetsAtLevel = levels.get(level);
            if (itemsetsAtLevel != null && !itemsetsAtLevel.isEmpty()) {
                writer.println("--- " + (level + 1) + "-itemsets ---");
                System.out.println("--- " + (level + 1) + "-itemsets ---");
                for (Itemset itemset : itemsetsAtLevel) {
                    String line = itemset.toString() + " #SUP: " + itemset.getAbsoluteSupport();
                    writer.println(line);
                    System.out.println(line);
                    totalCount++;
                }
                writer.println();
                System.out.println();
            }
        }
        
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số itemsets tìm được: " + totalCount);
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số itemsets tìm được: " + totalCount);
        writer.println();
    }

    /**
     * Ghi thống kê hiệu suất ra file
     */
    private static void writePerformanceStats(PrintWriter writer, long startTime) {
        // Kiểm tra bộ nhớ sử dụng lần cuối
        MemoryLogger.getInstance().checkMemory();

        // Tính thời gian thực thi thuật toán
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Chuyển đổi thời gian từ millisecond sang phút:giây.millisecond
        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;
        long milliseconds = executionTime % 1000;
        
        // Tạo chuỗi thời gian theo định dạng "mm:ss.ms"
        String formattedTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        
        // Lấy thông tin bộ nhớ tối đa đã sử dụng
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();
        
        // Định dạng bộ nhớ tối đa sang MB
        String formattedMemory = String.format("%.2f", maxMemory);
        
        // Ghi thông tin hiệu suất
        writer.println("========== THÔNG TIN HIỆU SUẤT ==========");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
    }
}