package clustering.optics;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import patterns.cluster.Cluster;
import patterns.cluster.DoubleArray;
public class MainTestOPTICS_extractDBScan_saveToMemory {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("inputDBScan.txt");
		int minPts=2;
		double epsilon = 5d;
		double epsilonPrime = 5d;
		String separator = " ";
		AlgoOPTICS algo = new AlgoOPTICS();  
		List<DoubleArrayOPTICS> clusterOrdering = algo.computerClusterOrdering(input, minPts, epsilon, separator);
		System.out.println("THE CLUSTER ORDERING:");
		System.out.println(" [data point] - reachability distance");
		for(DoubleArrayOPTICS arrayOP : clusterOrdering) {
			System.out.println(" " + arrayOP.toString());
		}
		List<Cluster> dbScanClusters = algo.extractDBScan(minPts,epsilonPrime);
		System.out.println();
		System.out.println("CLUSTER(S) FOUND:");
		int i=0;
		for(Cluster cluster : dbScanClusters) {
			System.out.println("Cluster " + i++);
			for(DoubleArray dataPoint : cluster.getVectors()) {
				System.out.println("   " + dataPoint);
			}
		}
		algo.printStatistics();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOPTICS_extractDBScan_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

