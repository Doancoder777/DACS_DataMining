package patterns.cluster;
import java.util.List;
import clustering.dbscan.AlgoDBSCAN;
import clustering.distanceFunctions.DistanceFunction;
import clustering.kmeans.AlgoBisectingKMeans;
import clustering.kmeans.AlgoKMeans;
public class ClustersEvaluation {
	public static  double calculateSSE(List<ClusterWithMean> clusters, DistanceFunction distanceFunction) {
		double sse = 0;
		for(ClusterWithMean cluster : clusters) {
			for(DoubleArray vector : cluster.getVectors()) {
				sse += Math.pow(distanceFunction.calculateDistance(vector, cluster.getMean()), 2);
			}
		}
		return sse;
	}
	public static double getSSE(List<Cluster> clusters, DistanceFunction distanceFunction) {
		double sse = 0;
		for(Cluster cluster : clusters) {
			if(cluster.getVectors().size() > 0) {
				DoubleArray mean = calculateClusterMeans(cluster);
				for(DoubleArray vector : cluster.getVectors()) {
					sse += Math.pow(distanceFunction.calculateDistance(vector, mean), 2);
				}
			}
		}
		return sse;
	}
	public static DoubleArray calculateClusterMeans(Cluster cluster) {
		int dimensionCount = cluster.getVectors().get(0).data.length;
		double mean [] = new double[dimensionCount];
		for(DoubleArray vector : cluster.getVectors()) {
			for(int i=0; i < dimensionCount; i++){
				mean[i] += vector.data[i];
			}
		}
		for(int i=0; i < dimensionCount; i++){
			mean[i] = mean[i]  / cluster.getVectors().size();
		}
		return new  DoubleArray(mean);
	}
}
