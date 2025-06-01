package timeseries.simplelinearregression;
import java.util.Arrays;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoTimeSeriesLinearRegressionLeastSquare {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	double bias = 0d;
	double coefficient = 0d;
	public AlgoTimeSeriesLinearRegressionLeastSquare() {
	}
	public void trainModel(TimeSeries timeSeries) {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		if(DEBUG_MODE){
			System.out.println(" Time series: " + Arrays.toString(timeSeries.data));
		} 
		trainRegressionModel(timeSeries.data);
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
	}
	public TimeSeries calculateRegressionLine(TimeSeries series){
		double[] timeSeries = series.data;
		double[] regressionLine = new double[timeSeries.length];
		for (int i = 0; i < timeSeries.length; i++) {
			regressionLine[i] = performPrediction(i);
		}
		if(DEBUG_MODE){
			System.out.println(" Time-series obtained by the regression: " + Arrays.toString(regressionLine));
		}	
		return  new TimeSeries(regressionLine,  series.getName() + "_LR");
	}
	private void trainRegressionModel(double[] timeSeries) {
		double sumXvalues = 0d;
		for (int i = 0; i < timeSeries.length; i++) {
			sumXvalues += timeSeries[i]; // x value
		}
		double averageXvalues = sumXvalues / timeSeries.length;
		double sumYvalues = 0d;
		for (int i = 0; i < timeSeries.length; i++) {
			sumYvalues += i; // y value
		}
		double averageYvalues = sumYvalues / timeSeries.length;
		double sumOfErrorsXwithX = 0d;
		double sumOfErrorsXwithY = 0d;
		for (int i = 0; i < timeSeries.length; i++) {
			double xi = timeSeries[i];
			double difference = (xi - averageXvalues);
			sumOfErrorsXwithX += difference * difference;
			double yi = i;
			sumOfErrorsXwithY += difference * (yi - averageYvalues);
		}
		 coefficient = sumOfErrorsXwithY / sumOfErrorsXwithX;
		 bias = averageYvalues - (coefficient * averageXvalues);
		if(DEBUG_MODE){
			System.out.println(" Number of data points = " + timeSeries.length);
			System.out.println(" Regression line is: ");
			System.out.println("  Y(x) = " + bias + " + " + coefficient + " * x");
		}	
	}
	public double performPrediction(double x){
		return bias + x * coefficient;
	}
	public double getBias() {
		return bias;
	}
	public double getCoefficient() {
		return coefficient;
	}
	public void printStats() {
		System.out.println("=============  Linear regression (least squares) v2.19- STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("===================================================");
	}
}
