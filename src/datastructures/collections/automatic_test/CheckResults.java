package datastructures.collections.automatic_test;
class CheckResults {
	private static long testCount = 0;
	static void checkResult(boolean value) {
		testCount++;
		if (value == false) {
			throw new RuntimeException("Failed");
		}
	}
	public static long getTestDoneCount() {
		return testCount;
	}
}

