package datastructures.collections.list;
import datastructures.collections.comparators.ComparatorInt;
public class ArrayListInt extends ListInt {
	private int size = 0;
	private int[] data;
	private int DEFAULT_SIZE = 20;
	public ArrayListInt() {
		data = new int[DEFAULT_SIZE];
	}
	public ArrayListInt(int size) {
		data = new int[size];
	}
	public boolean isEmpty() {
		return size == 0;
	}
	public void clear() {
		size = 0;
	}
	public void add(int element) {
		if (size == data.length) {
			increaseSize();
		}
		size = size + 1;
		data[size - 1] = element;
	}
	public void set(int index, int value) {
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
	public void remove(int value) {
		int i = 0;
		int newSize = size;
		for (int j = 0; j < size; j++) {
			if (data[j] != value) {
				data[i] = data[j];
				i++;
			} else {
				newSize--;
			}
		}
		size = newSize;
	}
	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		return data[index];
	}
	public int size() {
		return size;
	}
	private void increaseSize() {
		int[] newData = new int[data.length * 2];
		System.arraycopy(data, 0, newData, 0, size);
		data = newData;
	}
	public boolean contains(int value) {
		for (int i = 0; i < size; i++) {
			if (data[i] == value) {
				return true;
			}
		}
		return false;
	}
	public int indexOf(int value) {
		for (int i = 0; i < size; i++) {
			if (data[i] == value) {
				return i;
			}
		}
		return -1;
	}
	public void sortByIncreasingOrder() {
		int temp;
		for (int i = 0; i < size; i++) {
			int minIndex = i;
			for (int j = i + 1; j < size; j++) {
				if (data[j] < data[minIndex]) {
					minIndex = j;
				}
			}
			temp = data[i];
			data[i] = data[minIndex];
			data[minIndex] = temp;
		}
	}
	public void sortByDecreasingOrder() {
		int temp;
		for (int i = 0; i < size; i++) {
			int minIndex = i;
			for (int j = i + 1; j < size; j++) {
				if (data[j] > data[minIndex]) {
					minIndex = j;
				}
			}
			temp = data[i];
			data[i] = data[minIndex];
			data[minIndex] = temp;
		}
	}
	public void sort(ComparatorInt comparator) {
		int temp;
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
	public ListInt immutableSubList(int fromPosition, int toPosition) {
		if (fromPosition < 0) {
			throw new IndexOutOfBoundsException("This is outside the range of indices: " + fromPosition);
		} else if (toPosition > size) {
			throw new IndexOutOfBoundsException("This is outside the range of indices: " + toPosition);
		} else {
			return new ImmutableSublistObject(this, fromPosition, toPosition);
		}
	}
	public class ImmutableSublistObject extends ListInt {
		ListInt array;
		int from;
		int to;
		int length;
		public ImmutableSublistObject(ListInt arrayListObject, int fromPosition, int toPosition) {
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
		public void add(int element) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void set(int index, int value) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void removeAt(int index) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void remove(int value) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public int get(int position) {
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
		public boolean contains(int value) {
			for (int i = from; i < length; i++) {
				if (array.get(i) == value) {
					return true;
				}
			}
			return false;
		}
		@Override
		public int indexOf(int value) {
			for (int i = from; i < length; i++) {
				if (array.get(i) == value) {
					return i;
				}
			}
			return -1;
		}
		@Override
		public void sort(ComparatorInt comparator) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public ListInt immutableSubList(int fromPosition, int toPosition) {
			return array.immutableSubList(from + fromPosition, from + toPosition);
		}
		@Override
		public void sortByIncreasingOrder() {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public void sortByDecreasingOrder() {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public int binarySearch(int item, ComparatorInt itemComparator) {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
		@Override
		public IteratorList iterator() {
			throw new java.lang.UnsupportedOperationException("Unavailable operation for ImmutableSublist.");
		}
	}
	@Override
	public int binarySearch(int element, ComparatorInt comparator) {
		if (size == 0) {
			return -1;
		}
		int lowPosition = 0;
		int highPosition = size - 1;
		while (lowPosition <= highPosition) {
			int midPosition = (lowPosition + highPosition) / 2;
			int midvalue = data[midPosition];
			int result = comparator.compare(element, midvalue);
			if (result == 0) {
				return midPosition;
			} else if (result > 0) {
				lowPosition = midPosition + 1;
			} else if (result < 0) {
				highPosition = midPosition - 1;
			}
		}
		return -(lowPosition + 1);
	}
	public IteratorList iterator() {
		return new Iterator();
	}
	private class Iterator extends IteratorList {
		private int index = 0;
		public boolean hasNext() {
			return index < size();
		}
		public int next() {
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
