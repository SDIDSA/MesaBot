package squidBot.driver;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Driver {
	protected static void sendKeys(WebDriver driver, String query, String keys) {
		WebElement element = driver.findElement(By.cssSelector(query));
		sendKeys(driver, element, keys);
	}

	protected static void sendKeys(WebDriver driver, WebElement element, String keys) {
		for (char c : keys.toCharArray()) {
			boolean success = false;
			while (!success) {
				try {
					element.sendKeys(Character.toString(c));
					success = true;
				} catch (Exception x) {
					System.err.println(x.getClass().getSimpleName());
					sleep(100);
				}
			}

		}
	}

	protected static void click(WebDriver driver, String query) {
		boolean success = false;
		while (!success) {
			try {
				driver.findElement(By.cssSelector(query)).click();
				success = true;
			} catch (Exception x) {
				x.printStackTrace();
				sleep(100);
			}
		}
	}
	
	protected WebElement waitFor(WebDriver driver, String query) {
		WebElement res = null;
		while(res == null) {
			try {
				res = driver.findElement(By.cssSelector(query));
			}catch(Exception x) {
			}
		}
		return res;
	}

	protected static void load(String url, WebDriver driver) {
		driver.get(url);
		waitForLoad(driver);
	}

	protected static void waitForLoad(WebDriver driver) {
		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(pageLoadCondition);
	}

	protected static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			// ignore exception
		}
	}
}
