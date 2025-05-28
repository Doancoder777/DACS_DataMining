package frequentpatterns.itemsettree;

import java.util.ArrayList;
import java.util.List;

import patterns.itemset_array_integers_with_count.Itemset;
 

public class HashTableIT {
	
	// mảng nội bộ của bảng băm
	public List<Itemset>[] table;
	
	/**
	 * Hàm khởi tạo
	 * @param size kích thước của mảng nội bộ
	 */
	public HashTableIT(int size){
		table = new ArrayList[size];
	}
	

	/**
	 * Thêm một itemset vào bảng băm.
	 * @param items một itemset được biểu diễn dưới dạng mảng int.
	 * @param support độ hỗ trợ của itemset
	 */
	public void put(int[] items, int support) {
		// tính toán mã băm của itemset để biết
		// nơi để chèn nó
		int hashcode = hashCode(items);
		// nếu không có danh sách tại vị trí đó
		if(table[hashcode] ==  null){
			// tạo một danh sách mới để lưu trữ itemset này và các va chạm trong tương lai
			table[hashcode] = new ArrayList<Itemset>();
			// tạo một đối tượng itemset
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			// thêm nó vào danh sách
			table[hashcode].add(itemset);
		}else{
			// Ngược lại, điều đó có nghĩa là có một va chạm.
			// Chúng ta sẽ kiểm tra xem itemset đã có ở đó chưa.
			
			// Đối với mỗi itemset đã có tại vị trí đó trong bảng băm
			for(Itemset existingItemset : table[hashcode]){
				// nếu itemset là cái mà chúng ta muốn thêm
				if(same(items, existingItemset.itemset)){
					// cập nhật số lượng hỗ trợ của nó và sau đó dừng lại
					existingItemset.support += support; 
					return;
				}
			}
			// ngược lại, nó chưa có ở đó, vì vậy
			// tạo một itemset mới
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			// và thêm nó vào danh sách va chạm cho vị trí đó
			table[hashcode].add(itemset);
		}
	}

	/**
	 * Tính toán mã băm của một itemset.
	 * @param items một itemset
	 * @return mã băm dưới dạng số nguyên
	 */
	public int hashCode(int[] items){
		// mã băm là tổng của mỗi mục i nhân với i * 10, chia lấy dư
		// cho kích thước của mảng nội bộ của bảng băm.
		int hashcode = 0;
		for (int i=0; i< items.length; i++) {
			hashcode += (items[i] + (i*10));
	    }
		// để sửa lỗi tràn kích thước của một số nguyên
		if(hashcode < 0){
			hashcode = 0 - hashcode;
		}
		return (hashcode % items.length);
	}
	
	/**
	 * Phương thức này kiểm tra xem hai itemset có giống nhau không
	 * @param itemset1 itemset thứ nhất
	 * @param itemset2 itemset thứ hai
	 * @return true nếu chúng giống nhau, ngược lại false
	 */
	private boolean same(int[] itemset1, int[] itemset2) {
		// nếu một trong số chúng là null, trả về false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// nếu kích thước không giống nhau, trả về false
		if(itemset1.length != itemset2.length){
			return false;
		}
		// duyệt qua các mục và nếu một mục không giống nhau, trả về false
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		// chúng giống nhau, trả về true
		return true;
	}
}