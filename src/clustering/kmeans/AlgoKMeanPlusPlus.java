package clustering.kmeans;
import java.util.List;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.ClustersEvaluation;
import patterns.cluster.DoubleArray;
import tools.MemoryLogger;
public class AlgoKMeanPlusPlus extends AlgoKMeans {
	@SuppressWarnings("unused")
	private void initializeCentroids(List<DoubleArray> vectors, int k, int vectorsSize,
	        List<ClusterWithMean> newClusters) {
	    int randomChoice = random.nextInt(vectors.size());
	    DoubleArray firstCentroid = vectors.get(randomChoice);
	    ClusterWithMean firstCluster = new ClusterWithMean(vectorsSize);
	    firstCluster.setMean(firstCentroid);
	    newClusters.add(firstCluster);
	    boolean[] usedIndices = new boolean[vectors.size()];
	    usedIndices[randomChoice] = true;
	    for (int i = 1; i < k; i++) {
	        double[] distances = new double[vectors.size()];
	        double totalDistance = 0;
	        for (int j = 0; j < vectors.size(); j++) {
	            double nearestDistance = Double.MAX_VALUE;
	            for (ClusterWithMean cluster : newClusters) {
	                double distance = distanceFunction.calculateDistance(cluster.getMean(), vectors.get(j));
	                nearestDistance = Math.min(nearestDistance, distance);
	            }
	            distances[j] = nearestDistance * nearestDistance;
	            totalDistance += distances[j];
	        }
	        while (true) {
	            double r = random.nextDouble() * totalDistance;
	            double sum = 0;
	            for (int j = 0; j < distances.length; j++) {
	                sum += distances[j];
	                if (sum >= r && !usedIndices[j]) {
	                    DoubleArray nextCentroid = vectors.get(j);
	                    ClusterWithMean nextCluster = new ClusterWithMean(vectorsSize);
	                    nextCluster.setMean(nextCentroid);
	                    newClusters.add(nextCluster);
	                    usedIndices[j] = true;
	                    break;
	                }
	            }
	            if (newClusters.size() > i) {
	                break; // Exit the while loop if a new centroid was added
	            }
	        }
	    }
	}
	public void printStatistics() {
		System.out.println("========== KMEANS++ - SPMF 2.09 - STATS ============");
		System.out.println(" Distance function: " + distanceFunction.getName());
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" SSE (Sum of Squared Errors) (lower is better) : "
				+ ClustersEvaluation.calculateSSE(clusters, distanceFunction));
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println("=====================================");
	}
}
