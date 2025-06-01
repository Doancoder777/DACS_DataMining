package datastructures.kdtree;
import patterns.cluster.DoubleArray;
public class KNNPoint implements Comparable<KNNPoint>{
	public DoubleArray values;  // a vector
	public double distance; // a distance
	public KNNPoint(DoubleArray values, double distance){
		this.values = values;
		this.distance = distance;
	}
	public int compareTo(KNNPoint point2) {
		return Double.compare(this.distance, point2.distance);
	}
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("(");
		for(Double element : values.data ){
			buffer.append(" " + element);
		}
		buffer.append(")");
		return buffer.toString();
	}
	public boolean equals(Object point2){
		if(point2 == null){
			return false;
		}
		KNNPoint o2 = (KNNPoint)point2;
		for(int i=0; i < values.size(); i++ ){
			if(o2.values.data[i] != values.data[i]){
				return false;
			}
		}
		return true;
	}
}

