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

import frequentpatterns.fin_prepost.PrePost;
import tools.MemoryLogger;
import frequentpatterns.fin_prepost.OutputFormatter;;

/**
 * Chương trình sử dụng thuật toán PrePost/PrePost+ để khai thác các mẫu thường xuyên
 * với cách đọc file giống ItemsetTree và format output giống ItemsetTree
 */
public class MainTestPrePostPlus {

    public static void main(String[] arg) throws IOException {
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
        String inputFile = files[choice - 1].getAbsolutePath();
        String fileName = files[choice - 1].getName();
        System.out.println("Đã chọn tệp: " + fileName);

        // Nhập minsup dưới dạng phần trăm
        System.out.print("Nhập minsup (phần trăm, từ 0 đến 100): ");
        double minsupPercent = scanner.nextDouble();
        while (minsupPercent < 0 || minsupPercent > 100) {
            System.out.print("Minsup không hợp lệ. Vui lòng nhập lại (0-100): ");
            minsupPercent = scanner.nextDouble();
        }

        // Chuyển đổi format dữ liệu trước khi chạy thuật toán
        String convertedFile = "temp_converted_" + fileName;
        int transactionCount = convertDataFormat(inputFile, convertedFile);
        
        // Tính minsup tuyệt đối
        double minsup = minsupPercent / 100.0; // PrePost sử dụng minsup relative
        System.out.println("Minsup tương đối: " + minsup + " (dựa trên " + transactionCount + " giao dịch)");

        // Tạo thư mục release nếu chưa tồn tại
        File releaseDir = new File("release");
        if (!releaseDir.exists()) {
            releaseDir.mkdirs();
        }

        // Tạo tên file output
        String outputBaseName = fileName.replaceAll("\\.[^.]*$", "");
        String finalOutputPath = "release" + File.separator + outputBaseName + "_prepost_output_" + (int)minsupPercent + ".txt";
        System.out.println("Kết quả sẽ được lưu vào file: " + finalOutputPath);

        // Chạy thuật toán PrePost+ và lưu vào file tạm
        String tempOutputPath = "temp_prepost_result.txt";
        PrePost prepost = new PrePost();
        prepost.setUsePrePostPlus(true); // Sử dụng PrePost+ thay vì PrePost
        prepost.runAlgorithm(convertedFile, minsup, tempOutputPath);

        // In thống kê
        prepost.printStats();
        
        // Tính thời gian thực thi tổng
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        double maxMemory = MemoryLogger.getInstance().getMaxMemory();
        
        // Chuyển đổi kết quả sang format giống ItemsetTree
        OutputFormatter.convertToItemsetTreeFormat(tempOutputPath, finalOutputPath, 
                                                 fileName, transactionCount, minsupPercent,
                                                 startTime, executionTime, maxMemory, 
                                                 prepost.outputCount);

        // Xóa các file tạm
        new File(convertedFile).delete();
        new File(tempOutputPath).delete();
        
        System.out.println();
        System.out.println("=== HOÀN THÀNH ===");
        System.out.println("Kết quả đã được ghi vào file: " + finalOutputPath);
        
        scanner.close();
    }

    /**
     * Chuyển đổi format dữ liệu từ định dạng ItemsetTree sang định dạng chuẩn
     * Input: transaction_id item_id [count]
     * Output: item1 item2 item3 ... (một dòng cho mỗi transaction)
     */
    private static int convertDataFormat(String inputFile, String outputFile) throws IOException {
        Map<Integer, List<Integer>> transactionMap = new HashMap<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        
        // Bỏ qua dòng đầu tiên (header)
        reader.readLine();
        
        // Đọc dữ liệu và nhóm theo transaction_id
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
                // Thêm item vào transaction (không lọc gì cả, để PrePost tự xử lý)
                transactionMap.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(itemId);
            }
        }
        reader.close();
        
        // Ghi ra file mới theo định dạng chuẩn
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        for (Map.Entry<Integer, List<Integer>> entry : transactionMap.entrySet()) {
            List<Integer> transaction = entry.getValue();
            
            // Sắp xếp items trong transaction
            transaction.sort(null);
            
            // Ghi transaction
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