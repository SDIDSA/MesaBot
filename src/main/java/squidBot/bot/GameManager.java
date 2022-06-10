package squidBot.bot;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import adult.Adult;
import ai.Ai;
import ai.Triangulated;
import content.TextImageUtils;
import squidBot.bot.data.Message;
import squidBot.bot.data.Message.MessageType;
import squidBot.driver.Driver;
import squidBot.files.Files;

public class GameManager extends Driver {
	private static BufferedWriter LOG;
	static {
		try {
			LOG = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File("log.txt")), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean running = false;

	private WebDriver driver;
	private NameSolver names;

	public GameManager(WebDriver driver, NameSolver names) {
		this.driver = driver;
		this.names = names;
	}

	private int pos;

	private HashMap<String, Long> lastRequest = new HashMap<>();

	public void start() {
		if (running) {
			throw new IllegalStateException("this game manager is already running");
		}

		// HashMap<String, ArrayList<String>> allVoters = new HashMap<>();

		running = true;
		new Thread() {
			@Override
			public void run() {
				while (running) {
					try {
						for (Message msg : getLastMessages()) {
							LOG.append(msg.toString()).append('\n');
							LOG.flush();

							String cont = msg.getContent();
							String[] params = cont.split("\n")[0].trim().split(" ");
							if (cont.equals("!fuck me")) {
								fuck(msg.getSender());
//							} else if (cont.startsWith("!fuck reset ")) {
//								String user = cont.split("!fuck reset ")[1].replace("@", "");
//								
//								if(names.resolveName(user) == null) {
//									gameDriverAction(() -> sendMessage("[" +user + "] is not a member in the group"));
//									return;
//								}
//								String fu = names.resolveName(user);
//								
//								if(allVoters.containsKey(fu)) {
//									allVoters.get(fu).clear();
//								}
//							} else if (cont.startsWith("!fuck ")) {
//								String user = cont.split("!fuck ")[1].replace("@", "");
//
//								gameDriverAction(() -> {
//									if(names.resolveName(user) == null) {
//										sendMessage("[" +user + "] is not a member in the group");
//										return;
//									}
//									
//									String fu = names.resolveName(user);
//									
//									ArrayList<String> voters = allVoters.get(fu);
//
//									if (voters == null) {
//										voters = new ArrayList<>();
//										allVoters.put(fu, voters);
//									}
//
//									if (voters.contains(msg.getSender())) {
//										sendMessage("you already voted to fuck " + fu);
//									} else {
//										voters.add(msg.getSender());
//										sendMessage("you voted to fuck " + fu + "\nvote count : " + voters.size()
//												+ "/3\nvoters : " + voters.toString());
//										if (voters.size() == 3) {
//											fuck(fu);
//										}
//									}
//								});
							} else if (cont.startsWith("!ascii")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									new Thread(() -> {
										try {
											BufferedImage img = ImageIO.read(new URL(msg.getReplyToWhat()));
											File result = Ai.ascii(img, cont.toLowerCase().contains("black"));
											sendImage(result);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}).start();
								}
							} else if (cont.startsWith("!text2img")) {
								String text = cont.split("!text2img")[1].trim();
								gameDriverAction(() -> {
									try {
										sendImage(saveImage(TextImageUtils.textToImage(text, msg.getSender(),
												names.getPfp(msg.getSender()), Message.MessageType.TEXT)));
									} catch (JSONException | IOException e) {
										e.printStackTrace();
									}
								});
							} else if (cont.startsWith("!quote")) {
								if (msg.getReplyToWhat() != null) {
									gameDriverAction(() -> {
										try {
											sendImage(saveImage(TextImageUtils.textToImage(
													(cont.replace("!quote", "").trim() + " " + msg.getReplyToWhat())
															.trim(),
													msg.getReplyToWho(), names.getPfp(msg.getReplyToWho()),
													msg.getReplyToType())));
										} catch (JSONException | IOException e) {
											e.printStackTrace();
										}
									});
								}
//							} else if (cont.startsWith("!mock")) {
//								if(msg.getReplyToWhat() != null) {
//									StringBuilder sb = new StringBuilder();
//									
//									boolean caps = true;
//									for(char c : msg.getReplyToWhat().toCharArray()) {
//										if(Character.isAlphabetic(c)) {
//											caps = !caps;
//											sb.append(caps ? Character.toUpperCase(c):Character.toLowerCase(c));
//										}else {
//											sb.append(c);
//										}	
//									}
//									
//									sb.append(" 🥴");
//									
//									gameDriverAction(() -> sendMessage(sb.toString()));
//								}
//								
							} else if (cont.startsWith("!ytv ")) {
								String query = cont.replace("!ytv", "").trim();

								yt(query, msg.getSender(), true);
							} else if (cont.startsWith("!yt ")) {
								String query = cont.replace("!yt", "").trim();

								yt(query, msg.getSender(), false);
							} else if (cont.startsWith("!khmaj ")) {
								String query = cont.replace("!khmaj", "").trim();

								khmaj(query, msg.getSender());
							} else if (cont.equals("!faces")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									new Thread() {
										@Override
										public void run() {
											try {
												List<File> faces = Ai
														.detectFace(ImageIO.read(new URL(msg.getReplyToWhat())));
												gameDriverAction(() -> {
													sendImage(faces.toArray(new File[faces.size()]));
												});
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}
							} else if (cont.equals("!fish")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									new Thread() {
										@Override
										public void run() {
											try {
												File faces = Ai.fishEye(ImageIO.read(new URL(msg.getReplyToWhat())));
												gameDriverAction(() -> sendImage(faces));
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}
							} else if (cont.startsWith("!triv")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									int gen = 500;
									try {
										gen = Integer.parseInt(cont.split(" ")[1]);
									}catch(Exception x) {
										
									}

									final int fg = gen;
									new Thread() {
										@Override
										public void run() {
											try {
												Triangulated faces = Ai.triangulateV(ImageIO.read(new URL(msg.getReplyToWhat())), fg);
												gameDriverAction(() -> sendImage(faces.getVideo(), faces.getImage()));
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}
							} else if (cont.startsWith("!tri")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									
									int gen = 500;
									try {
										gen = Integer.parseInt(cont.split(" ")[1]);
									}catch(Exception x) {
										
									}
									
									final int fg = gen;
									new Thread() {
										@Override
										public void run() {
											try {
												File faces = Ai.triangulate(ImageIO.read(new URL(msg.getReplyToWhat())), fg);
												gameDriverAction(() -> sendImage(faces));
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}
							} else if (cont.equals("!edge")) {
								if (msg.getReplyToType() == Message.MessageType.IMAGE) {
									new Thread() {
										@Override
										public void run() {
											try {
												File faces = Ai.edge(ImageIO.read(new URL(msg.getReplyToWhat())));
												gameDriverAction(() -> sendImage(faces));
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}.start();
								}
							} else if (cont.startsWith("!set pic")) {
								String url = params[params.length - 1];
								if (url.equalsIgnoreCase("default")) {
									changeGroupPic("mesa");
								} else {
									BufferedImage img = ImageIO.read(new URL(url));
									File file = saveImage(img);
									changeGroupPic(file);
								}
							} else if (cont.startsWith("!set name")) {
								String name = cont.split("!set name ")[1];
								changeGroupName(name);
							} else if (cont.startsWith("!nick")) {
								String sep = "/sep/";
								String[] parts = cont.split("!nick ")[1].trim().replace("(", sep).replace(")", "")
										.split(sep);
								String user = parts[0].replace("@", "").trim();
								String value = parts[1].trim();
								setNick(user, () -> value, SET_NICK, true, null);
							} else if (cont.startsWith("!num")) {
								String sep = "/sep/";
								String[] parts = cont.split("!num ")[1].trim().replace("(", sep).replace(")", "")
										.split(sep);
								String user = parts[0].replace("@", "").trim();
								String value = parts[1].trim();
								setNick(user, () -> value, SET_NUM, true, null);
							} else if (cont.equals("!reset nums")) {
								String data = Files.read("/order.json");
								JSONArray arr = new JSONArray(data);

								pos = 1;

								ArrayList<Integer> used = new ArrayList<>();
								for (int i = 0; i < arr.length(); i++) {
									final int fi = i;
									JSONArray subArr = arr.getJSONArray(i);
									for (int j = 0; j < subArr.length(); j++) {
										final int fj = j;
										setNick(subArr.getString(j), () -> Integer.toString(pos), SET_NUM, false, b -> {
											if (!used.contains(fi) && b.booleanValue()) {
												used.add(fi);
											}
											if (fj == subArr.length() - 1 && used.contains(fi)) {
												pos++;
											}
										});
									}
								}

								driver.findElement(By.cssSelector("*[aria-label=Nicknames] div[role]")).click();
							}
						}
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void stop() {
		running = false;
	}

	private List<Message> getLastMessages() {
		ArrayList<Message> res = new ArrayList<Message>();

		List<WebElement> rows = driver.findElements(By.cssSelector("*[role=main] *[role=row][class]"));
		if (rows.isEmpty())
			return res;

		for (int i = Math.max(0, rows.size() - 5); i < rows.size(); i++) {
			WebElement msgNode = rows.get(i);
			if (msgNode.getAttribute("checked") != null) {
				continue;
			}
			((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('checked','true')", msgNode);

			WebElement headElement = null;

			try {
				headElement = msgNode.findElement(By.cssSelector("h4"));
			} catch (Exception x) {
				// IGNORE
			}

			if (headElement == null) {
				headElement = msgNode.findElement(By.cssSelector("div > div > div > span"));
			}

			String head = headElement.getAttribute("textContent").trim();

			String replyToWho = null;
			String replyToWhat = null;
			Message.MessageType replyToType = null;

			MessageType type = MessageType.TEXT;

			String content = null;
			try {
				content = msgNode
						.findElement(By.cssSelector("*[data-testid=message-container] > div > div > div > span"))
						.getText().trim();

				if (content.isEmpty()) {
					try {
						content = msgNode
								.findElement(
										By.cssSelector("*[data-testid=message-container] > div > div > div > span img"))
								.getAttribute("src");
						type = MessageType.IMAGE;
					} catch (Exception xx) {
						content = "Unknown Type";
					}
				}
			} catch (Exception x) {
				content = "Unknown Type";
			}

			if (!headElement.findElements(By.cssSelector("svg")).isEmpty()) {
				String[] parts = head.split(" replied to ");

				head = parts[0];
				replyToWho = names.resolveName(parts[1]);

				replyToType = Message.MessageType.TEXT;
				try {
					replyToWhat = msgNode.findElement(By.cssSelector("h4 + div > div > div > div > div > div > div"))
							.getAttribute("textContent").trim();
				} catch (Exception x) {
					try {
						replyToWhat = msgNode
								.findElement(By.cssSelector("h4 + div + div > div > div > div > div + div"))
								.getAttribute("textContent").trim();
					} catch (Exception xx) {
						replyToWhat = "";
					}
				}

				if (replyToWhat.isBlank()) {
					String imgSrc = null;
					try {
						imgSrc = msgNode.findElement(By.cssSelector("h4 + div > div img")).getAttribute("src");
					} catch (Exception x) {
						try {
							imgSrc = msgNode.findElement(By.cssSelector("h4 + div + div > div img"))
									.getAttribute("src");
						} catch (Exception xx) {
							replyToType = null;
							x.printStackTrace();
						}
					}

					if (imgSrc != null) {
						replyToWhat = imgSrc;
						replyToType = Message.MessageType.IMAGE;
						try {
							imgSrc = driver.findElement(By.cssSelector("img[src*='" + imgSrc.split("\\?")[0] + "']"))
									.getAttribute("src");
							replyToWhat = imgSrc;
						} catch (Exception xx) {

						}
					}

				}
			}

			if (content.indexOf(((char) 8206) + "") == 0) {
				continue;
			}
			Message msg = new Message(type, msgNode, names.resolveName(head), content, replyToWho, replyToWhat,
					replyToType);

			res.add(msg);
		}

		return res;
	}

	private void changeGroupName(String name) {
		gameDriverAction(() -> {
			String side = "*[aria-label='Conversation information'][role=button]";
			String expand = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div div[role]";
			String changeName = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div > div + div > div";

			if (driver.findElements(By.cssSelector(changeName)).isEmpty()) {
				if (driver.findElements(By.cssSelector(expand)).isEmpty()) {
					click(driver, side);
					sleep(500);
				}

				if (driver.findElements(By.cssSelector(changeName)).isEmpty()) {
					click(driver, expand);
					sleep(500);
				}
			}

			click(driver, changeName);

			WebElement in = waitFor(driver, "*[role=dialog] input");
			in.clear();
			sendKeys(driver, in, name);

			click(driver, "*[role=dialog] *:not([aria-hidden]) > *[role=button][aria-label=Save]");
		});
	}

	private static final int SET_NICK = 1, SET_NUM = 2;

	private void setNick(String user, Supplier<String> valueSupplier, int mode, boolean close,
			Consumer<Boolean> onResult) {
		gameDriverAction(() -> {
			String value = valueSupplier.get();
			System.out.println(user + " : " + value);
			String side = "*[aria-label='Conversation information'][role=button]";
			String expand = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div div[role]";
			String editNicks = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div > div + div > div +div + div + div";
			String getRows = "*[aria-label=Nicknames] div + div + div > div > div > div > div > div[style]";

			BooleanSupplier preCondition = () -> !driver.findElements(By.cssSelector(getRows)).isEmpty();

			if (!preCondition.getAsBoolean()) {
				if (driver.findElements(By.cssSelector(editNicks)).isEmpty()) {
					if (driver.findElements(By.cssSelector(expand)).isEmpty()) {
						click(driver, side);
						waitUntil(() -> !driver.findElements(By.cssSelector(expand)).isEmpty());
					}

					if (driver.findElements(By.cssSelector(editNicks)).isEmpty()) {
						click(driver, expand);
						waitUntil(() -> !driver.findElements(By.cssSelector(editNicks)).isEmpty());
					}

				}

				click(driver, editNicks);
				waitUntil(preCondition);
			}

			List<WebElement> rows = driver.findElements(By.cssSelector(getRows));

			for (WebElement e : rows) {
				try {
					String real = e
							.findElement(By.cssSelector("div[role=button] div + div > div > div > div > div + div"))
							.getText().trim();

					int index = rows.indexOf(e);
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

					if (real.equalsIgnoreCase(user)) {
						String sep = "/sep/";
						String[] parts = nick.replace("(", sep).replace(")", "").split(sep);

						String oldNick = parts[0];
						int number = parts.length == 2 ? Integer.parseInt(parts[1]) : -1;

						String newNick = null;
						switch (mode) {
						case SET_NICK: {
							newNick = value + " (" + number + ")";
							break;
						}
						case SET_NUM: {
							newNick = oldNick.trim() + " (" + value + ")";
							break;
						}
						default:
							throw new IllegalArgumentException("Unexpected value: " + mode);
						}

						if (!nick.equals(newNick)) {
							e.findElement(By.cssSelector("div[role]")).click();

							WebElement row = driver.findElements(By.cssSelector(getRows)).get(index);

							WebElement input = row.findElement(By.tagName("input"));

							input.clear();

							sendKeysSeq(newNick, input);

							row.findElement(By.cssSelector("div[role]")).click();
						}

						if (close)
							driver.findElement(By.cssSelector("*[aria-label=Nicknames] div[role]")).click();

						if (onResult != null) {
							onResult.accept(true);
						}
						return;
					}

				} catch (Exception x) {
					x.printStackTrace();

					if (onResult != null) {
						onResult.accept(false);
					}
				}
			}

			if (close)
				driver.findElement(By.cssSelector("*[aria-label=Nicknames] div[role]")).click();

			if (onResult != null) {
				onResult.accept(false);
			}
		}, true);
	}

	private void changeGroupPic(String name) {
		try {
			File file = new File(
					URLDecoder.decode(getClass().getResource("/images/" + name + ".png").getFile(), "utf-8"));

			changeGroupPic(file);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void changeGroupPic(File file) {
		gameDriverAction(() -> {
			String side = "*[aria-label='Conversation information'][role=button]";
			String expand = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div div[role]";
			String input = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div > div + div > div + input";

			if (driver.findElements(By.cssSelector(input)).isEmpty()) {
				if (driver.findElements(By.cssSelector(expand)).isEmpty()) {
					click(driver, side);
					sleep(500);
				}

				if (driver.findElements(By.cssSelector(input)).isEmpty()) {
					click(driver, expand);
					sleep(500);
				}

			}

			WebElement in = driver.findElement(By.cssSelector(input));
			in.sendKeys(file.getAbsolutePath());
		});
	}

	private void fuck(String name) {
		gameDriverAction(() -> {
			String side = "*[aria-label='Conversation information'][role=button]";
			String expand = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div + div + div > div > div";
			String members = "[role=main] > div > div > div > div + div > div > div > div > div > div > div > div + div > div > div + div + div > div > div:not([role])";

			BooleanSupplier predic1 = () -> driver.findElements(By.cssSelector(expand)).isEmpty();
			BooleanSupplier predic2 = () -> driver.findElement(By.cssSelector(expand))
					.getAttribute("aria-expanded").equals("false");
			BooleanSupplier predic3 = () -> driver.findElements(By.cssSelector(members)).isEmpty();

			if (predic1.getAsBoolean()) {
				click(driver, side);
				waitWhile(predic1);
			}

			if (predic2.getAsBoolean()) {
				click(driver, expand);
				waitWhile(predic2);
			}

			waitWhile(predic3);

			for (WebElement member : driver.findElements(By.cssSelector(members))) {
				String memName = member.findElement(By.cssSelector("div + div span")).getText();

				if (memName.trim().equals(name.trim())) {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView()", member);
					sleep(100);

					member.findElement(By.cssSelector("div + div + div div[role=button]")).click();
					sleep(100);

					BooleanSupplier predic4 = () -> driver
							.findElements(By.cssSelector("*[role=menu] *[role=menuitem]")).isEmpty();
					waitWhile(predic4);

					for (WebElement item : driver.findElements(By.cssSelector("*[role=menu] *[role=menuitem]"))) {
						String txt = item.getText().toLowerCase();
						if (txt.contains("remove") && !txt.contains("admin")) {
							item.click();
							sleep(100);

							click(driver,
									"[role=dialog] *:not([aria-hidden]) > [role=button][aria-label*=Remove]");

							break;
						}
					}

					break;
				}
			}
		});
	}

	private long cooldown = 60000;

	private void yt(String query, String sender, boolean video) {
		media(query, sender, video, true);
	}

	private void khmaj(String query, String sender) {
		media(query, sender, true, false);
	}
	
	private void media(String query, String sender, boolean video, boolean youtube) {
		long now = System.currentTimeMillis();

		long last = lastRequest.containsKey(sender) ? lastRequest.get(sender) : 0;
		if (now - last < cooldown) {
			gameDriverAction(() -> sendMessage(
					"arbat 9a3ek a [" + sender + "], try again in " + (cooldown - (now - last)) / 1000 + " seconds"));
		} else {
			lastRequest.put(sender, now);

			new Thread() {
				@Override
				public void run() {
					try {
						gameDriverAction(() -> sendMessage(
								"hold tight [" + sender + "], your request is being taken care of..."));
						File f;
						if(youtube) {
							f = Media.yt(query, video);
						}else {
							f = Adult.adult(query);
						}
						
						if (f == null) {
							gameDriverAction(
									() -> sendMessage("The video/audio [" + sender + "] requested can't be served"));
							lastRequest.put(sender, 0L);
						} else {
							gameDriverAction(() -> sendImage(f));
						}
					} catch (Exception x) {
						gameDriverAction(
								() -> sendMessage("The video/audio [" + sender + "] requested can't be served"));
						lastRequest.put(sender, 0L);
					}
				}
			}.start();
		}
	}

	private Semaphore mutex = new Semaphore(1);

	private void gameDriverAction(Runnable run) {
		gameDriverAction(run, false);
	}

	private void gameDriverAction(Runnable run, boolean waitFor) {
		Thread th = new Thread() {
			@Override
			public void run() {
				mutex.acquireUninterruptibly();
				try {
					run.run();
				} catch (Exception x) {
					x.printStackTrace();
				}
				mutex.release();
			}
		};

		th.start();

		if (waitFor) {
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	protected void sendMessage(String message) {
		sendKeys(message.replace("\n", Keys.chord(Keys.SHIFT, Keys.ENTER)), "div[role='textbox']");
		driver.findElement(By.cssSelector(
				"*[role=main] > div > div > div > div > div + div > div > div > div + div > div > div + span > div"))
				.click();
	}

	protected void sendImage(File... imgs) {
		for (File img : imgs) {
			driver.findElement(By.cssSelector("div:nth-child(2) input:nth-child(1)")).sendKeys(img.getAbsolutePath());
		}
		sendMessage(" ");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private void sendKeys(String message, String selector) {
		WebElement element = driver.findElement(By.cssSelector(selector));

		element.clear();

		for (char c : message.toCharArray()) {
			element.sendKeys(Character.toString(c));
		}
	}

	private void waitUntil(BooleanSupplier predicate) {
		while (!predicate.getAsBoolean()) {
			sleep(100);
		}
	}

	private void waitWhile(BooleanSupplier predicate) {
		while (predicate.getAsBoolean()) {
			sleep(100);
		}
	}

	private void sendKeysSeq(String message, WebElement element) {
		element.sendKeys(message);
	}

	public static File saveImage(BufferedImage image) {
		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		img.getGraphics().drawImage(image, 0, 0, null);
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(0.95f);
		try {
			File res = new File("temp" + (int) (Math.random() * 1000) + ".jpg");
			ImageOutputStream outputStream = new FileImageOutputStream(res);
			jpgWriter.setOutput(outputStream);
			IIOImage outputImage = new IIOImage(img, null, null);
			jpgWriter.write(null, outputImage, jpgWriteParam);
			jpgWriter.dispose();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
