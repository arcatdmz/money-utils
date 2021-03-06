import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class EposPDFParser implements TransactionFileParser {
	private static Pattern datePattern = Pattern.compile("([0-9]{4})\\/([0-9]{1,2})\\/([0-9]{1,2})");
	private static Pattern amountPattern = Pattern.compile("([0-9,]+)円");
	private static String blankContents = "\n－\n";
	private static String startMark = "前回ログイン";
	private static String endMark = "ご利用合計";
	private List<TransactionEntry> entries = new LinkedList<TransactionEntry>();

	/**
	 * Parse PDF files generated by Google Chrome + EPOS Net.
	 * (Outdated as of June 2014)
	 * 
	 * @param args parameters for specifying target PDF files.
	 * @see <a href="https://www.eposcard.co.jp/member/">EPOS Net</a>
	 */
	public static void main(String[] args) {

		// Show help?
		if (args.length < 1 || args.length > 2) {
			String cmd = "java -cp \"bin;lib\\itextpdf-5.4.5.jar\" EposPDFParser";
			System.out.println("Usage:");
			System.out.print(cmd); System.out.println("dirpath");
			System.out.print(cmd); System.out.println("fiscal-year dirpath");
			return;
		}

		// Get parameters.
		int year;
		String format;
		if (args.length == 1) {
			year = Calendar.getInstance().get(Calendar.YEAR);
			format = Utils.getDirPath(args[0]);
		} else {
			year = Integer.valueOf(args[0]);
			format = Utils.getDirPath(args[1]);
		}
		format += "%04d%02d.pdf";

		// Parse PDF files.
		EposPDFParser parser = new EposPDFParser();
		for (int m = 0; m < 12; m ++) {
			int month = m + 3;
			parser.parse(String.format(format, (month / 12) + year, (month % 12) + 1));
			for (TransactionEntry e : parser.getResults()) {
				System.out.println(e.toString());
			}
		}
	}

	public EposPDFParser() {
	}

	public List<TransactionEntry> getResults() {
		return entries;
	}

	public void parse(String fileName) {
		entries.clear();
		PdfReader reader;
		try {
			reader = new PdfReader(fileName);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		PrintWriter out = new PrintWriter(System.out);
		EposPDFRenderListener listener = new EposPDFRenderListener();
		PdfContentStreamProcessor processor = new PdfContentStreamProcessor(
				listener);
		for (int i = 1; i <= reader.getNumberOfPages(); i ++) {
			listener.reset();
			PdfDictionary pageDic = reader.getPageN(i);
			PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
			try {
				processor.processContent(
						ContentByteUtils.getContentBytesForPage(reader, i),
						resourcesDic);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		out.flush();
		reader.close();
	}

	public class EposPDFRenderListener implements RenderListener {
		private TransactionEntry entry;
		private StringBuffer sb;
		private boolean isStarted;
		private boolean isEnded;

		public EposPDFRenderListener() {
			reset();
		}

		public void reset() {
			entry = null;
			sb = new StringBuffer();
			isStarted = false;
			isEnded = false;
		}

		@Override
		public void beginTextBlock() {
		}

		@Override
		public void renderText(TextRenderInfo renderInfo) {
			if (isEnded) return;
			sb.append(renderInfo.getText());
		}

		@Override
		public void endTextBlock() {
			if (isEnded) return;
			String text = sb.toString().trim();
			sb.append("\n");

			// Start mark found?
			if (!isStarted) {
				if (startMark.equals(text)) {
					isStarted = true;
				}
				sb.setLength(0);
				return;
			}

			// Calendar format found?
			Matcher matcher;
			matcher = datePattern.matcher(text);
			if (matcher.matches()) {

				// Create a new entry instance.
				entry = new TransactionEntry();
				entries.add(entry);

				// Set calendar property.
				entry.calendar = Calendar.getInstance();
				int year = Integer.valueOf(matcher.group(1));
				int month = Integer.valueOf(matcher.group(2)) - 1;
				int day = Integer.valueOf(matcher.group(3));
				entry.calendar.set(year, month, day);
				sb.setLength(0);
				return;
			}

			// Amount format found?
			matcher = amountPattern.matcher(text);
			if (matcher.find()) {

				// Amount format found before the instantiation?
				if (entry == null) {
					sb.setLength(0);
					return;
				}

				// Get entry detail.
				if (entry.detail == null) {
					entry.detail = sb.substring(0, matcher.start(1));
					if (entry.detail.endsWith(blankContents)) {
						entry.detail = entry.detail.substring(
								0, entry.detail.length() - blankContents.length());
					}
					entry.detail = entry.detail.replaceAll("\n", "").trim();
				}

				// Get amount.
				String amount = matcher.group(1);
				entry.amount = Integer.valueOf(amount.replaceAll(",", ""));
				sb.setLength(0);
				return;
			}

			// End mark found?
			if (endMark.equals(text)) {
				isEnded = true;
			}

			// Clear the buffer?
			if (entry == null || entry.calendar == null || entry.detail != null) {
				sb.setLength(0);
			}
		}

		@Override
		public void renderImage(ImageRenderInfo renderInfo) {
		}
	}

}
