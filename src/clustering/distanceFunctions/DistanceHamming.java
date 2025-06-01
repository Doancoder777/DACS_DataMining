package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public class DistanceHamming extends DistanceFunction {
	static String NAME = "HAMMING";
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double distance = 0;
		for (int i = 0; i < vector1.data.length; i++) {
			if (vector1.data[i] != vector2.data[i]) {
				distance++;
			}
		}
		return distance;
	}
	@Override
	public String getName() {
		return NAME;
	}
}
