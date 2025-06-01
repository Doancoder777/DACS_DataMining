package datastructures.collections.set;
import java.util.Arrays;
import java.util.NoSuchElementException;
public class LHashSetInt extends SetInt {
	private SetEntry[] buckets;
	private int elementCount;
	private int DEFAULT_BUCKET_COUNT = 100;
	private static double DEFAULT_MAXIMUM_LOAD_FACTOR = 0.80d;
	private final double maximum_load_factor;
	private boolean rehashingEnabled = true;
	private class SetEntry {
		int key;
		SetEntry next;
		public SetEntry(int key) {
			this.key = key;
		}
	}
	public LHashSetInt() {
		buckets = new SetEntry[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LHashSetInt(int initialCapacity) {
		elementCount = 0;
		buckets = new SetEntry[initialCapacity];
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LHashSetInt(SetInt set) {
		this(set.size());
		EntryIterator x = set.iterator();
		while (x.hasNext()) {
			this.add(x.next());
		}
	}
	public void addAll(SetInt set) {
		EntryIterator x = set.iterator();
		while (x.hasNext()) {
			this.add(x.next());
		}
	}
	public LHashSetInt(int initialCapacity, double maximum_load) {
		elementCount = 0;
		buckets = new SetEntry[initialCapacity];
		maximum_load_factor = maximum_load;
	}
	public void setRehashingEnabled(boolean value) {
		this.rehashingEnabled = value;
	}
	public void clear() {
		Arrays.fill(buckets, null);
		elementCount = 0;
	}
	public int size() {
		return elementCount;
	}
	public boolean isEmpty() {
		return elementCount == 0;
	}
	protected int hash(int key) {
		return key % buckets.length;
	}
	public boolean contains(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		if (buckets[initialIndex].key == key) {
			return true;
		}
		SetEntry next = buckets[initialIndex].next;
		while (next != null) {
			if (next.key == key) {
				return true;
			}
			next = next.next;
		}
		return false;
	}
	public void add(int key) {
		if (rehashingEnabled) {
			resizeAndPerformRehashingIfNeeded();
		}
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new SetEntry(key);
			elementCount++;
			return;
		}
		if (buckets[initialIndex].key == key) {
			return;
		}
		SetEntry current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key == key) {
				return;
			}
			current = current.next;
		}
		current.next = new SetEntry(key);
		elementCount++;
		return;
	}
	private void resizeAndPerformRehashingIfNeeded() {
		double loadFactor = (1.0d * elementCount) / buckets.length;
		if (loadFactor >= maximum_load_factor) {
			SetEntry[] previousBuckets = buckets;
			int newSize = previousBuckets.length * 2;
			buckets = new SetEntry[newSize];
			Arrays.fill(buckets, null);
			for (int i = 0; i < previousBuckets.length; i++) {
				SetEntry currentEntry = previousBuckets[i];
				while (currentEntry != null) {
					SetEntry nextOne = currentEntry.next;
					reInsertEntry(currentEntry);
					currentEntry = nextOne;
				}
			}
		}
	}
	private void reInsertEntry(SetEntry reinsertedEntry) {
		reinsertedEntry.next = null;
		int initialIndex = hash(reinsertedEntry.key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = reinsertedEntry;
			return;
		}
		SetEntry current = buckets[initialIndex];
		while (current.next != null) {
			current = current.next;
		}
		current.next = reinsertedEntry;
	}
	public boolean remove(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		if (buckets[initialIndex].key == key) {
			buckets[initialIndex] = buckets[initialIndex].next;
			elementCount--;
			return true;
		}
		SetEntry current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key == key) {
				current.next = current.next.next;
				elementCount--;
				return true;
			}
			current = current.next;
		}
		return false;
	}
	public EntryIterator iterator() {
		return new LHashSetIntIterator();
	}
	public class LHashSetIntIterator extends EntryIterator {
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private SetEntry previousEntry = null;
		private SetEntry currentEntry = null;
		private SetEntry nextEntry = null;
		public LHashSetIntIterator() {
			if (elementCount == 0) {
				return;
			}
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null) {
					nextEntry = buckets[bucketIndexNextEntry];
					return;
				}
			}
		}
		public int next() {
			if (nextEntry == null) {
				throw new NoSuchElementException();
			}
			previousEntry = currentEntry;
			currentEntry = nextEntry;
			bucketIndexCurrentEntry = bucketIndexNextEntry;
			if (currentEntry.next != null) {
				nextEntry = currentEntry.next;
				return currentEntry.key;
			}
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null) {
					nextEntry = buckets[bucketIndexNextEntry];
					return currentEntry.key;
				}
			}
			nextEntry = null;
			return currentEntry.key;
		}
		public boolean hasNext() {
			return nextEntry != null;
		}
		public void remove() {
			if (currentEntry == null) {
				throw new IllegalStateException();
			}
			if (buckets[bucketIndexCurrentEntry] == currentEntry) {
				buckets[bucketIndexCurrentEntry] = currentEntry.next;
				currentEntry = previousEntry;
				bucketIndexCurrentEntry = -1;
			}
			else if (previousEntry != null && previousEntry.next == currentEntry) {
				previousEntry.next = currentEntry.next;
				currentEntry = previousEntry;
			}
			elementCount--;
		}
	}
}
