package Algo.two_phase;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class ItemsetsTP {
	private final List<List<ItemsetTP>> levels = new ArrayList<List<ItemsetTP>>(); 
	private int itemsetsCount = 0;
	private String name;
	public ItemsetsTP(String name) {
		this.name = name;
		levels.add(new ArrayList<ItemsetTP>()); 
	}
	public void printItemsets(int transactionCount) {
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		for (List<ItemsetTP> level : levels) {
			System.out.println("  L" + levelCount + " ");
			for (ItemsetTP itemset : level) {
				System.out.print("  pattern " + patternCount + "  ");
				itemset.print();
				System.out.print(" #SUP: "
						+ itemset.getAbsoluteSupport());
				System.out.print(" #UTIL: " + itemset.getUtility());
				patternCount++;
				System.out.println("");
			}
			levelCount++; // next level
		}
		System.out.println(" --------------------------------");
	}
	public void saveResultsToFile(String output, int transactionCount) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for (List<ItemsetTP> level : levels) {
			for (ItemsetTP itemset : level) {
				writer.write(itemset.toString());
				writer.write(" #SUP: "
						+ itemset.getRelativeSupport(transactionCount));
				writer.write(" #UTIL: " + itemset.getUtility());
				writer.newLine();
			}
		}
		writer.close();
	}	
	public void addItemset(ItemsetTP itemset, int k) {
		while (levels.size() <= k) {
			levels.add(new ArrayList<ItemsetTP>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}
	public List<List<ItemsetTP>> getLevels() {
		return levels;
	}
	public int getItemsetsCount() {
		return itemsetsCount;
	}
	public void decreaseCount() {
		itemsetsCount--;
	}
}

