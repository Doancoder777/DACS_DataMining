package datastructures.collections.set;
import java.util.Arrays;
import java.util.NoSuchElementException;
import datastructures.collections.list.ArrayListInt;
public class AHashSetInt extends SetInt{
	private ArrayListInt[] buckets;
	private int elementCount;
	private final int DEFAULT_BUCKET_COUNT = 100;
	private int initialCollisionListSize = 10;
	public AHashSetInt() {
		buckets = new ArrayListInt[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
	}
	public AHashSetInt(int initialCapacity) {
		elementCount = 0;
		buckets = new ArrayListInt[initialCapacity];
	}
	public AHashSetInt(SetInt set) {
		this(set.size());
		EntryIterator x = set.iterator();
		while(x.hasNext()) {
			this.add(x.next());
		}
	}
	public void addAll(SetInt set) {
		EntryIterator x = set.iterator();
		while(x.hasNext()) {
			this.add(x.next());
		}
	}
	public AHashSetInt(int initialCapacity, int initialCollisionListSize) {
		elementCount = 0;
		buckets = new ArrayListInt[initialCapacity];
		this.initialCollisionListSize = initialCollisionListSize;
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
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			int bucketKey = buckets[initialIndex].get(i);
			if (bucketKey == key) {
				return true;
			}
		}
		return false;
	}
	public void add(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new ArrayListInt(initialCollisionListSize);
			buckets[initialIndex].add(key);
			elementCount++;
			return;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			int bucketKey = buckets[initialIndex].get(i);
			if (bucketKey == key) {
				return;
			}
		}
		buckets[initialIndex].add(key);
		elementCount++;
		return;
	}
	public boolean remove(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			int bucketKey = buckets[initialIndex].get(i);
			if (bucketKey == key) {
				buckets[initialIndex].removeAt(i);
				elementCount--;
				return true;
			}
		}
		return false;
	}
	public EntryIterator iterator() {
		return new AHashSetIntIterator();
	}
	public class AHashSetIntIterator extends EntryIterator{
		private int bucketIndexNextEntry = 0;
		private int arrayIndexNextEntry = 0;
		private int bucketIndexCurrentEntry = -1;
		private int arrayIndexCurrentEntry = -1;
		private int nextEntry = -1;
		private int currentEntry = -1;
		public AHashSetIntIterator() {
			if (elementCount == 0) {
				return;
			}
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null && buckets[bucketIndexNextEntry].size() != 0) {
					nextEntry = buckets[bucketIndexNextEntry].get(0);
					return;
				}
			}
		}
		public int next() {
			if (nextEntry == -1) {
				throw new NoSuchElementException();
			}
			currentEntry = nextEntry;
			bucketIndexCurrentEntry = bucketIndexNextEntry;
			arrayIndexCurrentEntry = arrayIndexNextEntry;
			if (arrayIndexNextEntry < buckets[bucketIndexNextEntry].size() - 1) {
				arrayIndexNextEntry++;
				nextEntry = buckets[bucketIndexNextEntry].get(arrayIndexNextEntry);
				return currentEntry;
			}
			arrayIndexNextEntry = 0;
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null && buckets[bucketIndexNextEntry].size() != 0) {
					nextEntry = buckets[bucketIndexNextEntry].get(0);
					return currentEntry;
				}
			}
			nextEntry = -1;
			return currentEntry;
		}
		public boolean hasNext() {
			return nextEntry != -1;
		}
		public void remove() {
			if (currentEntry == -1) {
				throw new IllegalStateException();
			}
			buckets[bucketIndexCurrentEntry].removeAt(arrayIndexCurrentEntry);
			if (bucketIndexCurrentEntry == bucketIndexNextEntry) {
				arrayIndexNextEntry--;
			}
			elementCount--;
		}
	}
}
