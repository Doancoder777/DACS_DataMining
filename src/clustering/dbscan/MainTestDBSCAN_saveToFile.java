package clustering.dbscan;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import clustering.dbscan.AlgoDBSCAN;
public class MainTestDBSCAN_saveToFile {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("inputDBScan.txt");
		String output = ".//output.txt";
		int minPts=2;
		double epsilon = 5d;
		String separator = " ";
		AlgoDBSCAN algo = new AlgoDBSCAN();  
		algo.runAlgorithm(input, minPts, epsilon, separator);
		algo.printStatistics();
		algo.saveToFile(output);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDBSCAN_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

