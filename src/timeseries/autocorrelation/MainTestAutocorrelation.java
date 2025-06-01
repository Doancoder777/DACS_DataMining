package timeseries.autocorrelation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestAutocorrelation {
	public static void main(String [] arg) throws IOException{
		int maxlag = 16;
		double [] dataPoints = new double[]{3.0,2.0,8.0,9.0,8.0,9.0,8.0,7.0,6.0,7.0,5.0,4.0,2.0,7.0,9.0,8.0,5.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoLagAutoCorrelation algorithm = new AlgoLagAutoCorrelation();
		TimeSeries aSeries = algorithm.runAlgorithm(timeSeries,maxlag);
		algorithm.printStats();
		System.out.println(" Auto-correlation for lag: 1 to : " + maxlag);
		System.out.println(aSeries.toString());
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAutocorrelation.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

