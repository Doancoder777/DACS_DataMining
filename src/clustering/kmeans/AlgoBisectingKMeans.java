package clustering.kmeans;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import clustering.distanceFunctions.DistanceFunction;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.ClustersEvaluation;
import patterns.cluster.DoubleArray;
import tools.MemoryLogger;
public class AlgoBisectingKMeans extends AlgoKMeans{
	int iter = -1;
	public AlgoBisectingKMeans() { 
	}
	public List<ClusterWithMean> runAlgorithm(String inputFile, int k, 
			DistanceFunction distanceFunction, int iter, String separator) throws NumberFormatException, IOException {
		this.iter = iter;
		return runAlgorithm(inputFile, k, distanceFunction, separator);
	}
	void applyAlgorithm(int k, DistanceFunction distanceFunction,
			List<DoubleArray> vectors, double minValue, double maxValue,
			int vectorsSize) {
		clusters = new ArrayList<ClusterWithMean>();
		List<DoubleArray> currentVectors = vectors;
		while(true) {
			List<ClusterWithMean> bestClustersUntilNow = null;
			double smallestSSE = Double.MAX_VALUE;
			for(int i = 0; i < iter; i++) {
				List<ClusterWithMean> newClusters = applyKMeans(2, distanceFunction, currentVectors, minValue, maxValue, vectorsSize);
				double sse = ClustersEvaluation.calculateSSE(newClusters, distanceFunction);
				if(sse < smallestSSE) {
					bestClustersUntilNow = newClusters;
					smallestSSE = sse;
				}
			}
			clusters.addAll(bestClustersUntilNow);
			if(clusters.size() == k){
				break;
			}
			int biggestClusterSize = -1;
			int biggestClusterIndex = -1;
			for(int i =0; i < clusters.size(); i++) {
				ClusterWithMean cluster = clusters.get(i);
				if(cluster.getVectors().size() > biggestClusterSize) {
					biggestClusterIndex = i;
					biggestClusterSize = cluster.getVectors().size();
					currentVectors = cluster.getVectors();
				}
			}
			clusters.remove(biggestClusterIndex);
		}
	}
	public void printStatistics() {
		System.out.println("========== BISECTING KMEANS - SPMF 2.09 - STATS ============");
		System.out.println(" Distance function: " + distanceFunction.getName());
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" SSE (Sum of Squared Errors) (lower is better) : " + ClustersEvaluation.calculateSSE(clusters, distanceFunction));
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println("=====================================");
	}
}

