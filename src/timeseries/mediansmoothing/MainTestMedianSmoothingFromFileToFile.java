package timeseries.mediansmoothing;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestMedianSmoothingFromFileToFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextMovingAverage.txt");  
		String output = "./output.txt";
		int windowSize = 3;
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> medianSmoothingMultipleTimeSeries = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoMedianSmoothing algorithm = new AlgoMedianSmoothing();
			TimeSeries medianSmoothingSeries = algorithm.runAlgorithm(timeSeries, windowSize);
			medianSmoothingMultipleTimeSeries.add(medianSmoothingSeries);
			algorithm.printStats();
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, medianSmoothingMultipleTimeSeries, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMedianSmoothingFromFileToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

