package clusterreader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import timeseries.TimeSeries;
import patterns.cluster.Cluster;
import patterns.cluster.DoubleArray;
import patterns.cluster.DoubleArrayInstance;
import tools.MemoryLogger;
public class AlgoClusterReader {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	int clusterCount = 0;
	int dimensionCount = 0;
	private List<String> attributeNames = null;
	public AlgoClusterReader() {
	}
	public List<Cluster> runAlgorithm(String input) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		attributeNames = new ArrayList<String>();
		List<Cluster> clusters = new ArrayList<Cluster>();
		BufferedReader myInput = null;
		String thisLine;
		boolean instanceHaveAName = false;
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= READING THE INPUT FILE =======");
		}
		myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
		while ((thisLine = myInput.readLine()) != null) {
			if (thisLine.isEmpty() == true ||
					thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'){
				continue;
			}
			if(thisLine.charAt(0) == '@'){
				if(thisLine.startsWith("@ATTRIBUTEDEF=")){
					String attributeName = thisLine.substring(14, thisLine.length());
					attributeNames.add(attributeName);
				}
				continue;
			}
			if(DEBUG_MODE){
				System.out.println(System.lineSeparator() + "--- Reading cluster #" + (clusters.size()+1) + " ---");
			}
			thisLine = thisLine.substring(1,thisLine.length()).replace('[', ' ');
			String tokens[] = thisLine.split(" "); 
			if(clusters.size() == 0){
				for(String token : tokens){
					boolean lastDimension = false;
					if(token.charAt(token.length()-1)== ']'){
						token = token.substring(0, token.length()-1);
						lastDimension = true;
					}
					double value = 0;
					boolean isNumber = true;
					try {
						value = Double.parseDouble(token);
					} catch (NumberFormatException nfe) {
						isNumber = false;
						instanceHaveAName = true;
					}
					if(isNumber){
						dimensionCount++;
					}
					if(lastDimension){
						break;
					}
				}
				if(DEBUG_MODE){
					System.out.println("Number of dimensions: " + dimensionCount);
					System.out.println("Instances have names?: " + instanceHaveAName);
				}
			}
			if(instanceHaveAName) {
				DoubleArray instance = null;
				double[] values = null;
				Cluster cluster = new Cluster();
				int indexValue = 0;
				boolean newInstance = true;
				for(int i=0; i < tokens.length; i++){
					if(newInstance){
						String instanceName = tokens[i];
						values = new double[dimensionCount];
						instance =  new DoubleArrayInstance(values, instanceName);
						indexValue = 0;
						newInstance = false;
					}else{
						String token = tokens[i];
						if(token.charAt(token.length()-1) == ']'){
							newInstance = true;
							token = token.substring(0, token.length()-1);
							cluster.addVector(instance);
						}
						double value = 0;
						try {
							value = Double.parseDouble(token);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
							throw new RuntimeException("Error in input file - parseDouble");
						}
						values[indexValue++] = value; 
					}
				}
				clusters.add(cluster);
			}else{
				DoubleArray instance = null;
				double[] values = null;
				Cluster cluster = new Cluster();
				int indexValue = 0;
				boolean newInstance = true;
				for(int i=0; i < tokens.length; i++){
					if(newInstance){
						values = new double[dimensionCount];
						instance =  new DoubleArray(values);
						indexValue = 0;
						newInstance = false;
					} 
					String token = tokens[i];
					if(token.charAt(token.length()-1) == ']'){
						newInstance = true;
						token = token.substring(0, token.length()-1);
						cluster.addVector(instance);
					}
					double value = 0;
					try {
						value = Double.parseDouble(token);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						throw new RuntimeException("Error in input file - parseDouble");
					}
					values[indexValue++] = value; 
				}
				clusters.add(cluster);
			}
		}
		if(attributeNames.size() == 0 && clusters.size() > 0){
			int dimensionCount = clusters.get(0).getVectors().get(0).data.length;
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute"+i);
			}
		}
		clusterCount = clusters.size();
		myInput.close();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
		return clusters;
	}
	public void printStats() {
		System.out.println("======= READ CLUSTERS TO MEMORY v2.09 - STATS =======");
		System.out.println(" Number of clusters processed: " + clusterCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
	public int getDimensionCount() {
		return dimensionCount;
	}
	public List<String> getAttributeNames() {
		return attributeNames;
	}
}
