package squidBot.bot.bots.poll;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import squidBot.bot.bots.JsonReader;
import squidBot.bot.bots.image.BlackAndWhite;
import squidBot.bot.bots.image.BoxBlur;
import squidBot.bot.bots.image.Image;

public class PollViz {
	public static BufferedImage visualize(Poll poll, String message) throws JSONException, IOException {
		int padding = 20;
		int messageHeight = 80;
		int titleHeight = 100;
		int lineHeight = 150;
		int width = 1000;
		int height = padding + messageHeight + padding + titleHeight + padding
				+ (lineHeight + padding) * poll.getOptions().size();

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = img.createGraphics();

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);

		BufferedImage back = getBackgroundImage();
		double targetFactor = (double) width / (double) height;
		double sourceFactor = (double) back.getWidth() / (double) back.getHeight();

		BufferedImage targetBack = null;
		int b_x = 0;
		int b_y = 0;

		if (targetFactor < sourceFactor) {
			targetBack = toBufferedImage(back.getScaledInstance(-1, height, 0));
			b_x = -(targetBack.getWidth(null) - width) / 2;
		} else {
			targetBack = toBufferedImage(back.getScaledInstance(width, -1, 0));
			b_y = -(targetBack.getHeight(null) - height) / 2;
		}

		back.flush();

		Image final_back = new Image(targetBack);
		Image blured = final_back.applyFilter(new BoxBlur(5)).applyFilter(new BlackAndWhite());
		blured.setOpacity(.4);
		blured.draw(g2d, b_x, b_y);

		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		g2d.setColor(new Color(1, 1, 1, .75f));
		g2d.fillRoundRect(padding, padding, width - padding * 2, messageHeight, padding, padding);

		Font font = new Font("Tw Cen MT", Font.PLAIN, 42);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int mw = fm.stringWidth(message);

		g2d.setColor(Color.black);
		int my = (padding + (messageHeight / 2) + (FontHeight(fm) / 4));
		int mx = width / 2 - mw / 2;
		g2d.drawString(message, mx, my);

		font = new Font("Tw Cen MT", Font.PLAIN, 54);
		g2d.setFont(font);
		fm = g2d.getFontMetrics();

		g2d.setColor(Color.white);
		int tw = fm.stringWidth(poll.getTitle());
		int tx = width / 2 - tw / 2;
		int ty = padding + messageHeight + padding + titleHeight / 2 + FontHeight(fm) / 4;
		g2d.drawString(poll.getTitle(), tx, ty);

		int total = 0;
		for (PollOption op : poll.getOptions()) {
			total += op.getVotes().size();
		}

		for (int i = 0; i < poll.getOptions().size(); i++) {
			PollOption op = poll.getOptions().get(i);
			int ox = padding;
			int oy = padding + messageHeight + padding + titleHeight + padding + i * (lineHeight + padding);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawRoundRect(ox, oy, width - padding * 2, lineHeight, padding, padding);
			Font imf = new Font("Tw Cen MT", Font.PLAIN, lineHeight);
			g2d.setFont(imf);
			fm = g2d.getFontMetrics();

			int lty = oy + lineHeight / 2 + FontHeight(fm) / 4 + 2;
			g2d.drawString(op.getId() + "", ox + padding, lty);

			int vx = ox + padding + fm.stringWidth(op.getId() + "") + padding;

			g2d.setFont(font);
			fm = g2d.getFontMetrics();

			int vy = oy + padding + FontHeight(fm) / 2 + 7;
			g2d.drawString(op.getValue(), vx, vy);

			String nov = op.getVotes().size() + " vote" + (op.getVotes().size() != 1 ? "s" : "");
			int px = width - padding - 2 - padding - fm.stringWidth(nov);
			int py = vy;

			g2d.drawString(nov, px, py);

			double factor = total == 0 ? 0.0 : ((double) op.getVotes().size() / (double) total);

			int bx = vx;
			int by = py + fm.getHeight() / 2;
			int bwmax = width - padding - 2 - padding - bx;
			int bh = fm.getHeight() / 2;
			int bw = (int) ((double) bwmax * factor);

			g2d.fillRoundRect(bx, by, bw, bh, padding / 2, padding / 2);
		}

		return img;
	}

	private static int FontHeight(FontMetrics fm) {
		return fm.getAscent() + fm.getDescent();
	}

	private static BufferedImage toBufferedImage(java.awt.Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		img.flush();
		return bimage;
	}

	private static BufferedImage getBackgroundImage() throws JSONException, IOException {
		JSONObject obj = JsonReader.readJsonFromUrl(
				"https://api.unsplash.com/photos/random?client_id=raIcDmGoI4piwUVIp_tGeCM7Bs27eSi4eAnjWZZU1VQ");
		URL url = new URL(obj.getJSONObject("urls").getString("regular").toString());
		return ImageIO.read(url);
	}
}
