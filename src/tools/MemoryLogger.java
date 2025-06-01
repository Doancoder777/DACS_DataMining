package tools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class MemoryLogger {
	private static MemoryLogger instance = new MemoryLogger();
	private double maxMemory = 0;
	private boolean recordingMode = false;
	private File outputFile = null;
	private BufferedWriter writer = null;
	public static MemoryLogger getInstance() {
		return instance;
	}
	public double getMaxMemory() {
		return maxMemory;
	}
	public void reset() {
		maxMemory = 0;
	}
	public double checkMemory() {
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
		if (recordingMode) {
			try {
				writer.write(currentMemory + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return currentMemory;
	}
	public void startRecordingMode(String fileName) {
		recordingMode = true;
		outputFile = new File(fileName);
		try {
			writer = new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void stopRecordingMode() {
		if (recordingMode) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			recordingMode = false;
		}
	}
}

