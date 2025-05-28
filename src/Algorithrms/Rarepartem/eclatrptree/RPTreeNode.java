package Rarepartem.eclatrptree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Lớp biểu diễn một nút trong cây RPTree
 */
public class RPTreeNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Tập mục được lưu trữ tại nút
    private int[] itemset;
    
    // BitSet biểu diễn giao dịch chứa tập mục
    private BitSet bitset;
    
    // Độ hỗ trợ (số lần xuất hiện)
    private int support;
    
    // Cờ đánh dấu tính hợp lệ để cắt tỉa
    private boolean isValid;
    
    // Danh sách các nút con
    private List<RPTreeNode> children;
    
    /**
     * Hàm khởi tạo
     * @param itemset tập mục lưu trữ trong nút
     * @param bitset bitset biểu diễn giao dịch chứa tập mục
     */
    public RPTreeNode(int[] itemset, BitSet bitset) {
        this.itemset = itemset;
        this.bitset = bitset;
        this.support = bitset != null ? bitset.cardinality() : 0;
        this.isValid = true;
        this.children = new ArrayList<>();
    }
    
    /**
     * Lấy itemset của nút
     * @return mảng các số nguyên biểu diễn itemset
     */
    public int[] getItemset() {
        return itemset;
    }
    
    /**
     * Đặt itemset cho nút
     * @param itemset mảng các số nguyên biểu diễn itemset mới
     */
    public void setItemset(int[] itemset) {
        this.itemset = itemset;
    }
    
    /**
     * Lấy bitset của nút
     * @return bitset biểu diễn giao dịch chứa tập mục
     */
    public BitSet getBitset() {
        return bitset;
    }
    
    /**
     * Đặt bitset cho nút
     * @param bitset bitset mới
     */
    public void setBitset(BitSet bitset) {
        this.bitset = bitset;
        this.support = bitset != null ? bitset.cardinality() : 0;
    }
    
    /**
     * Lấy độ hỗ trợ của nút
     * @return độ hỗ trợ
     */
    public int getSupport() {
        return support;
    }
    
    /**
     * Đặt độ hỗ trợ cho nút
     * @param support độ hỗ trợ mới
     */
    public void setSupport(int support) {
        this.support = support;
    }
    
    /**
     * Kiểm tra xem nút có hợp lệ không
     * @return true nếu nút hợp lệ, false nếu ngược lại
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * Đặt trạng thái hợp lệ cho nút
     * @param isValid trạng thái hợp lệ mới
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }
    
    /**
     * Lấy danh sách các nút con
     * @return danh sách các nút con
     */
    public List<RPTreeNode> getChildren() {
        return children;
    }
    
    /**
     * Thêm một nút con
     * @param child nút con cần thêm
     */
    public void addChild(RPTreeNode child) {
        this.children.add(child);
    }
    
    /**
     * Xóa một nút con
     * @param child nút con cần xóa
     * @return true nếu xóa thành công, false nếu ngược lại
     */
    public boolean removeChild(RPTreeNode child) {
        return this.children.remove(child);
    }
    
    /**
     * Trả về biểu diễn chuỗi của nút
     * @param indent khoảng thụt lề
     * @return chuỗi biểu diễn nút
     */
    public String toString(String indent) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(indent);
        if (itemset == null) {
            sb.append("{}");
        } else {
            sb.append("{");
            for (int i = 0; i < itemset.length; i++) {
                sb.append(itemset[i]);
                if (i < itemset.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        }
        sb.append(": ").append(support).append("\n");
        
        for (RPTreeNode child : children) {
            sb.append(child.toString(indent + "  "));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toString("");
    }
}