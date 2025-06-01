package datastructures.kdtree;
import patterns.cluster.DoubleArray;
class KDNode {
	DoubleArray values;  // contains a vector
	int d;  // a dimension
	KDNode above;  // node above
	KDNode below;  // node below
	public KDNode(DoubleArray doubleArray, int d){
		this.values = doubleArray;
		this.d = d;
	}
}

