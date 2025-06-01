package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public class DistanceChebyshev extends DistanceFunction {
	static String NAME = "chebyshev";
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double maxDiff = 0;
		for (int i = 0; i < vector1.data.length; i++) {
			double diff = Math.abs(vector1.data[i] - vector2.data[i]);
			if (diff > maxDiff) {
				maxDiff = diff;
			}
		}
		return maxDiff;
	}
	@Override
	public String getName() {
		return NAME;
	}
}
