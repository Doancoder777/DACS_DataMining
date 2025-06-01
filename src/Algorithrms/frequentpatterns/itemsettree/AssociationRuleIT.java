package Algorithrms.frequentpatterns.itemsettree;
public class AssociationRuleIT {
	public int support; 
	public double confidence;
	public int[] itemset1;
	public int[] itemset2;
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ ");
		for(Integer item : itemset1){
			buffer.append(item);
			buffer.append(" ");
		}
		buffer.append(" ] ==> [");
		for(Integer item : itemset2){
			buffer.append(item);
			buffer.append(" ");
		}
		buffer.append(" ]  #SUP: ");
		buffer.append(support);
		buffer.append("  #CONF:");
		buffer.append(confidence);
		buffer.append("\n");
		return buffer.toString();
	}
}

