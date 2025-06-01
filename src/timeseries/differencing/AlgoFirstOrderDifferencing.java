package timeseries.differencing;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoFirstOrderDifferencing {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoFirstOrderDifferencing() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		if( timeSeries.size() < 2){
			throw new IllegalArgumentException(" The time series should contain at least 2 points to apply differencing.");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] resultTimeSeriesArray = calculateFirstOrderDifferencing(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_FODIFF");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return transformedTimeSeries;
	}
	private double[] calculateFirstOrderDifferencing(double[] timeSeries) {
		double[] result = new double[timeSeries.length - 1]; 
		for(int i =1; i < timeSeries.length; i++){
			result[i-1] = timeSeries[i] - timeSeries[i-1];
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" First order differencing: " + Arrays.toString(result));
		}	
		return result;
	}
	public void printStats() {
		System.out.println("=============  First order differencing transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
