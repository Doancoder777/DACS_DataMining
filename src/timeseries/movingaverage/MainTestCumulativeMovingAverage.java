package timeseries.movingaverage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestCumulativeMovingAverage {
	public static void main(String [] arg) throws IOException{
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoCumulativeMovingAverage algorithm = new AlgoCumulativeMovingAverage();
		TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries);
		algorithm.printStats();
		System.out.println(" Moving average: ");
		System.out.println(movingAverageSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCumulativeMovingAverage.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

