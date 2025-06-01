package datastructures.collections.set;
import java.util.Arrays;
import java.util.NoSuchElementException;
public class LHashSetObject<T> extends SetObject<T> {
	private SetEntry<T>[] buckets;
	private int elementCount;
	private int DEFAULT_BUCKET_COUNT = 100;
	private static double DEFAULT_MAXIMUM_LOAD_FACTOR = 0.80d;
	private final double maximum_load_factor;
	private boolean rehashingEnabled = true;
	private class SetEntry<S> {
		S key;
		SetEntry<S> next;
		public SetEntry(S key) {
			this.key = key;
		}
	}
	@SuppressWarnings("unchecked")
	public LHashSetObject() {
		buckets = new SetEntry[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	@SuppressWarnings("unchecked")
	public LHashSetObject(int initialCapacity) {
		elementCount = 0;
		buckets = new SetEntry[initialCapacity];
		maximum_load_factor = DEFAULT_MAXIMUM_LOAD_FACTOR;
	}
	@SuppressWarnings("unchecked")
	public LHashSetObject(int initialCapacity, double maximum_load) {
		elementCount = 0;
		buckets = new SetEntry[initialCapacity];
		maximum_load_factor = maximum_load;
	}
	public LHashSetObject(SetObject<T> set) {
		this(set.size());
		AEntryIterator x = set.iterator();
		while(x.hasNext()) {
			this.add(x.next());
		}
	}
	public void addAll(SetObject<T> set) {
		AEntryIterator x = set.iterator();
		while(x.hasNext()) {
			this.add(x.next());
		}
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
	protected int hash(T key) {
		return Math.abs(key.hashCode()) % buckets.length;
	}
	public boolean contains(T key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		if (buckets[initialIndex].key.equals(key)) {
			return true;
		}
		SetEntry<T> next = buckets[initialIndex].next;
		while (next != null) {
			if (next.key.equals(key)) {
				return true;
			}
			next = next.next;
		}
		return false;
	}
	public void add(T key) {
		if(rehashingEnabled) {
			resizeAndPerformRehashingIfNeeded();
		}
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new SetEntry<T>(key);
			elementCount++;
			return;
		}
		if (buckets[initialIndex].key.equals(key)) {
			return;
		}
		SetEntry<T> current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key.equals(key)) {
				return;
			}
			current = current.next;
		}
		current.next = new SetEntry<T>(key);
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
	private void reInsertEntry(SetEntry<T> reinsertedEntry) {
		reinsertedEntry.next = null;
		int initialIndex = hash(reinsertedEntry.key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = reinsertedEntry;
			return;
		}
		SetEntry<T> current = buckets[initialIndex];
		while (current.next != null) {
			current = current.next;
		}
		current.next = reinsertedEntry;
	}
	public boolean remove(T key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		if (buckets[initialIndex].key.equals(key)) {
			buckets[initialIndex] = buckets[initialIndex].next;
			elementCount--;
			return true;
		}
		SetEntry<T> current = buckets[initialIndex];
		while (current.next != null) {
			if (current.next.key.equals(key)) {
				current.next = current.next.next;
				elementCount--;
				return true;
			}
			current = current.next;
		}
		return false;
	}
	public AEntryIterator iterator() {
		return new LEntryIterator();
	}
	public class LEntryIterator extends AEntryIterator {
		private int bucketIndexCurrentEntry = 0;
		private int bucketIndexNextEntry = 0;
		private SetEntry<T> previousEntry = null;
		private SetEntry<T> currentEntry = null;
		private SetEntry<T> nextEntry = null;
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
		public T next() {
			if (nextEntry == null) {
				throw new NoSuchElementException();
			}
			previousEntry = currentEntry;
			currentEntry = nextEntry;
			bucketIndexCurrentEntry = bucketIndexNextEntry;
			if (currentEntry.next != null) {
				nextEntry = currentEntry.next;
				return (T) currentEntry.key;
			}
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null) {
					nextEntry = buckets[bucketIndexNextEntry];
					return (T) currentEntry.key;
				}
			}
			nextEntry = null;
			return (T) currentEntry.key;
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
