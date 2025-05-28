package frequentpatterns.eclatrptree;

import java.util.BitSet;

/**
 * Lớp biểu diễn một mục đơn với BitSet trong khai thác mẫu hiếm
 */
public class Item implements Comparable<Item> {
    
    // Giá trị của mục
    private int item;
    
    // BitSet để biểu diễn giao dịch chứa mục
    private BitSet bitset;
    
    // Độ hỗ trợ (số lần xuất hiện)
    private int support;
    
    // Cờ đánh dấu mục hiếm
    private boolean isRare;
    
    /**
     * Hàm khởi tạo
     * @param item giá trị của mục
     * @param bitset bitset biểu diễn giao dịch chứa mục
     */
    public Item(int item, BitSet bitset) {
        this.item = item;
        this.bitset = bitset;
        this.support = bitset.cardinality();
        this.isRare = false;
    }
    
    /**
     * Lấy giá trị của mục
     * @return giá trị của mục
     */
    public int getItem() {
        return item;
    }
    
    /**
     * Lấy BitSet
     * @return BitSet biểu diễn giao dịch chứa mục
     */
    public BitSet getBitset() {
        return bitset;
    }
    
    /**
     * Lấy độ hỗ trợ của mục
     * @return độ hỗ trợ
     */
    public int getSupport() {
        return support;
    }
    
    /**
     * Kiểm tra xem mục có phải hiếm không
     * @return true nếu mục hiếm, false nếu ngược lại
     */
    public boolean isRare() {
        return isRare;
    }
    
    /**
     * Đặt trạng thái hiếm cho mục
     * @param isRare trạng thái hiếm mới
     */
    public void setRare(boolean isRare) {
        this.isRare = isRare;
    }
    
    /**
     * So sánh hai mục theo độ hỗ trợ giảm dần
     */
    @Override
    public int compareTo(Item other) {
        return Integer.compare(other.support, this.support);
    }
    
    @Override
    public String toString() {
        return "{" + item + "}: " + support + (isRare ? " (hiếm)" : " (phổ biến)");
    }
}