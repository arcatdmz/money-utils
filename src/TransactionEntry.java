import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TransactionEntry {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

	public String detail;
	public Calendar calendar;
	public int amount;

	@Override
	public String toString() {
		return String.format("%s,,,%d,%s",
				format.format(calendar.getTime()), amount, detail);
	}
}