package datastructures.collections.linkedlist;
import datastructures.collections.comparators.ComparatorInt;
import datastructures.collections.list.ListInt;
public class LinkedListInt{
	private int size = 0;
	private Entry head;
	private Entry lastInsertedEntry;
	private class Entry {
		int key;
		Entry next;
		public Entry(int key) {
			this.key = key;
		}
	}
	public LinkedListInt() {
		clear();
	}
	public boolean isEmpty() {
		return size == 0;
	}
	public void clear() {
		size = 0;
		head = null;
		lastInsertedEntry = null;
	}
	public void add(int element) {
		Entry entry = new Entry(element);
		if (size == 0) {
			head = entry;
		} else {
			lastInsertedEntry.next = entry;
		}
		lastInsertedEntry = entry;
		size++;
	}
	public void set(int index, int value) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		Entry entry = head;
		while (index != 0) {
			index--;
			entry = entry.next;
		}
		entry.key = value;
	}
	public void removeAt(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		if (index == 0) {
			head = head.next;
			size--;
			return;
		}
		Entry prev = head;
		for (int i = 0; i < index - 1; i++) {
			prev = prev.next;
		}
		prev.next = prev.next.next;
		size--;
	}
	public void remove(int value) {
		if (size == 0) {
			return;
		}
		while (head != null && head.key == value) {
			size--;
			head = head.next;
		}
		if (head == null) {
			return;
		}
		Entry current = head;
		Entry previous = null;
		while (current != null) {
			if (current.key == value) {
				previous.next = current.next;
				size--;
			} else {
				previous = current;
			}
			current = current.next;
		}
	}
	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		Entry currentEntry = head;
		for (int i = 0; i < index; i++) {
			currentEntry = currentEntry.next;
		}
		return currentEntry.key;
	}
	public int size() {
		return size;
	}
	public boolean contains(int value) {
		if (size == 0) {
			return false;
		}
		Entry currentEntry = head;
		while (currentEntry != null) {
			if (currentEntry.key == value) {
				return true;
			}
			currentEntry = currentEntry.next;
		}
		return false;
	}
	public int indexOf(int value) {
		if (size == 0) {
			return -1;
		}
		Entry currentEntry = head;
		int index = 0;
		while (currentEntry != null) {
			if (currentEntry.key == value) {
				return index;
			}
			currentEntry = currentEntry.next;
			index++;
		}
		return -1;
	}
	public void sortByIncreasingOrder() {
		throw new java.lang.UnsupportedOperationException("Unavailable operation for LinkedListInt.");
	}
	public void sortByDecreasingOrder() {
		throw new java.lang.UnsupportedOperationException("Unavailable operation for LinkedListInt.");
	}
	public void sort(ComparatorInt comparator) {
		throw new java.lang.UnsupportedOperationException("Unavailable operation for LinkedListInt.");
	}
	public ListInt immutableSubList(int fromPosition, int toPosition) {
		throw new java.lang.UnsupportedOperationException("Unavailable operation for LinkedListInt.");
	}
	public int binarySearch(int element, ComparatorInt comparator) {
		throw new java.lang.UnsupportedOperationException("Unavailable operation for LinkedListInt.");
	}
}
