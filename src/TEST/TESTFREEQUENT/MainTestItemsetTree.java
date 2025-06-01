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
import Algorithrms.frequentpatterns.itemsettree.HashTableIT;
import Algorithrms.frequentpatterns.itemsettree.ItemsetTree;
import patterns.itemset_array_integers_with_count.Itemset;
import tools.MemoryLogger;
public class MainTestItemsetTree {
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
        String input = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);
        System.out.print("Nhập minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("Minsup không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", ""); // Loại bỏ extension
        String outputPath = "release" + File.separator + outputBaseName + "_output_" + (int)minsupPercent + ".txt";
        System.out.println("Kết quả sẽ được lưu vào file: " + outputPath);
        int transactionCount = countTransactions(input);
        int minsup = (int) Math.ceil((minsupPercent / 100.0) * transactionCount);
        System.out.println("Minsup tuyệt đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        writer.println("=== ITEMSET TREE - FREQUENT PATTERN MINING ===");
        writer.println("File dữ liệu: " + fileName);
        writer.println("Minsup tuyệt đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");
        writer.println("Minsup tương đối: " + minsupPercent + "%");
        writer.println("Thời gian bắt đầu: " + new java.util.Date(startTime));
        writer.println();
        ItemsetTree itemsetTree = new ItemsetTree(minsup);
        itemsetTree.buildTree(input);
        itemsetTree.printStatistics();
        writer.println("========== ITEMSET TREE CONSTRUCTION - STATS ============");
        writer.println(" Số lượng giao dịch: " + itemsetTree.getTransactionCount());
        writer.println();
        System.out.println("ĐÂY LÀ CÂY:");
        writer.println("ĐÂY LÀ CÂY:");
        String treeString = itemsetTree.toString();
        System.out.println(treeString);
        writer.println(treeString);
        writer.println();
        List<Itemset> allFrequentItemsets = mineAllFrequentItemsets(itemsetTree, input, minsup);
        Collections.sort(allFrequentItemsets, new Comparator<Itemset>() {
            @Override
            public int compare(Itemset o1, Itemset o2) {
                return Integer.compare(o2.support, o1.support);
            }
        });
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
        MemoryLogger.getInstance().checkMemory();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;
        long milliseconds = executionTime % 1000;
        String formattedTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();
        String formattedMemory = String.format("%.2f", maxMemory);
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
    private static List<Itemset> mineAllFrequentItemsets(ItemsetTree itemsetTree, String inputFile, int minsup) throws IOException {
        List<Itemset> allFrequentItemsets = new ArrayList<>();
        Set<Integer> allItems = findAllItems(inputFile);
        List<Integer> frequentItems = new ArrayList<>();
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
        Collections.sort(frequentItems);
        System.out.println("Tìm thấy " + frequentItems.size() + " frequent 1-itemsets");
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
    private static List<List<Integer>> generateCandidates(List<List<Integer>> previousLevel, int k) {
        List<List<Integer>> candidates = new ArrayList<>();
        for (int i = 0; i < previousLevel.size(); i++) {
            for (int j = i + 1; j < previousLevel.size(); j++) {
                List<Integer> itemset1 = previousLevel.get(i);
                List<Integer> itemset2 = previousLevel.get(j);
                boolean canJoin = true;
                for (int idx = 0; idx < k - 2; idx++) {
                    if (!itemset1.get(idx).equals(itemset2.get(idx))) {
                        canJoin = false;
                        break;
                    }
                }
                if (canJoin && !itemset1.get(k-2).equals(itemset2.get(k-2))) {
                    List<Integer> candidate = new ArrayList<>(itemset1);
                    candidate.add(itemset2.get(k-2));
                    Collections.sort(candidate); // Đảm bảo thứ tự
                    if (!containsCandidate(candidates, candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
        }
        return candidates;
    }
    private static boolean containsCandidate(List<List<Integer>> candidates, List<Integer> candidate) {
        for (List<Integer> existing : candidates) {
            if (existing.equals(candidate)) {
                return true;
            }
        }
        return false;
    }
    private static Set<Integer> findAllItems(String inputFilePath) throws IOException {
        Set<Integer> items = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("%") || line.startsWith("@")) {
                continue; // Bỏ qua các dòng bình luận hoặc trống
            }
            String[] parts = line.trim().split(" ");
            if (parts.length < 2) {
                continue; // Bỏ qua dòng không đủ dữ liệu
            }
            int itemId = Integer.parseInt(parts[1]);
            items.add(itemId);
        }
        reader.close();
        return items;
    }
    private static int countTransactions(String inputFilePath) throws IOException {
        Set<Integer> transactionIds = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        reader.readLine();
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

