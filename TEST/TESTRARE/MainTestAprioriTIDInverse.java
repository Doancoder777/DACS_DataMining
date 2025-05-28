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

import frequentpatterns.aprioriTID_inverse.AlgoAprioriTIDInverse;
import input.transaction_database_list_integers.TransactionDatabase;
import patterns.itemset_array_integers_with_tids.Itemset;
import patterns.itemset_array_integers_with_tids.Itemsets;
import tools.MemoryLogger;

/**
 * Chương trình test thuật toán AprioriTID Inverse để tìm rare itemsets
 * Đọc dữ liệu ItemsetTree format và convert sang TransactionDatabase
 * Output được lưu trong folder riêng: release/AprioriTIDInverse/
 * 
 * ĐỊNH NGHĨA: Rare Item có MRT < Support(X) <= MFT
 */
public class MainTestAprioriTIDInverse {

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
        
        // Hiển thị thông báo và định nghĩa
        System.out.println("=== APRIORI-TID INVERSE - RARE ITEMSETS MINING ===");
        System.out.println("ĐỊNH NGHĨA:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("==========================================================");
        
        // Hiển thị danh sách tệp và yêu cầu người dùng chọn
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

        // Lấy đường dẫn tệp được chọn
        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);

        // Nhập MinRareSupport (minsup) - ngưỡng support tối thiểu
        System.out.print("Nhập MinRareSupport - minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("MinRareSupport không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }

        // Nhập MinFrequentSupport (maxsup) - ngưỡng support tối đa
        System.out.print("Nhập MinFrequentSupport - maxsup (phần trăm, từ 0 đến 100): ");
        double maxsupPercent = scanner.nextDouble();
        while (maxsupPercent < minsupPercent || maxsupPercent > 100) {
            System.out.print("MinFrequentSupport không hợp lệ (phải >= minsup). Vui lòng nhập lại: ");
            maxsupPercent = scanner.nextDouble();
        }

        // Tạo thư mục release/AprioriTIDInverse nếu chưa tồn tại
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        
        File aprioriDir = new File("release" + File.separator + "AprioriTIDInverse");
        if (!aprioriDir.exists()) {
            aprioriDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + aprioriDir.getAbsolutePath());
        }

        // Tạo tên file output trong thư mục riêng
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", ""); // Loại bỏ extension
        double minsup = minsupPercent / 100.0;
        double maxsup = maxsupPercent / 100.0;
        String outputPath = aprioriDir.getAbsolutePath() + File.separator + outputBaseName + 
                           "_AprioriInverse_" + (int)minsupPercent + "_" + (int)maxsupPercent + ".txt";

        try {
            runAprioriTIDInverse(input, outputPath, minsup, maxsup, fileName, startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    /**
     * Chạy thuật toán AprioriTID Inverse
     */
    private static void runAprioriTIDInverse(String input, String outputPath, double minsup, double maxsup, 
                                           String fileName, long startTime) throws IOException {
        System.out.println("\n=== CHẠY APRIORI-TID INVERSE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        // Tạo file output
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== APRIORI-TID INVERSE - RARE ITEMSETS MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + (minsup * 100) + "%");
        writer.println("MinFrequentSupport (maxsup): " + (maxsup * 100) + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/AprioriTIDInverse/");
        writer.println();
        
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println();

        // BƯỚC 1: Convert ItemsetTree format sang TransactionDatabase
        System.out.println("Đang convert dữ liệu từ ItemsetTree format...");
        TransactionDatabase database = convertItemsetTreeToTransactionDatabase(input);
        
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + database.size());
        writer.println();
        
        // BƯỚC 2: Chạy thuật toán
        AlgoAprioriTIDInverse algo = new AlgoAprioriTIDInverse();
        
        // Tùy chọn: hiển thị transaction IDs
        algo.setShowTransactionIdentifiers(true);
        
        Itemsets rareItemsets = algo.runAlgorithm(database, minsup, maxsup);
        
        // In thống kê
        algo.printStats();
        
        // BƯỚC 3: Ghi kết quả ra file
        writeResults(writer, rareItemsets, "RARE ITEMSETS", database.size(), minsup, maxsup);
        
        // Ghi thống kê hiệu suất
        writePerformanceStats(writer, startTime);
        writer.close();
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/AprioriTIDInverse/");
    }

    /**
     * Convert ItemsetTree format sang TransactionDatabase
     * ItemsetTree format: transaction_id item_id [count]
     * TransactionDatabase: List<List<Integer>> - mỗi transaction là 1 list items
     */
    private static TransactionDatabase convertItemsetTreeToTransactionDatabase(String inputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;

        // Đọc dòng đầu tiên (header) và bỏ qua
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("File đầu vào rỗng hoặc không hợp lệ");
        }

        // Đọc các dòng dữ liệu và nhóm theo transaction_id
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
                // Thêm item vào transaction (tránh duplicate)
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                }
            }
        }
        reader.close();

        // Tạo TransactionDatabase từ Map
        TransactionDatabase database = new TransactionDatabase();
        
        // Sắp xếp theo transaction ID để đảm bảo thứ tự
        transactionMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                List<Integer> transaction = entry.getValue();
                // Sắp xếp items trong transaction
                transaction.sort(Integer::compareTo);
                database.addTransaction(transaction);
            });

        System.out.println("Đã convert thành công " + transactionMap.size() + " transactions");
        return database;
    }

    /**
     * Ghi kết quả rare itemsets ra file
     */
    private static void writeResults(PrintWriter writer, Itemsets rareItemsets, String title, 
                                   int transactionCount, double minsup, double maxsup) {
        writer.println("========== " + title + " ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + (minsup*100) + "%, " + (maxsup*100) + "%]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println();
        
        int totalCount = 0;
        
        // Duyệt qua từng level (kích thước itemset)
        List<List<Itemset>> levels = rareItemsets.getLevels();
        for (int level = 0; level < levels.size(); level++) {
            List<Itemset> itemsetsAtLevel = levels.get(level);
            if (itemsetsAtLevel != null && !itemsetsAtLevel.isEmpty()) {
                // Tính kích thước thực tế từ itemset đầu tiên trong level
                int actualSize = itemsetsAtLevel.get(0).size();
                writer.println("--- " + actualSize + "-itemsets HIẾM ---");
                System.out.println("--- " + actualSize + "-itemsets HIẾM ---");
                
                for (Itemset itemset : itemsetsAtLevel) {
                    int support = itemset.getTransactionsIds().size();
                    double supportPercent = (support * 100.0) / transactionCount;
                    
                    String line = String.format("%s #SUP: %d (%.2f%%) #TID: %s", 
                                               itemset.toString(), 
                                               support, 
                                               supportPercent,
                                               itemset.getTransactionsIds().toString());
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
        writer.println("Thuật toán: AprioriTID Inverse");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/AprioriTIDInverse/");
        
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: AprioriTID Inverse");
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
        writer.println("Thuật toán: AprioriTID Inverse");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/AprioriTIDInverse/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: AprioriTID Inverse");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}