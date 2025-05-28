package frequentpatterns.eclatrptree;

import java.util.Arrays;

/**
 * Lớp biểu diễn một mẫu hiếm tìm thấy bởi thuật toán Eclat_RPTree
 */
public class RarePattern implements Comparable<RarePattern> {
    
    // Tập mục
    private int[] itemset;
    
    // Độ hỗ trợ
    private int support;
    
    /**
     * Hàm khởi tạo
     * @param itemset tập mục
     * @param support độ hỗ trợ
     */
    public RarePattern(int[] itemset, int support) {
        this.itemset = itemset;
        this.support = support;
    }
    
    /**
     * Lấy tập mục
     * @return mảng các số nguyên biểu diễn tập mục
     */
    public int[] getItemset() {
        return itemset;
    }
    
    /**
     * Lấy độ hỗ trợ
     * @return độ hỗ trợ của mẫu hiếm
     */
    public int getSupport() {
        return support;
    }
    
    /**
     * So sánh hai mẫu hiếm, đầu tiên theo kích thước, sau đó theo support
     */
    @Override
    public int compareTo(RarePattern other) {
        // Sắp xếp trước tiên theo kích thước
        int sizeCompare = Integer.compare(this.itemset.length, other.itemset.length);
        if (sizeCompare != 0) return sizeCompare;
        
        // Sau đó theo support (tăng dần vì là mẫu hiếm)
        return Integer.compare(this.support, other.support);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RarePattern that = (RarePattern) obj;
        return support == that.support && Arrays.equals(itemset, that.itemset);
    }
    
    @Override
    public int hashCode() {
        int result = support;
        result = 31 * result + Arrays.hashCode(itemset);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < itemset.length; i++) {
            sb.append(itemset[i]);
            if (i < itemset.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("}: ");
        sb.append(support);
        return sb.toString();
    }
}