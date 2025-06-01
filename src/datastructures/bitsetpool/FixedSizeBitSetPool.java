package datastructures.bitsetpool;
import java.util.BitSet;
public class FixedSizeBitSetPool extends BitSetPool {
	private final int sizeOfEachBitset;
	public FixedSizeBitSetPool(int initialPoolSize, int sizeOfEachBitset) {
		super(initialPoolSize);
		this.sizeOfEachBitset = sizeOfEachBitset;
	}
	protected BitSet instantiateNewBitSet() {
		return new BitSet(sizeOfEachBitset);
	}
}
