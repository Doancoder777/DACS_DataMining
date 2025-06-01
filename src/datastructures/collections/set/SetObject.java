package datastructures.collections.set;
public abstract class SetObject<T> {
	public abstract void clear();
	public abstract int size();
	public abstract boolean isEmpty();
	protected abstract int hash(T key);
	public abstract boolean contains(T key);
	public abstract void add(T key);
	public abstract boolean remove(T key);
	public abstract void addAll(SetObject<T> set);
	public abstract AEntryIterator iterator();
	public abstract class AEntryIterator {
		public abstract T next();
		public abstract boolean hasNext();
		public abstract void remove();
	}
}
