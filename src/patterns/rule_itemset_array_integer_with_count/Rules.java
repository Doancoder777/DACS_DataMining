package patterns.rule_itemset_array_integer_with_count;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import patterns.itemset_array_integers_with_count.Itemset;
public class Rules {
	private final List<Rule> rules = new ArrayList<Rule>();  // rules
	private final String name;
	public void sortByConfidence(){
		Collections.sort(rules, new Comparator<Rule>() {
			public int compare(Rule r1, Rule r2) {
				return (int)((r2.getConfidence() - r1.getConfidence() ) * Integer.MAX_VALUE);
			}
		});
	}
	public Rules(String name){
		this.name = name;
	}
	public void printRules(int databaseSize){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(Rule rule : rules){
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  " + rule.getRelativeSupport(databaseSize) +
					" (" + rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	public String toString(int databaseSize){
		StringBuilder buffer = new StringBuilder(" ------- ");
		buffer.append(name);
		buffer.append(" -------\n");
		int i=0;
		for(Rule rule : rules){
			buffer.append("   rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			buffer.append("support :  ");
			buffer.append(rule.getRelativeSupport(databaseSize));
			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(databaseSize);
			buffer.append(") ");
			buffer.append("confidence :  " );
			buffer.append(rule.getConfidence());
			buffer.append("\n");
			i++;
		}
		return buffer.toString(); // return the string
	}
	public void addRule(Rule rule){
		rules.add(rule);
	}
	public int getRulesCount(){
		return rules.size();
	}
	public List<Rule> getRules() {
		return rules;
	}
}

