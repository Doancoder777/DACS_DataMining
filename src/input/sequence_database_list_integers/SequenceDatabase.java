package input.sequence_database_list_integers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class SequenceDatabase {
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	public void loadFile(String path) throws IOException {
		String thisLine; // variable to read each line.
		try (BufferedReader myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))))) {
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == false && thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%'
						&& thisLine.charAt(0) != '@') {
					addSequence(thisLine.split(" "));
				}
			}
		}
	}
	void addSequence(String[] tokens) {
		Sequence sequence = new Sequence(sequences.size());
		List<Integer> itemset = new ArrayList<Integer>();
		for (String token : tokens) {
			if (token.codePointAt(0) == '<') {
			}
			else if (token.equals("-1")) {
				sequence.addItemset(itemset);
				itemset = new ArrayList<Integer>();
			}
			else if (token.equals("-2")) {
				sequences.add(sequence);
			} else {
				itemset.add(Integer.parseInt(token));
			}
		}
	}
	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}
	public void print() {
		System.out.println("============  SEQUENCE DATABASE ==========");
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
		for (Sequence sequence : sequences) {
			size += sequence.size();
		}
		double meansize = ((float) size) / ((float) sequences.size());
		System.out.println("mean size" + meansize);
	}
	public String toString() {
		StringBuilder r = new StringBuilder();
		for (Sequence sequence : sequences) {
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

