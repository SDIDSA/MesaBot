package squidBot.driver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Cookie.Builder;
import org.openqa.selenium.WebDriver;

import squidBot.files.Files;
import squidBot.utils.Links;

public class Auth extends Driver {
	public static final String HAYLA = "https://www.messenger.com/t/4534902433238734";
	
	
	
	
	public static final String MESA = "https://www.messenger.com/t/5154160028004413";
	
	
	
	
	public static final String BEST_PEOPLE = "https://www.messenger.com/t/4077352422287012";
	public static final String ROJALA = "https://www.messenger.com/t/2896793463738102";
	public static final String MOJITO = "https://www.messenger.com/t/3458953870788501";
	public static final String MESA2 = "https://www.messenger.com/t/5606545076030516/";
	
	private static File cookieStore;

	public static void auth(WebDriver driver) {

		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(3000));
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(3000));
		init();

		load(Links.MESSENGER, driver);
		loadCookies(driver);
		driver.navigate().refresh();
		waitForLoad(driver);

		if (!isLoggedIn(driver)) {
			tryLogin(driver);
			if (isLoggedIn(driver)) {
				saveCookies(driver);
			}
		}else {
			saveCookies(driver);
		}
		
		String selected = MESA;
		String id = selected.substring(selected.lastIndexOf("/"));
		if(!driver.getCurrentUrl().contains(id)) {
			driver.findElement(By.cssSelector("a[href*='"+id+"']")).click();
		}
		sleep(2000);
	}

	private static void init() {
		if (cookieStore == null)
			cookieStore = new File("cookies.ck");
	}

	private static boolean isLoggedIn(WebDriver driver) {
		sleep(5000);
		try {
			driver.findElement(By.cssSelector("#email"));
		} catch (Exception x) {
			return true;
		}
		return false;
	}

	private static void tryLogin(WebDriver driver) {
		sendKeys(driver, "#email", "bot.mesa.bot@gmail.com");
		sendKeys(driver, "#pass", "nikmalik");
		click(driver, "#loginbutton");
		waitForLoad(driver);
	}

	private static void saveCookies(WebDriver driver) {
		Set<Cookie> cookies = driver.manage().getCookies();

		JSONArray arr = new JSONArray();
		for (Cookie cookie : cookies) {
			JSONObject obj = new JSONObject();
			Map<String, Object> map = cookie.toJson();
			for (String key : map.keySet()) {
				obj.put(key, map.get(key));
			}
			arr.put(obj);
		}
		Files.write(arr.toString(), cookieStore);
	}

	private static void loadCookies(WebDriver driver) {
		String cks = Files.read(cookieStore);
		JSONArray arr = new JSONArray(cks);

		for (Object obj : arr) {
			JSONObject cookie = (JSONObject) obj;
			String domain = cookie.getString("domain");
			if (domain.toLowerCase().contains(driver.getCurrentUrl().toLowerCase())
					|| driver.getCurrentUrl().toLowerCase().contains(domain.toLowerCase())) {
				try {
					driver.manage().addCookie(decode(cookie, domain));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static Cookie decode(JSONObject cookie, String domain) throws ParseException {
		Cookie.Builder builder = new Cookie.Builder(cookie.getString("name"), cookie.getString("value"));

		cookie.keySet().forEach(key -> {
			handleCookieKey(builder, key, cookie.get(key));
		});

		return builder.build();
	}

	private static void handleCookieKey(Builder builder, String key, Object value) {
		if (value instanceof Boolean) {
			handleCookieKey(builder, key, (boolean) value);
		} else {
			handleCookieKey(builder, key, (String) value);
		}
	}

	private static void handleCookieKey(Builder builder, String key, String value) {
		try {
			Builder.class.getMethod(key, String.class).invoke(builder, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			//e.printStackTrace();
		}
	}

	private static void handleCookieKey(Builder builder, String key, boolean value) {
		try {
			Builder.class.getMethod(cookieBooleanMethod(key), boolean.class).invoke(builder, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			//e.printStackTrace();
		}
	}

	private static String cookieBooleanMethod(String key) {
		return "is" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
	}

}
