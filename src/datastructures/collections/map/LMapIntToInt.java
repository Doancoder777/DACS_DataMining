package datastructures.collections.map;
import java.util.Arrays;
import java.util.NoSuchElementException;
public class LMapIntToInt extends MapIntToInt {
	private Entry[] buckets;
	private int elementCount;
	private final static int DEFAULT_BUCKET_COUNT = 100;
	private static double DEFAULT_MAXIMUM_LOAD_FACTOR = 0.80d;
	private final double maximum_load_factor;
	private boolean rehashingEnabled = true;
	public class Entry extends MapEntryIntToInt {
		private int key;
		private int value;
		Entry next;
		public Entry(int key, int value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public int getKey() {
			return key;
		}
		@Override
		public int getValue() {
			return value;
		}
	}
	public LMapIntToInt() {
		buckets = new Entry[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LMapIntToInt(int initialCapacity) {
		elementCount = 0;
		buckets = new Entry[initialCapacity];
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LMapIntToInt(int initialCapacity, double maximum_load) {
		elementCount = 0;
		buckets = new Entry[initialCapacity];
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
	public boolean containsKey(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		if (buckets[initialIndex].key == key) {
			return true;
		}
		Entry next = buckets[initialIndex].next;
		while (next != null) {
			if (next.key == key) {
				return true;
			}
			next = next.next;
		}
		return false;
	}
	public int get(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return -1;
		}
		if (buckets[initialIndex].key == key) {
			return buckets[initialIndex].value;
		}
		Entry next = buckets[initialIndex].next;
		while (next != null) {
			if (next.key == key) {
				return next.value;
			}
			next = next.next;
		}
		return -1;
	}
	@Override
	public void getAndIncreaseValueBy(int key, int valueToAdd) {
		if(rehashingEnabled) {
			resizeAndPerformRehashingIfNeeded();
		}
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new Entry(key, valueToAdd);
			elementCount++;
			return;
		}
		if (buckets[initialIndex].key == key) {
			buckets[initialIndex].value += valueToAdd;
			return;
		}
		Entry current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key == key) {
				current.next.value += valueToAdd;
				return;
			}
			current = current.next;
		}
		current.next = new Entry(key, valueToAdd);
		elementCount++;
	}
	public void put(int key, int value) {
		if(rehashingEnabled) {
			resizeAndPerformRehashingIfNeeded();
		}
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new Entry(key, value);
			elementCount++;
			return;
		}
		if (buckets[initialIndex].key == key) {
			buckets[initialIndex].value = value;
			return;
		}
		Entry current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key == key) {
				current.next.value = value;
				return;
			}
			current = current.next;
		}
		current.next = new Entry(key, value);
		elementCount++;
		return;
	}
	private void resizeAndPerformRehashingIfNeeded() {
		double loadFactor = (1.0d * elementCount) / buckets.length;
		if (loadFactor >= maximum_load_factor) {
			Entry[] previousBuckets = buckets;
			int newSize = previousBuckets.length * 2;
			buckets = new Entry[newSize];
			Arrays.fill(buckets, null);
			for (int i = 0; i < previousBuckets.length; i++) {
				Entry currentEntry = previousBuckets[i];
				while (currentEntry != null) {
					Entry nextOne = currentEntry.next;
					reInsertEntry(currentEntry);
					currentEntry = nextOne;
				}
			}
		}
	}
	private void reInsertEntry(Entry reinsertedEntry) {
		reinsertedEntry.next = null;
		int initialIndex = hash(reinsertedEntry.key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = reinsertedEntry;
			return;
		}
		Entry current = buckets[initialIndex];
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
		Entry current = buckets[initialIndex];
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
		return new LEntryIterator();
	}
	public class LEntryIterator extends EntryIterator {
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private Entry previousEntry = null;
		private Entry currentEntry = null;
		private Entry nextEntry = null;
		public LEntryIterator() {
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
		public Entry next() {
			if (nextEntry == null) {
				throw new NoSuchElementException();
			}
			previousEntry = currentEntry;
			currentEntry = nextEntry;
			bucketIndexCurrentEntry = bucketIndexNextEntry;
			if (currentEntry.next != null) {
				nextEntry = currentEntry.next;
				return currentEntry;
			}
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null) {
					nextEntry = buckets[bucketIndexNextEntry];
					return currentEntry;
				}
			}
			nextEntry = null;
			return currentEntry;
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
	@Override
	public KeyIterator iteratorForKeys() {
		return new LKeyIterator();
	}
	public class LKeyIterator extends KeyIterator {
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private Entry previousEntry = null;
		private Entry currentEntry = null;
		private Entry nextEntry = null;
		public LKeyIterator() {
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
	@Override
	public LValueIterator iteratorForValues() {
		return new LValueIterator();
	}
	public class LValueIterator extends ValueIterator {
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private Entry previousEntry = null;
		private Entry currentEntry = null;
		private Entry nextEntry = null;
		public LValueIterator() {
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
				return currentEntry.value;
			}
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null) {
					nextEntry = buckets[bucketIndexNextEntry];
					return currentEntry.value;
				}
			}
			nextEntry = null;
			return currentEntry.value;
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
