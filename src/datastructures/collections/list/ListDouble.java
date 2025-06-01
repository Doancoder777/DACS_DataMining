package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorDouble;
public abstract class ListDouble {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(double element);
	public void addAll(ListDouble list) {
		for(int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, double value);
	public abstract void removeAt(int index);
	public abstract void remove(double value);
	public void removeAll(ListDouble list) {
		for(int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract double get(int index);
	public abstract int size();
	public abstract boolean contains(double value);
	public abstract int indexOf(double value);
	public abstract void sort(ComparatorDouble comparator);
	public abstract void sortByIncreasingOrder();
	public abstract void sortByDecreasingOrder();
	public abstract ListDouble immutableSubList(int fromPosition, int toPosition);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract double next();
		public abstract void remove();
	}
}
