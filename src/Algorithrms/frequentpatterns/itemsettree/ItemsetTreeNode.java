package frequentpatterns.itemsettree;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class ItemsetTreeNode implements Serializable{
	
	// itemset
	int[] itemset;
	// độ hỗ trợ
	int support;
	// danh sách các nút con
	Collection<ItemsetTreeNode> childs = new HashSet<ItemsetTreeNode>();
	
	/**
	 * Hàm khởi tạo
	 * @param itemset itemset được lưu trữ trong nút này.
	 * @param support độ hỗ trợ được liên kết với nút này.
	 */
	public ItemsetTreeNode(int[] itemset, int support){
		this.itemset = itemset;
		this.support = support;
	}

	/**
	 * Trả về biểu diễn chuỗi của nút này
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
		buffer.append("\n");
		
		for(ItemsetTreeNode node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	
	public String toString(){
		return toString(new StringBuilder(), "  ");
	}
	
}