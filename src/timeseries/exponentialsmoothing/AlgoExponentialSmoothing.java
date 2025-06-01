package timeseries.exponentialsmoothing;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoExponentialSmoothing {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoExponentialSmoothing() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries, double alpha) throws IOException {
		if( alpha < 0 || alpha > 1){
			throw new IllegalArgumentException(" The alpha constant should be a value in the [0,1] interval");
		}
		if( timeSeries.size() <= 1){
			throw new IllegalArgumentException(" The time series should contain at least 2 points to apply exponential smoothing.");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] transformedTimeSeriesArray = calculateExponentialSmoothing(timeSeries.data, alpha);
		TimeSeries transformedTimeSeries = new TimeSeries(transformedTimeSeriesArray,  timeSeries.getName() + "_EXPSTHG");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return transformedTimeSeries;
	}
	private double[] calculateExponentialSmoothing(double[] timeSeries, double alpha) {
		double[] exponentialSmoothing = new double[timeSeries.length]; 
		exponentialSmoothing[0] = timeSeries[0];
		for(int i =1; i < timeSeries.length; i++){
			exponentialSmoothing[i] = (timeSeries[i] * alpha) + ( exponentialSmoothing[i-1] * (1.0 - alpha)) ;
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Alpha = " + alpha);
			System.out.println(" Exponential smoothing transformation: " + Arrays.toString(exponentialSmoothing));
		}	
		return exponentialSmoothing;
	}
	public void printStats() {
		System.out.println("=============  Transform to Exponential Smoothing v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
