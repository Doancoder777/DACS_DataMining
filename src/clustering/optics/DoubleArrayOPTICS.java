package clustering.optics;
import java.util.Collections;
import java.util.List;
import datastructures.kdtree.KNNPoint;
import patterns.cluster.DoubleArray;
import patterns.cluster.DoubleArrayInstance;
public class DoubleArrayOPTICS extends DoubleArrayInstance implements Comparable<DoubleArrayOPTICS>{
	boolean visited = false;
	public double reachabilityDistance = Double.POSITIVE_INFINITY; // undefined
	double core_distance = Double.POSITIVE_INFINITY;  // undefined
	public DoubleArrayOPTICS(double[] data, String name) {
		super(data, name);
	}
	public void setCoreDistance(List<KNNPoint> neighboors, double epsilon,
			int minPts) {
		if(neighboors.size() < minPts - 1) {
			core_distance  = Double.POSITIVE_INFINITY;
		}else {
			Collections.sort(neighboors);
			core_distance = (neighboors.get(minPts-2)).distance;
		}
	}
	public int compareTo(DoubleArrayOPTICS point2) {
		return Double.compare(this.reachabilityDistance, point2.reachabilityDistance);
	}
	public String toString() {
		return  super.toString();
	}
}

