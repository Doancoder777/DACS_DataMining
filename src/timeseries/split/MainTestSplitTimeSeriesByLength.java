package timeseries.split;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestSplitTimeSeriesByLength {
	public static void main(String [] arg) throws IOException{
		int sizeOfSegment = 3;
		double [] dataPoints = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		AlgoSplitTimeSeries algorithm = new AlgoSplitTimeSeries();
		TimeSeries[] splittedTimeSeries = algorithm.runAlgorithm(timeSeries, sizeOfSegment);
		algorithm.printStats();
		System.out.println(" Splitted time series: ");
		for(int i=0; i < splittedTimeSeries.length; i++){
			System.out.println("Time series " + i + " " + splittedTimeSeries[i]);
		}
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSplitTimeSeriesByLength.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

