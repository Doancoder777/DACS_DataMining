package timeseries.mediansmoothing;
import java.io.IOException;
import java.util.Arrays;
import Algo.sort.Select;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoMedianSmoothing {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoMedianSmoothing() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int windowSize) throws IOException {
		if(windowSize >= timeSeries.data.length  ||  windowSize < 2 ){
			throw new IllegalArgumentException(" The window size must be greater than 1, and no larger than the number of points in the time series");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}
		double[] movingMedianData;
		if((windowSize % 2) == 1){
			movingMedianData = calculateMedianSmoothingOdd(timeSeries.data, windowSize);
		}else{
			movingMedianData = calculateMedianSmoothingEven(timeSeries.data, windowSize);
		}
		TimeSeries medianSmoothing = new TimeSeries(movingMedianData,  timeSeries.getName() + "_CEMEDSMT");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return medianSmoothing;
	}
	private double[] calculateMedianSmoothingOdd(double[] timeSeries, int windowSize) {
		int alpha = (windowSize - 1) / 2;
		double[] medianSmoothing = new double[timeSeries.length - (windowSize -1)];
		double[] window = new double[windowSize];
		for(int i = alpha; i < timeSeries.length - alpha; i++){
			int smoothingPosition = i - alpha;
			System.arraycopy(timeSeries, smoothingPosition, window, 0, windowSize);
			 medianSmoothing[smoothingPosition] = Select.randomizedSelect(window, alpha);
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Median smoothing transformation: " + Arrays.toString(medianSmoothing));
		}	
		return medianSmoothing;
	}
	private double[] calculateMedianSmoothingEven(double[] timeSeries, int windowSize) {
		int alpha = (windowSize - 2) / 2;
		double[] medianSmoothing = new double[timeSeries.length - (windowSize - 2) - 1];
		double[] window = new double[windowSize];
		for(int i = alpha; i < timeSeries.length - alpha-1; i++){
			int smoothingPosition = i - alpha;
			System.arraycopy(timeSeries, smoothingPosition, window, 0, windowSize);
			double leftMiddle = Select.randomizedSelect(window, alpha);
			double rightMiddle = Select.randomizedSelect(window, alpha+1);
			 medianSmoothing[smoothingPosition] = (leftMiddle + rightMiddle) / 2.0d;
		}
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Window size = " + windowSize);
			System.out.println(" Median smoothing transformation: " + Arrays.toString(medianSmoothing));
		}	
		return medianSmoothing;
	}
	public void printStats() {
		System.out.println("=============  Transform to Median Smoothing v2.25 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
