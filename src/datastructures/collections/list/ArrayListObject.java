package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorObject;
public class ArrayListObject<T> extends ListObject<T> {
	private int size = 0;
	private T[] data;
	private int DEFAULT_SIZE = 10;
	@SuppressWarnings("unchecked")
	public ArrayListObject() {
		super();
		data = (T[]) new Object[DEFAULT_SIZE];
	}
	@SuppressWarnings("unchecked")
	public ArrayListObject(int size) {
		super();
		data = (T[]) new Object[size];
	}
	public boolean isEmpty() {
		return size == 0;
	}
	public void clear() {
		size = 0;
	}
	public void add(T element) {
		if (size == data.length) {
			increaseSize();
		}
		size = size + 1;
		data[size - 1] = element;
	}
	public void set(int index, T value) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		data[index] = value;
	}
	public void removeAt(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = index; i < size - 1; i++) {
			data[i] = data[i + 1];
		}
		size = size - 1;
	}
	public void remove(T value) {
		int i = 0;
		int newSize = size;
		for (int j = 0; j < size; j++) {
			if (data[j].equals(value) == false) {
				data[i] = data[j];
				i++;
			} else {
				newSize--;
			}
		}
		size = newSize;
	}
	@SuppressWarnings("unchecked")
	public T get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		return (T) data[index];
	}
	public int size() {
		return size;
	}
	private void increaseSize() {
		T[] newData = (T[]) new Object[data.length * 2];
		System.arraycopy(data, 0, newData, 0, size);
		data = newData;
	}
	public boolean contains(T value) {
		for (int i = 0; i < size; i++) {
			if (data[i].equals(value)) {
				return true;
			}
		}
		return false;
	}
	public int indexOf(T value) {
		for (int i = 0; i < size; i++) {
			if (data[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}
	public void sort(ComparatorObject<T> comparator) {
		T temp;
		for (int i = 0; i < size; i++) {
			int minIndex = i;
			for (int j = i + 1; j < size; j++) {
				if (comparator.compare(data[j], data[minIndex]) < 0) {
					minIndex = j;
				}
			}
			temp = data[i];
			data[i] = data[minIndex];
			data[minIndex] = temp;
		}
	}
	public ListObject<T> immutableSubList(int fromPosition, int toPosition) {
		if (fromPosition < 0) {
			throw new IndexOutOfBoundsException("This is outside the range of indices: " + fromPosition);
		} else if (toPosition > size) {
			throw new IndexOutOfBoundsException("This is outside the range of indices: " + toPosition);
		} else {
			return new ImmutableSublistObject(this, fromPosition, toPosition);
		}
	}
	public class ImmutableSublistObject extends ListObject<T> {
		ListObject<T> array;
		int from;
		int to;
		int length;
		public ImmutableSublistObject(ListObject<T> arrayListObject, int fromPosition, int toPosition) {
			this.array = arrayListObject;
			this.from = fromPosition;
			this.to = toPosition;
			this.length = (to - from);
		}
		@Override
		public boolean isEmpty() {
			return this.length == 0;
		}
		@Override
		public void clear() {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void add(Object element) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void set(int index, Object value) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void removeAt(int index) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void remove(Object value) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public T get(int position) {
			if (position < 0) {
				throw new IndexOutOfBoundsException(" index " + position + " is out of bound");
			} else if (position >= to) {
				throw new IndexOutOfBoundsException(" index " + position + " is out of bound");
			}
			return array.get(from + position);
		}
		@Override
		public int size() {
			return length;
		}
		@Override
		public boolean contains(T value) {
			for (int i = from; i < length; i++) {
				if (array.get(i).equals(value)) {
					return true;
				}
			}
			return false;
		}
		@Override
		public int indexOf(T value) {
			for (int i = from; i < length; i++) {
				if (array.get(i).equals(value)) {
					return i;
				}
			}
			return -1;
		}
		@Override
		public void sort(ComparatorObject<T> comparator) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public ListObject<T> immutableSubList(int fromPosition, int toPosition) {
			return array.immutableSubList(from + fromPosition, from + toPosition);
		}
		@Override
		public IteratorList iterator() {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
	}
	public IteratorList iterator() {
		return new Iterator();
	}
	private class Iterator extends IteratorList {
		private int index = 0;
		public boolean hasNext() {
			return index < size();
		}
		public T next() {
			return data[index++];
		}
		@Override
		public void remove() {
			if (index == 0) {
				throw new IllegalStateException();
			}
			removeAt(--index);
		}
	}
}
