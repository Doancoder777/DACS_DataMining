package timeseries.exponentialsmoothing;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestExponentialSmoothing {
	public static void main(String [] arg) throws IOException{
		double alpha = 1;
		double [] dataPoints = new double[]{1.0, 4.5, 6.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0};;
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoExponentialSmoothing algorithm = new AlgoExponentialSmoothing();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries, alpha);
		algorithm.printStats();
		System.out.println(" Exponential smoothing: ");
		System.out.println(aSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestExponentialSmoothing.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

