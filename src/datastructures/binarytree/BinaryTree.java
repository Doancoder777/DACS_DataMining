package datastructures.binarytree;
public class BinaryTree<T extends Comparable<T>> {
	private int size = 0; 
	private Node root = null; 
	boolean allowSameElementMultipleTimes = true;
	public BinaryTree(boolean allowSameElementMultipleTimes) {
		this.allowSameElementMultipleTimes = allowSameElementMultipleTimes;
	}
	public BinaryTree() {
	}
	public int size() {
		return size;
	}
	public void add(T element) {
		Node z = new Node();
		z.key = element;
		Node y = null;
		Node x = root; // the root
		while (x != null) {
			y = x;
			int compare = z.key.compareTo(x.key);
			if (compare < 0) {
				x = x.left;
			} else {
				if (compare == 0 && !allowSameElementMultipleTimes) {
					return; // we don't add it
				}
				x = x.right;
			}
		}
		z.parent = y;
		if (y == null) { // case of an empty tree
			root = z;  // set z as the root
		}// if z is small than y 
		else if (z.key.compareTo(y.key) < 0) {
			y.left = z;
		} else {
			y.right = z;
		}
		size++;
	}
	public boolean isEmpty(){
		return root == null;
	}
	public void remove(T element) {
		Node z = search(root, element);
		if (z == null) { // if the element is not in the tree
			return;
		}
		performDelete(z);
	}
	private void performDelete(Node z) {
		Node y;
		if (z.left == null || z.right == null) {
			y = z;
		} else {
			y = successor(z);
		}
		Node x;
		if (y.left != null) {
			x = y.left;
		} else {
			x = y.right;
		}
		if (x != null) {
			x.parent = y.parent;
		}
		if (y.parent == null) {
			root = x;
		} else if (y.equals(y.parent.left)) {
			y.parent.left = x;
		} else {
			y.parent.right = x;
		}
		if (y != z) {
			z.key = y.key;
		}
		size--;
	}
	private Node successor(Node x) {
		if (x.right != null) {
			return minimum(x.right);
		}
		Node y = x.parent;
		while (y != null && x.equals(y.right)) {
			x = y;
			y = y.parent;
		}
		return y;
	}
	public T popMinimum() {
		if (root == null) {
			return null;
		}
		Node x = root;
		while (x.left != null) {
			x = x.left;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}
	public T lower(T k) {
		Node result = lowerNode(k);
		if (result == null) {
			return null;
		} else {
			return result.key;
		}
	}
	private Node lowerNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) > 0) {
				if (x.right != null) {
					x = x.right;
				} else {
					return x;
				}
			} else {
				if (x.left != null) {
					x = x.left;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.left == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}
	public T higher(T k) {
		Node result = higherNode(k);
		if (result == null) {
			return null;
		} else {
			return result.key;
		}
	}
	private Node higherNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) < 0) {
				if (x.left != null) {
					x = x.left;
				} else {
					return x;
				}
			} else {
				if (x.right != null) {
					x = x.right;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.right == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}
	public T minimum() {
		if (root == null) {
			return null;
		}
		return minimum(root).key;
	}
	private Node minimum(Node x) {
		while (x.left != null) {
			x = x.left;
		}
		return x;
	}
	public T popMaximum() {
		if (root == null) {
			return null;
		}
		Node x = root;
		while (x.right != null) {
			x = x.right;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}
	public T maximum() {
		if (root == null) {
			return null;
		}
		return maximum(root).key;
	}
	private Node maximum(Node x) {
		while (x.right != null) {
			x = x.right;
		}
		return x;
	}
	public boolean contains(T k) {
		return search(root, k) != null;
	}
	private Node search(Node x, T k) {
		while (x != null && !k.equals(x.key)) {
			if (k.compareTo(x.key) < 0) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		return x;
	}
	public String toString() {
		if (root == null) {
			return "";
		}
		return print(root, new StringBuilder()).toString();
	}
	private StringBuilder print(Node x, StringBuilder buffer) {
		if (x != null && x.key != null) {
			print(x.left, buffer);
			buffer.append(x.key + " ");
			print(x.right, buffer);
		}
		return buffer;
	}
	public class Node {
		T key = null;  // the value stored in this node
		Node left = null; // pointer to left child
		Node right = null; // pointer to right child
		Node parent = null; // pointer to parent
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(key.toString());
			if (left != null) {
				buffer.append(" L= " + left.key);
			}
			if (right != null) {
				buffer.append(" R= " + right.key);
			}
			return buffer.toString();
		}
	}
}

