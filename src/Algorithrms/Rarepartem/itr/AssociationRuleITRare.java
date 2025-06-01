package Algorithrms.Rarepartem.itr;
public class AssociationRuleITRare {
	public int support; 
	public double confidence;
	public int[] itemset1;
	public int[] itemset2;
	public String ruleType;
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ ");
		if(itemset1 != null) {
			for(Integer item : itemset1){
				buffer.append(item);
				buffer.append(" ");
			}
		}
		buffer.append(" ] ==> [");
		if(itemset2 != null) {
			for(Integer item : itemset2){
				buffer.append(item);
				buffer.append(" ");
			}
		}
		buffer.append(" ]  #SUP: ");
		buffer.append(support);
		buffer.append("  #CONF:");
		buffer.append(String.format("%.4f", confidence));
		if(ruleType != null) {
			buffer.append("  #TYPE:");
			buffer.append(ruleType);
		}
		buffer.append("\n");
		return buffer.toString();
	}
}
