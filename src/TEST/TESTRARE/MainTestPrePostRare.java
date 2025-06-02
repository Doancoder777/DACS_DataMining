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

/**
 * Main test class cho thuật toán PrePost Rare
 * Cung cấp interface người dùng và xử lý file I/O
 */
public class MainTestPrePostRare {
    
    /**
     * Entry point của chương trình
     * Xử lý user interaction và orchestrate toàn bộ quá trình
     */
    public static void main(String[] args) throws IOException {
        
        // ===== INITIALIZATION =====
        MemoryLogger.getInstance().reset();
        long startTime = System.currentTimeMillis();
        
        // ===== SETUP DATA DIRECTORY =====
        String dataDir = "Data";
        File directory = new File(dataDir);
        
        // Tìm tất cả files có extension .txt hoặc .hui
        File[] files = directory.listFiles((dir, name) -> 
            name.endsWith(".txt") || name.endsWith(".hui"));
        
        // Validation
        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy tệp dữ liệu trong thư mục: " + dataDir);
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        
        // ===== USER INTERFACE =====
        System.out.println("=== PREPOST RARE - RARE ITEMSETS MINING ===");
        System.out.println("ĐỊNH NGHĨA:");
        System.out.println("- Rare Item: MRT < Support(X) <= MFT");
        System.out.println("- Frequent Item: Support(X) > MFT");
        System.out.println("- Infrequent Item: Support(X) <= MRT");
        System.out.println("==========================================================");
        
        // ===== FILE SELECTION =====
        System.out.println("\nDanh sách tệp dữ liệu trong thư mục:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        
        // Get user choice với validation
        System.out.print("Vui lòng chọn số thứ tự của tệp (1-" + files.length + "): ");
        int choice = scanner.nextInt();
        while (choice < 1 || choice > files.length) {
            System.out.print("Lựa chọn không hợp lệ. Vui lòng chọn lại (1-" + files.length + "): ");
            choice = scanner.nextInt();
        }
        
        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);
        
        // ===== PARAMETER INPUT =====
        // MinRareSupport (MRT) input với validation
        System.out.print("Nhập MinRareSupport - minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("MinRareSupport không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }
        
        // MinFrequentSupport (MFT) input với validation
        System.out.print("Nhập MinFrequentSupport - maxsup (phần trăm, từ 0 đến 100): ");
        double maxsupPercent = scanner.nextDouble();
        while (maxsupPercent < minsupPercent || maxsupPercent > 100) {
            System.out.print("MinFrequentSupport không hợp lệ (phải >= minsup). Vui lòng nhập lại: ");
            maxsupPercent = scanner.nextDouble();
        }
        
        // ===== OUTPUT DIRECTORY SETUP =====
        // Tạo thư mục output nếu chưa có
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        
        File prepostRareDir = new File("release" + File.separator + "PrePostRare");
        if (!prepostRareDir.exists()) {
            prepostRareDir.mkdirs();
            System.out.println("Đã tạo thư mục: " + prepostRareDir.getAbsolutePath());
        }
        
        // ===== GENERATE OUTPUT FILENAME =====
        // Format: filename_PrePostRare_minsup_maxsup.txt
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");  // Remove extension
        double minsup = minsupPercent / 100.0;   // Convert to decimal
        double maxsup = maxsupPercent / 100.0;
        String outputPath = prepostRareDir.getAbsolutePath() + File.separator + outputBaseName + 
                        "_PrePostRare_" + (int)minsupPercent + "_" + (int)maxsupPercent + ".txt";
        
        // ===== RUN ALGORITHM =====
        try {
            runPrePostRare(input, outputPath, minsup, maxsup, fileName, startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }
    
    /**
     * Core method để chạy thuật toán PrePost Rare
     * Bao gồm data conversion, algorithm execution, và result formatting
     * 
     * @param input Input file path
     * @param outputPath Output file path
     * @param minsup MinRareSupport threshold (0.0-1.0)
     * @param maxsup MaxFrequentSupport threshold (0.0-1.0)
     * @param fileName Original filename for documentation
     * @param startTime Start timestamp for performance measurement
     */
    private static void runPrePostRare(String input, String outputPath, double minsup, double maxsup, 
                                    String fileName, long startTime) throws IOException {
        
        System.out.println("\n=== CHẠY PREPOST RARE ===");
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        
        // ===== SETUP OUTPUT FILE =====
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        
        // Ghi header information
        writer.println("=== PREPOST RARE - RARE ITEMSETS MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("MinRareSupport (minsup): " + (minsup * 100) + "%");
        writer.println("MinFrequentSupport (maxsup): " + (maxsup * 100) + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println("Output folder: release/PrePostRare/");
        writer.println();
        
        // Ghi definitions
        writer.println("========== ĐỊNH NGHĨA ==========");
        writer.println("- Rare Item: MRT < Support(X) <= MFT");
        writer.println("- Frequent Item: Support(X) > MFT");
        writer.println("- Infrequent Item: Support(X) <= MRT");
        writer.println();
        
        // ===== DATA CONVERSION =====
        // Convert từ ItemsetTree format sang standard transaction format
        System.out.println("Đang convert dữ liệu từ ItemsetTree format...");
        String convertedFile = "temp_converted_" + System.currentTimeMillis() + ".txt";
        int transactionCount = convertItemsetTreeToStandardFormat(input, convertedFile);
        
        // Ghi database info
        writer.println("========== DATABASE INFO ==========");
        writer.println("Số lượng giao dịch: " + transactionCount);
        writer.println();
        
        // ===== RUN PREPOST RARE ALGORITHM =====
        String tempOutputPath = "temp_prepost_rare_" + System.currentTimeMillis() + ".txt";
        PrePostRare algo = new PrePostRare();
        
        // Execute algorithm
        algo.runAlgorithm(convertedFile, minsup, maxsup, tempOutputPath);
        
        // Print statistics to console
        algo.printStats();
        
        // ===== FORMAT AND WRITE RESULTS =====
        // FIXED: Use getter method instead of direct field access
        writeFormattedResults(writer, tempOutputPath, transactionCount, minsup, maxsup, algo.getOutputCount());
        
        // ===== WRITE PERFORMANCE STATS =====
        writePerformanceStats(writer, startTime);
        
        // ===== CLEANUP =====
        writer.close();
        new File(convertedFile).delete();     // Xóa temp file
        new File(tempOutputPath).delete();    // Xóa temp output
        
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        System.out.println("Thư mục output: release/PrePostRare/");
    }
    
    /**
     * Convert data từ ItemsetTree format sang standard transaction format
     * 
     * ItemsetTree format: transactionId itemId [count]
     * Standard format: item1 item2 item3 ... (mỗi dòng là 1 transaction)
     * 
     * @param inputFile Input file trong ItemsetTree format
     * @param outputFile Output file trong standard format
     * @return Số lượng transactions sau khi convert
     */
    private static int convertItemsetTreeToStandardFormat(String inputFile, String outputFile) throws IOException {
        
        // ===== READ AND PARSE INPUT =====
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        // Skip first line (có thể là header)
        line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            reader.close();
            throw new IOException("File đầu vào rỗng hoặc không hợp lệ");
        }
        
        // ===== PROCESS EACH LINE =====
        while ((line = reader.readLine()) != null) {
            // Skip comment lines và empty lines
            if (line.isEmpty() || line.charAt(0) == '#' || 
                line.charAt(0) == '%' || line.charAt(0) == '@') {
                continue;
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue;  // Invalid line format
            }
            
            // Parse line: transactionId itemId [count]
            int transactionId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            
            // Chỉ thêm nếu count > 0 và item chưa có trong transaction
            if (count > 0) {
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>());
                if (!transactionMap.get(transactionId).contains(itemId)) {
                    transactionMap.get(transactionId).add(itemId);
                }
            }
        }
        reader.close();
        
        // ===== WRITE STANDARD FORMAT =====
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        
        // Sort by transaction ID và sort items trong mỗi transaction
        transactionMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())  // Sort by transactionId
            .forEach(entry -> {
                List<Integer> transaction = entry.getValue();
                transaction.sort(Integer::compareTo);  // Sort items trong transaction
                
                // Ghi transaction: item1 item2 item3 ...
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
    
    /**
     * Đọc kết quả từ temp file và format lại cho output
     * Phân loại theo kích thước itemset và tính percentage support
     * 
     * @param writer Output writer
     * @param tempOutputPath Temp file chứa raw results
     * @param transactionCount Tổng số transactions (để tính percentage)
     * @param minsup MinRareSupport threshold
     * @param maxsup MaxFrequentSupport threshold
     * @param outputCount Số itemsets tìm được (từ algorithm)
     */
    private static void writeFormattedResults(PrintWriter writer, String tempOutputPath, 
                                            int transactionCount, double minsup, double maxsup, 
                                            int outputCount) throws IOException {
        
        // ===== WRITE SECTION HEADER =====
        writer.println("========== RARE ITEMSETS ==========");
        writer.println("Tìm itemsets với support trong khoảng (" + (minsup*100) + "%, " + (maxsup*100) + "%]");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println();
        
        // ===== READ AND PARSE RESULTS =====
        BufferedReader reader = new BufferedReader(new FileReader(tempOutputPath));
        String line;
        Map<Integer, List<String>> itemsetsBySize = new HashMap<>();  // Group by itemset size
        
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            
            if (line.contains("#SUP:")) {
                // Parse line: "item1 item2 ... #SUP: support"
                String[] parts = line.split("#SUP:");
                String itemsPart = parts[0].trim();
                int support = Integer.parseInt(parts[1].trim());
                
                // Calculate percentage support
                double supportPercent = (support * 100.0) / transactionCount;
                
                // Determine itemset size
                int itemsetSize = itemsPart.isEmpty() ? 0 : itemsPart.split(" ").length;
                
                // Format line với percentage
                String formattedLine = String.format("%s #SUP: %d (%.2f%%)", 
                                                itemsPart, support, supportPercent);
                
                // Group by size
                itemsetsBySize.computeIfAbsent(itemsetSize, k -> new ArrayList<>()).add(formattedLine);
            }
        }
        reader.close();
        
        // ===== WRITE RESULTS BY SIZE =====
        int totalCount = 0;
        
        // Duyệt từ 1-itemsets đến 10-itemsets
        for (int size = 1; size <= 10; size++) {
            List<String> itemsets = itemsetsBySize.get(size);
            if (itemsets != null && !itemsets.isEmpty()) {
                
                // Write section header
                writer.println("--- " + size + "-itemsets HIẾM ---");
                System.out.println("--- " + size + "-itemsets HIẾM ---");
                
                // Write all itemsets of this size
                for (String itemset : itemsets) {
                    writer.println(itemset);
                    System.out.println(itemset);  // Also print to console
                    totalCount++;
                }
                
                writer.println();
                System.out.println();
            }
        }
        
        // ===== WRITE SUMMARY =====
        writer.println("========== TỔNG KẾT ==========");
        writer.println("Tổng số rare itemsets tìm được: " + totalCount);
        writer.println("Thuật toán: PrePost Rare");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        writer.println("Output folder: release/PrePostRare/");
        
        // Also print summary to console
        System.out.println("========== TỔNG KẾT ==========");
        System.out.println("Tổng số rare itemsets tìm được: " + totalCount);
        System.out.println("Thuật toán: PrePost Rare");
        
        writer.println();
    }
    
    /**
     * Ghi thông tin hiệu suất (execution time, memory usage)
     * 
     * @param writer Output writer
     * @param startTime Start timestamp
     */
    private static void writePerformanceStats(PrintWriter writer, long startTime) {
        
        // ===== CALCULATE PERFORMANCE METRICS =====
        MemoryLogger.getInstance().checkMemory();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Format execution time as MM:SS.mmm
        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;
        long milliseconds = executionTime % 1000;
        String formattedTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        
        // Format memory usage
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();
        String formattedMemory = String.format("%.2f", maxMemory);
        
        // ===== WRITE PERFORMANCE STATS =====
        writer.println("========== THÔNG TIN HIỆU SUẤT ==========");
        writer.println("Thuật toán: PrePost Rare");
        writer.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        writer.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
        writer.println("Thời gian kết thúc: " + new java.util.Date(endTime));
        writer.println("Output folder: release/PrePostRare/");
        writer.println("Định nghĩa: MRT < Support(X) <= MFT");
        
        // ===== ALSO PRINT TO CONSOLE =====
        System.out.println("========== THÔNG TIN HIỆU SUẤT ==========");
        System.out.println("Thuật toán: PrePost Rare");
        System.out.println("Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]");
        System.out.println("Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB");
    }
}