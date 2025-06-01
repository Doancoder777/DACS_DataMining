package datastructures.collections.map;
import datastructures.collections.map.MapIntToInt.KeyIterator;
import datastructures.collections.map.MapIntToInt.ValueIterator;
public abstract class MapIntToFloat {
	public abstract void clear();
	public abstract int size();
	public abstract  boolean isEmpty();
	protected abstract int hash(int key);
	public abstract boolean containsKey(int key);
	public abstract  float get(int key);
	public abstract void getAndIncreaseValueBy(int key, float valueToAdd);
	public abstract void put(int key, float value);
	public abstract boolean remove(int key);
	public abstract EntryIterator iterator();
	public abstract class EntryIterator {
		public abstract MapEntryIntToFloat next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract class MapEntryIntToFloat{
		public abstract int getKey();
		public abstract float getValue();
	}
	public abstract KeyIterator iteratorForKeys();
	public abstract class KeyIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract ValueIterator iteratorForValues();
	public abstract class ValueIterator {
		public abstract float next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
