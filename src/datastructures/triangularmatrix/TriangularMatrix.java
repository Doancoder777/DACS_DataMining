package datastructures.triangularmatrix;
import Algo.charm.AlgoCharm_Bitset;
import Algo.eclat.AlgoEclat;
import Algo.eclat.AlgoEclat_Bitset;
public class TriangularMatrix implements AbstractTriangularMatrix {
	protected int[][] matrix;
	protected int elementCount;
	public TriangularMatrix(int elementCount){
		this.elementCount = elementCount;
		matrix = new int[elementCount-1][]; // -1 cause we want it shorter of 1 element
		for(int i=0; i< elementCount-1; i++){ // -1 cause we want it shorter of 1 element
			matrix[i] = new int[elementCount - i -1];
		}
	}
	public int getSizeIthRow(int i){
		return matrix[i].length;
	}
	public static void main(String[] args) {
		TriangularMatrix a = new TriangularMatrix(5);
		System.out.println(a.toString());
		a.incrementCount(1, 0);
		System.out.println(a.toString());
		a.incrementCount(1, 4);
		a.incrementCount(1, 3);
		a.incrementCount(2, 4);
		a.incrementCount(2, 4);
		a.incrementCount(4, 3);
		System.out.println(a.toString());
		a.incrementCount(0, 2);
		a.incrementCount(0, 3);
		a.incrementCount(0, 4);
		System.out.println(a.toString());
	}
	public String toString() {
		System.out.println("Element count = " + elementCount);
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < matrix.length; i++) {
			temp.append(i);
			temp.append(": ");
			for (int j = 0; j < matrix[i].length; j++) {
				temp.append(matrix[i][j]); // add the value at position i,j
				temp.append(" ");
			}
			temp.append("\n");
		}
		return temp.toString();
	}
	public void incrementCount(int i, int j) {
		if(j < i){
			matrix[elementCount - i -1][j]++;  // so that id is always smaller than j
		}else{
			matrix[elementCount - j -1][i]++;
		}
	}
	public void incrementCount(int i, int j, int value) {
		if(j < i){
			matrix[elementCount - i -1][j] += value;  
		}else{
			matrix[elementCount - j -1][i] += value;
		}
	}
	public int getSupportForItems(int i, int j){
		if(j < i){
			return matrix[elementCount - i -1][j];  // so that id is always smaller than id2
		}else{
			return matrix[elementCount - j -1][i];
		}
	}
	public void setSupport(Integer i, Integer j, int support) {
		if(j < i){
			matrix[elementCount - i -1][j] = support;  // so that id is always smaller than j
		}else{
			matrix[elementCount - j -1][i] = support;
		}
	}
	public void setSupport(int i, int j, int support) {
		if(j < i){
			matrix[elementCount - i -1][j] = support;  // so that id is always smaller than j
		}else{
			matrix[elementCount - j -1][i] = support;
		}
	}
	public int getElementCount(){
		return elementCount;
	}
}

