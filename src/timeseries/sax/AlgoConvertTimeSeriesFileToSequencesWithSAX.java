package timeseries.sax;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoConvertTimeSeriesFileToSequencesWithSAX {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	BufferedWriter writer = null;  
	boolean DEBUG_MODE = false;
	int timeSeriesCount = 0;
	SAXSymbol[] symbols;
	public AlgoConvertTimeSeriesFileToSequencesWithSAX() {
	}
	public void runAlgorithm(List<TimeSeries> multipleTimeSeries, String output, int numberOfSegments, int numberOfSymbols, boolean deactivatePAA) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output));
		writer.write("@CONVERTED_FROM_TIME_SERIES");
		timeSeriesCount = multipleTimeSeries.size();
		if(DEBUG_MODE){
			System.out.println(System.lineSeparator() + "======= APPLYING SAX  ========" + System.lineSeparator());
		}
		AlgoSAX algo = new AlgoSAX();
		SAXSymbol[][] saxSequences = algo.runAlgorithm(multipleTimeSeries, numberOfSegments, numberOfSymbols, deactivatePAA);
		if(DEBUG_MODE){
			System.out.println("======= WRITING THE OUTPUT FILE  ========" + System.lineSeparator());
		}
		symbols = algo.getSymbols();
		writeSAXSymbolsToOutputFile(symbols);
		for(int i=0; i< multipleTimeSeries.size(); i++){
			TimeSeries timeSeries = multipleTimeSeries.get(i);
			SAXSymbol[] saxSequence = saxSequences[i];
			writeSAXRepresentationToOutputFile(saxSequence, timeSeries.getName());
		}
		timeSeriesCount = multipleTimeSeries.size();
		MemoryLogger.getInstance().checkMemory();
		writer.close();
		endTimestamp = System.currentTimeMillis();
	}
	public SAXSymbol[] getSymbols() {
		return symbols;
	}
	private void writeSAXRepresentationToOutputFile(SAXSymbol[] saxRepresentation, String name) throws IOException {
		writer.newLine();
		writer.write("@NAME=" + name);
		writer.newLine();
		for(SAXSymbol symbol : saxRepresentation){
			writer.write(symbol.symbol + " -1 ");
		}
		writer.write("-2");
	}
	private void writeSAXSymbolsToOutputFile(SAXSymbol[] symbols) throws IOException {
		for(SAXSymbol symbol : symbols){
			writer.newLine();
			writer.append("@ITEM=" + symbol.symbol + "=[" + symbol.lowerBound + "," + symbol.upperBound + "]");
		}
	}
	public void printStats() {
		System.out.println("======= CONVERT TIME SERIES TO SEQUENCES WITH SAX v2.05- STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}
