package timeseries.sax;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
public class MainTestConvertTimeSeriesFiletoSequenceFileWithSAX {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAX.txt");  
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> timeSeries = reader.runAlgorithm(input, separator);
		reader.printStats();
		String output = ".//output.txt";  
		int numberOfSegments = 8;
		int numberOfSymbols = 4;
		boolean deactivatePAA = false;
		AlgoConvertTimeSeriesFileToSequencesWithSAX algorithm = new AlgoConvertTimeSeriesFileToSequencesWithSAX();
		algorithm.runAlgorithm(timeSeries, output, numberOfSegments, numberOfSymbols, deactivatePAA);
		algorithm.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestConvertTimeSeriesFiletoSequenceFileWithSAX.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

