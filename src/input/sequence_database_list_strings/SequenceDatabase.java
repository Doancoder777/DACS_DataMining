package input.sequence_database_list_strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class SequenceDatabase {
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line.
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == false &&
						thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					addSequence(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	void addSequence(String[] tokens) { 
		Sequence sequence = new Sequence(sequences.size());
		List<String> itemset = new ArrayList<String>();
		for (String item : tokens) {
			if (item.codePointAt(0) == '<') { 
			} 
			else if (item.equals("-1")) { 
				Collections.sort(itemset, new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.hashCode() - arg1.hashCode();
					}
				});
				sequence.addItemset(itemset);
				itemset = new ArrayList<String>();
			} 
			else if (item.equals("-2")) { 
				sequences.add(sequence);
			} 
			else { 
				itemset.add(item);	
			}
		}
	}
	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}
	public void printDatabase() {
		System.out.println("============  Database ==========");
		for (Sequence sequence : sequences) { // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("Average sequence size : " + meansize);
	}
	public String toString() {
		StringBuilder r = new StringBuilder();
		for (Sequence sequence : sequences) { // pour chaque objet
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
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
	public Set<Integer> getSequenceIDs() {
		Set<Integer> set = new HashSet<Integer>();
		for (Sequence sequence : getSequences()) {
			set.add(sequence.getId()); // add the id to the set.
		}
		return set; // return the set.
	}
}

