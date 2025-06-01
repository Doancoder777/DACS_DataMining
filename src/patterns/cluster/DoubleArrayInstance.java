package patterns.cluster;
public class DoubleArrayInstance extends DoubleArray{
	private String name = "";
	public String getName() {
		return name;
	}
	public DoubleArrayInstance(double[] values, String name){
		super(values);
		this.name = name;
	}
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(" ");
		for(int i=0; i < this.data.length; i++){
			builder.append(this.data[i]);
			if(i != this.data.length-1){
				builder.append(" ");
			}
		}
		return  builder.toString();
	}
}

