package input.arff;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
public class ARFFDatabase {
	private List<List<String>> records;
	private List<String> attributeNames;
	public ARFFDatabase() {
		records = new ArrayList<List<String>>();
		attributeNames = new ArrayList<String>();
	}
	public void loadFile(String filepath) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		String line = null;
		boolean inData = false;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("%")) {
				continue;
			}
			if (line.startsWith("@RELATION") || line.startsWith("@relation")) {
				continue;
			}
			if (line.startsWith("@ATTRIBUTE") || line.startsWith("@attribute")) {
				String[] parts = line.split("\\s+");
				String name = parts[1];
				if (name.startsWith("'") && name.endsWith("'")) {
					name = name.substring(1, name.length() - 1);
				}
				attributeNames.add(name);
			}
			if (line.startsWith("@DATA") || line.startsWith("@data")) {
				inData = true;
				continue;
			}
			if (inData) {
				String[] values = line.split(",");
				List<String> record = new ArrayList<String>();
				for (String value : values) {
					value = value.trim();
					if (value.startsWith("'") && value.endsWith("'")) {
						value = value.substring(1, value.length() - 1);
					}
					record.add(value);
				}
				records.add(record);
			}
		}
		reader.close();
	}
	public List<List<String>> getRecords() {
		return records;
	}
	public List<String> getAttributeNames() {
		return attributeNames;
	}
	public int size() {
		return records.size();
	}
}

