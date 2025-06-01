package timeseries.sax;
public class SAXSymbol {
	int symbol;
	double lowerBound;
	double upperBound;
	public SAXSymbol(int symbol, double lowerBound, double upperBound){
		this.symbol = symbol;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	public String toString() {
		return "(" + symbol + " [" + lowerBound + "," + upperBound + "])";
	}
}

