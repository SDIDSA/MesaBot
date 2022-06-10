package content;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONException;

import squidBot.bot.data.Message;

public class TextImageUtils {
	private TextImageUtils() {}
	
	public static BufferedImage textToImage(String text, String from, String pfp, Message.MessageType type)
			throws IOException, IllegalArgumentException {

		Color fc = Color.WHITE;
		if (text.startsWith("#")) {
			String color = text.split(" ")[0].split("\n")[0].trim().substring(1);
			text = text.substring(color.length() + 1).trim();
			if (color.length() == 3) {
				char r = color.charAt(0);
				char g = color.charAt(1);
				char b = color.charAt(2);

				StringBuilder cb = new StringBuilder();
				cb.append('#');
				cb.append(r).append(r);
				cb.append(g).append(g);
				cb.append(b).append(b);

				fc = Color.decode(cb.toString());
			} else if (color.length() == 6) {
				fc = Color.decode('#' + color);
			}
		}
		
		text = text.replace("!quote ", "");
		text = text.replace("!text2img ", "");

		int pfpSize = 80;

		Image pf = ImageIO.read(new URL(pfp)).getScaledInstance(pfpSize, pfpSize, 0);
		BufferedImage spf = new BufferedImage(pfpSize, pfpSize, BufferedImage.TYPE_4BYTE_ABGR);
		spf.getGraphics().drawImage(pf, 0, 0, null);
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g2d = img.createGraphics();
		int width = 0, height = 0;

		LinedText lq = null;
		BufferedImage quotedImage = null;

		if (type.equals(Message.MessageType.IMAGE)) {
			String[] parts = text.split(" ");
			quotedImage = ImageIO.read(new URL(parts[parts.length - 1]));
			width = quotedImage.getWidth();
			height = quotedImage.getHeight();

			String preText = text.replace(parts[parts.length - 1], "");
			if (!preText.isBlank()) {
				lq = new LinedText(preText);
				height += 20 + Part.fontSize * lq.size();
				for (Line line : lq) {
					int wi = line.calculateWidth(g2d);
					if (wi > width) {
						width = wi;
					}
				}
			}
		} else {
			lq = new LinedText(text);
			height = Part.fontSize * lq.size();
			for (Line line : lq) {
				int wi = line.calculateWidth(g2d);
				if (wi > width) {
					width = wi;
				}
			}
		}

		width += 20;
		height += 30;

		height += pfpSize;

		int padding = 300;
		width += padding;
		height += padding;

		g2d.dispose();

		img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		g2d = img.createGraphics();

		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);

		BufferedImage back = getBackgroundImage(width, height);

		double targetFactor = (double) width / (double) height;
		double sourceFactor = (double) back.getWidth() / (double) back.getHeight();

		BufferedImage targetBack = null;

		if (targetFactor < sourceFactor) {
			targetBack = toBufferedImage(back.getScaledInstance(-1, height, 0));
		} else {
			targetBack = toBufferedImage(back.getScaledInstance(width, -1, 0));
		}

		back.flush();

		g2d.drawImage(targetBack, 0, 0, null);

		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		int round = 0;

		int opacity = (int) (150 + (1 - (getBrightness(fc) / 256.0)) * 50);
		g2d.setColor(makeTransparent(fc, opacity));
		g2d.fillRoundRect(round, round, width - round * 2, height - round * 2, round, round);

		g2d.setColor(invert(fc));
		if (quotedImage != null) {
			g2d.drawImage(makeRoundedCorner(quotedImage, 20), (width - quotedImage.getWidth()) / 2, padding / 2 + pfpSize, null);
			if (lq != null) {
				for (int i = 0; i < lq.size(); i++) {
					int dx = (width - (lq.get(i).calculateWidth(g2d))) / 2;
					int dy = Part.fontSize * (i + 1) + padding / 2 + pfpSize + quotedImage.getHeight() + 20;
					lq.get(i).drawLine(g2d, dx, dy, width - padding / 2);
				}
			}
		} else {
			for (int i = 0; i < lq.size(); i++) {
				int dx = 10 + padding / 2;
				int dy = Part.fontSize * (i + 1) + padding / 2 + pfpSize;
				lq.get(i).drawLine(g2d, dx, dy, width - padding / 2);
			}
		}

		Line credits = new Line(from);

		int top_width = credits.calculateWidth(g2d, 40);
		credits.drawLine(g2d, (width - top_width) / 2, round + pfpSize + 100, width, 40);

		g2d.drawImage(mask(spf, ImageIO.read(TextImageUtils.class.getResourceAsStream("/mask.png"))),
				(width - pfpSize) / 2, 40, null);

		return img;
	}

	private static BufferedImage toBufferedImage(java.awt.Image img) {
		if (img instanceof BufferedImage bimg) {
			return bimg;
		}

		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		img.flush();
		return bimage;
	}

	private static BufferedImage getBackgroundImage(int width, int height) throws JSONException, IOException {
		URL url = new URL("https://picsum.photos/" + width + "/" + height + "?random=1?grayscale&blur=2.jpg");
		return ImageIO.read(url);
	}

	private static Color makeTransparent(Color source, int alpha) {
		return new Color(source.getRed(), source.getGreen(), source.getBlue(), alpha);
	}

	public static final int MAX_RGB_VALUE = 255;

	public static Color invert(Color c) {
		if (getBrightness(c) > 128) {
			return Color.black;
		} else {
			return Color.white;
		}
	}

	public static final int getBrightness(Color color) {
		final double cr = 0.241;
		final double cg = 0.691;
		final double cb = 0.068;

		double r, g, b;
		r = color.getRed();
		g = color.getGreen();
		b = color.getBlue();

		double result = Math.sqrt(cr * r * r + cg * g * g + cb * b * b);

		return (int) result;
	}

	private static BufferedImage mask(BufferedImage src, BufferedImage mask) {
		BufferedImage res = new BufferedImage(src.getWidth(), src.getWidth(), BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g2d = (Graphics2D) res.getGraphics();
		for (int y = 0; y < res.getHeight(); y++) {
			for (int x = 0; x < res.getWidth(); x++) {
				Color mc = new Color(mask.getRGB(x, y), true);
				Color c = new Color(src.getRGB(x, y));

				g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), mc.getAlpha()));
				g2d.fillRect(x, y, 1, 1);
			}
		}

		return res;
	}
	
	public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
	    
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(image, 0, 0, null);
	    
	    g2.dispose();
	    
	    return output;
	}
}
