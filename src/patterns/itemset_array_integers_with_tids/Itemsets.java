package patterns.itemset_array_integers_with_tids;
import java.util.ArrayList;
import java.util.List;
public class Itemsets {
	private final List<List<Itemset>> levels = new ArrayList<List<Itemset>>(); 
	private int itemsetsCount = 0;
	private final String name;
	public Itemsets(String name) {
		this.name = name;
		levels.add(new ArrayList<Itemset>()); // We create an empty level 0 by
	}
	public void printItemsets(int nbObject) {
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		for (List<Itemset> level : levels) {
			System.out.println("  L" + levelCount + " ");
			for (Itemset itemset : level) {
				System.out.print("  pattern " + patternCount + ":  ");
				itemset.print();
				System.out.print("support :  "
						+ itemset.getRelativeSupportAsString(nbObject));
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}
	public void addItemset(Itemset itemset, int k) {
		while (levels.size() <= k) {
			levels.add(new ArrayList<Itemset>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}
	public List<List<Itemset>> getLevels() {
		return levels;
	}
	public int getItemsetsCount() {
		return itemsetsCount;
	}
}

