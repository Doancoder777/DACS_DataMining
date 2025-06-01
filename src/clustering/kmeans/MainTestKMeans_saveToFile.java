package clustering.kmeans;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import clustering.distanceFunctions.DistanceEuclidian;
import clustering.distanceFunctions.DistanceFunction;
import clustering.kmeans.AlgoKMeans;
public class MainTestKMeans_saveToFile {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("configKmeans.txt");
		String output = ".//output.txt";
		int k=3;
		String separator = " ";
		DistanceFunction distanceFunction = new DistanceEuclidian(); 
		AlgoKMeans algoKMeans = new AlgoKMeans();  
		algoKMeans.runAlgorithm(input, k, distanceFunction, separator);
		algoKMeans.printStatistics();
		algoKMeans.saveToFile(output);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestKMeans_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

