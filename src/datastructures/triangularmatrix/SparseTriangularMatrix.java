package datastructures.triangularmatrix;
import java.util.HashMap;
import java.util.Map;
import Algo.charm.AlgoCharm_Bitset;
import Algo.eclat.AlgoEclat;
import Algo.eclat.AlgoEclat_Bitset;
public class SparseTriangularMatrix implements AbstractTriangularMatrix {
	private Map<Integer, Map<Integer, Integer>> matrix = new HashMap<Integer, Map<Integer, Integer>>();
	public SparseTriangularMatrix(){
	}
	public SparseTriangularMatrix(int itemCount){
	}
	public String toString() {
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < matrix.keySet().size(); i++) {
			temp.append(i);
			temp.append(": ");
			for (int j = 0; j < matrix.get(i).size(); j++) {
				temp.append(matrix.get(i).get(j)); // add the value at position i,j
				temp.append(" ");
			}
			temp.append("\n");
		}
		return temp.toString();
	}
	public void incrementCount(int i, int j) {
		if(i < j) {
			Map<Integer, Integer> mapCount = matrix.get(i);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(i, mapCount);
				mapCount.put(j, 1);
			}else {
				Integer count = mapCount.get(j);
				if(count == null) {
					mapCount.put(j, 1);
				}else {
					mapCount.put(j, ++count);
				}
			}
		}else {
			Map<Integer, Integer> mapCount = matrix.get(j);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(j, mapCount);
				mapCount.put(i, 1);
			}else {
				Integer count = mapCount.get(i);
				if(count == null) {
					mapCount.put(i, 1);
				}else {
					mapCount.put(i, ++count);
				}
			}
		}
	}
	public int getSupportForItems(int i, int j){
		if(i < j) {
			Map<Integer, Integer> mapCount = matrix.get(i);
			if(mapCount == null) {
				return 0;
			}else {
				Integer count = mapCount.get(j);
				if(count == null) {
					return 0;
				}else {
					return count;
				}
			}
		}else {
			Map<Integer, Integer> mapCount = matrix.get(j);
			if(mapCount == null) {
				return 0;
			}else {
				Integer count = mapCount.get(i);
				if(count == null) {
					return 0;
				}else {
					return count;
				}
			}
		}
	}
	public void setSupport(Integer i, Integer j, int support) {
		if(i < j) {
			Map<Integer, Integer> mapCount = matrix.get(i);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(i, mapCount);
				mapCount.put(j, support);
			}else {
				mapCount.put(j, support);
			}  
		}else {
			Map<Integer, Integer> mapCount = matrix.get(j);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(j, mapCount);
				mapCount.put(i, support);
			}else {
				mapCount.put(i, support);
			}  
		}
	}
	public void setSupport(int i, int j, int support) {
		if(i < j) {
			Map<Integer, Integer> mapCount = matrix.get(i);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(i, mapCount);
				mapCount.put(j, support);
			}else {
				mapCount.put(j, support);
			}  
		}else {
			Map<Integer, Integer> mapCount = matrix.get(j);
			if(mapCount == null) {
				mapCount = new HashMap<Integer,Integer>();
				matrix.put(j, mapCount);
				mapCount.put(i, support);
			}else {
				mapCount.put(i, support);
			}  
		}
	}
}

