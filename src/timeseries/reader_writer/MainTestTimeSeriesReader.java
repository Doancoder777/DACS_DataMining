package timeseries.reader_writer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import timeseries.TimeSeries;
public class MainTestTimeSeriesReader {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAX.txt");  
		String separator = ",";
		AlgoTimeSeriesReader algorithm = new AlgoTimeSeriesReader();
		List<TimeSeries> timeSeries = algorithm.runAlgorithm(input, separator);
		algorithm.printStats();
		System.out.println("TIME-SERIES");
		for(TimeSeries series : timeSeries){
			System.out.println(" "  + series);
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTimeSeriesReader.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

