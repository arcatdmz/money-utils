
public class Utils {

	public static String getDirPath(String arg) {
		String dirPath = trim(arg);
		if (dirPath.charAt(dirPath.length() - 1) != '\\')
			dirPath += "\\";
		return dirPath;
	}

	public static String trim(String text) {
		text = text.trim();
		if (text.charAt(0) == '\"')
			text = text.substring(1);
		if (text.charAt(text.length() - 1) == '\"')
			text = text.substring(0, text.length() - 1);
		return text;
	}

}
