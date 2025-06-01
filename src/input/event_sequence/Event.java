package input.event_sequence;
public class Event {
	private int item;
	private long timestamp;
	public Event(int item, long timestamp) {
		this.item = item;
		this.timestamp = timestamp;
	}
	public int getItem() {
		return item;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String toString() {
		return item + "|" + timestamp;
	}
}

