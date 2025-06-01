package datastructures.triangularmatrix;
public interface AbstractTriangularMatrixDouble {
	public abstract String toString();
	public abstract void incrementCount(int i, int j);
	public abstract double getSupportForItems(int i, int j);
	public abstract void setSupport(Integer i, Integer j, double support);
}
