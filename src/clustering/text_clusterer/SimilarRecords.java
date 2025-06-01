package clustering.text_clusterer;
public class SimilarRecords {
int record1Pos; //index of the first record
int record2Pos; //index of the second record
double similarity; //similarity measure between the 2 records
public int getRecord1Pos() {
	return record1Pos;
}
public void setRecord1Pos(int record1Pos) {
	this.record1Pos = record1Pos;
}
public int getRecord2Pos() {
	return record2Pos;
}
public void setRecord2Pos(int record2Pos) {
	this.record2Pos = record2Pos;
}
public double getSimilarity() {
	return similarity;
}
public void setSimilarity(double similarity) {
	this.similarity = similarity;
}
}

