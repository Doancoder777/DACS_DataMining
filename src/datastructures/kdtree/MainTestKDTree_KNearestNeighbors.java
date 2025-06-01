package datastructures.kdtree;
import java.util.ArrayList;
import java.util.List;
import datastructures.redblacktree.RedBlackTree;
import patterns.cluster.DoubleArray;
class MainTestKDTree_KNearestNeighbors {
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
		DoubleArray query = new DoubleArray(new double[]{4d,4d});
		int k = 3;
		RedBlackTree<KNNPoint> result = tree.knearest(query, k);
		System.out.println("THE K NEAREST NEIGHBOORS ARE : " + result.toString());	
	}
	public static String toString(double [] values){
		StringBuilder buffer = new StringBuilder();
		for(Double element : values ){
			buffer.append("   " + element);
		}
		return buffer.toString();
	}
}

