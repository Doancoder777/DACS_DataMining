package timeseries.paa;
import java.io.IOException;
import java.util.Arrays;
import timeseries.TimeSeries;
import timeseries.sax.AlgoSAX;
import tools.MemoryLogger;
public class AlgoPiecewiseAggregateApproximation {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	public AlgoPiecewiseAggregateApproximation() {
	}
	public TimeSeries runAlgorithm(TimeSeries timeSeries, int numberOfSegments) throws IOException {
		if(timeSeries.data.length < numberOfSegments){
			throw new IllegalArgumentException(" The number of segments should be less than or equal to the number of data points in the time series");
		}
		if(numberOfSegments < 2){
			throw new IllegalArgumentException(" This implementation only support a number of segments > 1");
		}
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + timeSeries.toString());
		}
		double[] piecewiseTransformedData = transformTimeSeriesToPAARepresentation(timeSeries.data, numberOfSegments);
		TimeSeries paaSeries = new TimeSeries(piecewiseTransformedData, 
				timeSeries.getName() + "_PAA");
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return paaSeries;
	}
	private double[] transformTimeSeriesToPAARepresentation(double[] dataPoints, int numberOfSegments) {
		double[] piecewiseTransformedData = new double[numberOfSegments];
		double segmentSize = dataPoints.length  / (double)numberOfSegments;
		double wholePartSegmentSize = Math.floor(segmentSize);
		boolean isExactlyDivisible = segmentSize == (int) segmentSize;
		double currentPoint = 0;
		for(; currentPoint < wholePartSegmentSize; currentPoint++){
			piecewiseTransformedData[0] += dataPoints[(int)currentPoint];
		}
		if(isExactlyDivisible == false){
			double weight = segmentSize - Math.floor(segmentSize);
			piecewiseTransformedData[0] += dataPoints[(int)currentPoint] * weight;
			currentPoint += weight;
		}
		piecewiseTransformedData[0]  /= segmentSize;
		for(int currentSegment = 1; currentSegment < numberOfSegments; currentSegment++){
			double remainingSegmentSize = segmentSize;
			boolean currenPointIsExactlyDivisible = currentPoint == (int) currentPoint;
			if(currenPointIsExactlyDivisible == false){
				double weight = Math.ceil(currentPoint) - currentPoint;
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint] * weight;
				currentPoint += weight;
				remainingSegmentSize -= weight;
			}
			for(; remainingSegmentSize>=1; remainingSegmentSize--, currentPoint++){
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint];
			}
			if(remainingSegmentSize > 0 && currentPoint < dataPoints.length){
				double weight = remainingSegmentSize;
				piecewiseTransformedData[currentSegment] += dataPoints[(int)currentPoint] * weight;
				currentPoint += weight;
			}
			piecewiseTransformedData[currentSegment]  /= segmentSize;
		}
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + " --- Dividing time series into segments ---");
			System.out.println(" Number of data points = " + dataPoints.length);
			System.out.println(" Number of segments = " + numberOfSegments);
			System.out.println(" Segment size = " + segmentSize);
			System.out.println(" Is exactly divisible into segment? " + isExactlyDivisible);
			System.out.println(" Piecewise-Aggregate-transformation: " + Arrays.toString(piecewiseTransformedData));
		}	
		return piecewiseTransformedData;
	}
	public void printStats() {
		System.out.println("=============  Transform To PAA  ALGORITHM v2.05- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
