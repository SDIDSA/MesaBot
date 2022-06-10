package squidBot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import ai.Ai;
import squidBot.bot.GameManager;
import squidBot.bot.Media;
import squidBot.bot.NameSolver;
import squidBot.driver.Auth;

public class Launcher {

//	public static void main(String[] args) throws IOException {
//		InputStream imageStream = Launcher.class.getResourceAsStream("/images/kim.jpg");
//		
//		BufferedImage input = ImageIO.read(imageStream);
//		
//		File triangulated = Ai.triangulate(input, 1000);
//		
//		System.out.println(triangulated.getAbsolutePath());
//	}
//	
//}

	static FirefoxDriver chatDriver;
	static FirefoxDriver nameDriver;
	
	public static void main(String[] args)
			throws LangDetectException, InterruptedException, IllegalArgumentException, IOException {
		//System.out.println(Media.yt("-f 48 -d 60 -q low Apashe - Uebok VIP [Music Video]", true).getAbsolutePath());

		//System.out.println(Ai.triangulate(ImageIO.read(Launcher.class.getResourceAsStream("/kim.jpg")), 1000));

		System.setProperty("webdriver.gecko.driver",
				URLDecoder.decode(Launcher.class.getResource("/drivers/geckodriver.exe").getFile(), "utf-8"));
		System.setProperty("webdriver.chrome.driver",
				URLDecoder.decode(Launcher.class.getResource("/drivers/chromedriver.exe").getFile(), "utf-8"));
		DetectorFactory
				.loadProfile(URLDecoder.decode(DetectorFactory.class.getResource("/shorttext").getFile(), "utf-8"));

		Thread ac = new Thread() {
			@Override
			public void run() {
				chatDriver = new FirefoxDriver(fp());
				Auth.auth(chatDriver);
			}
		};
		Thread an = new Thread() {
			@Override
			public void run() {
				nameDriver = new FirefoxDriver(fp());
				Auth.auth(nameDriver);
			}
		};

		ac.start();
		an.start();

		ac.join();
		an.join();

		NameSolver names = new NameSolver(nameDriver);
		GameManager manager = new GameManager(chatDriver, names);

		manager.start();
	}

	private static FirefoxOptions fp() {
		FirefoxOptions fp = new FirefoxOptions();
		fp.addPreference("permissions.default.stylesheet", 2);
		fp.addPreference("permissions.default.image", 2);
		fp.addPreference("dom.ipc.plugins.enabled.libflashplayer.so", "false");
		return fp;
	}
}
