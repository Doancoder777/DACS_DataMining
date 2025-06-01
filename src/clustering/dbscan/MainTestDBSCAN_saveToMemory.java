package clustering.dbscan;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import clustering.dbscan.AlgoDBSCAN;
import patterns.cluster.Cluster;
import patterns.cluster.DoubleArray;
public class MainTestDBSCAN_saveToMemory {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("inputDBScan.txt");
		int minPts = 2;
		double epsilon = 5d;
		String separator = " ";
		AlgoDBSCAN algo = new AlgoDBSCAN();  
		List<Cluster> clusters = algo.runAlgorithm(input, minPts, epsilon, separator);
		algo.printStatistics();
		int i=0;
		for(Cluster cluster : clusters) {
			System.out.println("Cluster " + i++);
			for(DoubleArray dataPoint : cluster.getVectors()) {
				System.out.println("   " + dataPoint);
			}
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDBSCAN_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

