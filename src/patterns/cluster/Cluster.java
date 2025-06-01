package patterns.cluster;
import java.util.ArrayList;
import java.util.List;
import clustering.dbscan.AlgoDBSCAN;
public class Cluster {
	protected List<DoubleArray> vectors = new ArrayList<DoubleArray>();
	public Cluster() {
	}
	public void addVector(DoubleArray vector) {
		vectors.add(vector);
	}
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if(vectors.size() >=1){
			for(DoubleArray vector : vectors){
				buffer.append("[");
				buffer.append(vector.toString());
				buffer.append("]");
			}
		}
		return buffer.toString();
	}
	public List<DoubleArray> getVectors() {
		return vectors;
	}
	public void remove(DoubleArray vector) {
		vectors.remove(vector);		
	}
	public boolean contains(DoubleArray vector) {
		return vectors.contains(vector);
	}
}
