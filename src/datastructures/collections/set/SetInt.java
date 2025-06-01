package datastructures.collections.set;
public abstract class SetInt {
	public abstract void clear();
	public abstract int size();
	public abstract boolean isEmpty();
	protected abstract int hash(int key);
	public abstract boolean contains(int key);
	public abstract void add(int key);
	public abstract boolean remove(int key);
	public abstract void addAll(SetInt set);
	public abstract EntryIterator iterator();
	public abstract class EntryIterator {
		public abstract int next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
