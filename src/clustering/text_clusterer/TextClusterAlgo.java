package clustering.text_clusterer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import tools.MemoryLogger;
import tools.PorterStemmer;
import tools.StopWordAnalyzer;
public class TextClusterAlgo {
	private HashSet<String> allWords = new HashSet<String>();
	private HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>(); // map
	private long startTimestamp = 0; // last execution start time
	private long endTimeStamp = 0; // last execution end time
	private boolean stemFlag; // stemming to be done or not
	private boolean stopWordFlag; // stop words to be removed or not
	private PorterStemmer stemmer;
	public void runAlgorithm(String inputPath, String outputPath, boolean stemFlag, boolean stopWordFlag) {
		this.stemFlag = stemFlag;
		this.stopWordFlag = stopWordFlag;
		runAlgorithm(inputPath, outputPath);
	}
	public void runAlgorithm(String inputPath, String outputPath) {
		startTimestamp = System.currentTimeMillis();
		stemmer = new PorterStemmer();
		try {
			BufferedReader inputReader = new BufferedReader(new FileReader(new File(inputPath)));
			if (inputPath != null && outputPath != null) {
				BufferedWriter outputWriter = new BufferedWriter(new FileWriter(new File(outputPath)));
				ArrayList<Record> records = this.loadInput(inputReader, stemFlag, stopWordFlag);
				for (Record record : records) {
					double tfIdfVector[] = new double[allWords.size()];
					int vectorIncrementer = 0;
					for (String word : allWords) {
						tfIdfVector[vectorIncrementer] = this.FindTFIDF(record.getAttribute(), word, records);
						vectorIncrementer++;
					}
					record.setTfVector(tfIdfVector);
				}
				double sim[][] = new double[records.size()][records.size()];
				for (int i = 0; i < records.size(); i++) {
					for (int j = 0; j < records.size(); j++) {
						sim[i][j] = this.calculateSimilarity(records.get(i).getTfVector(),
								records.get(j).getTfVector());
					}
				}
				ArrayList<SimilarRecords> similarRecordPairs = new ArrayList<SimilarRecords>();
				for (int i = 0; i < records.size(); i++) {
					double max = 0.0;
					int ipos = 0;
					int jpos = 0;
					for (int j = 0; j < records.size(); j++) {
						if (i != j) {
							if (sim[i][j] > max) {
								max = sim[i][j];
								ipos = i;
								jpos = j;
							}
						}
					}
					SimilarRecords pair = new SimilarRecords();
					pair.setRecord1Pos(ipos);
					pair.setRecord2Pos(jpos);
					pair.setSimilarity(max);
					similarRecordPairs.add(pair);
				}
				Set<TextCluster> clusters = new HashSet<TextCluster>();
				for (SimilarRecords similarPair : similarRecordPairs) {
					int i = similarPair.getRecord1Pos();
					int j = similarPair.getRecord2Pos();
					ArrayList<Integer> tempList = new ArrayList<Integer>();
					TextCluster result = new TextCluster();
					tempList.add(i);
					tempList.add(j);
					result.setCluster(tempList);
					clusters.add(result);
				}
				Set<TextCluster> clusterSet = new HashSet<TextCluster>(clusters);
				Iterator<TextCluster> clusterIterator = clusterSet.iterator();
				int clusterNum = 0;
				outputWriter.write("RecordId\tClusternum\n");
				while (clusterIterator.hasNext()) {
					TextCluster output = (TextCluster) clusterIterator.next();
					ArrayList<Integer> list = output.getCluster();
					for (int i = 0; i < list.size(); i++) {
						outputWriter.write(idMap.get(list.get(i)) + "\t" + clusterNum + "\n");
					}
					clusterNum++;
				}
				outputWriter.close();
				endTimeStamp = System.currentTimeMillis();
			} else {
				System.out.println("Please pass the path of the input");
			}
		} catch (Exception e) {
			System.out.println("Either file didn't exist or error while clustering");
			e.printStackTrace();
		}
	}
	public void printStatistics() {
		System.out.println("========== Text Clusterer - STATS ============");
		System.out.println(" Total time ~: " + (endTimeStamp - startTimestamp) + " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory() + " mb ");
		System.out.println("=====================================");
	}
	private double calculateSimilarity(double[] tfIdfVector1, double[] tfIdfVector2) {
		double similarity = 0;
		for (int i = 0; i < tfIdfVector1.length; i++) {
			similarity += tfIdfVector1[i] * tfIdfVector2[i];
		}
		return similarity;
	}
	private ArrayList<Record> loadInput(BufferedReader inputReader, boolean stemFlag, boolean stopWordFlag) {
		ArrayList<Record> records = new ArrayList<Record>();
		String currentLine;
		String[] line;
		int recordId;
		String words[];
		try {
			int i = 0;
			while ((currentLine = inputReader.readLine()) != null) {
				line = currentLine.split("\t", -1);
				Record record = new Record();
				recordId = Integer.parseInt(line[0]);
				record.setRecordId(recordId);
				String attribute = line[1].toLowerCase();
				attribute = attribute.replaceAll("[^a-zA-Z0-9]+", " ");
				if (stopWordFlag == true) {
					StopWordAnalyzer analyzer = new StopWordAnalyzer();
					attribute = analyzer.removeStopWords(attribute);
				}
				idMap.put(i, recordId);
				words = attribute.split(" ");
				attribute = "";
				for (String word : words) {
					if (stemFlag == true) {
						word = stemmer.stem(word);
					}
					attribute += word + " ";
					allWords.add(word);
				}
				record.setAttribute(attribute);
				records.add(record);
				i++;
			}
			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}
	private double FindTFIDF(String document, String term, ArrayList<Record> records) {
		double tf = this.FindTermFrequency(document, term);
		float idf = this.FindInverseDocumentFrequency(term, records);
		return tf * idf;
	}
	private float FindInverseDocumentFrequency(String term, ArrayList<Record> records) {
		int occurance = 0;
		for (Record record : records) {
			if (record.getAttribute().contains(term)) {
				occurance++;
			}
		}
		return (float) Math.log((float) occurance / (1 + (float) records.size()));
	}
	private double FindTermFrequency(String document, String term) {
		int occurance = 0;
		String[] words = document.split(" ");
		for (String word : words) {
			if (word.equalsIgnoreCase(term)) {
				occurance++;
			}
		}
		return (double) ((float) occurance / (float) (words.length));
	}
}

