package frequentpatterns.fin_prepost;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để chuyển đổi format output của PrePost sang giống ItemsetTree
 */
public class OutputFormatter {
    
    /**
     * Class đơn giản để lưu itemset và support
     */
    public static class ItemsetResult {
        public List<Integer> items;
        public int support;
        
        public ItemsetResult() {
            this.items = new ArrayList<>();
        }
        
        public String itemsToString() {
            if (items == null || items.isEmpty()) {
                return "";
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(items.get(i));
            }
            return sb.toString();
        }
    }
    
    /**
     * Chuyển đổi kết quả từ format PrePost sang format giống ItemsetTree
     * 
     * @param prepostOutputPath đường dẫn file kết quả PrePost (format: "item1 item2 #SUP: support")
     * @param finalOutputPath đường dẫn file output cuối cùng (format giống ItemsetTree)
     * @param fileName tên file dữ liệu gốc
     * @param transactionCount số lượng transaction
     * @param minsupPercent minsup phần trăm
     * @param startTime thời gian bắt đầu
     * @param executionTime thời gian thực thi (ms)
     * @param maxMemory bộ nhớ tối đa (MB)
     * @param outputCount số lượng itemset tìm được
     * @throws IOException nếu có lỗi đọc/ghi file
     */
    public static void convertToItemsetTreeFormat(String prepostOutputPath, String finalOutputPath,
                                                String fileName, int transactionCount, double minsupPercent,
                                                long startTime, long executionTime, double maxMemory,
                                                int outputCount) throws IOException {
        
        List<ItemsetResult> itemsets = new ArrayList<>();
        
        // Đọc kết quả từ PrePost
        BufferedReader reader = new BufferedReader(new FileReader(prepostOutputPath));
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            
            // Parse dòng: "item1 item2 item3 #SUP: support"
            if (line.contains("#SUP:")) {
                String[] parts = line.split("#SUP:");
                String itemsPart = parts[0].trim();
                int support = Integer.parseInt(parts[1].trim());
                
                // Parse items
                ItemsetResult result = new ItemsetResult();
                if (!itemsPart.isEmpty()) {
                    String[] itemStrings = itemsPart.split(" ");
                    for (String itemStr : itemStrings) {
                        if (!itemStr.trim().isEmpty()) {
                            result.items.add(Integer.parseInt(itemStr.trim()));
                        }
                    }
                }
                result.support = support;
                itemsets.add(result);
            }
        }
        reader.close();

        // Sắp xếp theo support giảm dần (giống ItemsetTree)
        itemsets.sort((a, b) -> Integer.compare(b.support, a.support));

        // Ghi ra file theo format ItemsetTree
        PrintWriter writer = new PrintWriter(new FileWriter(finalOutputPath));
        
        // Header giống ItemsetTree
        writer.println("=== PREPOST+ - FREQUENT PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        int minsupAbsolute = (int) Math.ceil((minsupPercent / 100.0) * transactionCount);
        writer.println("Minsup tuyệt đối: " + minsupAbsolute + " (dựa trên " + transactionCount + " giao dịch)");
        writer.println("Minsup tương đối: " + minsupPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println();

        // Ghi kết quả giống ItemsetTree
        String header = "========== TẤT CẢ CÁC ITEMSET THƯỜNG XUYÊN VỚI MINSUP >= " + minsupAbsolute + " ==========";
        System.out.println(header);
        writer.println(header);
        
      // In từng itemset giống ItemsetTree: "[item1 item2] support: X"
        for (ItemsetResult itemset : itemsets) {
            String outputLine = String.format("%-30s support: %d", "[" + itemset.itemsToString() + "]", itemset.support);
            System.out.println(outputLine);
            writer.println(outputLine);
        }
        
        writer.println();
        String footer = "========== TỔNG KẾT ==========";
        System.out.println(footer);
        writer.println(footer);
        
        String summary = "Tổng số itemset thường xuyên tìm được: " + outputCount;
        System.out.println(summary);
        writer.println(summary);

        // Thông tin hiệu suất giống ItemsetTree
        writer.println();
        String performanceInfo = "========== THÔNG TIN HIỆU SUẤT ==========";
        System.out.println(performanceInfo);
        writer.println(performanceInfo);
        
        // Format thời gian giống ItemsetTree
        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;
        long milliseconds = executionTime % 1000;
        String formattedTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        
        String timeInfo = "Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]";
        System.out.println(timeInfo);
        writer.println(timeInfo);
        
        String memoryInfo = "Bộ nhớ tối đa sử dụng: " + String.format("%.2f", maxMemory) + " MB";
        System.out.println(memoryInfo);
        writer.println(memoryInfo);
        
        String endTimeInfo = "Thời gian kết thúc: " + new java.util.Date(startTime + executionTime);
        writer.println(endTimeInfo);
        
        writer.println("Thuật toán: PrePost+");
        
        writer.close();
    }
}