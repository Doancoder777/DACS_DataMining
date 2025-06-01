package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorFloat;
public abstract class ListFloat {
	public abstract boolean isEmpty();
	public abstract void clear();
	public abstract void add(float element);
	public void addAll(ListFloat list) {
		for(int z = 0; z < list.size(); z++) {
			this.add(list.get(z));
		}
	}
	public abstract void set(int index, float value);
	public abstract void removeAt(int index);
	public void removeAll(ListFloat list) {
		for(int z = 0; z < list.size(); z++) {
			this.remove(list.get(z));
		}
	}
	public abstract void remove(float value);
	public abstract float get(int index);
	public abstract int size();
	public abstract boolean contains(float value);
	public abstract int indexOf(float value);
	public abstract void sort(ComparatorFloat comparator);
	public abstract void sortByIncreasingOrder();
	public abstract void sortByDecreasingOrder();
	public abstract ListFloat immutableSubList(int fromPosition, int toPosition);
	public abstract IteratorList iterator();
	public abstract class IteratorList {
		public abstract boolean hasNext();
		public abstract float next();
		public abstract void remove();
	}
}
