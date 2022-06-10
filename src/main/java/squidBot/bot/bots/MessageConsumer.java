package squidBot.bot.bots;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import squidBot.bot.data.Message;

public abstract class MessageConsumer {
	private static Semaphore writeMutex = new Semaphore(1);

	private long delay;
	private WebDriver mDriver;
	protected String lastMsg = "";
	private long last = 0;

	public MessageConsumer(WebDriver mDriver, long delay) {
		this.mDriver = mDriver;
		this.delay = delay;
	}

	protected void sendMessage(String message) {
		writeMutex.acquireUninterruptibly();
		sendKeys(message.replace("\n", Keys.chord(Keys.SHIFT, Keys.ENTER)), "div[role='textbox']");
		mDriver.findElement(By.cssSelector("*[role=main] > div > div > div > div > div + div > div > div > div + div > div > div + div + span > div")).click();
		lastMsg = message;
		writeMutex.release();
	}

	private void sendKeys(String message, String selector) {
		WebElement element = mDriver.findElement(By.cssSelector(selector));

		for(char c : message.toCharArray()) {
			element.sendKeys(Character.toString(c));
		}
	}
	
	protected void sendImage(File img) {
		writeMutex.acquireUninterruptibly();
		mDriver.findElement(By.cssSelector("div:nth-child(2) > input:nth-child(1)")).sendKeys(img.getAbsolutePath());
		sendMessage(" ");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		writeMutex.release();
	}

	protected File saveImage(BufferedImage image) {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(0.95f);
		try {
			File res = new File("temp" + (int) (Math.random() * 1000) + ".jpg");
			ImageOutputStream outputStream = new FileImageOutputStream(res);
			jpgWriter.setOutput(outputStream);
			IIOImage outputImage = new IIOImage(image, null, null);
			jpgWriter.write(null, outputImage, jpgWriteParam);
			jpgWriter.dispose();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void consume(Message m) {
		if (m.getContent().indexOf("-") == 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - last > delay) {
			if (m.getContent().equals(lastMsg)) {
				return;
			}
			postconsume(m);
			last = now;
		}
	}

	public abstract void postconsume(Message message);
}
