package TEST.TESTFREEQUENT;
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
import Algorithrms.frequentpatterns.fin_prepost.PrePost;
import tools.MemoryLogger;
import Algorithrms.frequentpatterns.fin_prepost.OutputFormatter;;
public class MainTestPrePostPlus {
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
        String inputFile = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);
        System.out.print("Nhập minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("Minsup không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }
        String convertedFile = "temp_converted_" + fileName;
        int transactionCount = convertDataFormat(inputFile, convertedFile);
        double minsup = minsupPercent / 100.0; // PrePost sử dụng minsup relative
        System.out.println("Minsup tương đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        String finalOutputPath = "release" + File.separator + outputBaseName + "_prepost_output_" + (int)minsupPercent + ".txt";
        System.out.println("Kết quả sẽ được lưu vào file: " + finalOutputPath);
        String tempOutputPath = "temp_prepost_result.txt";
        PrePost prepost = new PrePost();
        prepost.setUsePrePostPlus(true); // Sử dụng PrePost+ thay vì PrePost
        prepost.runAlgorithm(convertedFile, minsup, tempOutputPath);
        prepost.printStats();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();
        OutputFormatter.convertToItemsetTreeFormat(tempOutputPath, finalOutputPath, 
                                                 fileName, transactionCount, minsupPercent,
                                                 startTime, executionTime, maxMemory, 
                                                 prepost.outputCount);
        new File(convertedFile).delete();
        new File(tempOutputPath).delete();
        System.out.println();
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + finalOutputPath);
        scanner.close();
    }
    private static int convertDataFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
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
            int itemId = Integer.parseInt(parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            if (count > 0) {
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(itemId);
            }
        }
        reader.close();
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            transaction.sort(null);
            for (int i = 0; i < transaction.size(); i++) {
                if (i > 0) writer.print(" ");
                writer.print(transaction.get(i));
            }
            writer.println();
        }
        writer.close();
        return transactionMap.size();
    }
}
