package datastructures.kdtree;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import datastructures.redblacktree.RedBlackTree;
import patterns.cluster.DoubleArray;
public class KDTree {
	private int nodeCount = 0; // number of nodes in the tree
	private KDNode root = null; // the tree root
	int dimensionCount = 0; // number of dimensions
	private static Random random = new Random(System.currentTimeMillis());  
	DistanceFunction distanceFunction = new DistanceEuclidian(); 
	public KDTree() {
	}
	public int size() {
		return nodeCount;
	}
	public void buildtree(List<DoubleArray> points) {
		if (points.size() == 0) {
			return;
		}
		dimensionCount = points.get(0).size();
		root = generateNode(0, points, 0, points.size() - 1); 
	}
	private KDNode generateNode(int currentD, List<DoubleArray> points, int left, int right) {
		if (right < left) {
			return null;
		}
		nodeCount++;
		if (right == left) {
			return new KDNode(points.get(left), currentD);
		}
		int m = (right - left) / 2;
		DoubleArray medianNode = randomizedSelect(points, m, left, right, currentD);
		KDNode node = new KDNode(medianNode, currentD);
		currentD++;
		if (currentD == dimensionCount) {
			currentD = 0;
		}
		node.below = generateNode(currentD, points, left, left + m -1);
		node.above = generateNode(currentD, points, left + m +1, right);
		return node;
	}
	private DoubleArray randomizedSelect(List<DoubleArray> points, int i, int left,
			int right, int currentD) {
		int p = left;
		int r = right;
		while (true) {
			if (p == r) {
				return points.get(p);
			} 
			int q = randomizedPartition(points, p, r, currentD);
			int k = q - p + 1;
			if (i == k - 1) {
				return points.get(q);
			} else if (i < k) {
				r = q - 1;
			} else {
				i = i - k;
				p = q + 1;
			}
		}
	}
	private int randomizedPartition(List<DoubleArray> points, int p, int r, int currentD) {
		int i = 0;
		if (p < r) {
			i = p + random.nextInt(r - p);
		} else {
			i = r + random.nextInt(p - r);
		}
		swap(points, r, i);
		return partition(points, p, r, currentD); // call the partition method of
	}
	private int partition(List<DoubleArray> points, int p, int r, int currentD) {
		DoubleArray x = points.get(r);
		int i = p - 1;
		for (int j = p; j <= r - 1; j++) {
			if (points.get(j).data[currentD] <= x.data[currentD]) {
				i = i + 1;
				swap(points, i, j);
			}
		}
		swap(points, i + 1, r);
		return i + 1;
	}
	private void swap(List<DoubleArray> points, int i, int j) {
		DoubleArray valueI = points.get(i);
		points.set(i,points.get(j));
		points.set(j, valueI);
	}
	DoubleArray nearestNeighboor = null;  // the current nearest neighboor.
	double minDist = 0;  // the distance of the current nearest neighbor with the target point.
	public DoubleArray nearest(DoubleArray targetPoint) {
		if (root == null){
			return null;
		}
		findParent(targetPoint, root, 0);
		nearest(root, targetPoint);
		return nearestNeighboor;
	}
	private void findParent(DoubleArray target, KDNode node, int d) {		
		if(target.data[d] < node.values.data[d]){
			if (++d == dimensionCount) {
				d = 0;
			}
			if(node.below == null){
				nearestNeighboor = node.values;
				minDist = distanceFunction.calculateDistance(node.values, target);
				return;
			}
			findParent(target, node.below, d);
		}
		if(++d == dimensionCount) {
			d = 0;
		}
		if(node.above == null){
			nearestNeighboor = node.values;
			minDist = distanceFunction.calculateDistance(node.values, target);
			return;
		}
		findParent(target, node.above, d);
	}
	private void nearest(KDNode node, DoubleArray targetPoint) {
		double d = distanceFunction.calculateDistance(node.values, targetPoint);
		if (d < minDist) {
			minDist = d;
			nearestNeighboor = node.values;
		}
		int dMinus1 = node.d-1;
		if(dMinus1 <0){
			dMinus1 = dimensionCount - 1;
		}
		double perpendicularyDistance = Math.abs(node.values.data[node.d] - targetPoint.data[node.d]);
		if (perpendicularyDistance < minDist) { 
			if (node.above != null) {
				nearest(node.above, targetPoint);
			}
			if (node.below != null) {
				nearest(node.below, targetPoint);
			}
		} else {
			if (targetPoint.data[dMinus1] < node.values.data[dMinus1]) {
				if (node.below != null) {
					nearest(node.below, targetPoint);
				}
			} else if (node.above != null) {
				nearest(node.above, targetPoint);
			}
		}
	}
	RedBlackTree<KNNPoint> resultKNN = null; // field to store the current k nearest neighboor with the target point
	int k =0; // the parameter k.
	public RedBlackTree<KNNPoint> knearest(DoubleArray targetPoint, int k) {
		this.k = k;
		this.resultKNN = new RedBlackTree<KNNPoint>();
		if (root == null){
			return null;
		}
		findParent_knn(targetPoint, root, 0);
		nearest_knn(root, targetPoint);
		return resultKNN;
	}
	private void findParent_knn(DoubleArray target, KDNode node, int d) {		
		if(target.data[d]  < node.values.data[d]){
			if (++d == dimensionCount) {
				d = 0;
			}
			if(node.below == null){
				tryToSave(node, target);
				return;
			}
			tryToSave(node.below, target);
			findParent_knn(target, node.below, d);
		}
		if(++d == dimensionCount) {
			d = 0;
		}
		if(node.above == null){
			tryToSave(node, target);
			return;
		}
		tryToSave(node.above, target);
		findParent_knn(target, node.above, d);
	}
	private void tryToSave(KDNode node, DoubleArray target) {
		if(node == null){
			return;
		}
		double distance = distanceFunction.calculateDistance(target, node.values);
		if(resultKNN.size() == k  && resultKNN.maximum().distance < distance){ 
			return;
		}
		KNNPoint point = new KNNPoint(node.values, distance);
		if(resultKNN.contains(point)){
			return;
		}
		resultKNN.add(point);
		if(resultKNN.size() > k){
			resultKNN.popMaximum();
		}
	}
	private void nearest_knn(KDNode node, DoubleArray targetPoint) {
		tryToSave(node, targetPoint); 
		double perpendicularDistance = Math.abs(node.values.data[node.d] - targetPoint.data[node.d]);
		if (perpendicularDistance < resultKNN.maximum().distance) { 
			if (node.above != null) {
				nearest_knn(node.above, targetPoint);
			}
			if (node.below != null) {
				nearest_knn(node.below, targetPoint);
			}
		} else {
			if (targetPoint.data[node.d] < node.values.data[node.d]) {
				if (node.below != null) {
					nearest_knn(node.below, targetPoint);
				}
			} else {
				if (node.above != null) {
					nearest_knn(node.above, targetPoint);
				}
			}
		}
	}
	public List<DoubleArray> pointsWithinRadiusOf(DoubleArray targetPoint, double radius) {
		List<DoubleArray> result = new ArrayList<DoubleArray>();
		return pointsWithinRadiusOf(targetPoint, radius, result);
	}
	public List<DoubleArray> pointsWithinRadiusOf(DoubleArray targetPoint, double radius, List<DoubleArray> result) {
		if (root == null){
			return null;
		}
		findPointsWithinRadius(root, targetPoint, result, radius);
		return result;
	}
	private void findPointsWithinRadius(KDNode node, DoubleArray targetPoint, List<DoubleArray> result, double radius) {
		if(node.values != targetPoint) {
			tryToSaveRadius(node, targetPoint, result, radius); 
		}
		double perpendicularDistance = Math.abs(node.values.data[node.d] - targetPoint.data[node.d]);
		if (perpendicularDistance < radius) { 
			if (node.above != null) {
				findPointsWithinRadius(node.above, targetPoint, result, radius);
			}
			if (node.below != null) {
				findPointsWithinRadius(node.below, targetPoint, result, radius);
			}
		} else {
			if (targetPoint.data[node.d] < node.values.data[node.d]) {
				if (node.below != null) {
					findPointsWithinRadius(node.below, targetPoint, result, radius);
				}
			} else {
				if (node.above != null) {
					findPointsWithinRadius(node.above, targetPoint, result, radius);
				}
			}
		}
	}
	private void tryToSaveRadius(KDNode node, DoubleArray target, List<DoubleArray> result, double radius) {
		if(node == null){
			return;
		}
		double distance = distanceFunction.calculateDistance(target, node.values);
		if(distance <= radius){ 
			result.add(node.values);
		}
	}
	public List<KNNPoint> pointsWithinRadiusOfWithDistance(DoubleArray targetPoint, double radius) {
		if (root == null){
			return null;
		}
		List<KNNPoint> result = new ArrayList<KNNPoint>();
		return pointsWithinRadiusOfWithDistance(targetPoint, radius, result);
	}
	public List<KNNPoint> pointsWithinRadiusOfWithDistance(DoubleArray targetPoint, double radius, List<KNNPoint> result) {
		if (root == null){
			return null;
		}
		findPointsWithinRadiusWithDistance(root, targetPoint, result, radius);
		return result;
	}
	private void findPointsWithinRadiusWithDistance(KDNode node, DoubleArray targetPoint, List<KNNPoint> result, double radius) {
		if(node.values != targetPoint) {
			tryToSaveRadiusWithDistance(node, targetPoint, result, radius); 
		}
		double perpendicularDistance = Math.abs(node.values.data[node.d] - targetPoint.data[node.d]);
		if (perpendicularDistance < radius) { 
			if (node.above != null) {
				findPointsWithinRadiusWithDistance(node.above, targetPoint, result, radius);
			}
			if (node.below != null) {
				findPointsWithinRadiusWithDistance(node.below, targetPoint, result, radius);
			}
		} else {
			if (targetPoint.data[node.d] < node.values.data[node.d]) {
				if (node.below != null) {
					findPointsWithinRadiusWithDistance(node.below, targetPoint, result, radius);
				}
			} else {
				if (node.above != null) {
					findPointsWithinRadiusWithDistance(node.above, targetPoint, result, radius);
				}
			}
		}
	}
	private void tryToSaveRadiusWithDistance(KDNode node, DoubleArray target, List<KNNPoint> result, double radius) {
		if(node != null){
			double distance = distanceFunction.calculateDistance(target, node.values);
			if(distance <= radius){ 
				result.add(new KNNPoint(node.values, distance));
			}
		}
	}
	public String toString(){
		return toString(root, " ");
	}
	private String toString(KDNode node, String indent){
		if(node == null){
			return "";
		}
		String newIndent1 =  indent + "   |";
		String newIndent2 =  indent + "   |";
		return node.values + " (" + node.d +") \n" 
				+ indent + toString(node.above, newIndent1) + "\n" 
		        + indent + toString(node.below, newIndent2);
	}
}

