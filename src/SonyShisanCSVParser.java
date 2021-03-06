import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SonyShisanCSVParser {
	private static Pattern fileNamePattern = Pattern.compile("lb_shisan_([0-9]{4})([0-9]{2})([0-9]{2})[0-9]+\\.csv");
	private static String startMark = "金額（円）";
	private static String[] order = {"三井住友", "みずほ", "ソニー銀行 普通", "ソニー銀行 定期", "ソニー銀行 外貨(", "ソニー銀行 外貨定期", "ソニー銀行 投信"};
	private List<ShisanEntry> entries = new LinkedList<ShisanEntry>();

	/**
	 * Parse CSV files generated by SonyBank web service.
	 * 
	 * @param args parameters for specifying target CSV files.
	 * @see <a href="http://moneykit.net/">SonyBank web service</a>
	 */
	public static void main(String[] args) {

		// Show help?
		if (args.length < 1 || args.length > 1) {
			String cmd = "java -cp \"bin\" SonyShisanCSVParser";
			System.out.println("Usage:");
			System.out.print(cmd); System.out.println(" dirpath");
			return;
		}

		// Get source CSV files.
		String dirPath = Utils.getDirPath(args[0]);
		String[] fileNames = new File(dirPath).list();
		Arrays.sort(fileNames);

		// Parse CSV files to collect categories.
		HashSet<String> categories = new HashSet<String>();
		SonyShisanCSVParser parser = new SonyShisanCSVParser();
		for (String fileName : fileNames) {
			Matcher matcher = fileNamePattern.matcher(fileName);
			if (matcher.matches()) {
				parser.parse(dirPath + fileName);
				for (ShisanEntry e : parser.getResults()) {
					categories.add(e.category);
				}
			}
		}

		// Sort category names.
		LinkedList<String> sortedCategories = new LinkedList<String>();
		for (String o : order) {
			Iterator<String> it = categories.iterator();
			while (it.hasNext()) {
				String category = it.next();
				if (category.startsWith(o)) {
					it.remove();
					sortedCategories.add(category);
				}
			}
		}
		sortedCategories.addAll(categories);
		categories.clear();

		// Print header.
		System.out.print("日付");
		for (String category : sortedCategories) {
			System.out.print(",");
			System.out.print(category);
		}
		System.out.println();

		// Parse CSV files again.
		Map<String, Integer> entryMap = new HashMap<String, Integer>();
		for (String fileName : fileNames) {
			Matcher matcher = fileNamePattern.matcher(fileName);
			if (matcher.matches()) {

				// Print date.
				System.out.print(matcher.group(1));
				System.out.print("-");
				System.out.print(matcher.group(2));
				System.out.print("-");
				System.out.print(matcher.group(3));

				// Print entries.
				parser.parse(dirPath + fileName);
				entryMap.clear();
				for (ShisanEntry e : parser.getResults()) {
					entryMap.put(e.category, e.amount);
				}
				for (String category : sortedCategories) {
					Integer amount = entryMap.get(category);
					if (amount == null) amount = 0;
					System.out.print(",");
					System.out.print(amount);
				}

				// End of this line.
				System.out.println();
			}
		}
	}

	public SonyShisanCSVParser() {
	}

	public List<ShisanEntry> getResults() {
		return entries;
	}

	public void parse(String fileName) {
		entries.clear();

		BufferedReader reader;
		try {
			reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fileName), "Shift_JIS"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
			return;
		}

		try {
			String line;
			boolean isEntry = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains(startMark)) {
					isEntry = true;
					continue;
				}
				if (!isEntry) continue;

				// Split CSV items.
				String[] items = line.split(",");
				if (items.length < 7) continue;

				// Create a new entry instance.
				if (!"銀行".equals(items[0])) continue;
				ShisanEntry entry = new ShisanEntry();
				entries.add(entry);

				// Get field data.
				entry.category = items[1] + " ";
				if ("ソニー銀行".equals(items[1])) {
					switch (items[2]) {
					case "普通預金等":
						entry.category += "普通預金";
						break;
					case "外貨普通預金":
						entry.category += "外貨(" + items[4] + ")";
						break;
					case "外貨定期預金":
						entry.category += "外貨定期(" + items[4] + ")";
						break;
					case "投資信託":
						entry.category += "投信(" + getShortName(items[3]) + ")";
						break;
					default:
						entry.category += items[2];
						break;
					}
				} else {
					if ("普通預金等".equals(items[2])) {
						entry.category += "普通預金";
					} else {
						entry.category += items[2];
					}
				}
				entry.amount = Integer.valueOf(items[6]);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try { reader.close(); }
			catch (IOException e) {}
		}
	}

	private String getShortName(String string) {
		switch (string) {
		case "ダイワ日本国債ファンド（毎月分配型）":
			return "国債";
		case "インデックスファンド２２５":
			return "日経225";
		case "ＤＷＳ・グローバル・アグリビジネス株式ファンド":
			return "グローバル・アグリ";
		case "コモンズ３０ファンド":
			return "コモンズ30";
		case "香港ハンセン指数ファンド":
			return "地域:香港";
		case "日興アフリカ株式ファンド":
			return "地域:アフリカ";
		default:
			return string;
		}
	}

	public static class ShisanEntry {
		public String category;
		public int amount;
		@Override
		public String toString() {
			return category + ": " + amount;
		}
	}
}
