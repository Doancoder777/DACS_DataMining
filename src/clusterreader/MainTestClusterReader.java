package clusterreader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import timeseries.TimeSeries;
import patterns.cluster.Cluster;
public class MainTestClusterReader {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("clustersDBScan.txt");  
		AlgoClusterReader algorithm = new AlgoClusterReader();
		List<Cluster> clusters = algorithm.runAlgorithm(input);
		algorithm.printStats();
		List<String> attributeNames = algorithm.getAttributeNames();
		System.out.println("ATTRIBUTES");
		for(String attributeName : attributeNames){
			System.out.println(" "  + attributeName);
		}
		System.out.println("Clusters");
		for(Cluster cluster : clusters){
			System.out.println(" "  + cluster);
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestClusterReader.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

