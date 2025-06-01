package datastructures.collections.automatic_test;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import datastructures.collections.set.AHashSetObject;
import datastructures.collections.set.SetObject;
import datastructures.collections.set.SetObject.AEntryIterator;
public class MainTestAHashSetObject {
	public static void main(String[] args) {
		for (int bucketCount = 1; bucketCount <= 100; bucketCount++) {
			for (int collisionArraySize = 1; collisionArraySize <= 10; collisionArraySize++) {
				runExperiment(bucketCount, collisionArraySize);
			}
		}
	}
	private static void runExperiment(int bucketCount, int collisionArraySize) {
		SetObject<Integer> set = new AHashSetObject<Integer>(bucketCount, collisionArraySize);
		System.out.println("SET size = " + set.size());
		System.out.println("ADD 1");
		set.add(1);
		System.out.println("MAP size = " + set.size());
		CheckResults.checkResult(set.size() == 1);
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("SET size = " + set.size());
		CheckResults.checkResult(set.contains(1));
		CheckResults.checkResult(set.size() == 1);
		System.out.println("ADD 1");
		set.add(1);
		CheckResults.checkResult(set.contains(1));
		System.out.println("ADD 2");
		set.add(2);
		CheckResults.checkResult(set.contains(2));
		System.out.println("ADD 3");
		set.add(3);
		CheckResults.checkResult(set.contains(3));
		System.out.println("ADD 4");
		set.add(4);
		CheckResults.checkResult(set.contains(4));
		System.out.println("ADD 5");
		set.add(5);
		CheckResults.checkResult(set.contains(5));
		System.out.println("ADD 6");
		set.add(6);
		CheckResults.checkResult(set.contains(6));
		System.out.println("SET size = " + set.size());
		CheckResults.checkResult(set.size() == 6);
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("2 is in the set? " + set.contains(2));
		System.out.println("3 is in the set? " + set.contains(3));
		System.out.println("4 is in the set? " + set.contains(4));
		System.out.println("5 is in the set? " + set.contains(5));
		System.out.println("6 is in the set? " + set.contains(6));
		System.out.println("REMOVE 1");
		set.remove(1);
		CheckResults.checkResult(set.contains(1) == false);
		CheckResults.checkResult(set.size() == 5);
		CheckResults.checkResult(set.contains(2));
		CheckResults.checkResult(set.contains(3));
		CheckResults.checkResult(set.contains(4));
		CheckResults.checkResult(set.contains(5));
		CheckResults.checkResult(set.contains(6));
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("2 is in the set? " + set.contains(2));
		System.out.println("3 is in the set? " + set.contains(3));
		System.out.println("4 is in the set? " + set.contains(4));
		System.out.println("5 is in the set? " + set.contains(5));
		System.out.println("6 is in the set? " + set.contains(6));
		System.out.println("SET size = " + set.size());
		System.out.println("REMOVE 2");
		set.remove(2);
		CheckResults.checkResult(set.contains(1) == false);
		CheckResults.checkResult(set.contains(2) == false);
		CheckResults.checkResult(set.contains(3));
		CheckResults.checkResult(set.contains(4));
		CheckResults.checkResult(set.contains(5));
		CheckResults.checkResult(set.contains(6));
		CheckResults.checkResult(set.size() == 4);
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("2 is in the set? " + set.contains(2));
		System.out.println("3 is in the set? " + set.contains(3));
		System.out.println("4 is in the set? " + set.contains(4));
		System.out.println("5 is in the set? " + set.contains(5));
		System.out.println("6 is in the set? " + set.contains(6));
		System.out.println("SET size = " + set.size());
		System.out.println("REMOVE 6");
		set.remove(6);
		CheckResults.checkResult(set.contains(1) == false);
		CheckResults.checkResult(set.contains(2) == false);
		CheckResults.checkResult(set.contains(3));
		CheckResults.checkResult(set.contains(4));
		CheckResults.checkResult(set.contains(5));
		CheckResults.checkResult(set.contains(6) == false);
		CheckResults.checkResult(set.size() == 3);
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("2 is in the set? " + set.contains(2));
		System.out.println("3 is in the set? " + set.contains(3));
		System.out.println("4 is in the set? " + set.contains(4));
		System.out.println("5 is in the set? " + set.contains(5));
		System.out.println("6 is in the set? " + set.contains(6));
		System.out.println("SET size = " + set.size());
		System.out.println("ADD 4");
		set.add(4);
		System.out.println("ADD 5");
		set.add(5);
		System.out.println("ADD 6");
		set.add(6);
		CheckResults.checkResult(set.contains(1) == false);
		CheckResults.checkResult(set.contains(2) == false);
		CheckResults.checkResult(set.contains(3));
		CheckResults.checkResult(set.contains(4));
		CheckResults.checkResult(set.contains(5));
		CheckResults.checkResult(set.contains(6));
		CheckResults.checkResult(set.size() == 4);
		System.out.println("1 is in the set? " + set.contains(1));
		System.out.println("2 is in the set? " + set.contains(2));
		System.out.println("3 is in the set? " + set.contains(3));
		System.out.println("4 is in the set? " + set.contains(4));
		System.out.println("5 is in the set? " + set.contains(5));
		System.out.println("6 is in the set? " + set.contains(6));
		System.out.println("SET size = " + set.size());
		System.out.println("ITERATING OVER THE KEY,VALUES");
		AEntryIterator iter3 = set.iterator();
		Set<Integer> setResults3 = new HashSet<Integer>();
		int count3 = 0;
		Integer removedElement = -1;
		int elementToBeRemoved = new Random().nextInt(4) + 1;
		while (iter3.hasNext()) {
			Integer value = (Integer) iter3.next();
			System.out.println("  Entry :" + value);
			count3++;
			if (count3 == elementToBeRemoved) {
				System.out.println("REMOVE THE RANDOM ELEMENT : " + value);
				iter3.remove();
				removedElement = value;
			} else {
				setResults3.add(value);
			}
			if (count3 < 4) {
				CheckResults.checkResult(iter3.hasNext() == true);
			} else {
				CheckResults.checkResult(iter3.hasNext() == false);
			}
		}
		CheckResults.checkResult(setResults3.size() == 3);
		System.out.println("ADD THE REMOVED ELEMENT :" + removedElement);
		set.add(removedElement);
		System.out.println("CLEARING THE SET");
		set.clear();
		CheckResults.checkResult(set.isEmpty() == true);
		CheckResults.checkResult(set.size() == 0);
		CheckResults.checkResult(set.contains(1) == false);
		CheckResults.checkResult(set.contains(2) == false);
		CheckResults.checkResult(set.contains(3) == false);
		CheckResults.checkResult(set.contains(4) == false);
		CheckResults.checkResult(set.contains(5) == false);
		CheckResults.checkResult(set.contains(6) == false);
		System.out.println("WE DO AN ITERATOR ON AN EMPTY SET");
		AEntryIterator iter4 = set.iterator();
		CheckResults.checkResult(iter4.hasNext() == false);
		CheckResults.checkResult(set.size() == 0);
		CheckResults.checkResult(set.isEmpty() == true);
		System.out.println("OK.");
		System.out.println("ADD 9");
		set.add(9);
		CheckResults.checkResult(set.size() == 1);
		CheckResults.checkResult(set.contains(9));
		System.out.println("WE DO AN ITERATOR ON THAT SET");
		AEntryIterator iter5 = set.iterator();
		int x = (Integer) iter5.next();
		System.out.println(" It contains : " + x);
		CheckResults.checkResult(x == 9);
		CheckResults.checkResult(iter5.hasNext() == false);
		CheckResults.checkResult(set.size() == 1);
		CheckResults.checkResult(set.isEmpty() == false);
		System.out.println("WE DO AN ITERATOR AGAIN ON THAT SET");
		AEntryIterator iter6 = set.iterator();
		int x6 = (Integer) iter6.next();
		System.out.println(" It contains : " + x);
		CheckResults.checkResult(x == 9);
		CheckResults.checkResult(iter6.hasNext() == false);
		CheckResults.checkResult(set.size() == 1);
		CheckResults.checkResult(set.isEmpty() == false);
		System.out.println("Now remove the current entry from the map");
		iter6.remove();
		CheckResults.checkResult(iter6.hasNext() == false);
		CheckResults.checkResult(iter6.hasNext() == false);
		CheckResults.checkResult(set.size() == 0);
		CheckResults.checkResult(set.size() == 0);
		CheckResults.checkResult(set.isEmpty() == true);
		CheckResults.checkResult(set.isEmpty() == true);
		System.out.println("The map is empty, and hasNext = " + iter6.hasNext());
		set.clear();
		CheckResults.checkResult(set.isEmpty() == true);
		CheckResults.checkResult(set.size() == 0);
		HashSet<Integer> mirror = new HashSet<Integer>();
		Random rand = new Random(System.currentTimeMillis());
		int i = 0;
		while (i < 50) {
			int randomNumber = rand.nextInt(100) + 1;
			set.add(randomNumber);
			mirror.add(randomNumber);
			i++;
		}
		CheckResults.checkResult(set.size() == mirror.size());
		System.out.println(set.size());
		i = 0;
		while (i < 50) {
			int randomNumber = rand.nextInt(100) + 1;
			set.remove(randomNumber);
			mirror.remove(randomNumber);
			i++;
		}
		CheckResults.checkResult(set.size() == mirror.size());
		System.out.println(set.size());
	}
}

