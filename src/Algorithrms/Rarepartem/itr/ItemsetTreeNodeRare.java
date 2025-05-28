package Rarepartem.itr;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class ItemsetTreeNodeRare implements Serializable{
	
	// FIXED: Thêm public để truy cập từ package khác
	public int[] itemset;
	public int support;
	public Collection<ItemsetTreeNodeRare> childs = new HashSet<ItemsetTreeNodeRare>();
	
	/**
	 * Hàm khởi tạo
	 * @param itemset itemset được lưu trữ trong nút này.
	 * @param support độ hỗ trợ được liên kết với nút này.
	 */
	public ItemsetTreeNodeRare(int[] itemset, int support){
		this.itemset = itemset;
		this.support = support;
	}

	/**
	 * ENHANCED: Trả về biểu diễn chuỗi của nút này với thông tin pattern type
	 * @param buffer một strinbuffer để nối một biểu diễn chuỗi
	 * @param space khoảng thụt lề nên được sử dụng trên mỗi dòng
	 * @return bộ đệm đã cập nhật dưới dạng chuỗi.
	 */
	public String toString(StringBuilder buffer, String space){
		buffer.append(space);
		if(itemset == null){
			buffer.append("{}");
		}else{
			buffer.append("[");
			for(Integer item : itemset){
				buffer.append(item);
				buffer.append(" ");
			}
			buffer.append("]");
		}
		buffer.append("   sup=");
		buffer.append(support);
		
		// ADDED: Thông tin phân loại pattern (sử dụng ngưỡng mặc định)
		// Trong thực tế, có thể truyền MRT/MFT từ tree parent
		String patternType = getPatternType(support);
		buffer.append(" [").append(patternType).append("]");
		
		buffer.append("\n");
		
		for(ItemsetTreeNodeRare node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	
	/**
	 * ADDED: Xác định loại pattern dựa trên support
	 * Note: Sử dụng ngưỡng mặc định, trong thực tế nên lấy từ tree
	 */
	private String getPatternType(int support) {
		// Ngưỡng mặc định - có thể được inject từ tree parent
		int tempMRT = 1;  
		int tempMFT = 5;  
		
		if(support <= tempMRT) {
			return "NOISE";
		} else if(support > tempMRT && support <= tempMFT) {
			return "RARE";
		} else {
			return "FREQUENT";
		}
	}
	
	public String toString(){
		return toString(new StringBuilder(), "  ");
	}
	
}