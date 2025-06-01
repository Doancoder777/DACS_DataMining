package Algo;
import java.util.HashMap;
import java.util.Map;
public class ItemNameConverter {
	int[] newNamesToOldNames;
	Map<Integer, Integer> oldNamesToNewNames;
	int currentIndex;
	public ItemNameConverter(int itemCount) {
		newNamesToOldNames = new int[itemCount+1];
		oldNamesToNewNames = new HashMap<Integer, Integer>(itemCount);
		currentIndex = 1;
	}
	public int assignNewName(int oldName) {
		int newName = currentIndex;
		oldNamesToNewNames.put(oldName, newName);
		newNamesToOldNames[newName] = oldName;
		currentIndex++;
		return newName;
	}
	public int toNewName(int oldName) {
		return oldNamesToNewNames.get(oldName);
	}
	public int toNewName(Integer oldName) {
		return oldNamesToNewNames.get(oldName);
	}
	public int toOldName(int newName) {
		return newNamesToOldNames[newName];
	}
}

