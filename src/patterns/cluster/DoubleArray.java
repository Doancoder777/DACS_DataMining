package patterns.cluster;
public class DoubleArray {
	public double[] data;
	public DoubleArray(double [] data){
		this.data = data;
	}
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<data.length; i++){
			buffer.append(data[i]);
			if(i < data.length -1){
				buffer.append(",");
			}
		}
		return buffer.toString();
	}
	public DoubleArray clone(){
		return new DoubleArray(data.clone());
	}
	public int size() {
		return data.length;
	}
	public double get(int index) {
		return data[index];
	}
}

