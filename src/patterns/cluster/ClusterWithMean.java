package patterns.cluster;
public class ClusterWithMean extends Cluster {
	private DoubleArray mean;  // the mean of the vectors in this cluster
	private DoubleArray sum; // the sum of all vectors in this clusters 
	public ClusterWithMean(int vectorsSize){
		sum = new DoubleArray(new double[vectorsSize]);
	}
	public void setMean(DoubleArray mean){
		this.mean = mean;
	}
	public void addVector(DoubleArray vector) {
		super.addVector(vector);
		for(int i=0; i < vector.data.length; i++){
			sum.data[i] += vector.data[i];
		}
	}
	public DoubleArray getMean() {
		return mean;
	}
	public void recomputeClusterMean() {
		for(int i=0; i < sum.data.length; i++){
			mean.data[i] = sum.data[i] / vectors.size();
		}
	}
	public void remove(DoubleArray vector) {
		super.remove(vector);
		for(int i=0; i < vector.data.length; i++){
			sum.data[i] -= vector.data[i];
		}
	}
}

