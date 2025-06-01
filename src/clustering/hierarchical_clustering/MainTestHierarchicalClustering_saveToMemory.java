package clustering.hierarchical_clustering;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import clustering.hierarchical_clustering.AlgoHierarchicalClustering;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.DoubleArray;
public class MainTestHierarchicalClustering_saveToMemory {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("configKmeans.txt");
		int maxdistance = 4;
		String separator = " ";
		DistanceFunction distanceFunction = new DistanceEuclidian(); 
		AlgoHierarchicalClustering algo = new AlgoHierarchicalClustering();  // we request 3 clusters
		List<ClusterWithMean> clusters = algo.runAlgorithm(input, maxdistance, distanceFunction, separator);
		algo.printStatistics();
		int i=0;
		for(ClusterWithMean cluster : clusters) {
			System.out.println("Cluster " + i++ + " (containing " + cluster.getVectors().size() + " vector(s))");
			for(DoubleArray dataPoint : cluster.getVectors()) {
				System.out.println("   " + dataPoint);
			}
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHierarchicalClustering_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

