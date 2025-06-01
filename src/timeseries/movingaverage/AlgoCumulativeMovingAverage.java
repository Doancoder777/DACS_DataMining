package timeseries.movingaverage;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoCumulativeMovingAverage {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoCumulativeMovingAverage() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] movingAverageData = calculateCumulativeMovingAverage(timeSeries.data);
		TimeSeries movingAverage = new TimeSeries(movingAverageData,  timeSeries.getName() + "_CUMAVG");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return movingAverage;
	}
	private double[] calculateCumulativeMovingAverage(double[] timeSeries) {
		double[] movingAverage = new double[timeSeries.length];
		double sum = 0d;
		for(int i =0; i < timeSeries.length; i++){
			sum+= timeSeries[i];
			movingAverage[i] = sum / (i+1); //  JUST FILL THE SAME VALUE
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Cumulative Moving average transformation: " + Arrays.toString(movingAverage));
		}	
		return movingAverage;
	}
	public void printStats() {
		System.out.println("=============  Transform to Cumulative Moving Average v2.05- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
