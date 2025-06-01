package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorLong;
public abstract class ListLong {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(long element);
	public void addAll(ListLong list) {
		for(int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, long value);
	public abstract void removeAt(int index);
	public void removeAll(ListLong list) {
		for(int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract void remove(long value);
	public abstract long get(int index);
	public abstract int size();
	public abstract boolean contains(long value);
	public abstract int indexOf(long value);
	public abstract void sort(ComparatorLong comparator);
	public abstract void sortByIncreasingOrder();
	public abstract void sortByDecreasingOrder();
	public abstract ListLong immutableSubList(int fromPosition, int toPosition);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract long next();
		public abstract void remove();
	}
}
