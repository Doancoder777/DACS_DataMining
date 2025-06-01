package clustering.dbscan;
import patterns.cluster.Cluster;
import patterns.cluster.DoubleArray;
import patterns.cluster.DoubleArrayInstance;
public class DoubleArrayDBS extends DoubleArrayInstance{
	boolean visited = false;
	Cluster cluster = null;
	public DoubleArrayDBS(double[] data, String name) {
		super(data, name);
	}
}

