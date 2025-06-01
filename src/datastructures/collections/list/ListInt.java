package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorInt;
public abstract class ListInt {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(int element);
	public void addAll(ListInt list) {
		for (int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, int value);
	public abstract void removeAt(int index);
	public abstract void remove(int value);
	public void removeAll(ListInt list) {
		for (int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract int get(int index);
	public abstract int size();
	public abstract boolean contains(int value);
	public abstract int indexOf(int value);
	public abstract void sort(ComparatorInt comparator);
	public abstract void sortByIncreasingOrder();
	public abstract void sortByDecreasingOrder();
	public abstract ListInt immutableSubList(int fromPosition, int toPosition);
	public abstract int binarySearch(int key, ComparatorInt comparator);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract int next();
		public abstract void remove();
	}
}
