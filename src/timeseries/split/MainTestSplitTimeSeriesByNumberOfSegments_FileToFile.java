package timeseries.split;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestSplitTimeSeriesByNumberOfSegments_FileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAX.txt");  
		String output = "./output.txt";
		int numberOfSeries = 2;
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> allSplittedTimeSeries = new ArrayList<TimeSeries>(multipleTimeSeries.size()*numberOfSeries);
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoSplitTimeSeries algorithm = new AlgoSplitTimeSeries();
			TimeSeries[] splittedTimeSeries =  algorithm.runAlgorithm(numberOfSeries, timeSeries);
			algorithm.printStats();
			for(TimeSeries series: splittedTimeSeries){
				allSplittedTimeSeries.add(series);
			}
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, allSplittedTimeSeries, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSplitTimeSeriesByNumberOfSegments_FileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

