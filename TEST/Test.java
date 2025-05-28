package TEST;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Test runner để chọn thư mục và chạy Main class trong đó
 */
public class Test {
    
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            // Tìm tất cả Main classes trong các thư mục TEST
            List<String> availableTests = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            
            // Quét thư mục TEST
            File testDir = new File("TEST");
            if (testDir.exists() && testDir.isDirectory()) {
                scanForMainClasses(testDir, "", availableTests, descriptions);
            }
            
            if (availableTests.isEmpty()) {
                System.out.println("Không tìm thấy Main class nào trong thư mục TEST!");
                return;
            }
            
            // Hiển thị menu
            System.out.println("=== CHỌN THUẬT TOÁN ĐỂ CHẠY ===");
            for (int i = 0; i < availableTests.size(); i++) {
                System.out.println((i + 1) + ". " + descriptions.get(i));
            }
            System.out.println((availableTests.size() + 1) + ". Thoát");
            
            // Chọn thuật toán
            System.out.print("Chọn thuật toán (1-" + (availableTests.size() + 1) + "): ");
            int choice = scanner.nextInt();
            
            if (choice == availableTests.size() + 1) {
                System.out.println("Tạm biệt!");
                return;
            }
            
            if (choice < 1 || choice > availableTests.size()) {
                System.out.println("Lựa chọn không hợp lệ!");
                return;
            }
            
            String selectedClass = availableTests.get(choice - 1);
            String selectedDesc = descriptions.get(choice - 1);
            
            System.out.println("Đã chọn: " + selectedDesc);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("ĐANG CHẠY: " + selectedDesc);
            System.out.println("CLASS: " + selectedClass);
            System.out.println("=".repeat(60) + "\n");
            
            // Chạy Main class đã chọn
            runMainClass(selectedClass);
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Quét thư mục để tìm các Main classes
     */
    private static void scanForMainClasses(File dir, String packagePath, 
                                         List<String> availableTests, List<String> descriptions) {
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        // Tạo package path
        String currentPackage = packagePath.isEmpty() ? "TEST" : packagePath;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Đệ quy quét thư mục con
                String newPackagePath = packagePath.isEmpty() ? 
                    "TEST." + file.getName() : 
                    packagePath + "." + file.getName();
                scanForMainClasses(file, newPackagePath, availableTests, descriptions);
                
            } else if (file.getName().startsWith("Main") && file.getName().endsWith(".java")) {
                // Tìm thấy Main class
                String className = file.getName().replace(".java", "");
                String fullClassName = currentPackage + "." + className;
                
                // Tạo description dựa trên tên class và package
                String description = createDescription(currentPackage, className);
                
                availableTests.add(fullClassName);
                descriptions.add(description);
            }
        }
    }
    
    /**
     * Tạo description cho Main class
     */
    private static String createDescription(String packagePath, String className) {
        String[] parts = packagePath.split("\\.");
        String category = parts.length > 1 ? parts[parts.length - 1] : "TEST";
        
        // Xử lý tên thuật toán
        String algorithmName = className.replace("MainTest", "").replace("Main", "");
        
        // Tạo description dựa trên category
        switch (category.toUpperCase()) {
            case "TESTFREEQUENT":
                return "FREQUENT - " + algorithmName;
            case "TESTRARE":
                return "RARE - " + algorithmName;
            default:
                return category + " - " + algorithmName;
        }
    }
    
    /**
     * Chạy Main class
     */
    private static void runMainClass(String fullClassName) {
        try {
            // Load class
            Class<?> clazz = Class.forName(fullClassName);
            
            // Tìm main method
            Method mainMethod = clazz.getMethod("main", String[].class);
            
            // Chạy main method
            String[] args = new String[0]; // Empty args
            mainMethod.invoke(null, (Object) args);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy class: " + fullClassName);
            System.err.println("Đảm bảo class đã được compile!");
            System.err.println("Chạy lệnh compile:");
            System.err.println("javac -cp . " + fullClassName.replace(".", File.separator) + ".java");
        } catch (NoSuchMethodException e) {
            System.err.println("Không tìm thấy main method trong class " + fullClassName);
        } catch (Exception e) {
            System.err.println("Lỗi khi chạy " + fullClassName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}