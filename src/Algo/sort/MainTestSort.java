package Algo.sort;
class MainTestSort {
	public static void main(String[] args) {
		int []arrayInt = new int[]{5,2,6,7,9, 4,2, 1};
		Sort.mergeSort(arrayInt);
		System.out.println(arrayToString(arrayInt));
		System.out.println(arrayToString(arrayInt));
	}
	 public static String arrayToString(int[] a) {
		    StringBuilder result = new StringBuilder();
		    if (a.length > 0) {
		        result.append(a[0]);
		        for (int i=1; i<a.length; i++) {
		            result.append(" ");
		            result.append(a[i]);
		        }
		    }
		    return result.toString();
		}
}

