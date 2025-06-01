package patterns;
import java.text.DecimalFormat;
public abstract class AbstractItemset {
	public AbstractItemset() {
		super();
	}
	public abstract int size();
	public abstract String toString();
	public void print() {
		System.out.print(toString());
	}
	public abstract int getAbsoluteSupport();
	public abstract double getRelativeSupport(int nbObject);
	public String getRelativeSupportAsString(int nbObject) {
		double frequence = getRelativeSupport(nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(frequence);
	}
	public abstract boolean contains(Integer item);
}
