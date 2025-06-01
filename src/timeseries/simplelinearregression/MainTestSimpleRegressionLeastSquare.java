package timeseries.simplelinearregression;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import timeseries.TimeSeries;
public class MainTestSimpleRegressionLeastSquare {
	public static void main(String [] arg) throws IOException{
		double [] dataPoints = new double[]{-1, -2, -3};
		TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");
		System.out.println("The input data is: ");
		System.out.println(" " + timeSeries.toString());
		System.out.println();
		AlgoTimeSeriesLinearRegressionLeastSquare algorithm = new AlgoTimeSeriesLinearRegressionLeastSquare();
		algorithm.trainModel(timeSeries);
		algorithm.printStats();
		System.out.println();
		System.out.println("The following regression model is obtained: ");
		double bias = algorithm.getBias();
		double coefficient = algorithm.getCoefficient();
		System.out.println("  Y(x) = " + bias + " + " + coefficient + " * x \n");
		TimeSeries regressionLine = algorithm.calculateRegressionLine(timeSeries);
		System.out.println("The regression line for the input data is: ");
		System.out.println(" " + regressionLine.toString());
		System.out.println();
		System.out.println("We can use the model to make a prediction for a new value of x. \nFor example:");
		double prediction = algorithm.performPrediction(11d);
		System.out.println(" The prediction for x = 11 is  y = " + prediction);
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSimpleRegressionLeastSquare.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}

