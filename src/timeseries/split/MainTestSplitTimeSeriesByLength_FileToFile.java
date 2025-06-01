package timeseries.split;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestSplitTimeSeriesByLength_FileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAX.txt");  
		String output = "./output.txt";
		int sizeOfSegment = 3;
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> allSplittedTimeSeries = new ArrayList<TimeSeries>(multipleTimeSeries.size()*2);
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoSplitTimeSeries algorithm = new AlgoSplitTimeSeries();
			TimeSeries[] splittedTimeSeries = algorithm.runAlgorithm(timeSeries, sizeOfSegment);
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
		URL url = MainTestSplitTimeSeriesByLength_FileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

