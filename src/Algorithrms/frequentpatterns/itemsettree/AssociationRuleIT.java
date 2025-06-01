package Algorithrms.frequentpatterns.itemsettree;

public class AssociationRuleIT {
	// support of the rule
	public int support; 
	// confidence of the rule
	public double confidence;
	// the antecedent of the rule
	public int[] itemset1;
	// the consequent of the rule
	public int[] itemset2;
	
	/**
	 * Get a string representation of this rule
	 */
	public String toString(){
		// create a StringBuilder
		StringBuilder buffer = new StringBuilder();
		// append  items from the antecedent
		buffer.append("[ ");
		for(Integer item : itemset1){
			buffer.append(item);
			buffer.append(" ");
		}
		// arrow
		buffer.append(" ] ==> [");
		// append items from the consequent
		for(Integer item : itemset2){
			buffer.append(item);
			buffer.append(" ");
		}
		// append the support and confidence
		buffer.append(" ]  #SUP: ");
		buffer.append(support);
		buffer.append("  #CONF:");
		buffer.append(confidence);
		buffer.append("\n");
		// return the string
		return buffer.toString();
	}
	
}
