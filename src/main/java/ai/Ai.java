package ai;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;

import squidBot.bot.Command;
import squidBot.bot.Media;

public class Ai {

	private Ai() {
	}

	public static List<File> detectFace(BufferedImage img) {
		Mat mat = Java2DFrameUtils.toMat(img);
		RectVector faces = FaceDetector.detect(mat);
		ArrayList<File> res = new ArrayList<>();
		for (long i = 0; i < faces.size(); i++) {
			Rect r = faces.get(i);
			try {
				File temp = File.createTempFile("face_", ".jpg");
				ImageIO.write(img.getSubimage(r.x(), r.y(), r.width(), r.height()), "jpg", temp);
				res.add(temp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public static File edge(BufferedImage img) {
		Mat mat = Java2DFrameUtils.toMat(img);

		Mat res = new Mat();
		opencv_imgproc.Canny(mat, res, 130, 130);

		BufferedImage resImg = Java2DFrameUtils.toBufferedImage(res);

		try {
			File temp = File.createTempFile("edge_", ".jpg");
			ImageIO.write(resImg, "jpg", temp);
			return temp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File ascii(BufferedImage img, boolean black) {
		double w = img.getWidth();
		double h = img.getHeight();

		double ratio = w / h;

		double nw = 0;
		double nh = 0;

		nw = 60;
		nh = nw / ratio;

		BufferedImage scaled = scale(img, (int) nw, (int) nh);

		StringBuilder ascii = new StringBuilder();

		int lineCount = 0;
		StringBuilder longestLine = null;
		StringBuilder line = null;
		for (int y = 0; y < scaled.getHeight(); y++) {
			line = new StringBuilder();
			for (int x = 0; x < scaled.getWidth(); x++) {
				Color pixcol = new Color(scaled.getRGB(x, y));
				int pixval = (int) (pixcol.getRed() * 0.30 + pixcol.getBlue() * 0.59 + pixcol.getGreen() * 0.11);
				char c = strChar(pixval);
				ascii.append(c);
				line.append(c);
			}
			if (longestLine == null || line.length() > longestLine.length()) {
				longestLine = line;
			}
			ascii.append("\n");
			lineCount++;
		}

		if (longestLine == null) {
			return null;
		}

		String lineString = longestLine.toString();
		Graphics2D g2d = scaled.createGraphics();
		Font f = new Font("Courier New", Font.PLAIN, 20);

		String asciiString = ascii.toString().trim();
		int width = g2d.getFontMetrics(f).stringWidth(lineString);
		int height = g2d.getFontMetrics(f).getHeight() * lineCount;

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D ng2d = result.createGraphics();
		ng2d.setColor(black ? Color.black : Color.white);
		ng2d.fillRect(0, 0, width, height);

		ng2d.setColor(black ? Color.white : Color.black);

		ng2d.setFont(f);
		String[] lines = asciiString.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String xline = lines[i];
			ng2d.drawString(xline, 0, (i + 1) * ng2d.getFontMetrics(f).getHeight());
		}

		try {
			File tempFile = File.createTempFile("ascii_", ".jpg");
			ImageIO.write(scale(result, result.getWidth(), (int) (result.getHeight() * 0.5)), "jpg", tempFile);
			return tempFile;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public static Triangulated triangulateV(BufferedImage img, int gen) {
		List<File> svgs = triangulateAll(img, gen);
		List<File> jpgs = svgs2jpgs(svgs);
		Collections.sort(jpgs);
		try {
			return new Triangulated(Video.encode(jpgs), jpgs.get(jpgs.size() - 1));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static File triangulate(BufferedImage img, int gen) {
		File svg = triangulateOnce(img, gen);
		return svg2jpg(svg);
	}

	public static File svg2jpg(File src) {
		try {
			File triangula = new File(URLDecoder.decode(Media.class.getResource("/Triangula.exe").getFile(), "utf-8"));

			File jpg = new File(src.getAbsolutePath().replace(".svg", ".jpg"));

			new Command("cmd", "/c", "magick convert -background none -size 1024x1024 " + src.getAbsolutePath() + " "
					+ jpg.getAbsolutePath()).execute(triangula.getParentFile()).waitFor();

			new Command("cmd", "/c",
					"magick convert -scale 512x512 " + jpg.getAbsolutePath() + " " + jpg.getAbsolutePath())
					.execute(triangula.getParentFile()).waitFor();

			return jpg;
		} catch (UnsupportedEncodingException | InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public static File triangulateOnce(BufferedImage img, int generations) {
		try {
			File triangula = new File(URLDecoder.decode(Media.class.getResource("/Triangula.exe").getFile(), "utf-8"));

			File src = File.createTempFile("src_", ".jpg");
			File json = File.createTempFile("dst_", ".json");

			ImageIO.write(normalize(img), "jpg", src);

			new Command(line -> {
				if (line.contains("Generation")) {
					int gen = Integer.parseInt(line.split(" ")[1]);
					if (gen > generations) {
						return false;
					}
				}
				return true;
			}, "cmd", "/c", "triangula run -t 8 -p 600 -r " + generations + " -img " + src.getAbsolutePath() + " -out "
					+ json.getAbsolutePath()).execute(triangula.getParentFile()).waitFor();

			return render(src, json, generations, triangula);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return null;
	}

	public static File render(File src, File json, int gen, File triangula) {
		try {
			File svg = File.createTempFile("dst_" + ((gen < 100 ? "0" : "") + (gen < 10 ? "0" : "") + gen) + "_",
					".svg");

			new Command("cmd", "/c", "triangula render -in " + json.getAbsolutePath() + " -out " + svg.getAbsolutePath()
					+ " -img " + src.getAbsolutePath()).execute(triangula.getParentFile()).waitFor();

			return svg;
		} catch (IOException x) {
			x.printStackTrace();
		} catch (InterruptedException xx) {
			xx.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return null;
	}

	public static List<File> triangulateAll(BufferedImage img, int generations) {
		try {
			File triangula = new File(URLDecoder.decode(Media.class.getResource("/Triangula.exe").getFile(), "utf-8"));

			File src = File.createTempFile("src_", ".jpg");
			File json = File.createTempFile("dst_", ".json");
			List<File> svgs = new ArrayList<>();

			ImageIO.write(normalize(img), "jpg", src);

			new Command(line -> {
				if (line.contains("Generation")) {
					int gen = Integer.parseInt(line.split(" ")[1]);
					if (gen > generations) {
						return false;
					}
					svgs.add(render(src, json, gen, triangula));
				}
				return true;
			}, "cmd", "/c", "triangula run -t 1 -r 1 -p 600 -img " + src.getAbsolutePath() + " -out " + json.getAbsolutePath())
					.execute(triangula.getParentFile()).waitFor();

			return svgs;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return new ArrayList<>();
	}

	public static List<File> svgs2jpgs(List<File> src) {
		ArrayList<File> res = new ArrayList<>();

		new ThreadedTask<>(src, svg -> res.add(svg2jpg(svg)),
				(pos, total) -> System.out.println("converting svgs to pngs : " + pos + " / " + total), 8).execute();

		return res;
	}

	public static BufferedImage normalize(BufferedImage src) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		dst.createGraphics().drawImage(src, 0, 0, null);
		return dst;
	}

	public static char strChar(double g) {
		char str;
		if (g >= 240) {
			str = ' ';
		} else if (g >= 210) {
			str = '.';
		} else if (g >= 190) {
			str = '*';
		} else if (g >= 170) {
			str = '+';
		} else if (g >= 120) {
			str = '^';
		} else if (g >= 110) {
			str = '&';
		} else if (g >= 80) {
			str = '8';
		} else if (g >= 60) {
			str = '#';
		} else {
			str = '@';
		}
		return str;
	}

	private static BufferedImage scale(BufferedImage src, int width, int height) {
		Image preScaled = src.getScaledInstance(width, height, 0);

		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		res.createGraphics().drawImage(preScaled, 0, 0, null);

		return res;
	}

	public static File fishEye(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = dest.createGraphics();

		Mat mat = Java2DFrameUtils.toMat(img);
		RectVector faces = FaceDetector.detect(mat);
		for (long i = 0; i < faces.size(); i++) {
			Rect r = faces.get(i);

			Circle face = new Circle(r);

			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {

					if (face.contains(x, y)) {
						int distance = face.getDistance(x, y);
						int distortedDistance = face.getDistortedDistance(x, y);
						int dx = face.cx - x;
						int dy = face.cy - y;
						double cosA = dx / (double) distance;
						double sinA = dy / (double) distance;
						int fdx = (int) (cosA * distortedDistance);
						int fdy = (int) (sinA * distortedDistance);
						int fx = face.cx - fdx;
						int fy = face.cy - fdy;
						g2d.setColor(new Color(img.getRGB(fx, fy)));
					} else {
						g2d.setColor(new Color(img.getRGB(x, y)));
					}

					g2d.fillRect(x, y, 1, 1);
				}
			}
		}

		try {
			File res = File.createTempFile("fishEye_", ".jpg");
			ImageIO.write(dest, "jpg", res);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static class Circle {
		int cx;
		int cy;
		int r;

		public Circle(int cx, int cy, int r) {
			this.cx = cx;
			this.cy = cy;
			this.r = r;
		}

		public Circle(Rect rect) {
			cx = rect.x() + rect.width() / 2;
			cy = rect.y() + rect.height() / 2;
			r = rect.width() / 2;
		}

		public int getCx() {
			return cx;
		}

		public int getCy() {
			return cy;
		}

		public int getR() {
			return r;
		}

		public boolean contains(int x, int y) {
			return getDistance(x, y) <= r;
		}

		public int getDistortedDistance(int x, int y) {
			double t = (double) getDistance(x, y) / r;
			double res = t * t * t;
			return (int) (res * r);
		}

		public int getDistance(int x, int y) {
			int dx = x - cx;
			int dy = y - cy;
			return (int) Math.sqrt((double) dx * dx + dy * dy);
		}
	}
}
