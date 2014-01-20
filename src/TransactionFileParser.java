import java.util.List;


public interface TransactionFileParser {
	public List<TransactionEntry> getResults();
	public void parse(String fileName);
}
