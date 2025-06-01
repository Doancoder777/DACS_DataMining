package timeseries.paa;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestPAAFromFileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAXblog.txt");  
		String output = "./output.txt";
		int numberOfSegments = 8;
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> movingAveragemultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoPiecewiseAggregateApproximation algorithm = new AlgoPiecewiseAggregateApproximation();
			TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, numberOfSegments);
			movingAveragemultipleTimeSeries.add(movingAverageSeries);
			algorithm.printStats();
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, movingAveragemultipleTimeSeries, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPAAFromFileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

