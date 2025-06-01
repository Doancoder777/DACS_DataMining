package datastructures.collections.automatic_test;
public class RunAllTests {
	public static void main(String[] args) {
		MainTestLHashSetInt.main(null);
		MainTestLHashSetObject.main(null);
		MainTestAHashSetInt.main(null);
		MainTestAHashSetObject.main(null);
		MainTestLinkedListInt.main(null);
		MainTestArrayListInt.main(null);
		MainTestArrayListObject.main(null);
		MainTestArrayListDouble.main(null);
		MainTestArrayListLong.main(null);
		MainTestArrayListFloat.main(null);
		MainTestArrayListShort.main(null);
		MainTestAMapIntToInt.main(null);
		MainTestAMapIntToLong.main(null);
		MainTestAMapIntToDouble.main(null);
		MainTestAMapIntToObject.main(null);
		MainTestAMapIntToFloat.main(null);
		MainTestAMapIntToShort.main(null);
		MainTestLMapIntToDouble.main(null);
		MainTestLMapIntToInt.main(null);
		MainTestLMapIntToLong.main(null);
		MainTestLMapIntToObject.main(null);
		MainTestLMapIntToFloat.main(null);
		MainTestLMapIntToShort.main(null);
		System.out.println("==== All " + CheckResults.getTestDoneCount() + " tests completed successfully! ===");
	}
}

