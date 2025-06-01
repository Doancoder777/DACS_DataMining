package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorShort;
public abstract class ListShort {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(short element);
	public void addAll(ListShort list) {
		for(int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, short value);
	public abstract void removeAt(int index);
	public abstract void remove(short value);
	public void removeAll(ListShort list) {
		for(int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract short get(int index);
	public abstract int size();
	public abstract boolean contains(short value);
	public abstract int indexOf(short value);
	public abstract void sort(ComparatorShort comparator);
	public abstract void sortByIncreasingOrder();
	public abstract void sortByDecreasingOrder();
	public abstract ListShort immutableSubList(int fromPosition, int toPosition);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract short next();
		public abstract void remove();
	}
}
