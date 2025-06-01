package timeseries.standardization;



import java.io.IOException;
import java.util.Arrays;

import timeseries.TimeSeries;
import tools.MemoryLogger;

/**
 * An algorithm to calculate the standardization of a time series.
 * The purpose of using this algorithm is to transform a time series such that all values
 * are standardized (have a mean of 0 and a standard deviation of 1).  
 * Each value y is replaced by:  y = x - mean / standard_deviation
 * 
 * @author Philippe Fournier-Viger, 2017
 */
public class AlgoStandardization {
 
	/** the time the algorithm started */
	long startTimestamp = 0; 
	/** the time the algorithm terminated */
	long endTimestamp = 0;  
	
	/** This program will execute in DEBUG MODE if this variable is true */
	boolean DEBUG_MODE = false;
		
	/**
	 * Default constructor
	 */
	public AlgoStandardization() {
	}

	/**
	 * Generate the standardization of a time series containing at least 1 data point.
	 * @param timeSeries a time series 
	 * @return the standardization of the time series (a time series)
	 * @throws IOException exception if error while writing the file
	 */
	public TimeSeries runAlgorithm(TimeSeries timeSeries) throws IOException {
		// Validate parameters	
		if( timeSeries.size() < 1){
			throw new IllegalArgumentException(" The time series should contain at least 1 point.");
		}

		// reset memory logger
		MemoryLogger.getInstance().reset();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();

		// IF in debug mode
		if(DEBUG_MODE){
			// Print the time series
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		}

		// Create an array to store the result
		double[] resultTimeSeriesArray = calculateStandardization(timeSeries.data);
		TimeSeries transformedTimeSeries = new TimeSeries(resultTimeSeriesArray,  timeSeries.getName() + "_STDDZ");
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// record end time
		endTimestamp = System.currentTimeMillis();

		return transformedTimeSeries;
	}
	
	/**
	 * Generate the standardization of a time series
	 * @param timeSeries a time series represented by a double array
	 * @return the transformed time series
	 */
	private double[] calculateStandardization(double[] timeSeries) {
		// Create an array to store the resulting time series
		// ********************************************************************** //
		// NOTE : THIS ARRAY IS OF SIZE N -1  //
		//*********************************************************************** //
		double[] result = new double[timeSeries.length]; 
		
		// find the mean
		double mean = 0;
		for(int i =0; i < timeSeries.length; i++){
			mean += timeSeries[i] ;
		}
		mean = mean / timeSeries.length;
		
		// find the standard deviation
		double standardDeviation = 0;
		for(int i = 0; i < timeSeries.length; i++){
			standardDeviation += Math.pow(timeSeries[i] - mean, 2);
		}
		
		standardDeviation = Math.sqrt(standardDeviation / timeSeries.length);
		
		// For each data point, calculate the new value
		for(int i =0; i < timeSeries.length; i++){
			// y' = (x - mean) / standard_deviation
			result[i] = (timeSeries[i] - mean) / standardDeviation;
		}
		
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Standardization: " + Arrays.toString(result));
		}	
		
		return result;
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  Standardization transformation v2.21- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}

}