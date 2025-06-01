package datastructures.collections.map;
public abstract class MapIntToInt {
	public abstract void clear();
	public abstract int size();
	public abstract  boolean isEmpty();
	protected abstract int hash(int key);
	public abstract boolean containsKey(int key);
	public abstract  int get(int key);
	public abstract void getAndIncreaseValueBy(int key, int valueToAdd);
	public abstract void put(int key, int value);
	public abstract boolean remove(int key);
	public abstract EntryIterator iterator();
	public abstract class EntryIterator {
		public abstract MapEntryIntToInt next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract class MapEntryIntToInt{
		public abstract int getKey();
		public abstract int getValue();
	}
	public abstract KeyIterator iteratorForKeys();
	public abstract class KeyIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract ValueIterator iteratorForValues();
	public abstract class ValueIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
