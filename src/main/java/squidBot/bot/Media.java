package squidBot.bot;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;

public class Media {

	private static LineHandlingStrategy videoExists = new LineHandlingStrategy() {
		@Override
		public boolean lineMatters(String line) {
			return line.endsWith("has already been downloaded");
		}

		@Override
		public String extractFileName(String line) {
			String fileName = line.replace("[download] ", "").replace(" has already been downloaded", "");
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			return fileName;
		}
	};

	private static LineHandlingStrategy videoMerging = new LineHandlingStrategy() {
		@Override
		public boolean lineMatters(String line) {
			return line.startsWith("[Merger] Merging formats into ");
		}

		@Override
		public String extractFileName(String line) {
			String fileName = line.replace("[Merger] Merging formats into ", "").replace("\"", "");
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			return fileName;
		}
	};

	private static LineHandlingStrategy destination = new LineHandlingStrategy() {
		@Override
		public boolean lineMatters(String line) {
			return line.startsWith("[download] Destination: ");
		}

		@Override
		public String extractFileName(String line) {
			String fileName = line.replace("[download] Destination: ", "").replace("\"", "");
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			return fileName;
		}
	};

	private static LineHandlingStrategy videoConvertExist = new LineHandlingStrategy() {

		@Override
		public boolean lineMatters(String line) {
			return line.startsWith("[VideoConvertor] Not converting media file ");
		}

		@Override
		public String extractFileName(String line) {
			String fileName = line.replace("[VideoConvertor] Not converting media file ", "").replace("\"", "")
					.replace("; already is in target format mp4", "");
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			return fileName;
		}
	};

	private static LineHandlingStrategy audioExists = new LineHandlingStrategy() {
		@Override
		public boolean lineMatters(String line) {
			return line.endsWith("has already been downloaded");
		}

		@Override
		public String extractFileName(String line) {
			return line.replace("[download] ", "").replace(".mp3 has already been downloaded", "");
		}
	};

	private static LineHandlingStrategy audioExtact = new LineHandlingStrategy() {
		@Override
		public boolean lineMatters(String line) {
			return line.startsWith("[ExtractAudio] Destination: ");
		}

		@Override
		public String extractFileName(String line) {
			return line.replace("[ExtractAudio] Destination: ", "").replace(".mp3", "");
		}
	};

	public static File yt(String query, boolean video) throws UnsupportedEncodingException, InterruptedException {
		return dlp(query, video, true);
	}
	
	public static File dlp(String query, boolean video, boolean yt) throws UnsupportedEncodingException, InterruptedException {
		File ytDlp = new File(URLDecoder.decode(Media.class.getResource("/yt-dlp.exe").getFile(), "utf-8"));

		String[] parts = query.split(" ");

		String f = "0";
		int d = 30;
		String t = null;

		String q = "144";

		int lasti = -1;
		for (int i = 0; i < parts.length - 2; i++) {
			if (parts[i].equals("-f")) {
				lasti = i + 1;
				String sf = parts[i + 1];
				if (sf.contains(":")) {
					f = sf;
				} else {
					try {
						Integer.parseInt(sf);
						f = sf;
					} catch (NumberFormatException x) {

					}
				}
			}

			if (parts[i].equals("-d")) {
				lasti = i + 1;
				try {
					d = Integer.parseInt(parts[i + 1]);
				} catch (NumberFormatException x) {

				}
			}

			if (parts[i].equals("-t")) {
				lasti = i + 1;
				String st = parts[i + 1];
				if (st.contains(":")) {
					t = st;
				} else {
					try {
						Integer.parseInt(st);
						t = st;
					} catch (NumberFormatException x) {

					}
				}
			}

			if (parts[i].equals("-q")) {
				lasti = i + 1;
				String sq = parts[i + 1];
				switch (sq) {
				case "low":
					q = "240";
					break;
				case "medium":
					q = "360";
					break;
				case "high":
					q = "480";
					break;
				case "hd":
					q = "720";
					break;
				case "fhd":
					q = "1080";
					break;
				default:
					q = "144";
					break;
				}
			}
		}

		StringBuilder pq = new StringBuilder();

		for (int i = lasti + 1; i < parts.length; i++) {
			pq.append(parts[i]).append(" ");
		}

		query = pq.toString().trim();

		LocalDateTime now = LocalDateTime.now();

		StringBuilder stamp = new StringBuilder();
		stamp.append(now.getYear() % 100).append(now.getMonthValue()).append(now.getDayOfMonth()).append(now.getHour())
				.append(now.getMinute()).append(now.getSecond());

		String add = stamp.toString();

		File[] res = new File[1];

		String cm = (video ? "yt-dlp -o %(id)s_" + add + ".%(ext)s -S ext,res:" + q + ",+size --recode mp4"
				: "yt-dlp -x -o %(id)s_" + add + ".%(ext)s --audio-format mp3")
				+ " --external-downloader ffmpeg --external-downloader-args \"ffmpeg_i:-ss " + f
				+ (t == null ? (" -t " + d) : (" -to " + t)) + "\"" + " "+( yt ? ("ytsearch:\"" + query + "\"") : query);
		
		new Command(line -> {
			String fileName = null;
			if (videoExists.lineMatters(line) && video) {
				fileName = videoExists.extractFileName(line);
			}
			if (videoMerging.lineMatters(line) && video) {
				fileName = videoMerging.extractFileName(line);
			}
			if (videoConvertExist.lineMatters(line) && video) {
				fileName = videoConvertExist.extractFileName(line);
			}
			if (audioExists.lineMatters(line) && !video) {
				fileName = audioExists.extractFileName(line);
			}
			if (audioExtact.lineMatters(line) && !video) {
				fileName = audioExtact.extractFileName(line);
			}

			if (destination.lineMatters(line)) {
				fileName = destination.extractFileName(line);
			}

			if (fileName != null) {
				res[0] = new File(ytDlp.getParentFile().getAbsolutePath() + "/" + fileName + (video ? ".mp4" : ".mp3"));
			}

			return true;
		}, "cmd", "/c", cm).execute(ytDlp.getParentFile()).waitFor();

		return res[0];
	}

	static interface LineHandlingStrategy {
		public boolean lineMatters(String line);

		public String extractFileName(String line);
	}
}
