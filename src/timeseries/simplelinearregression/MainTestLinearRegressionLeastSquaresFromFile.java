package timeseries.simplelinearregression;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import timeseries.reader_writer.AlgoTimeSeriesReader;
import timeseries.reader_writer.AlgoTimeSeriesWriter;
public class MainTestLinearRegressionLeastSquaresFromFile {
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextSAX.txt");  
		String output = "./output.txt";
		String separator = ",";
		AlgoTimeSeriesReader reader = new AlgoTimeSeriesReader();
		List<TimeSeries> multipleTimeSeries = reader.runAlgorithm(input, separator);
		List<TimeSeries> regressionLines = new ArrayList<TimeSeries>();
		for(TimeSeries timeSeries : multipleTimeSeries){
			AlgoTimeSeriesLinearRegressionLeastSquare algorithm = new AlgoTimeSeriesLinearRegressionLeastSquare();
			algorithm.trainModel(timeSeries);
			TimeSeries regressionLine = algorithm.calculateRegressionLine(timeSeries);
			regressionLines.add(regressionLine);
			algorithm.printStats();
		}
		AlgoTimeSeriesWriter algorithm2 = new AlgoTimeSeriesWriter();
		algorithm2.runAlgorithm(output, regressionLines, separator);
		algorithm2.printStats();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestLinearRegressionLeastSquaresFromFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

