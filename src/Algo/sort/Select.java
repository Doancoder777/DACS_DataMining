package Algo.sort;
import java.util.Random;
public class Select {
	private static Random random = new Random(System.currentTimeMillis());
	public static int randomizedSelect(int[] a, int i, int p, int r) {
		while(true){
			if(p == r){
				return a[p];
			}
			int q = randomizedPartition(a, p, r);  
			int k = q-p+1;
			if(i == k-1){
				return a[q];
			}
			else if(i <k){
				r = q -1;
			}else{
				i = i - k;
				p = q + 1;
			}
		}
  	}
	public static int randomizedSelect(int[] a, int i) {
		int p = 0;
		int r = a.length-1;
		while(true){
			if(p == r){
				return a[p];
			}
			int q = randomizedPartition(a, p, r);  
			int k = q-p+1;
			if(i == k-1){
				return a[q];
			}
			else if(i <k){
				r = q -1;
			}else{
				i = i - k;
				p = q + 1;
			}
		}
  	}
    private static int randomizedPartition(int[] a, int p, int r) {
		int i = 0;
		if(p < r){
			i =  p + random.nextInt(r-p);
		}else{
			i =  r + random.nextInt(p-r);
		}
		swap(a, r, i);
		return partition(a, p, r);  // call the partition method of quicksort.
	}
    private static int partition(int[] a, int p, int r) {
		int x = a[r];
		int i = p - 1;
		for(int j = p; j <= r-1; j++){
			if(a[j] <= x){
				i = i+1;
				swap(a, i, j);
			}
		}
		swap(a, i+1, r);
		return i+1;
	}
	private static void swap(int[] array, int i, int j) {
        int valueI = array[i];
        array[i] = array[j];
        array[j] = valueI;  
    }
	public static double randomizedSelect(double[] a, int i) {
		int p = 0;
		int r = a.length-1;
		while(true){
			if(p == r){
				return a[p];
			}
			int q = randomizedPartition(a, p, r);  
			int k = q-p+1;
			if(i == k-1){
				return a[q];
			}
			else if(i <k){
				r = q -1;
			}else{
				i = i - k;
				p = q + 1;
			}
		}
  	}
    private static int randomizedPartition(double[] a, int p, int r) {
		int i = 0;
		if(p < r){
			i =  p + random.nextInt(r-p);
		}else{
			i =  r + random.nextInt(p-r);
		}
		swap(a, r, i);
		return partition(a, p, r);  // call the partition method of quicksort.
	}
    private static int partition(double[] a, int p, int r) {
    	double x = a[r];
		int i = p - 1;
		for(int j = p; j <= r-1; j++){
			if(a[j] <= x){
				i = i+1;
				swap(a, i, j);
			}
		}
		swap(a, i+1, r);
		return i+1;
	}
	private static void swap(double[] array, int i, int j) {
		double valueI = array[i];
        array[i] = array[j];
        array[j] = valueI;  
    }
}
