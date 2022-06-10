package squidBot.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import squidBot.driver.Driver;

public class NameSolver extends Driver {
	private WebDriver driver;

	private HashMap<String, String> names;
	private HashMap<String, String> reals;
	private HashMap<String, String> nicks;
	private HashMap<String, String> pfps;

	public NameSolver(WebDriver driver) {
		this.driver = driver;
		reals = new HashMap<>();
		names = new HashMap<>();
		nicks = new HashMap<>();
		pfps = new HashMap<>();
		reals.put("You", "Lukas Owen");
		reals.put("you", "Lukas Owen");
		reals.put("yourself", "Lukas Owen");
		reals.put("You sent", "Lukas Owen");
		reals.put("themself", "$recycle");

		String side = "*[aria-label='Conversation information'][role=button]";
		String expand = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div div[role]";
		String editNicks = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div > div + div > div +div + div + div";

		if (driver.findElements(By.cssSelector(editNicks)).isEmpty()) {
			if (driver.findElements(By.cssSelector(expand)).isEmpty()) {
				click(driver, side);
				sleep(500);
			}

			if (driver.findElements(By.cssSelector(editNicks)).isEmpty()) {
				click(driver, expand);
				sleep(500);
			}

		}

		click(driver, editNicks);
		sleep(2000);

		resolve();
	}

	public void resolve() {
		String rowSelector = "*[aria-label=Nicknames] div + div + div > div > div > div > div > div > div[style]";
		
		List<WebElement> rows = driver.findElements(
				By.cssSelector(rowSelector));

		while (rows.isEmpty()) {
			rows = driver.findElements(
					By.cssSelector(rowSelector));

			sleep(100);
		}

		for (WebElement e : rows) {
			try {
				String real = e.findElement(By.cssSelector("div[role=button] div + div > div > div > div > div + div"))
						.getText().trim();
				WebElement span = e
						.findElement(By.cssSelector("div[role=button] div + div > div > div > div > div > span"));

				List<WebElement> images = span.findElements(By.cssSelector("img"));
				if (!images.isEmpty()) {
					for (WebElement img : images) {
						JavascriptExecutor js = (JavascriptExecutor) driver;
						try {
							js.executeScript("arguments[0].parentNode.innerHTML = arguments[0].alt;", img);
						} catch (Exception x) {
							x.printStackTrace();
						}
					}
				}
				String nick = span.getAttribute("textContent").trim();
				if (real.equalsIgnoreCase("Set nickname")) {
					real = nick;
				}

				String pfp = e.findElement(By.tagName("image")).getAttribute("xlink:href");
				names.put(real, nick);

				pfps.put(real, pfp);
				pfps.put(nick, pfp);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	public String resolveName(String name) {
		name = name.replaceAll("" + (char) 8206, "");
		String found = reals.get(name);
		if (found == null) {
			for (Entry<String, String> entry : names.entrySet()) {
				if (entry.getKey().contains(name.trim()) || entry.getValue().contains(name.trim())) {
					found = entry.getKey().equalsIgnoreCase("Set nickname") ? entry.getValue() : entry.getKey();
					break;
				}
			}
			reals.put(name, found);
		}
		if (found == null) {
			resolve();
			return resolveName(name);
		} else {
			return found;
		}
	}

	public String resolveNick(String name) {
		name = name.replaceAll("" + (char) 8206, "");
		String found = nicks.get(name);
		if (found == null) {
			for (Entry<String, String> entry : names.entrySet()) {
				if (entry.getKey().contains(name.trim()) || entry.getValue().contains(name.trim())) {
					found = entry.getValue();
					break;
				}
			}
			nicks.put(name, found);
		}
		if (found == null) {
			resolve();
			return resolveNick(name);
		} else {
			return found;
		}
	}

	public String getPfp(String name) {
		String pfp = pfps.get(name);
		if (pfp == null) {
			resolve();
			return getPfp(name);
		} else {
			return pfp;
		}
	}
}
