package datastructures.bitsetpool;
import java.util.BitSet;
public class MainTestFixedSizeBitSetPool {
	public static void main(String[] args) {
		FixedSizeBitSetPool pool = new FixedSizeBitSetPool(1, 3);
		pool.printPoolInformation();
		BitSet bitSet1 = pool.getBitSet();
		bitSet1.set(0, true);
		bitSet1.set(1, false);
		bitSet1.set(2, true);
		BitSet bitSet2 = pool.getBitSet();
		bitSet2.set(0, false);
		bitSet2.set(1, true);
		bitSet2.set(2, true);
		System.out.println("Bitset 1: " + bitSet1);
		System.out.println("Bitset 2: " + bitSet2);
		pool.releaseBitSet(bitSet1);
		System.out.println("Release one bitset");
		pool.printPoolInformation();
		System.out.println("Get one bitset");
		BitSet bitSet3 = pool.getBitSet();
		pool.printPoolInformation();
		System.out.println("Bitset 3 (empty): " + bitSet3);
		System.out.println("Bitset 1 is bitset 3?: " + (bitSet3 == bitSet1));
		System.out.println("Release one bitset");
		pool.releaseBitSet(bitSet2);
		pool.printPoolInformation();
		System.out.println("Get one bitset");
		BitSet bitSet4 = pool.getBitSet();
		pool.printPoolInformation();
		System.out.println("Bitset 4 (empty): " + bitSet4);
		System.out.println("Bitset 4 is bitset 2?: " + (bitSet4 == bitSet2));
		System.out.println("Bitset 4 is not bitset 3?: " + (bitSet4 != bitSet3));
		System.out.println("Get four bitsets");
		BitSet bitSet5 = pool.getBitSet();
		BitSet bitSet6 = pool.getBitSet();
		BitSet bitSet7 = pool.getBitSet();
		BitSet bitSet8 = pool.getBitSet();
		pool.printPoolInformation();
		System.out.println("Release four bitsets");
		pool.releaseBitSet(bitSet5);
		pool.releaseBitSet(bitSet6);
		pool.releaseBitSet(bitSet7);
		pool.releaseBitSet(bitSet8);
		pool.printPoolInformation();
		System.out.println("Get two bitsets");
		@SuppressWarnings("unused")
		BitSet bitSet9 = pool.getBitSet();
		@SuppressWarnings("unused")
		BitSet bitSet10 = pool.getBitSet();
		pool.printPoolInformation();
		System.out.println("Get three bitsets");
		@SuppressWarnings("unused")
		BitSet bitSet11 = pool.getBitSet();
		@SuppressWarnings("unused")
		BitSet bitSet12 = pool.getBitSet();
		@SuppressWarnings("unused")
		BitSet bitSet13 = pool.getBitSet();
		pool.printPoolInformation();
		System.out.println("Clear the pool");
		pool.clear();
		pool.printPoolInformation();
	}
}
