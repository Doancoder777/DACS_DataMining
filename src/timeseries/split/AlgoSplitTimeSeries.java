package timeseries.split;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoSplitTimeSeries {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	private int  numberOfSeries;
	public AlgoSplitTimeSeries() {
	}
	public TimeSeries[] runAlgorithm(TimeSeries timeSeries, int sizeOfSegment) {
		if(sizeOfSegment < 1){
			throw new IllegalArgumentException(" The size of segment should be > 1");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		numberOfSeries =  (int) Math.ceil((double)timeSeries.data.length / sizeOfSegment);
		if(DEBUG_MODE){
			System.out.println(" The time series: " + timeSeries);
			System.out.println(" The length of the time series is: " + timeSeries.data.length);
			System.out.println(" The size of segment is : " + sizeOfSegment);
			System.out.println(" It will be splitted into " + numberOfSeries + " time series.");
		}
		TimeSeries[] splittedTimeSeries = splitTimeSeries(timeSeries, sizeOfSegment);
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return splittedTimeSeries;
	}
	public TimeSeries[] runAlgorithm(int number, TimeSeries timeSeries){
		if(number < 1){
			throw new IllegalArgumentException(" The number of segments should be > 1");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		int sizeOfSegment = (int) Math.ceil((double)timeSeries.size() / number);
		numberOfSeries = number;
		if(DEBUG_MODE){
			System.out.println(" The time series: " + timeSeries);
			System.out.println(" The length of the time series is: " + timeSeries.data.length);
			System.out.println(" The size of segment is : " + sizeOfSegment);
			System.out.println(" It will be splitted into " + numberOfSeries + " time series.");
		}
		TimeSeries[] splittedTimeSeries = splitTimeSeries(timeSeries, sizeOfSegment);
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return splittedTimeSeries;
	}
	private TimeSeries[] splitTimeSeries(TimeSeries timeSeries, int sizeOfSegment) {
		TimeSeries[] splittedTimeSeries = new TimeSeries[(int) Math.ceil(numberOfSeries)];
		int currentDataPoint = 0;
		for(int i = 0; i < numberOfSeries; i++){
			int numberOfPoints = timeSeries.data.length - currentDataPoint;
			if(numberOfPoints >= sizeOfSegment){
				numberOfPoints  = sizeOfSegment;
			}
			double[] dataPoints = new double[numberOfPoints];
			splittedTimeSeries[i] = 
					new TimeSeries(dataPoints, timeSeries.getName()+ "_PART" + i);
			for(int j=0; j < numberOfPoints; j++){
				double dataPoint = timeSeries.data[currentDataPoint++];
				splittedTimeSeries[i].data[j] = dataPoint;
			}
		}
		return splittedTimeSeries;
	}
	public void printStats() {
		System.out.println("=============  Split Time Series  ALGORITHM v2.06 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" The result is " + numberOfSeries + " time series.");
		System.out.println("===================================================");
	}
}
