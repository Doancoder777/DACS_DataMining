package datastructures.redblacktree;
import java.util.Iterator;
import java.util.Stack;
public class RedBlackTree<T extends Comparable<T>> implements Iterable<T> {
	static final boolean BLACK = true; // define black
	static final boolean RED = false; //define red
	final private  Node NULL =  new Node(); // the sentinel node
	private int size = 0; // number of elements currently in the tree
	private Node root = NULL;  // the tree root
	boolean allowSameElementMultipleTimes = true; 
	public RedBlackTree(boolean allowSameElementMultipleTimes) {
		this.allowSameElementMultipleTimes = allowSameElementMultipleTimes;
	}
	public RedBlackTree() {
	}
	public int size() {
		return size;
	}
	public boolean isEmpty(){
		return root == NULL;
	}
	private void leftRotate(Node x) {
		Node y = x.right;
		x.right = y.left;
		if(!y.left.equals(NULL)){
			y.left.parent = x;
		}
		y.parent = x.parent;
		if(x.parent.equals(NULL)){
			root = y;
		}else if(x.equals(x.parent.left)){
			x.parent.left = y;
		}else{
			x.parent.right = y;
		}
		y.left = x;
		x.parent = y;
	}
	private void rightRotate(Node x) {
		Node y = x.left;
		x.left = y.right;
		if(!y.right.equals(NULL)){
			y.right.parent = x;
		}
		y.parent = x.parent;
		if(x.parent.equals(NULL)){
			root = y;
		}else if(x.equals(x.parent.right)){
			x.parent.right = y;
		}else{
			x.parent.left = y;
		}
		y.right = x;
		x.parent = y;
	}
	public void add(T element) {
		Node z = new Node();
		z.key = element;
		Node y = NULL;
		Node x = root;
		while (x != NULL) {
			y = x;
			int compare = z.key.compareTo(x.key);
			if (compare < 0) {
				x = x.left;
			} else {
				if (compare == 0 && !allowSameElementMultipleTimes) {
					return;
				}
				x = x.right;
			}
		}
		z.parent = y;
		if (y == NULL) { // case of an empty tree
			root = z;
		} else if (z.key.compareTo(y.key) < 0) {
			y.left = z;
		} else {
			y.right = z;
		}
		size++;
		z.left = NULL;
		z.right = NULL;
		z.color = RED;
		insertFixup(z);
	}
	private void insertFixup(Node z) {
		while(z.parent.color == RED){
			if(z.parent.equals(z.parent.parent.left)){
				Node y = z.parent.parent.right;
				if(y.color == RED){
					z.parent.color = BLACK;
					y.color = BLACK;
					z.parent.parent.color = RED;
					z = z.parent.parent;
				}else{
					if(z.equals(z.parent.right)){
						z = z.parent;
						leftRotate(z);
					}
					z.parent.color = BLACK;
					z.parent.parent.color = RED;
					rightRotate(z.parent.parent);
				}
			}else{  
				Node y = z.parent.parent.left;
				if(y.color == RED){
					z.parent.color = BLACK;
					y.color = BLACK;
					z.parent.parent.color = RED;
					z = z.parent.parent;
				}else{
					if(z.equals(z.parent.left)){
						z = z.parent;
						rightRotate(z);              // ok?
					}
					z.parent.color = BLACK;
					z.parent.parent.color = RED;
					leftRotate(z.parent.parent);       // ok?
				}
			}
		}
		root.color = BLACK;
	}
	private void transplant(Node u, Node v){
		if(u.parent == NULL){
			root = v;
		}else if(u == u.parent.left){
			u.parent.left = v;
		}else{
			u.parent.right = v;
		}
		v.parent = u.parent;
	}
	public void remove(T element) {
		Node z = search(root, element);
		if (z == NULL) { // if the element is not in the tree
			return;
		}
		performDelete(z);
	}
	private void performDelete(Node z) {
		Node x;
		Node y = z;
		boolean yOriginalColor = y.color;
		if (z.left ==  NULL) {
			x = z.right;
			transplant(z,z.right);
		}else if(z.right == NULL){
			x = z.left;
			transplant(z, z.left);
		}
		else{
			y = minimum(z.right);
			yOriginalColor =  y.color;
			x = y.right;
			if(y.parent.equals(z)){
				x.parent = y;
			}else{
				transplant(y,y.right);
				y.right = z.right;
				y.right.parent = y;
			}
			transplant(z, y);
			y.left = z.left;
			y.left.parent = y;
			y.color = z.color;
		}
		if(yOriginalColor == BLACK){
			deleteFixup(x);
		}
		size--;
	}
	private void deleteFixup(Node x) {
		while(x != root && x.color == BLACK){
			if(x.equals(x.parent.left)){
				Node w = x.parent.right;
				if(w.color == RED){
					w.color = BLACK;
					x.parent.color = RED;
					leftRotate(x.parent);
					w = x.parent.right;
				}
				if(w.left.color == BLACK && w.right.color == BLACK){
					w.color = RED;
					x = x.parent;   // A V�RIFIER....
				}else{
					if(w.right.color == BLACK){
						w.left.color = BLACK;
						w.color = RED;
						rightRotate(w);
						w = x.parent.right;
					}
					w.color = x.parent.color;
					x.parent.color = BLACK;
					w.right.color = BLACK;
					leftRotate(x.parent);
					x = root;
				}
			}else{
				Node w = x.parent.left;
				if(w.color == RED){
					w.color = BLACK;
					x.parent.color = RED;
					rightRotate(x.parent);
					w = x.parent.left;
				}
				if(w.right.color == BLACK && w.left.color == BLACK){
					w.color = RED;
					x = x.parent;   // A V�RIFIER....
				}else{
					if(w.left.color == BLACK){
						w.right.color = BLACK;
						w.color = RED;
						leftRotate(w);
						w = x.parent.left;
					}
					w.color = x.parent.color;
					x.parent.color = BLACK;
					w.left.color = BLACK;
					rightRotate(x.parent);
					x = root;
				}
			}
		}
		x.color = BLACK;
	}
	public T lower(T k){
		Node result = lowerNode(k);
		if(result == NULL){
			return null;
		}else{
			return result.key;
		}
	}
	public Node lowerNode(T k) {
		Node x = root;
		while (x != NULL) {
			if (k.compareTo(x.key) >0) {
				if(x.right != NULL){
					x = x.right;
				}else{
					return x;
				}
			} else {
				if(x.left != NULL){
					x = x.left;
				}else{
                    Node current = x;
                    while(current.parent != NULL && current.parent.left == current) {
                        current = current.parent;
                    }
                    return current.parent;
				}
			}
		}
		return NULL;
	}
	public T higher(T k){
		Node result = higherNode(k);
		if(result == NULL){
			return null;
		}else{
			return result.key;
		}
	}
	private Node higherNode(T k) {
		Node x = root;
		while (x != NULL) {
			if (k.compareTo(x.key) <0) {
				if(x.left != NULL){
					x = x.left;
				}else{
					return x;
				}
			} else {
				if(x.right != NULL){
					x = x.right;
				}else{
					Node current = x;
                    while(current.parent != NULL && current.parent.right == current) {
                        current = current.parent;
                    }
                    return current.parent;
				}
			}
		}
		return NULL;
	}
	public T popMinimum() {
		if (root == NULL) {
			return null;
		}
		Node x = root;
		while (x.left != NULL) {
			x = x.left;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}
	public T popMaximum() {
		if (root == NULL) {
			return null;
		}
		Node x = root;
		while (x.right != NULL) {
			x = x.right;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}
	public T minimum() {
		if (root == NULL) {
			return null;
		}
		return minimum(root).key;
	}
	private Node minimum(Node x) {
		while (x.left != NULL) {
			x = x.left;
		}
		return x;
	}
	public T maximum() {
		if (root == NULL) {
			return null;
		}
		return maximum(root).key;
	}
	private Node maximum(Node x) {
		while (x.right != NULL) {
			x = x.right;
		}
		return x;
	}
	public boolean contains(T k) {
		return search(root, k) != NULL;
	}
	private Node search(Node x, T k) {
		while (x != NULL && !k.equals(x.key)) {
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
		public T key = null; // the value stored in that node
		boolean color = BLACK;  // BLACK, otherwise RED
		public Node left = NULL;  // pointer to left sub-tree
		public Node right = NULL; // pointer to right sub-tree
		Node parent = NULL;  // pointer to parent if not the root
	}
	public class RedBlackTreeIterator<S>  implements Iterator<S>   { 
		private Stack<Node> nodesToVisitNext;
		public RedBlackTreeIterator(Node root) {
			nodesToVisitNext = new Stack<Node>();
			nodesToVisitNext.push(root);
		}
		@Override
		public boolean hasNext() {
			return nodesToVisitNext.isEmpty() == false;
		}
		@SuppressWarnings("unchecked")
		@Override
		public S next() {
			Node currentNode = nodesToVisitNext.pop();
			if(currentNode.left != null && currentNode.left.key != null){
				nodesToVisitNext.push(currentNode.left);
			}
			if(currentNode.right != null && currentNode.right.key != null){
				nodesToVisitNext.push(currentNode.right);
			}
			return (S) currentNode.key;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not implemented yet.");
		}
	}
	@Override
	public Iterator<T> iterator() {
		return new RedBlackTreeIterator<T>(root);
	}	
}
