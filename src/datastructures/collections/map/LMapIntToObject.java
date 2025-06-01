package datastructures.collections.map;
import java.util.Arrays;
import java.util.NoSuchElementException;
public class LMapIntToObject<T> extends MapIntToObject<T> {
	private Entry<T>[] buckets;
	private int elementCount;
	private final static int DEFAULT_BUCKET_COUNT = 100;
	private static double DEFAULT_MAXIMUM_LOAD_FACTOR = 0.80d;
	private final double maximum_load_factor;
	private boolean rehashingEnabled = true;
	public class Entry<S> extends MapEntryIntToObject<S> {
		public int key;
		public S value;
		Entry<S> next;
		public Entry(int key, S value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public int getKey() {
			return key;
		}
		@Override
		public S getValue() {
			return value;
		}
	}
	public LMapIntToObject() {
		buckets = new Entry[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LMapIntToObject(int initialCapacity) {
		elementCount = 0;
		buckets = new Entry[initialCapacity];
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	public LMapIntToObject(int initialCapacity, double maximum_load) {
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
		Entry<T> next = buckets[initialIndex].next;
		while (next != null) {
			if (next.key == key) {
				return true;
			}
			next = next.next;
		}
		return false;
	}
	public T get(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return null;
		}
		if (buckets[initialIndex].key == key) {
			return buckets[initialIndex].value;
		}
		Entry<T> next = (Entry<T>) buckets[initialIndex].next;
		while (next != null) {
			if (next.key == key) {
				return next.value;
			}
			next = next.next;
		}
		return null;
	}
	public void put(int key, T value) {
		if(rehashingEnabled) {
			resizeAndPerformRehashingIfNeeded();
		}
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new Entry<T>(key, value);
			elementCount++;
			return;
		}
		if (buckets[initialIndex].key == key) {
			buckets[initialIndex].value = value;
			return;
		}
		Entry<T> current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key == key) {
				current.next.value = value;
				return;
			}
			current = current.next;
		}
		current.next = new Entry<T>(key, value);
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
		Entry<T> current = buckets[initialIndex];
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
	public class LEntryIterator extends EntryIterator{
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private Entry<T> previousEntry = null;
		private Entry<T> currentEntry = null;
		private Entry<T> nextEntry = null;
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
		public Entry<T> next() {
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
		private Entry<T> previousEntry = null;
		private Entry<T> currentEntry = null;
		private Entry<T> nextEntry = null;
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
		private Entry<T> previousEntry = null;
		private Entry<T> currentEntry = null;
		private Entry<T> nextEntry = null;
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
		public T next() {
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
