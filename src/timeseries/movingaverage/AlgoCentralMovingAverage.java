package timeseries.movingaverage;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoCentralMovingAverage {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoCentralMovingAverage() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		if(windowSize >= timeSeries.data.length  ||  windowSize < 3 || (windowSize % 2) != 1){
			throw new IllegalArgumentException(" The window size must be odd, greater than 1, and no larger than the number of points in the time series");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] movingAverageData = calculateCentralMovingAverage(timeSeries.data, windowSize);
		TimeSeries movingAverage = new TimeSeries(movingAverageData,  timeSeries.getName() + "_CEMAVG");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return movingAverage;
	}
	private double[] calculateCentralMovingAverage(double[] timeSeries, int windowSize) {
		double[] movingAverage = new double[timeSeries.length];
		int alpha = (windowSize-1) / 2;
		double sum = 0d;
		for(int i = 0; i <= alpha; i++){
			sum += timeSeries[i];
		}
		movingAverage[0] = sum / (alpha+1);
		for(int j = 1; j <= alpha; j++){
			sum += timeSeries[j+alpha];
			movingAverage[j] = sum / (alpha+1+j);
		}
		for(int j = alpha+1; j < (timeSeries.length-alpha); j++){
			sum+= timeSeries[j+alpha];
			sum-= timeSeries[j-alpha-1];
			movingAverage[j] = sum / windowSize;
		}
		int pointsRemoved = 0;
		for(int j = (timeSeries.length-alpha); j < timeSeries.length; j++){
			sum-= timeSeries[j-alpha-1];
			pointsRemoved++;
			movingAverage[j] = sum / (windowSize-pointsRemoved);
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Central Moving average transformation: " + Arrays.toString(movingAverage));
		}	
		return movingAverage;
	}
	public void printStats() {
		System.out.println("=============  Transform to Central Moving Average v2.21 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
