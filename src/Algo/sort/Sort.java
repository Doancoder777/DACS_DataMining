package Algo.sort;
public class Sort {
	public static void insertionSort(int [] a){
		for(int j=1; j< a.length; j++){
			int key = a[j];
			int i = j - 1;
			for(; i>=0 && (a[i] > key); i--){
				a[i+1] = a[i];
			}
			a[i+1] = key;
		}
	}
	public static void mergeSort(int [] a){
	      mergeSort(a, 0, a.length-1);
	}
	private static void mergeSort(int [] a, int p, int r){
	      if (p < r)
	      {
	    	 int q = (p+r) >> 1; // divide by 2
	         mergeSort(a, p, q);      
	         mergeSort(a, q+1, r); 
	         merge(a, p, q, r);
	      }
	}
	private static void merge(int [] a, int p, int q, int r){
		int n1 = q-p+1;
		int n2 = r-q;
		int [] tabL = new int[n1+1];
		int [] tabR = new int[n2+1];
		for(int i=0; i<n1; i++){
			tabL[i] = a[p+i];  // -1
		}
		for(int j=0; j<n2; j++){
			tabR[j] = a[q+j+1];
		}
		tabL[n1]= Integer.MAX_VALUE;
		tabR[n2]= Integer.MAX_VALUE;
		int i =0;
		int j =0;
		for(int k=p; k<r+1;k++){
			if(tabL[i] <= tabR[j]){
				a[k] = tabL[i++];
			}else{
				a[k] = tabR[j++];
			}
		}
	}
	public static void bubbleSort(int [] a){
		for(int i=0; i < a.length; i++){
			for(int j= a.length -1; j>= i+1; j--){
				if(a[j] < a[j-1]){
					int temp = a[j];
					a[j] = a[j-1];
					a[j-1] = temp;
				}
			}
		}
	}
	public static void quicksort( int[] a) {
		quicksort(a, 0, a.length-1);
	}
	private static void quicksort( int[] a, int p, int r) {
		if(p < r){
			int q =  partition(a, p, r);
			quicksort(a, p, q-1);
			quicksort(a, q+1, r);
		}
  	}
	 static int partition(int[] a, int p, int r) {
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
}

