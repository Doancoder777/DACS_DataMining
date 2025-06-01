package clustering.kmeans;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import clustering.kmeans.AlgoBisectingKMeans;
public class MainTestBisectingKMeans_saveToFile {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("configKmeans.txt");
		String output = ".//output.txt";
		int k=3;
		int iter = 10;
		String separator = " ";
		DistanceFunction distanceFunction = new DistanceEuclidian(); 
		AlgoBisectingKMeans algo = new AlgoBisectingKMeans();  
		algo.runAlgorithm(input, k, distanceFunction, iter, separator);
		algo.printStatistics();
		algo.saveToFile(output);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBisectingKMeans_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

