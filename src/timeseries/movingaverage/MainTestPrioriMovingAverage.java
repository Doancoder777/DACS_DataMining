package timeseries.movingaverage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestPrioriMovingAverage {
	public static void main(String [] arg) throws IOException{
		int windowSize = 3;
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoPriorMovingAverage algorithm = new AlgoPriorMovingAverage();
		TimeSeries movingAverageSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
		System.out.println(" Prior Moving average: ");
		System.out.println(movingAverageSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrioriMovingAverage.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

