package timeseries.autocorrelation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestAutocorrelationFileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextAutocorrelation.txt");  
		String output = "./output.txt";
		String separator = ",";
		int maxlag = 16;
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> resultMultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoLagAutoCorrelation algorithm = new AlgoLagAutoCorrelation();
			TimeSeries aSeries = algorithm.runAlgorithm(timeSeries, maxlag);
			resultMultipleTimeSeries.add(aSeries);
			algorithm.printStats();
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, resultMultipleTimeSeries, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAutocorrelationFileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

