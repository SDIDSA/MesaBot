package squidBot.bot.bots.poll;

import java.util.ArrayList;
import java.util.List;

import squidBot.bot.data.Message;

public class Poll {
	private static int autoInc = 1;

	private int id;
	private int optionId = 1;
	private Message source;
	private String title;
	private ArrayList<PollOption> options;

	public Poll(Message source, String title) {
		id = autoInc++;
		this.source = source;
		this.title = title;
		options = new ArrayList<>();
	}

	public void addOption(String val) {
		options.add(new PollOption(optionId++, val));
	}

	public int getId() {
		return id;
	}

	public Message getSource() {
		return source;
	}

	public String getTitle() {
		return title;
	}

	public List<PollOption> getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return "Poll [id=" + id + ", source=" + source + ", title=" + title + ", options=" + options + "]";
	}
}
