package squidBot.bot.data;

//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//
//import javax.imageio.ImageIO;

import org.openqa.selenium.WebElement;

public class Message {
	private MessageType type;
	private WebElement element;
	private String sender;
	private String content;
	private String replyToWho;
	private String replyToWhat;
	private MessageType replyToType;

	public Message(MessageType type, WebElement element, String sender, String content, String replyToWho,
			String replyToWhat, MessageType replyToType) {
		this.type = type;
		this.element = element;
		this.sender = sender;
		this.content = content;
		this.replyToWho = replyToWho != null ? replyToWho.replace("$recycle", sender) : null;
		this.replyToWhat = replyToWhat;
		this.replyToType = replyToType;
	}
	
	public MessageType getType() {
		return type;
	}

	public String getSender() {
		return sender;
	}

	public String getContent() {
		return content;
	}

	public String getReplyToWho() {
		return replyToWho;
	}

	public String getReplyToWhat() {
		return replyToWhat;
	}
	
	public MessageType getReplyToType() {
		return replyToType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Message) {
			Message msg = (Message) obj;
			if (msg.element.equals(element)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Message {\n\ttype : " + type.name() + "\n\tsender : " + sender + "\n\tcontent : " + content + "\n\treplyToWho : " + replyToWho
				+ "\n\treplyToWhat : " + replyToWhat + "\n}";
	}

	public static enum MessageType {
		IMAGE(), TEXT();
	}
}
