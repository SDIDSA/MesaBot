package squidBot.bot.games;

import squidBot.bot.data.Message;

public abstract class Game {
	//private String player1;
	//private String player2;
	
	public abstract boolean handle(Message msg);
	public abstract String describe();
}
