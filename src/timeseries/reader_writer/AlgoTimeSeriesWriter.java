package timeseries.reader_writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import timeseries.TimeSeries;
import tools.MemoryLogger;
public class AlgoTimeSeriesWriter {
	long startTimestamp = 0; 
	long endTimestamp = 0;  
	boolean DEBUG_MODE = false;
	int timeSeriesCount = 0;
	BufferedWriter writer = null;  
	public AlgoTimeSeriesWriter() {
	}
	public void runAlgorithm(String output, List<TimeSeries> multipleTimeSeries, String separator) throws IOException {
		MemoryLogger.getInstance().reset();
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output));
		for(int j = 0; j < multipleTimeSeries.size(); j++){
			TimeSeries timeSeries  = multipleTimeSeries.get(j);
			writer.write("@NAME=" + timeSeries.getName() );
			writer.newLine();
			for(int i=0; i < timeSeries.data.length; i++){
				double value = timeSeries.data[i];
				writer.write(Double.toString(value));
				if(i != timeSeries.data.length  -1){
					writer.write(separator);
				}	
			}
			if(j != multipleTimeSeries.size() -1){
				writer.newLine();
			}	
		}
		writer.close();
		timeSeriesCount = multipleTimeSeries.size();
		MemoryLogger.getInstance().checkMemory();
		endTimestamp = System.currentTimeMillis();
	}
	public void printStats() {
		System.out.println("======= WRITE TIME SERIES TO FILE v2.06 - STATS =======");
		System.out.println(" Number of time series processed: " + timeSeriesCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("=====================================================================");
	}
}
