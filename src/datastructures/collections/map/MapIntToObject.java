package datastructures.collections.map;
import datastructures.collections.map.MapIntToInt.KeyIterator;
import datastructures.collections.map.MapIntToInt.ValueIterator;
public abstract class MapIntToObject<T> {
	public abstract void clear();
	public abstract int size();
	public abstract  boolean isEmpty();
	protected abstract int hash(int key);
	public abstract boolean containsKey(int key);
	public abstract  T get(int key);
	public abstract void put(int key, T value);
	public abstract boolean remove(int key);
	public abstract EntryIterator<T> iterator();
	public abstract class EntryIterator<S> {
		public abstract MapEntryIntToObject<S> next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract class MapEntryIntToObject<S>{
		public abstract int getKey();
		public abstract S getValue();
	}
	public abstract KeyIterator iteratorForKeys();
	public abstract class KeyIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
	public abstract ValueIterator<T> iteratorForValues();
	public abstract class ValueIterator<T> {
		public abstract T next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
