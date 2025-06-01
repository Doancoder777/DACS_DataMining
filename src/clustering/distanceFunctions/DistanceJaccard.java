package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public class DistanceJaccard extends DistanceFunction {
	static String NAME = "jaccard";
	public double calculateDistance(DoubleArray vector1, DoubleArray vector2) {
		double count11 = 0;	  // count of M11
		double count10or01or11 = 0; // count of M01, M10 and M11
		for(int i=0; i< vector1.data.length; i++){
			if(vector1.data[i] != 0  || vector2.data[i] != 0) {
				if(vector1.data[i] == 1  && vector2.data[i] == 1) {
					count11++;
				}
				count10or01or11++;
			}
		}
		return count11 / count10or01or11;
	}
	@Override
	public String getName() {
		return NAME;
	}
	public static void main(String[] args) {
		DoubleArray array1 = new DoubleArray(new double[] {0,1,0,1});
		DoubleArray array2 = new DoubleArray(new double[] {1,0,0,1});
		System.out.println(new DistanceJaccard().calculateDistance(array1,array2));
		DoubleArray array4 = new DoubleArray(new double[] {1, 0});
		DoubleArray array3 = new DoubleArray(new double[] {1, 0});
		System.out.println(new DistanceCosine().calculateDistance(array3,array4));
	}
}

