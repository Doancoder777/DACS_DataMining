package timeseries.standardization;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoStandardization {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoStandardization() {
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
		double[] resultTimeSeriesArray = calculateStandardization(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_STDDZ");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return transformedTimeSeries;
	}
	private double[] calculateStandardization(double[] timeSeries) {
		double[] result = new double[timeSeries.length]; 
		double mean = 0;
		for(int i =0; i < timeSeries.length; i++){
			mean += timeSeries[i] ;
		}
		mean = mean / timeSeries.length;
		double standardDeviation = 0;
		for(int i = 0; i < timeSeries.length; i++){
			standardDeviation += Math.pow(timeSeries[i] - mean, 2);
		}
		standardDeviation = Math.sqrt(standardDeviation / timeSeries.length);
		for(int i =0; i < timeSeries.length; i++){
			result[i] = (timeSeries[i] - mean) / standardDeviation;
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Standardization: " + Arrays.toString(result));
		}	
		return result;
	}
	public void printStats() {
		System.out.println("=============  Standardization transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
