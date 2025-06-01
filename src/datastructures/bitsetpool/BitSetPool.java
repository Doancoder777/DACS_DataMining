package datastructures.bitsetpool;
import java.util.Arrays;
import java.util.BitSet;
public class BitSetPool {
	private int actualPoolSize;
	private final int initialPoolSize;
	private BitSet[] pool;
	public BitSetPool(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
		clear();
	}
	public BitSet getBitSet() {
		if (actualPoolSize == 0) {
			BitSet bitset = instantiateNewBitSet();
			return bitset;
		} else {
			BitSet bitset = pool[--actualPoolSize];
			return bitset;
		}
	}
	protected BitSet instantiateNewBitSet() {
		return new BitSet();
	}
	public void releaseBitSet(BitSet bitset) {
		bitset.clear();
		if (actualPoolSize == pool.length) {
			resize();
		}
		pool[actualPoolSize++] = bitset;
	}
	private void resize() {
		BitSet[] newPool = new BitSet[2 * pool.length];
		System.arraycopy(pool, 0, newPool, 0, pool.length);
		pool = newPool;
	}
	public void clear() {
		actualPoolSize = 0;
		pool = new BitSet[initialPoolSize];
	}
	void printPoolInformation() {
		System.out.println(" POOL: actualPoolSize = " + actualPoolSize + " Pool array = " + Arrays.toString(pool));
	}
}
