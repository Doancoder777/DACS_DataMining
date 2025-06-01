package datastructures.collections.map;
import java.util.Arrays;
import java.util.NoSuchElementException;
import datastructures.collections.list.ArrayListObject;
import datastructures.collections.map.AMapIntToLong.Entry;
import datastructures.collections.map.AMapIntToShort.AKeyIterator;
import datastructures.collections.map.AMapIntToShort.AValueIterator;
import datastructures.collections.map.MapIntToShort.KeyIterator;
import datastructures.collections.map.MapIntToShort.ValueIterator;
public class AMapIntToInt extends MapIntToInt{
	private ArrayListObject<Entry>[] buckets;
	private int elementCount;
	private static final int DEFAULT_BUCKET_COUNT = 100;
	private int initialCollisionListSize = 3;
	public class Entry extends MapEntryIntToInt {
		public int key;
		public int value;
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
	public AMapIntToInt() {
		buckets = new ArrayListObject[DEFAULT_BUCKET_COUNT];
		elementCount = 0;
	}
	public AMapIntToInt(int initialCapacity) {
		elementCount = 0;
		buckets = new ArrayListObject[initialCapacity];
	}
	public AMapIntToInt(int initialCapacity, int initialCollisionListSize) {
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
			Entry entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				return true;
			}
		}
		return false;
	}
	public int get(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return -1;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				return entry.value;
			}
		}
		return -1;
	}
	@Override
	public void getAndIncreaseValueBy(int key, int valueToAdd) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new ArrayListObject<>(initialCollisionListSize);
			buckets[initialIndex].add(new Entry(key, valueToAdd));
			elementCount++;
			return;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				entry.value += valueToAdd;
				return;
			}
		}
		buckets[initialIndex].add(new Entry(key, valueToAdd));
		elementCount++;
		return;
	}
	public void put(int key, int value) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			buckets[initialIndex] = new ArrayListObject<>(initialCollisionListSize);
			buckets[initialIndex].add(new Entry(key, value));
			elementCount++;
			return;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				entry.value = value;
				return;
			}
		}
		buckets[initialIndex].add(new Entry(key, value));
		elementCount++;
		return;
	}
	public boolean remove(int key) {
		int initialIndex = hash(key);
		if (buckets[initialIndex] == null) {
			return false;
		}
		for (int i = 0; i < buckets[initialIndex].size(); i++) {
			Entry entry = buckets[initialIndex].get(i);
			if (entry.key == key) {
				buckets[initialIndex].removeAt(i);
				elementCount--;
				return true;
			}
		}
		return false;
	}
	public EntryIterator iterator() {
		return new AEntryIterator();
	}
	public class AEntryIterator extends EntryIterator{
		private int bucketIndexNextEntry = 0;
		private int arrayIndexNextEntry = 0;
		private int bucketIndexCurrentEntry = -1;
		private int arrayIndexCurrentEntry = -1;
		private Entry nextEntry = null;
		private Entry currentEntry = null;
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
		public Entry next() {
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
		private Entry nextEntry = null;
		private Entry currentEntry = null;
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
		public ValueIterator iteratorForValues() {
			return new AValueIterator();
		}
		public class AValueIterator extends ValueIterator{
			private int bucketIndexNextEntry = 0;
			private int arrayIndexNextEntry = 0;
			private int bucketIndexCurrentEntry = -1;
			private int arrayIndexCurrentEntry = -1;
			private Entry nextEntry = null;
			private Entry currentEntry = null;
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
