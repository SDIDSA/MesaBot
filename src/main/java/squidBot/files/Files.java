package squidBot.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class Files {
	private static final int dephase = 100;

	public static void write(String content, File file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, Charset.forName("UTF-8")));
			bw.write(encode(content));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String read(String file) {
		try (BufferedReader br = new BufferedReader(
				new FileReader(new File(URLDecoder.decode(Files.class.getResource(file).getFile(), "utf-8")), Charset.forName("utf-8")))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				if (sb.length() != 0) {
					sb.append('\n');
				}
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException x) {
			x.printStackTrace();
			return "[]";
		}
	}

	public static String read(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file, Charset.forName("utf-8")))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			if ((line = br.readLine()) != null) {
				if (sb.length() != 0) {
					sb.append('\n');
				}
				sb.append(decode(line));
			}
			return sb.toString();
		} catch (IOException x) {
			x.printStackTrace();
			return "[]";
		}
	}

	private static String encode(String input) {
		return dephase(input, dephase);
	}

	private static String decode(String input) {
		return dephase(input, -dephase);
	}

	private static String dephase(String input, int by) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			sb.append((char) (input.charAt(i) + by));
		}
		return sb.toString();
	}
}
