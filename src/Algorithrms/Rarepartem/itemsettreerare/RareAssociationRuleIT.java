package Algorithrms.Rarepartem.itemsettreerare;

public class RareAssociationRuleIT {
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
		buffer.append(String.format("%.4f", confidence));
		buffer.append(" (RARE RULE)");
		buffer.append("\n");
		return buffer.toString();
	}
}