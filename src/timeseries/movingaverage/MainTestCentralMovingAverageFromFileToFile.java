package timeseries.movingaverage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestCentralMovingAverageFromFileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAXblog.txt");  
		String output = "./output.txt";
		int windowSize = 3;
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> movingAveragemultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoCentralMovingAverage algorithm = new AlgoCentralMovingAverage();
			TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, windowSize);
			movingAveragemultipleTimeSeries.add(movingAverageSeries);
			algorithm.printStats();
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, movingAveragemultipleTimeSeries, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCentralMovingAverageFromFileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

