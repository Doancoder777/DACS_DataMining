package timeseries.reader_writer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import Algo.ArraysAlgos;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoTimeSeriesReader {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	int timeSeriesCount = 0;
	public AlgoTimeSeriesReader() {
	}
	public List<TimeSeries> runAlgorithm(String input, String separator) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		List<TimeSeries> multipleTimeSeries = new ArrayList<TimeSeries>();
		BufferedReader myInput = null;
		String thisLine;
		timeSeriesCount = 0;
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		String currentTimeSeriesName = null;
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		while ((thisLine = myInput.readLine()) != null) {
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			if(thisLine.charAt(0) == '@'){
				if(thisLine.startsWith("@NAME=")){
					currentTimeSeriesName = thisLine.substring(6, thisLine.length());
				}
				continue;
			}
			if(DEBUG_MODE){
				System.out.println(System.lineSeparator() + "--- Reading time series #" + (timeSeriesCount + 1) + " ---");
			}
			String tokens[] = thisLine.split(separator); 
			double[] dataPoints = ArraysAlgos.convertStringArrayToDoubleArray(tokens);
			String nameToUse = currentTimeSeriesName == null ?  "Series " + timeSeriesCount : currentTimeSeriesName;
			currentTimeSeriesName = null;
			multipleTimeSeries.add(new TimeSeries(dataPoints, nameToUse));
			timeSeriesCount++;
		}
		timeSeriesCount = multipleTimeSeries.size();
		myInput.close();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return multipleTimeSeries;
	}
	public void printStats() {
		System.out.println("======= READ TIME SERIES TO MEMORY v2.07- STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}
