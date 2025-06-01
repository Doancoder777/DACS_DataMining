package clustering.optics;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
public class MainTestOPTICS_extractDBScan_saveToFile {
	public static void main(String []args) throws NumberFormatException, IOException{
		String input = fileToPath("inputDBScan.txt");
		String output = ".//output.txt";
		int minPts=2;
		double epsilon = 5d;
		double epsilonPrime = 5d;
		String separator = " ";
		AlgoOPTICS algo = new AlgoOPTICS();  
		algo.computerClusterOrdering(input, minPts, epsilon, separator);
		algo.extractDBScan(minPts,epsilonPrime);
		algo.printStatistics();
		algo.saveToFile(output);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestOPTICS_extractDBScan_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

