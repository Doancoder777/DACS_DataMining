package clustering.kmeans;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import clustering.kmeans.AlgoKMeans;
import patterns.cluster.ClusterWithMean;
import patterns.cluster.DoubleArray;
public class MainTestKMeans_saveToMemory {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("inputDBScan2.txt");
		int k=3;	
		String separator = " ";
		DistanceFunction distanceFunction = new DistanceEuclidian(); 
		AlgoKMeans algoKMeans = new AlgoKMeans();  
		List<ClusterWithMean> clusters = algoKMeans.runAlgorithm(input, k, distanceFunction, separator);
		algoKMeans.printStatistics();
		int i=0;
		for(ClusterWithMean cluster : clusters) {
			System.out.println("Cluster " + i++);
			for(DoubleArray dataPoint : cluster.getVectors()) {
				System.out.println("   " + dataPoint);
			}
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestKMeans_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

