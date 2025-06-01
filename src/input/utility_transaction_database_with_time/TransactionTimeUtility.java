package input.utility_transaction_database_with_time;
import java.util.List;
import input.utility_transaction_database.ItemUtility;
import input.utility_transaction_database.TransactionTP;
public class TransactionTimeUtility extends TransactionTP {
	protected final int timestamp;
	public TransactionTimeUtility(List<ItemUtility> itemsUtilities, int transactionUtility, Integer timestamp){
		super(itemsUtilities, transactionUtility);
		this.timestamp = timestamp;
	}
	public int getTimeStamp() {
		return timestamp;
	}
}

