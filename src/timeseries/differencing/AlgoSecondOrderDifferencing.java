package timeseries.differencing;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoSecondOrderDifferencing {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoSecondOrderDifferencing() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		if( timeSeries.size() < 3){
			throw new IllegalArgumentException(" The time series should contain at least 3 points to apply differencing.");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] resultTimeSeriesArray = calculateSecondOrderDifferencing(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_SODIFF");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return transformedTimeSeries;
	}
	private double[] calculateSecondOrderDifferencing(double[] timeSeries) {
		double[] result = new double[timeSeries.length - 2]; 
		for(int i = 2; i < timeSeries.length; i++){
			result[i-2] = timeSeries[i] - (2* timeSeries[i-1]) + (timeSeries[i-2]);
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Second order differencing: " + Arrays.toString(result));
		}	
		return result;
	}
	public void printStats() {
		System.out.println("=============  Second order differencing transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
