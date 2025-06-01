package datastructures.collections.set;
import java.util.Arrays;
import java.util.NoSuchElementException;
import datastructures.collections.list.ArrayListObject;
public class AHashSetObject<T> extends SetObject<T>{
	private ArrayListObject<T>[] buckets;
	private int elementCount;
	private final int DEFAULT_BUCKET_COUNT = 100;
	private int initialCollisionListSize = 10;
	@SuppressWarnings("unchecked")
	public AHashSetObject() {
		buckets = new ArrayListObject[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
	}
	@SuppressWarnings("unchecked")
	public AHashSetObject(int initialCapacity) {
		elementCount = 0;
		buckets = new ArrayListObject[initialCapacity];
	}
	@SuppressWarnings("unchecked")
	public AHashSetObject(int initialCapacity, int initialCollisionListSize) {
		elementCount = 0;
		buckets = new ArrayListObject[initialCapacity];
		this.initialCollisionListSize = initialCollisionListSize;
	}
	public AHashSetObject(SetObject<T> set) {
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
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			T bucketKey = buckets[initialIndex].get(i);
			if (bucketKey.equals(key)) {
				return true;
			}
		}
		return false;
	}
	public void add(T key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new ArrayListObject<T>(initialCollisionListSize);
			buckets[initialIndex].add(key);
			elementCount++;
			return;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			T bucketKey = buckets[initialIndex].get(i);
			if (bucketKey.equals(key)) {
				return;
			}
		}
		buckets[initialIndex].add(key);
		elementCount++;
		return;
	}
	public boolean remove(T key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			T bucketKey = buckets[initialIndex].get(i);
			if (bucketKey.equals(key)) {
				buckets[initialIndex].removeAt(i);
				elementCount--;
				return true;
			}
		}
		return false;
	}
	public AEntryIterator iterator() {
		return new AHashSetObjectIterator();
	}
	public class AHashSetObjectIterator extends AEntryIterator {
		private int bucketIndexNextEntry = 0;
		private int arrayIndexNextEntry = 0;
		private int bucketIndexCurrentEntry = -1;
		private int arrayIndexCurrentEntry = -1;
		private T nextEntry = null;
		private T currentEntry = null;
		public AHashSetObjectIterator() {
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
		public T next() {
			if (nextEntry == null) {
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
			buckets[bucketIndexCurrentEntry].removeAt(arrayIndexCurrentEntry);
			if (bucketIndexCurrentEntry == bucketIndexNextEntry) {
				arrayIndexNextEntry--;
			}
			elementCount--;
		}
	}
}
