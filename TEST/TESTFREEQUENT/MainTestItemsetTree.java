package TEST.TESTFREEQUENT;

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

import frequentpatterns.itemsettree.HashTableIT;
import frequentpatterns.itemsettree.ItemsetTree;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;

/**
 * Chương trình sử dụng cấu trúc dữ liệu cây itemset để khai thác TẤT CẢ các mẫu thường xuyên từ tệp dữ liệu
 * và xuất kết quả ra thư mục release.
 */
public class MainTestItemsetTree {

    public static void main(String[] arg) throws IOException {
        // Đặt lại bộ đếm bộ nhớ
        MemoryLogger.getInstance().reset();
        
        // Đánh dấu thời gian bắt đầu thuật toán
        long startTime = System.currentTimeMillis();
        
        // Đường dẫn thư mục chứa dữ liệu (relative path từ project root)
        String dataDir = "Data";

        // Quét thư mục để lấy danh sách tệp
        File directory = new File(dataDir);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".hui"));
        if (files == null || files.length == 0) {
            System.out.println("Không tìm thấy tệp dữ liệu trong thư mục: " + dataDir);
            return;
        }

        // Hiển thị danh sách tệp và yêu cầu người dùng chọn
        System.out.println("Danh sách tệp dữ liệu trong thư mục:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        System.out.print("Vui lòng chọn số thứ tự của tệp (1-" + files.length + "): ");

        Scanner scanner = new Scanner(System.in);
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

        // Tạo tên file output với hậu tố là giá trị minsup và lưu trong thư mục release
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", ""); // Loại bỏ extension
        String outputPath = "release" + File.separator + outputBaseName + "_output_" + (int)minsupPercent + ".txt";
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);

        // Đếm số giao dịch trong file để tính minsup tuyệt đối
        int transactionCount = countTransactions(input);
        int minsup = (int) Math.ceil((minsupPercent / 100.0) * transactionCount);
        System.out.println("Minsup tuyệt đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");
        
        // Tạo file output
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== ITEMSET TREE - FREQUENT PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("Minsup tuyệt đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");
        writer.println("Minsup tương đối: " + minsupPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println();

        // Xây dựng cây itemset với minsup
        ItemsetTree itemsetTree = new ItemsetTree(minsup);
        itemsetTree.buildTree(input);
        
        // In thống kê
        itemsetTree.printStatistics();
        writer.println("========== ITEMSET TREE CONSTRUCTION - STATS ============");
        writer.println(" Số lượng giao dịch: " + itemsetTree.getTransactionCount());
        writer.println();
        
        // Xuất cây
        System.out.println("ĐÂY LÀ CÂY:");
        writer.println("ĐÂY LÀ CÂY:");
        String treeString = itemsetTree.toString();
        System.out.println(treeString);
        writer.println(treeString);
        writer.println();

        // ===== KHAI THÁC TẤT CẢ FREQUENT ITEMSETS =====
        List<Itemset> allFrequentItemsets = mineAllFrequentItemsets(itemsetTree, input, minsup);

        // Sắp xếp frequent itemsets theo support giảm dần
        Collections.sort(allFrequentItemsets, new Comparator<Itemset>() {
            @Override
            public int compare(Itemset o1, Itemset o2) {
                return Integer.compare(o2.support, o1.support);
            }
        });

        // Ghi kết quả ra file và màn hình
        String header = "========== TẤT CẢ CÁC ITEMSET THƯỜNG XUYÊN VỚI MINSUP >= " + minsup + " ==========";
        System.out.println(header);
        writer.println(header);
        
        int count = 0;
        for (Itemset itemset : allFrequentItemsets) {
            String line = String.format("%-30s support: %d", "[" + itemset.toString() + "]", itemset.support);
            System.out.println(line);
            writer.println(line);
            count++;
        }
        
        writer.println();
        String footer = "========== TỔNG KẾT ==========";
        System.out.println(footer);
        writer.println(footer);
        
        String summary = "Tổng số itemset thường xuyên tìm được: " + count;
        System.out.println(summary);
        writer.println(summary);

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
        
        // In thông tin hiệu suất
        writer.println();
        String performanceInfo = "========== THÔNG TIN HIỆU SUẤT ==========";
        System.out.println(performanceInfo);
        writer.println(performanceInfo);
        
        String timeInfo = "Tổng thời gian thực thi: " + formattedTime + " (phút:giây.mili giây) [" + executionTime + " ms]";
        System.out.println(timeInfo);
        writer.println(timeInfo);
        
        String memoryInfo = "Bộ nhớ tối đa sử dụng: " + formattedMemory + " MB";
        System.out.println(memoryInfo);
        writer.println(memoryInfo);
        
        String endTimeInfo = "Thời gian kết thúc: " + new java.util.Date(endTime);
        writer.println(endTimeInfo);

        writer.close();
        System.out.println();
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + outputPath);
        
        scanner.close();
    }
    
    /**
     * Khai thác TẤT CẢ frequent itemsets từ ItemsetTree
     * @param itemsetTree cây itemset đã được xây dựng
     * @param inputFile file dữ liệu input
     * @param minsup ngưỡng support tối thiểu
     * @return danh sách tất cả frequent itemsets
     */
    private static List<Itemset> mineAllFrequentItemsets(ItemsetTree itemsetTree, String inputFile, int minsup) throws IOException {
        List<Itemset> allFrequentItemsets = new ArrayList<>();
        
        // Tìm tất cả các mục xuất hiện trong dữ liệu
        Set<Integer> allItems = findAllItems(inputFile);
        List<Integer> frequentItems = new ArrayList<>();
        
        // Bước 1: Tìm tất cả 1-itemsets thường xuyên
        for (int item : allItems) {
            int[] itemset = new int[]{item};
            int support = itemsetTree.getSupportOfItemset(itemset);
            if (support >= minsup) {
                frequentItems.add(item);
                Itemset is = new Itemset(itemset);
                is.support = support;
                allFrequentItemsets.add(is);
            }
        }
        
        // Sắp xếp frequent items để đảm bảo thứ tự
        Collections.sort(frequentItems);
        
        System.out.println("Tìm thấy " + frequentItems.size() + " frequent 1-itemsets");
        
        // Bước 2: Tìm frequent k-itemsets với k >= 2
        // Sử dụng phương pháp level-wise: từ frequent 1-itemsets sinh ra 2-itemsets, etc.
        List<List<Integer>> currentLevel = new ArrayList<>();
        for (Integer item : frequentItems) {
            List<Integer> singleItem = new ArrayList<>();
            singleItem.add(item);
            currentLevel.add(singleItem);
        }
        
        int k = 2;
        while (!currentLevel.isEmpty() && k <= frequentItems.size()) {
            System.out.println("Đang tìm frequent " + k + "-itemsets...");
            
            List<List<Integer>> candidates = generateCandidates(currentLevel, k);
            List<List<Integer>> nextLevel = new ArrayList<>();
            
            for (List<Integer> candidate : candidates) {
                // Chuyển List thành array
                int[] itemsetArray = candidate.stream().mapToInt(i -> i).toArray();
                int support = itemsetTree.getSupportOfItemset(itemsetArray);
                
                if (support >= minsup) {
                    Itemset is = new Itemset(itemsetArray);
                    is.support = support;
                    allFrequentItemsets.add(is);
                    nextLevel.add(candidate);
                }
            }
            
            System.out.println("Tìm thấy " + nextLevel.size() + " frequent " + k + "-itemsets");
            currentLevel = nextLevel;
            k++;
        }
        
        return allFrequentItemsets;
    }
    
    /**
     * Sinh candidates cho level k từ frequent itemsets của level k-1
     * @param previousLevel frequent itemsets của level trước
     * @param k level hiện tại
     * @return danh sách candidates
     */
    private static List<List<Integer>> generateCandidates(List<List<Integer>> previousLevel, int k) {
        List<List<Integer>> candidates = new ArrayList<>();
        
        // Với mỗi cặp itemset trong level trước
        for (int i = 0; i < previousLevel.size(); i++) {
            for (int j = i + 1; j < previousLevel.size(); j++) {
                List<Integer> itemset1 = previousLevel.get(i);
                List<Integer> itemset2 = previousLevel.get(j);
                
                // Kiểm tra xem có thể join được không (k-2 items đầu giống nhau)
                boolean canJoin = true;
                for (int idx = 0; idx < k - 2; idx++) {
                    if (!itemset1.get(idx).equals(itemset2.get(idx))) {
                        canJoin = false;
                        break;
                    }
                }
                
                if (canJoin && !itemset1.get(k-2).equals(itemset2.get(k-2))) {
                    // Tạo candidate mới
                    List<Integer> candidate = new ArrayList<>(itemset1);
                    candidate.add(itemset2.get(k-2));
                    Collections.sort(candidate); // Đảm bảo thứ tự
                    
                    // Kiểm tra xem candidate đã tồn tại chưa
                    if (!containsCandidate(candidates, candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        
        return candidates;
    }
    
    /**
     * Kiểm tra xem candidate đã tồn tại trong danh sách chưa
     */
    private static boolean containsCandidate(List<List<Integer>> candidates, List<Integer> candidate) {
        for (List<Integer> existing : candidates) {
            if (existing.equals(candidate)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tìm tất cả các mục xuất hiện trong dữ liệu bằng cách đọc file
     */
    private static Set<Integer> findAllItems(String inputFilePath) throws IOException {
        Set<Integer> items = new HashSet<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        
        // Bỏ qua dòng đầu tiên (thường chứa số giao dịch và số mục)
        reader.readLine();
        
        // Đọc các dòng còn lại
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue; // Bỏ qua các dòng bình luận hoặc trống
            }
            
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue; // Bỏ qua dòng không đủ dữ liệu
            }
            
            // Định dạng chuẩn: transaction_id item_id [count]
            int itemId = Integer.parseInt(parts[1]);
            items.add(itemId);
        }
        
        reader.close();
        return items;
    }
    
    /**
     * Đếm số lượng giao dịch trong file
     */
    private static int countTransactions(String inputFilePath) throws IOException {
        Set<Integer> transactionIds = new HashSet<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        
        // Bỏ qua dòng đầu tiên
        reader.readLine();
        
        // Đọc các dòng còn lại và đếm số lượng transaction_id duy nhất
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
}
