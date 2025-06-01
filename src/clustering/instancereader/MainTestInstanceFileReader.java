package clustering.instancereader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import patterns.cluster.DoubleArray;
public class MainTestInstanceFileReader {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("inputDBScan.txt");  
		String separator = " ";
		AlgoInstanceFileReader algorithm = new AlgoInstanceFileReader();
		List<DoubleArray> instances = algorithm.runAlgorithm(input, separator);
		algorithm.printStats();
		List<String> attributeNames = algorithm.getAttributeNames();
		System.out.println("ATTRIBUTES");
		for(String attributeName : attributeNames){
			System.out.println(" "  + attributeName);
		}
		System.out.println("INSTANCES");
		for(DoubleArray instance : instances){
			System.out.println(" "  + instance);
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestInstanceFileReader.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

