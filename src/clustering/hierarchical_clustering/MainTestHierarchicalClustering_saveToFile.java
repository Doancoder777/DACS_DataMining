package clustering.hierarchical_clustering;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import clustering.hierarchical_clustering.AlgoHierarchicalClustering;
public class MainTestHierarchicalClustering_saveToFile {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("configKmeans.txt");
		String output = ".//output.txt";
		int maxdistance = 4;
		String separator = " ";
		DistanceFunction distanceFunction = new DistanceEuclidian(); 
		AlgoHierarchicalClustering algo = new AlgoHierarchicalClustering();  // we request 3 clusters
		algo.runAlgorithm(input, maxdistance, distanceFunction,separator);
		algo.printStatistics();
		algo.saveToFile(output);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHierarchicalClustering_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

