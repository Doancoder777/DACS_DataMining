package patterns.rule_itemset_array_integer_with_count;
import patterns.itemset_array_integers_with_count.Itemset;
public class Rule {
	private int[] itemset1; 
	private int[] itemset2;
	private int coverage;
	private int transactionCount; 
	private double confidence; 
	public Rule(int[] itemset1, int[] itemset2, int coverage,
			int transactionCount, double confidence) {
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.coverage = coverage;
		this.transactionCount = transactionCount;
		this.confidence = confidence;
	}
	public double getRelativeSupport(int databaseSize) {
		return ((double) transactionCount) / ((double) databaseSize);
	}
	public int getAbsoluteSupport() {
		return transactionCount;
	}
	public double getConfidence() {
		return confidence;
	}
	public int getCoverage() {
		return coverage;
	}
	public void print() {
		System.out.println(toString());
	}
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < itemset1.length; i++) {
			buffer.append(itemset1[i]);
			if (i != itemset1.length - 1) {
				buffer.append(" ");
			}
		}
		buffer.append(" ==> ");
		for (int i = 0; i < itemset2.length; i++) {
			buffer.append(itemset2[i]);
			buffer.append(" ");
		}
		return buffer.toString();
	}
	public int[] getItemset1() {
		return itemset1;
	}
	public int[] getItemset2() {
		return itemset2;
	}
}

