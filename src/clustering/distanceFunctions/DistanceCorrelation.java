package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public class DistanceCorrelation extends DistanceFunction {
	static String NAME = "correlation";
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double mean1 = calculateMean(vector1);	
		double mean2 = calculateMean(vector2);	
		double standardDeviation1 = calculateStdDeviation(vector1, mean1);	
		double standardDeviation2 = calculateStdDeviation(vector2, mean2);	
		double correlation = 0;
		for(int i=0; i< vector1.data.length; i++){
			correlation -= (vector1.data[i] - mean1) * (vector2.data[i] - mean2);
		}
		if(standardDeviation1 == 0) {
			standardDeviation1 = 0.0001;
		}
		if(standardDeviation2 == 0) {
			standardDeviation2 = 0.0001;
		}
		double bottom = (standardDeviation1 * standardDeviation2 * (vector1.data.length - 1));
		correlation = correlation / (bottom );
		return (1.0 + correlation) / 2.0;
	}
	private static double calculateMean(DoubleArray vector) {
		double sum = 0;
		for (double val : vector.data) {
			sum += val;
		}
		return sum / vector.data.length;
	}
	private static double calculateStdDeviation(DoubleArray vector, double mean) {
		double deviation = 0;
		for (double val : vector.data) {
			deviation += Math.pow(mean - val, 2);
		}
		return Math.sqrt(deviation / (vector.data.length - 1));
	}
	public static void main(String[] args) {
		DoubleArray array1 = new DoubleArray(new double[] {2, 3, 1, 1, 1});
		DoubleArray array2 = new DoubleArray(new double[] {2, 1, 1, 1, 1});
		System.out.println(new DistanceCorrelation().calculateDistance(array1,array2));
		DoubleArray array5 = new DoubleArray(new double[] {3, 6, 0, 3, 6});
		DoubleArray array6 = new DoubleArray(new double[] {1, 2, 0, 1, 2});
		System.out.println(new DistanceCorrelation().calculateDistance(array5,array6));
		DoubleArray array7 = new DoubleArray(new double[] {3, 6, 0, 3, 6});
		DoubleArray array8 = new DoubleArray(new double[] {-1, -2, 0, -1, -2});
		System.out.println(new DistanceCorrelation().calculateDistance(array7,array8));
		DoubleArray array3 = new DoubleArray(new double[] {3, -6, 0, 3, -6});
		DoubleArray array4 = new DoubleArray(new double[] {-1, 2, 0, -1, 2});
		System.out.println(new DistanceCorrelation().calculateDistance(array3,array4));
	}
	@Override
	public String getName() {
		return NAME;
	}
}

