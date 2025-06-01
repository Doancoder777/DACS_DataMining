package datastructures.triangularmatrix;
public interface AbstractTriangularMatrix {
	public abstract String toString();
	public abstract void incrementCount(int i, int j);
	public abstract int getSupportForItems(int i, int j);
	public abstract void setSupport(Integer i, Integer j, int support);
	public abstract void setSupport(int i, int j, int support);
}
