package Algorithrms.Rarepartem.itr;

public class AssociationRuleITRare {
	// support of the rule
	public int support; 
	// confidence of the rule
	public double confidence;
	// the antecedent of the rule
	public int[] itemset1;
	// the consequent of the rule
	public int[] itemset2;
	// ADDED: type of rule for rare pattern mining
	public String ruleType;
	
	/**
	 * MODIFIED: Get a string representation of this rule with rule type
	 */
	public String toString(){
		// create a StringBuilder
		StringBuilder buffer = new StringBuilder();
		// append  items from the antecedent
		buffer.append("[ ");
		if(itemset1 != null) {
			for(Integer item : itemset1){
				buffer.append(item);
				buffer.append(" ");
			}
		}
		// arrow
		buffer.append(" ] ==> [");
		// append items from the consequent
		if(itemset2 != null) {
			for(Integer item : itemset2){
				buffer.append(item);
				buffer.append(" ");
			}
		}
		// append the support and confidence
		buffer.append(" ]  #SUP: ");
		buffer.append(support);
		buffer.append("  #CONF:");
		buffer.append(String.format("%.4f", confidence));
		
		// ADDED: append rule type for rare pattern analysis
		if(ruleType != null) {
			buffer.append("  #TYPE:");
			buffer.append(ruleType);
		}
		
		buffer.append("\n");
		// return the string
		return buffer.toString();
	}
	
}