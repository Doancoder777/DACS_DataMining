package datastructures.collections.map;
import java.util.Arrays;
import java.util.NoSuchElementException;
import datastructures.collections.list.ArrayListObject;
public class AMapIntToObject<T> extends MapIntToObject<T> {
	private ArrayListObject<Entry<T>>[] buckets;
	private int elementCount;
	private static final int DEFAULT_BUCKET_COUNT = 100;
	private int initialCollisionListSize = 10;
	public class Entry<S> extends MapEntryIntToObject<S> {
		public int key;
		public S value;
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
	public AMapIntToObject() {
		buckets = new ArrayListObject[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
	}
	public AMapIntToObject(int initialCapacity) {
		elementCount = 0;
		buckets = new ArrayListObject[initialCapacity];
	}
	public AMapIntToObject(int initialCapacity, int initialCollisionListSize) {
		elementCount = 0;
		buckets = new ArrayListObject[initialCapacity];
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
	public boolean containsKey(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry<T> entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				return true;
			}
		}
		return false;
	}
	public T get(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return null;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry<T> entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				return entry.value;
			}
		}
		return null;
	}
	public void put(int key, T value) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new ArrayListObject<>(initialCollisionListSize);
			buckets[initialIndex].add(new Entry<T>(key, value));
			elementCount++;
			return;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry<T> entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				entry.value = value;
				return;
			}
		}
		buckets[initialIndex].add(new Entry<T>(key, value));
		elementCount++;
		return;
	}
	public boolean remove(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry<T> entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				buckets[initialIndex].removeAt(i);
				elementCount--;
				return true;
			}
		}
		return false;
	}
	public EntryIterator<T> iterator() {
		return new AEntryIterator();
	}
	public class AEntryIterator extends EntryIterator{
		private int bucketIndexNextEntry = 0;
		private int arrayIndexNextEntry = 0;
		private int bucketIndexCurrentEntry = -1;
		private int arrayIndexCurrentEntry = -1;
		private Entry<T> nextEntry = null;
		private Entry<T> currentEntry = null;
		public AEntryIterator() {
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
		public Entry<T> next() {
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
	public KeyIterator iteratorForKeys() {
		return new AKeyIterator();
	}
	public class AKeyIterator extends KeyIterator{
		private int bucketIndexNextEntry = 0;
		private int arrayIndexNextEntry = 0;
		private int bucketIndexCurrentEntry = -1;
		private int arrayIndexCurrentEntry = -1;
		private Entry<T> nextEntry = null;
		private Entry<T> currentEntry = null;
		public AKeyIterator() {
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
			if (nextEntry == null) {
				throw new NoSuchElementException();
			}
			currentEntry = nextEntry;
			bucketIndexCurrentEntry = bucketIndexNextEntry;
			arrayIndexCurrentEntry = arrayIndexNextEntry;
			if (arrayIndexNextEntry < buckets[bucketIndexNextEntry].size() - 1) {
				arrayIndexNextEntry++;
				nextEntry = buckets[bucketIndexNextEntry].get(arrayIndexNextEntry);
				return currentEntry.key;
			}
			arrayIndexNextEntry = 0;
			bucketIndexNextEntry++;
			for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
				if (buckets[bucketIndexNextEntry] != null && buckets[bucketIndexNextEntry].size() != 0) {
					nextEntry = buckets[bucketIndexNextEntry].get(0);
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
			buckets[bucketIndexCurrentEntry].removeAt(arrayIndexCurrentEntry);
			if (bucketIndexCurrentEntry == bucketIndexNextEntry) {
				arrayIndexNextEntry--;
			}
			elementCount--;
		}
	}
		public ValueIterator<T> iteratorForValues() {
			return new AValueIterator();
		}
		public class AValueIterator extends ValueIterator<T>{
			private int bucketIndexNextEntry = 0;
			private int arrayIndexNextEntry = 0;
			private int bucketIndexCurrentEntry = -1;
			private int arrayIndexCurrentEntry = -1;
			private Entry<T> nextEntry = null;
			private Entry<T> currentEntry = null;
			public AValueIterator() {
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
					return currentEntry.value;
				}
				arrayIndexNextEntry = 0;
				bucketIndexNextEntry++;
				for (; bucketIndexNextEntry < buckets.length; bucketIndexNextEntry++) {
					if (buckets[bucketIndexNextEntry] != null && buckets[bucketIndexNextEntry].size() != 0) {
						nextEntry = buckets[bucketIndexNextEntry].get(0);
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
				buckets[bucketIndexCurrentEntry].removeAt(arrayIndexCurrentEntry);
				if (bucketIndexCurrentEntry == bucketIndexNextEntry) {
					arrayIndexNextEntry--;
				}
				elementCount--;
			}
		}
}
