package squidBot.bot.bots;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.openqa.selenium.WebDriver;

import squidBot.bot.bots.poll.Poll;
import squidBot.bot.bots.poll.PollViz;
import squidBot.bot.data.Message;

public class PollBot extends MessageConsumer {
	private ArrayList<Poll> polls;
	
	private Poll poll = null;
	private String msg = "";

	public PollBot(WebDriver mDriver) {
		super(mDriver, 0);
		polls = new ArrayList<Poll>();
	}

	@Override
	public void postconsume(Message message) {
		System.out.println(message.getContent());
		String[] parts = message.getContent().split("_");
		System.out.println(Arrays.toString(parts));
		if (parts[0].equals("!poll")) {
			msg = null;
			poll = null;
			if (parts[1].equals("create")) {
				poll = parse(parts, message);
				msg = message.getSender() + " created a Poll - id : " + poll.getId();
				polls.add(poll);
			} else if (parts[1].equals("vote")) {
				int pollId = Integer.parseInt(parts[2]);
				int optionId = Integer.parseInt(parts[3]);

				polls.forEach(p -> {
					if (p.getId() == pollId) {
						poll = p;
						msg = message.getSender() + " voted";
						p.getOptions().forEach(o -> {
							o.getVotes().remove(message.getSender());
							if (o.getId() == optionId) {
								msg += " for " + o.getValue();
								o.getVotes().add(message.getSender());
							}
						});
					}
				});
			}else if(parts[1].equals("list")) {
				polls.forEach(p -> {
					System.out.println(p.getId() + " : " + p.getTitle());
				});
			}else if(parts[1].equals("results")) {
				int pollId = Integer.parseInt(parts[2]);
				polls.forEach(p -> {
					if(p.getId() == pollId) {
						poll = p;
						p.getOptions().forEach(o -> {
							System.out.println("\t" + o.getId() + " : " + o.getValue() + " - " + o.getVotes().size());
						});
					}
				});
			}else if(parts[1].equals("reset")) {
				int pollId = Integer.parseInt(parts[2]);
				polls.forEach(p -> {
					if(p.getId() == pollId) {
						poll = p;
						if(message.getSender().equals(p.getSource().getSender())) {
							p.getOptions().forEach(o -> {
								o.getVotes().clear();
							});
						}else {
							sendMessage("only " + p.getSource().getSender() + " can reset this poll");
						}
					}
				});
			}
			
			new Thread() {
				public void run() {
					try {
						File img = saveImage(PollViz.visualize(poll, msg));
						sendImage(img);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public static Poll parse(String[] parts, Message msg) {
		String title = parts[2];
		Poll p = new Poll(msg, title);

		for (int i = 3; i < parts.length; i++) {
			p.addOption(parts[i]);
		}

		return p;
	}

}
