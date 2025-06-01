package timeseries.normalization;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoMinMaxNormalization {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoMinMaxNormalization() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		if( timeSeries.size() < 1){
			throw new IllegalArgumentException(" The time series should contain at least 1 point.");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] resultTimeSeriesArray = calculateMinMaxNormalization(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_MMAX");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return transformedTimeSeries;
	}
	private double[] calculateMinMaxNormalization(double[] timeSeries) {
		double[] result = new double[timeSeries.length]; 
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for(int i =0; i < timeSeries.length; i++){
			if(timeSeries[i] < min){
				min = timeSeries[i];
			}
			if(timeSeries[i] > max){
				max = timeSeries[i];
			}
		}
		double MaxMinusMin = max - min;
		for(int i = 0; i < timeSeries.length; i++){
			result[i] = (timeSeries[i] - min) / MaxMinusMin;
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Min max normalization: " + Arrays.toString(result));
		}	
		return result;
	}
	public void printStats() {
		System.out.println("=============  Min Max Normalization transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
