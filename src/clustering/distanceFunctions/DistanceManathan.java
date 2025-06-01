package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public class DistanceManathan extends DistanceFunction {
	static String NAME = "manathan";
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double sum =0;	
		for(int i=0; i< vector1.data.length; i++){
			sum += Math.abs(vector1.data[i] - vector2.data[i]);
		}
		return sum;
	}
	@Override
	public String getName() {
		return NAME;
	}
}

