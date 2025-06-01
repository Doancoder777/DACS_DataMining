package timeseries.movingaverage;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoPriorMovingAverage {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoPriorMovingAverage() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		if(timeSeries.data.length < windowSize){
			throw new IllegalArgumentException(" The window size should be greater or equal to 1");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] movingAverageData = calculatePriorMovingAverage(timeSeries.data, windowSize);
		TimeSeries movingAverage = new TimeSeries(movingAverageData,  timeSeries.getName() + "_PMAVG");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return movingAverage;
	}
	private double[] calculatePriorMovingAverage(double[] timeSeries, int windowSize) {
		double[] movingAverage = new double[timeSeries.length];
		double sum = 0d;
		double firstSum = 0d;
		for(int i = 0; i < timeSeries.length; i++){
			if(i == 0){
				firstSum += timeSeries[0];
				sum+= timeSeries[0] / windowSize;
			}else if(i < windowSize){
				movingAverage[i-1] = firstSum / i; //  JUST FILL THE SAME VALUE
				firstSum += timeSeries[i];
				sum+= timeSeries[i] / windowSize;
			}else{
				movingAverage[i-1] = sum;
				sum+= timeSeries[i] / windowSize;
				sum-= timeSeries[i-windowSize] / windowSize;
			}
		}
		movingAverage[timeSeries.length-1] = sum;
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Prior Moving average transformation: " + Arrays.toString(movingAverage));
		}	
		return movingAverage;
	}
	public void printStats() {
		System.out.println("=============  Transform to Prior Moving Average v2.21 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
