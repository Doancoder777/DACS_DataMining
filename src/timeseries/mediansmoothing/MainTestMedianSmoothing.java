package timeseries.mediansmoothing;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestMedianSmoothing {
	public static void main(String [] arg) throws IOException{
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		int windowSize = 3;
		AlgoMedianSmoothing algorithm = new AlgoMedianSmoothing();
		TimeSeries medianSmoothingSeries = algorithm.runAlgorithm(timeSeries, windowSize);
		algorithm.printStats();
		System.out.println(" Median smoothing: ");
		System.out.println(medianSmoothingSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMedianSmoothing.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

