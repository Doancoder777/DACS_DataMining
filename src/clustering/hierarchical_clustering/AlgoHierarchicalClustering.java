package clustering.hierarchical_clustering;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import clustering.distanceFunctions.DistanceFunction;
import clustering.instancereader.AlgoInstanceFileReader;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.ClustersEvaluation;
import patterns.cluster.DoubleArray;
import tools.MemoryLogger;
public class AlgoHierarchicalClustering {
	private double maxDistance =0;  // maximum distance allowed for merging two clusters
	List<ClusterWithMean> clusters = null;
	private long startTimestamp;  // start time of latest execution
	private long endTimestamp;    // end time of latest execution
	private long iterationCount; // number of iterations performed
	private DistanceFunction distanceFunction = null;
	private List<String> attributeNames = null;
	public AlgoHierarchicalClustering() {
	}
	public List<ClusterWithMean> runAlgorithm(String inputFile, double maxDistance, DistanceFunction distanceFunction, String separator) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		this.maxDistance = maxDistance;
		this.distanceFunction = distanceFunction;
		clusters = new ArrayList<ClusterWithMean>();
		AlgoInstanceFileReader reader = new AlgoInstanceFileReader();
		List<DoubleArray> instances = reader.runAlgorithm(inputFile, separator);
		int dimensionCount = reader.getAttributeNames().size();
		attributeNames = reader.getAttributeNames();
		for(DoubleArray instance : instances){
			ClusterWithMean cluster = new ClusterWithMean(dimensionCount);
			cluster.addVector(instance);
			cluster.setMean(instance.clone());
			clusters.add(cluster);
		}
		boolean changed = false;
		do {
			changed = mergeTheClosestCluster();
			MemoryLogger.getInstance().checkMemory();
		} while (changed);
		endTimestamp = System.currentTimeMillis();
		return clusters;
	}
	private boolean mergeTheClosestCluster() {
		ClusterWithMean clusterToMerge1 = null;
		ClusterWithMean clusterToMerge2 = null;
		double minClusterDistance = Integer.MAX_VALUE;
		for (int i = 0; i < clusters.size(); i++) {
			for (int j = i + 1; j < clusters.size(); j++) {
				double distance = distanceFunction.calculateDistance(clusters.get(i).getMean(), clusters.get(j).getMean());
				if (distance < minClusterDistance && distance <= maxDistance) {
					minClusterDistance = distance;
					clusterToMerge1 = clusters.get(i);
					clusterToMerge2 = clusters.get(j);
				}
			}
		}
		if (clusterToMerge1 == null) {
			return false;
		}
		for(DoubleArray vector : clusterToMerge2.getVectors()){
			clusterToMerge1.addVector(vector);
		}
		clusterToMerge1.recomputeClusterMean();
		clusters.remove(clusterToMerge2);
		iterationCount++;
		return true;
	}
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(String attributeName : attributeNames){
			writer.write("@ATTRIBUTEDEF=" + attributeName);
			writer.newLine();
		}
		for(int i=0; i< clusters.size(); i++){
			if(clusters.get(i).getVectors().size() >= 1){
				writer.write(clusters.get(i).toString());
				if(i < clusters.size()-1){
					writer.newLine();
				}
			}
		}
		writer.close();
	}
	public void printStatistics() {
		System.out.println("========== HIERARCHICAL CLUSTERING SPMF 2.09 - STATS ============");
		System.out.println(" Distance function: " + distanceFunction.getName());
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" SSE (Sum of Squared Errors) (lower is better) : " + ClustersEvaluation.calculateSSE(clusters, distanceFunction));
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println(" Iteration count: " + iterationCount);
		System.out.println("=====================================");
	}
}

