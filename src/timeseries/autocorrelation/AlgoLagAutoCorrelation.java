package timeseries.autocorrelation;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoLagAutoCorrelation {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoLagAutoCorrelation() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries,  int maxlag) throws IOException {
		if( maxlag < 1 || maxlag > timeSeries.size()){
			throw new IllegalArgumentException(" The maxlag parameter must be set as follows:  1 <= maxlag <= timeSeries.length");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] resultingTimeSeriesArray = calculateAutocorrelationTimeSeries(timeSeries.data, maxlag);
		TimeSeries resultingTimeSeries = new TimeSeries(resultingTimeSeriesArray,  timeSeries.getName() + "_AUTOCOR");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return resultingTimeSeries;
	}
	private double[] calculateAutocorrelationTimeSeries(double[] timeSeries, int maxlag) {
		double[] autocorrelationResult = new double[maxlag+1]; 
		autocorrelationResult[0] = 1;
		double mean = 0d;
		for(int i = 0; i < timeSeries.length; i++){
			mean += timeSeries[i];
		}
		mean /= timeSeries.length;
		double denominator = 0d;
		for(int i = 0; i < timeSeries.length; i++){
			denominator += Math.pow(timeSeries[i] - mean, 2.0);
		}
		for(int k=1; k <= maxlag; k++){
			double numerator = 0d;
			for(int i = 0; i < timeSeries.length - k; i++){
				numerator += (timeSeries[i] - mean) * (timeSeries[i+k] - mean);
			}
			autocorrelationResult[k] = numerator / denominator;
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Minlag = " + maxlag);
			System.out.println(" Exponential smoothing transformation: " + Arrays.toString(autocorrelationResult));
		}	
		return autocorrelationResult;
	}
	public void printStats() {
		System.out.println("=============  Transform to lag k autocorrelation time series v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
