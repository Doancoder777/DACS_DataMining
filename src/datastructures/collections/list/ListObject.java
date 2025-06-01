package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorObject;
public abstract class ListObject<T> {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(T element);
	public void addAll(ListObject<T> list) {
		for(int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, T value);
	public abstract void removeAt(int index);
	public void removeAll(ListObject<T> list) {
		for(int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract void remove(T value);
	public abstract T get(int index);
	public abstract int size();
	public abstract boolean contains(T value);
	public abstract int indexOf(T value);
	public abstract void sort(ComparatorObject<T> comparator);
	public abstract ListObject<T> immutableSubList(int fromPosition, int toPosition);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract T next();
		public abstract void remove();
	}
}
