package datastructures.kdtree;
import java.util.ArrayList;
import java.util.List;
import patterns.cluster.DoubleArray;
class MainTestKDTree_NearestNeighboor {
	public static void main(String[] args) {
		KDTree tree = new KDTree();
		List<DoubleArray> points = new ArrayList<DoubleArray>();
		points.add(new DoubleArray(new double[]{2d,3d}));
		points.add(new DoubleArray(new double[]{5d,4d}));
		points.add(new DoubleArray(new double[]{9d,6d}));
		points.add(new DoubleArray(new double[]{4d,7d}));
		points.add(new DoubleArray(new double[]{8d,1d}));
		points.add(new DoubleArray(new double[]{7d,2d}));
		tree.buildtree(points);
		System.out.println("\nTREE: \n" + tree.toString() + "  \n\n Number of elements in tree: " + tree.size());
		DoubleArray query = new DoubleArray(new double[]{7.9d,4d});
		DoubleArray nearestpoint  = tree.nearest(query);
		System.out.println("The nearest neighboor is: :" + nearestpoint);
	}
	public static String toString(double [] values){
		StringBuilder buffer = new StringBuilder();
		for(Double element : values ){
			buffer.append("   " + element);
		}
		return buffer.toString();
	}
	@SuppressWarnings("unused")
	private static double distance(double[] node1, double[] node2) {
		double sum = 0;
		for(int i=0; i< node1.length; i++){
			sum +=  Math.pow(node1[i] - node2[i], 2);
		}
		return Math.sqrt(sum);
	}
}

