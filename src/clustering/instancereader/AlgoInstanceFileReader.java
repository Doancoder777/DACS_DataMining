package clustering.instancereader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import Algo.ArraysAlgos;
import patterns.cluster.DoubleArray;
import patterns.cluster.DoubleArrayInstance;
import tools.MemoryLogger;
public class AlgoInstanceFileReader {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	int instanceCount = 0;
	private List<DoubleArray> instances;
	private List<String> attributeNames = null;
	public AlgoInstanceFileReader() {
	}
	public List<DoubleArray> runAlgorithm(String input, String separator) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		instances = new ArrayList<DoubleArray>();
		attributeNames = new ArrayList<String>();
		BufferedReader myInput = null;
		String thisLine;
		instanceCount = 0;
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		String currentInstanceName = null;
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		while ((thisLine = myInput.readLine()) != null) {
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			if(thisLine.charAt(0) == '@'){
				if(thisLine.startsWith("@NAME=")){
					currentInstanceName = thisLine.substring(6, thisLine.length());
				}
				if(thisLine.startsWith("@ATTRIBUTEDEF=")){
					String attributeName = thisLine.substring(14, thisLine.length());
					attributeNames.add(attributeName);
				}
				continue;
			}
			if(DEBUG_MODE){
				System.out.println(System.lineSeparator() + "--- Reading instance #" + (instanceCount + 1) + " ---");
			}
			String tokens[] = thisLine.split(separator); 
			double[] dataPoints = ArraysAlgos.convertStringArrayToDoubleArray(tokens);
			String nameToUse = currentInstanceName == null ?  "Series" + instanceCount : currentInstanceName;
			currentInstanceName = null;
			instances.add(new DoubleArrayInstance(dataPoints, nameToUse));
			instanceCount++;
		}
		if(attributeNames.size() == 0 && instances.size() > 0){
			int dimensionCount = instances.get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}
		instanceCount = instances.size();   
		myInput.close();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return instances;
	}
	public void printStats() {
		System.out.println("======= READ INSTANCES TO MEMORY v2.09 - STATS =======");
		System.out.println(" Number of instances processed: " + instanceCount);
		System.out.println(" Number of attributes: " + attributeNames.size());
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
	public List<String> getAttributeNames() {
		return attributeNames;
	}
}
