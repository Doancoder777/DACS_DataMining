package clustering.optics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import datastructures.kdtree.KDTree;
import datastructures.kdtree.KNNPoint;
import patterns.cluster.Cluster;
import patterns.cluster.DoubleArray;
import tools.MemoryLogger;
public class AlgoOPTICS {
	protected long timeExtractClusterOrdering;
	DistanceFunction distanceFunction = new DistanceEuclidian();
	KDTree kdtree;
	List<DoubleArrayOPTICS> clusterOrdering = null;
	List<Cluster> clusters = null;
	List<KNNPoint> neighboorsBuffer1 = new ArrayList<KNNPoint>();
	List<KNNPoint> neighboorsBuffer2 = new ArrayList<KNNPoint>();
	private List<String> attributeNames = null;
	public AlgoOPTICS() {
	}
	public List<DoubleArrayOPTICS> computerClusterOrdering(String inputFile,
			int minPts, double epsilon, String separator)
			throws NumberFormatException, IOException {
		timeExtractClusterOrdering = 0;
		long startTimestampClusterOrdering = System.currentTimeMillis();
		List<DoubleArray> points = new ArrayList<DoubleArray>();
		attributeNames = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		String currentInstanceName = null;
		while (((line = reader.readLine()) != null)) {
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%') {
				continue;
			}
			if(line.charAt(0) == '@'){
				if(line.startsWith("@NAME=")){
					currentInstanceName = line.substring(6, line.length());
				}
				if(line.startsWith("@ATTRIBUTEDEF=")){
					String attributeName = line.substring(14, line.length());
					attributeNames.add(attributeName);
				}
				continue;
			}
			String nameToUse = currentInstanceName == null ?  "Instance" + points.size() : currentInstanceName;			
			currentInstanceName = null;
			line = line.trim();
			String[] lineSplited = line.split(separator);
			double[] vector = new double[lineSplited.length];
			for (int i = 0; i < lineSplited.length; i++) {
				double value = Double.parseDouble(lineSplited[i]);
				vector[i] = value;
			}
			points.add(new DoubleArrayOPTICS(vector, nameToUse));
		}
		reader.close();
		if(attributeNames.size() == 0 && points.size() > 0){
			int dimensionCount = points.get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}
		kdtree = new KDTree();
		kdtree.buildtree(points);
		clusterOrdering = new ArrayList<DoubleArrayOPTICS>();
		for (DoubleArray point : points) {
			DoubleArrayOPTICS pointDBS = (DoubleArrayOPTICS) point;
			if (pointDBS.visited == false) {
				expandClusterOrder(pointDBS, clusterOrdering, epsilon, minPts);
			}
		}
		MemoryLogger.getInstance().checkMemory();
		timeExtractClusterOrdering = System.currentTimeMillis() - startTimestampClusterOrdering;
		neighboorsBuffer1.clear();
		neighboorsBuffer2.clear();
		kdtree = null;
		return clusterOrdering;
	}
	private void expandClusterOrder(DoubleArrayOPTICS pointDBS,	List<DoubleArrayOPTICS> orderedFile, double epsilon, int minPts) {
		neighboorsBuffer1.clear();
		kdtree.pointsWithinRadiusOfWithDistance(pointDBS, epsilon, neighboorsBuffer1);
		pointDBS.visited = true;
		pointDBS.reachabilityDistance = Double.POSITIVE_INFINITY; // / /// &&*&*&*
		pointDBS.setCoreDistance(neighboorsBuffer1, epsilon, minPts); /// &&*&*&*
		orderedFile.add(pointDBS);
		if (pointDBS.core_distance != Double.POSITIVE_INFINITY) {
			PriorityQueue<DoubleArrayOPTICS> orderSeeds = new PriorityQueue<DoubleArrayOPTICS>();
			update(neighboorsBuffer1, pointDBS, orderSeeds, minPts, epsilon);
			while (orderSeeds.isEmpty() == false) {
				DoubleArrayOPTICS currentObject = (DoubleArrayOPTICS) orderSeeds.poll();
				neighboorsBuffer2.clear();
				kdtree.pointsWithinRadiusOfWithDistance(currentObject, epsilon, neighboorsBuffer2);  /// &$&$ CurrentObject
				currentObject.visited = true;
				currentObject.setCoreDistance(neighboorsBuffer2, epsilon, minPts);
				orderedFile.add(currentObject);
				if (currentObject.core_distance != Double.POSITIVE_INFINITY) {
					update(neighboorsBuffer2, currentObject, orderSeeds, minPts, epsilon);
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	private void update(List<KNNPoint> neighbors,
			DoubleArrayOPTICS centerObject, PriorityQueue<DoubleArrayOPTICS> orderSeeds, int minPts, double epsilon) {
		double cDist = centerObject.core_distance;
		for (KNNPoint object : neighbors) {
			DoubleArrayOPTICS objectOP = (DoubleArrayOPTICS) object.values;
			if (objectOP.visited == false) {
				double newRDistance = Math.max(cDist, distanceFunction
						.calculateDistance(objectOP, centerObject));
				if (objectOP.reachabilityDistance == Double.POSITIVE_INFINITY) {
					objectOP.reachabilityDistance = newRDistance;
					orderSeeds.add(objectOP);
				} else {
					if (newRDistance < objectOP.reachabilityDistance) {
						objectOP.reachabilityDistance = newRDistance;
						orderSeeds.remove(objectOP);
						orderSeeds.add(objectOP);
					}
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	public List<Cluster> extractDBScan(int minPts, double epsilonPrime)
			throws IOException {
		clusters = new ArrayList<Cluster>();
		Cluster currentCluster = new Cluster();
		for (DoubleArrayOPTICS objectOP : clusterOrdering) {
			if (objectOP.reachabilityDistance > epsilonPrime) {
				if (objectOP.core_distance <= epsilonPrime) {
					if (currentCluster.getVectors().size() > 0) {
						clusters.add(currentCluster);
					}
					currentCluster = new Cluster();
					currentCluster.addVector(objectOP);
				}// else, it is noise
			} else {
				currentCluster.addVector(objectOP);
			}
		}
		if (currentCluster.getVectors().size() > 0) {
			clusters.add(currentCluster);
		}
		return clusters;
	}
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(String attributeName : attributeNames){
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
	public void saveClusterOrderingToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(DoubleArrayOPTICS arrayOP : clusterOrdering) {
			writer.write(arrayOP.toString() + " " + arrayOP.reachabilityDistance);
			writer.newLine();
		}
		writer.close();
	}
	public void printStatistics() {
		System.out.println("========== OPTICS  SPMF 2.09 - STATS ============");
		System.out.println(" Time ExtractClusterOrdering() ~: "
				+ timeExtractClusterOrdering + " ms");
		System.out.println(" Max memory:"
				+ MemoryLogger.getInstance().getMaxMemory() + " mb ");
		if(clusters != null){
			System.out.println(" Number of clusters: " + clusters.size());
		}
		System.out.println("=====================================");
	}
}

