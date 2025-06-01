package input.sequence_database_array_integers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SequenceDatabase {
	public int minItem = Integer.MAX_VALUE;
	public int maxItem = 0;
	public int tidsCount = 0;
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	Map<Integer, String> mapItemIDtoStringValue = null;
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line.
		try (BufferedReader myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))))) {
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.startsWith("@ITEM")) {
					thisLine = thisLine.substring(6);
					int index = thisLine.indexOf("=");
					int itemID = Integer.parseInt(thisLine.substring(0, index));
					String stringValue = thisLine.substring(index + 1);
					if (mapItemIDtoStringValue == null) {
						mapItemIDtoStringValue = new HashMap<Integer, String>();
					}
					mapItemIDtoStringValue.put(itemID, stringValue);
				} else if (thisLine.isEmpty() == false && thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					addSequence(thisLine.split(" "));
				}
			}
		}
	}
	public void addSequence(String[] tokens) {
		Sequence sequence = new Sequence();
		List<Integer> itemset = new ArrayList<Integer>();
		for (String token : tokens) {
			if (token.codePointAt(0) == '<') {
			}
			else if (token.equals("-1")) {
				sequence.addItemset(itemset.toArray());
				itemset = new ArrayList<Integer>();
			}
			else if (token.equals("-2")) {
				sequences.add(sequence);
			} else {
				Integer item = Integer.parseInt(token);
				if (item >= maxItem) {
					maxItem = item;
				}
				if (item < minItem) {
					minItem = item;
				}
				itemset.add(item);
			}
		}
	}
	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}
	public void print() {
		System.out.println("============  CONTEXTE ==========");
		for (int i = 0; i < sequences.size(); i++) { // pour chaque objet
			System.out.print(i + ":  ");
			sequences.get(i).print();
			System.out.println("");
		}
	}
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		System.out.println("Min item:" + minItem);
		System.out.println("Max item:" + maxItem);
		long size = 0;
		for (Sequence sequence : sequences) {
			size += sequence.size();
		}
		double meansize = ((float) size) / ((float) sequences.size());
		System.out.println("mean size" + meansize);
	}
	public String toString() {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < sequences.size(); i++) {
			r.append(i);
			r.append(":  ");
			r.append(sequences.get(i).toString());
			r.append('\n');
		}
		return r.toString();
	}
	public int size() {
		return sequences.size();
	}
	public List<Sequence> getSequences() {
		return sequences;
	}
	public String getNameForItem(Integer item) {
		if (mapItemIDtoStringValue == null) {
			return null;
		}
		String name = mapItemIDtoStringValue.get(item);
		if (name == null) {
			return null;
		}
		return name;
	}
	public Map<Integer, String> getMapItemToStringValues() {
		return mapItemIDtoStringValue;
	}
	public int getMaxItemID() {
		return maxItem;
	}
}

