package datastructures.kdtree;
import java.util.ArrayList;
import java.util.List;
import patterns.cluster.DoubleArray;
class MainTestKDTree_allPointsWithinEpsilonRadius {
	public static void main(String[] args) {
		KDTree tree = new KDTree();
		List<DoubleArray> points = new ArrayList<DoubleArray>();
		points.add(new DoubleArray(new double[]{1d,1d}));
		points.add(new DoubleArray(new double[]{0d,1d}));
		points.add(new DoubleArray(new double[]{1d,0d}));
		points.add(new DoubleArray(new double[]{10d,10d}));
		points.add(new DoubleArray(new double[]{10d,13d}));
		points.add(new DoubleArray(new double[]{13d,13d}));
		points.add(new DoubleArray(new double[]{54d,54d}));
		points.add(new DoubleArray(new double[]{55d,55d}));
		points.add(new DoubleArray(new double[]{89d,89d}));
		points.add(new DoubleArray(new double[]{57d,55d}));
		tree.buildtree(points);
		System.out.println("\nTREE: \n" + tree.toString() + "  \n\n Number of elements in tree: " + tree.size());
		DoubleArray querypoint =  new DoubleArray(new double[]{1d,0d});
		double radius = 5;
		List<DoubleArray> result = tree.pointsWithinRadiusOf(querypoint, radius);
		System.out.println("THE POINTS WITHIN THE RADIUS ARE : ");	
		for(DoubleArray point : result) {
			System.out.println(" " + point);
		}
	}
	public static String toString(double [] values){
		StringBuilder buffer = new StringBuilder();
		for(Double element : values ){
			buffer.append("   " + element);
		}
		return buffer.toString();
	}
}

