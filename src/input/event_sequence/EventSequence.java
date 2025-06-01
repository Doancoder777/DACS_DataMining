package input.event_sequence;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class EventSequence {
	private List<Event> events;
	private Set<Integer> uniqueEvents;
	private long minTimestamp;
	private long maxTimestamp;
	private int maxItemID;
	Map<Integer, String> mapItemIDtoStringValue = null;
	public EventSequence() {
		this.events = new ArrayList<Event>();
		this.uniqueEvents = new HashSet<Integer>();
		this.minTimestamp = Long.MAX_VALUE;
		this.maxTimestamp = Long.MIN_VALUE;
	}
	public void loadFile(String filepath) throws IOException {
		maxItemID = 0;
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		String line = reader.readLine();
		while (line != null) {
			if (line.isEmpty() == false) {
				if (line.startsWith("@ITEM")) {
					line = line.substring(6);
					int index = line.indexOf("=");
					int itemID = Integer.parseInt(line.substring(0, index));
					String stringValue = line.substring(index + 1);
					if (mapItemIDtoStringValue == null) {
						mapItemIDtoStringValue = new HashMap<Integer, String>();
					}
					mapItemIDtoStringValue.put(itemID, stringValue);
					if(itemID > maxItemID) {
						maxItemID = itemID;
					}
				} else if (line.isEmpty() == false && line.charAt(0) != '#' && line.charAt(0) != '%'
						&& line.charAt(0) != '@') {
					String[] split = line.split("\\|");
					String eventSet = split[0];
					long timestamp = split.length > 1 ? Long.parseLong(split[1]) : events.size();
					String[] items = eventSet.split(" ");
					for (String item : items) {
						int event = Integer.parseInt(item);
						if(event > maxItemID) {
							maxItemID = event;
						}
						Event e = new Event(event, timestamp);
						events.add(e);
						uniqueEvents.add(event);
						if (timestamp < minTimestamp) {
							minTimestamp = timestamp;
						}
						if (timestamp > maxTimestamp) {
							maxTimestamp = timestamp;
						}
					}
				}
			}
			line = reader.readLine();
		}
		reader.close();
	}
	public int size() {
		return events.size();
	}
	public Event get(int index) {
		return events.get(index);
	}
	public Set<Integer> getUniqueEvents() {
		return uniqueEvents;
	}
	public long getMinTimestamp() {
		return minTimestamp;
	}
	public long getMaxTimestamp() {
		return maxTimestamp;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Event e : events) {
			sb.append(e).append(" ");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
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
		return maxItemID;
	}
}

