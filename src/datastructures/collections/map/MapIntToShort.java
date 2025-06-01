package datastructures.collections.map;
import datastructures.collections.map.MapIntToInt.KeyIterator;
import datastructures.collections.map.MapIntToInt.ValueIterator;
public abstract class MapIntToShort {
	public abstract void clear();
	public abstract int size();
	public abstract  boolean isEmpty();
	protected abstract int hash(int key);
	public abstract boolean containsKey(int key);
	public abstract  short get(int key);
	public abstract void getAndIncreaseValueBy(int key, short valueToAdd);
	public abstract void put(int key, short value);
	public abstract boolean remove(int key);
	public abstract EntryIterator iterator();
	public abstract class EntryIterator {
		public abstract MapEntryIntToShort next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract class MapEntryIntToShort{
		public abstract int getKey();
		public abstract short getValue();
	}
	public abstract KeyIterator iteratorForKeys();
	public abstract class KeyIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract ValueIterator iteratorForValues();
	public abstract class ValueIterator {
		public abstract short next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
