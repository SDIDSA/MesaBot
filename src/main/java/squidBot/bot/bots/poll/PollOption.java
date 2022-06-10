package squidBot.bot.bots.poll;

import java.util.ArrayList;

public class PollOption {
	private int id;
	private String value;
	private ArrayList<String> votes;

	public PollOption(int id, String value) {
		this.id = id;
		this.value = value;
		votes = new ArrayList<String>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ArrayList<String> getVotes() {
		return votes;
	}

	@Override
	public String toString() {
		return "PollOption [id=" + id + ", value=" + value + ", votes=" + votes + "]";
	}

}
