package timeseries.paa;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestPAA_of_SingleTimeSeries {
	public static void main(String [] arg) throws IOException{
		int numberOfSegments = 3;
		double [] dataPoints = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoPiecewiseAggregateApproximation algorithm = new AlgoPiecewiseAggregateApproximation();
		TimeSeries paaTimeSeries = algorithm.runAlgorithm(timeSeries, numberOfSegments);
		algorithm.printStats();
		System.out.println(" Piecewise Aggregation Approximation: ");
		System.out.println(paaTimeSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPAA_of_SingleTimeSeries.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

