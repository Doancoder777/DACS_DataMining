package clustering.distanceFunctions;
import patterns.cluster.DoubleArray;
public abstract class DistanceFunction {
	public abstract double calculateDistance(DoubleArray vector1, DoubleArray vector2);
	public  abstract String getName();
	public static DistanceFunction getDistanceFunctionByName(String name){
		if(DistanceCorrelation.NAME.equals(name)) {
			return new DistanceCorrelation();
		}else if(DistanceCosine.NAME.equals(name)) {
			return new DistanceCosine();
		}else if(DistanceEuclidian.NAME.equals(name)) {
			return new DistanceEuclidian();
		}else if(DistanceManathan.NAME.equals(name)) {
			return new DistanceManathan();
		}else if(DistanceJaccard.NAME.equals(name)) {
			return new DistanceJaccard();
		}else if(DistanceChebyshev.NAME.equals(name)) {
			return new DistanceChebyshev();
		}
		return null;
	}
}

