package clustering.kmeans;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import clustering.distanceFunctions.DistanceFunction;
import clustering.instancereader.AlgoInstanceFileReader;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.ClustersEvaluation;
import patterns.cluster.DoubleArray;
import tools.MemoryLogger;
public class AlgoKMeans {
	protected List<ClusterWithMean> clusters = null;
	protected final static Random random = new Random(System.currentTimeMillis());
	protected long startTimestamp; // the start time of the latest execution
	protected long endTimestamp; // the end time of the latest execution
	long iterationCount; // the number of iterations that was performed
	protected DistanceFunction distanceFunction = null;
	private List<String> attributeNames = null;
	boolean DEBUG_MODE = false;
	public AlgoKMeans() {
	}
	public List<ClusterWithMean> runAlgorithm(String inputFile, int k, DistanceFunction distanceFunction,
			String separator) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		iterationCount = 0;
		this.distanceFunction = distanceFunction;
		List<DoubleArray> instances;
		double minValue = Integer.MAX_VALUE;
		double maxValue = 0;
		AlgoInstanceFileReader reader = new AlgoInstanceFileReader();
		instances = reader.runAlgorithm(inputFile, separator);
		int dimensionCount = reader.getAttributeNames().size();
		attributeNames = reader.getAttributeNames();
		for (DoubleArray instance : instances) {
			for (double value : instance.data) {
				if (value < minValue) {
					minValue = value;
				}
				if (value > maxValue) {
					maxValue = value;
				}
			}
		}
		int vectorsSize = instances.get(0).data.length;
		if (k == 1) {
			clusters = new ArrayList<ClusterWithMean>();
			ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
			for (DoubleArray vector : instances) {
				cluster.addVector(vector);
			}
			cluster.setMean(new DoubleArray(new double[vectorsSize]));
			cluster.recomputeClusterMean();
			clusters.add(cluster);
			MemoryLogger.getInstance().checkMemory();
			endTimestamp = System.currentTimeMillis();
			return clusters;
		}
		if (instances.size() == 1) {
			clusters = new ArrayList<ClusterWithMean>();
			DoubleArray vector = instances.get(0);
			ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
			cluster.addVector(vector);
			cluster.recomputeClusterMean();
			cluster.setMean(new DoubleArray(new double[vectorsSize]));
			clusters.add(cluster);
			MemoryLogger.getInstance().checkMemory();
			endTimestamp = System.currentTimeMillis();
			return clusters;
		}
		if (k > instances.size()) {
			k = instances.size();
		}
		applyAlgorithm(k, distanceFunction, instances, minValue, maxValue, vectorsSize);
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return clusters;
	}
	void applyAlgorithm(int k, DistanceFunction distanceFunction, List<DoubleArray> vectors, double minValue,
			double maxValue, int vectorsSize) {
		clusters = applyKMeans(k, distanceFunction, vectors, minValue, maxValue, vectorsSize);
	}
	List<ClusterWithMean> applyKMeans(int k, DistanceFunction distanceFunction, List<DoubleArray> vectors,
			double minValue, double maxValue, int vectorsSize) {
		List<ClusterWithMean> newClusters = new ArrayList<ClusterWithMean>();
		if (vectors.size() == 1) {
			DoubleArray vector = vectors.get(0);
			ClusterWithMean cluster = new ClusterWithMean(vectorsSize);
			cluster.addVector(vector);
			newClusters.add(cluster);
			return newClusters;
		}
		initializeCentroids(vectors, k, vectorsSize, newClusters);
		if (DEBUG_MODE) {
			System.out.println("==== INPUT DATA =====");
			for (int i = 0; i < vectors.size(); i++) {
				System.out.println("Instance " + i + ": " + vectors.get(i));
			}
			System.out.println("==== INITIAL CENTROIDS =====");
			for (int i = 0; i < newClusters.size(); i++) {
				System.out.println("Centroid " + i + ": " + newClusters.get(i));
			}
		}
		boolean changed;
		while (true) {
			iterationCount++;
			if (DEBUG_MODE) {
				System.out.println("Iteration " + iterationCount);
			}
			changed = false;
			for (DoubleArray vector : vectors) {
				ClusterWithMean nearestCluster = null;
				ClusterWithMean containingCluster = null;
				double distanceToNearestCluster = Double.MAX_VALUE;
				for (ClusterWithMean cluster : newClusters) {
					double distance = distanceFunction.calculateDistance(cluster.getMean(), vector);
					if (distance < distanceToNearestCluster) {
						nearestCluster = cluster;
						distanceToNearestCluster = distance;
					}
					if (cluster.contains(vector)) {
						containingCluster = cluster;
					}
				}
				if (containingCluster != nearestCluster) {
					if (containingCluster != null) {
						containingCluster.remove(vector);
					}
					nearestCluster.addVector(vector);
					if(DEBUG_MODE) {
						System.out.println(" Instance " + vector + " is assigned to cluster  " + nearestCluster.getMean());
					}
					changed = true;
				}
			}
			MemoryLogger.getInstance().checkMemory();
			if (!changed) { // exit condition for main loop
				break;
			}
			for (ClusterWithMean cluster : newClusters) {
				cluster.recomputeClusterMean();
				if (DEBUG_MODE) {
					System.out.println("Cluster mean: " + cluster.getMean());
				}
			}
		}
		if (DEBUG_MODE) {
			boolean check = verifyClusterAssignments(newClusters, vectors, distanceFunction);
			System.out.println("Check : " + check);
		}
		return newClusters;
	}
	public boolean verifyClusterAssignments(List<ClusterWithMean> clusters, List<DoubleArray> allPoints,
			DistanceFunction distanceFunction) {
		for (DoubleArray point : allPoints) {
			ClusterWithMean closestCluster = null;
			double minDistance = Double.MAX_VALUE;
			for (ClusterWithMean cluster : clusters) {
				double distance = distanceFunction.calculateDistance(point, cluster.getMean());
				if (distance < minDistance) {
					minDistance = distance;
					closestCluster = cluster;
				}
			}
			if (!closestCluster.getVectors().contains(point)) {
				return false; // Point is not in the correct cluster
			}
		}
		return true; // All points are in the correct clusters
	}
	private void initializeCentroids(List<DoubleArray> vectors, int k, int vectorsSize,
			List<ClusterWithMean> newClusters) {
		List<DoubleArray> chosenCentroids = new ArrayList<DoubleArray>();
		Set<Integer> chosenIndices = new HashSet<Integer>();
		while (chosenCentroids.size() < k) {
			int randomIndex = random.nextInt(vectors.size());
			if (!chosenIndices.contains(randomIndex)) {
				chosenCentroids.add(vectors.get(randomIndex));
				chosenIndices.add(randomIndex);
			}
		}
		for (DoubleArray centroid : chosenCentroids) {
			ClusterWithMean cluster = new ClusterWithMean(centroid.data.length);
			cluster.setMean(centroid);
			newClusters.add(cluster);
		}
	}
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for (String attributeName : attributeNames) {
			writer.write("@ATTRIBUTEDEF=" + attributeName);
			writer.newLine();
		}
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getVectors().size() >= 1) {
				writer.write(clusters.get(i).toString());
				if (i < clusters.size() - 1) {
					writer.newLine();
				}
			}
		}
		writer.close();
	}
	public void printStatistics() {
		System.out.println("========== KMEANS - SPMF 2.09 - STATS ============");
		System.out.println(" Distance function: " + distanceFunction.getName());
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" SSE (Sum of Squared Errors) (lower is better) : "
				+ ClustersEvaluation.calculateSSE(clusters, distanceFunction));
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println(" Iteration count: " + iterationCount);
		System.out.println("=====================================");
	}
}

