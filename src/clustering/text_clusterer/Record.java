package clustering.text_clusterer;
public class Record {
public int recordId; //record/Attribute Id in the input file
String attribute;	//the text record in the input file
public double[] tfVector;
public int getRecordId() {
	return recordId;
}
public void setRecordId(int recordId) {
	this.recordId = recordId;
}
public String getAttribute() {
	return attribute;
}
public void setAttribute(String attribute) {
	this.attribute = attribute;
}
public double[] getTfVector() {
	return tfVector;
}
public void setTfVector(double[] tfVector) {
	this.tfVector = tfVector;
}
}

