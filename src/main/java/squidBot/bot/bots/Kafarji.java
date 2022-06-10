package squidBot.bot.bots;

import java.util.ArrayList;
import java.util.Collections;

import org.openqa.selenium.WebDriver;

import squidBot.bot.data.Message;

public class Kafarji extends MessageConsumer {

	private boolean active = false;

	private ArrayList<String> tya7a;
	private ArrayList<String> templates;

	public Kafarji(WebDriver mDriver) {
		super(mDriver, 2000);
		tya7a = new ArrayList<>();
		tya7a.add("nik mok");
		tya7a.add("na3tizomk");
		tya7a.add("nik babak l3attay");
		tya7a.add("ak 9a7ba");
		tya7a.add("ak ta3ti");
		tya7a.add("a7kemni men zebbi");
		tya7a.add("boushouli");
		tya7a.add("nik grabe3 malik");
		tya7a.add("nik rabbak");
		tya7a.add("a3tini mok nikha");
		tya7a.add("zokek nel7ashoulek");
		tya7a.add("t3addo");
		tya7a.add("ro7 ta3ti b3id");
		tya7a.add("wchbih rabbak");
		tya7a.add("wchbiha ssouwa ta3 mok");
		tya7a.add("wchbiha khtek");
		tya7a.add("al3abli bih");
		tya7a.add("nik tiz malik");
		tya7a.add("osket nik rbk");
		tya7a.add("a9fel 9a3ek");

		templates = new ArrayList<>();
		templates.add("$cuss a $name");
		templates.add("$name, $cuss");
	}

	@Override
	public void postconsume(Message message) {
		if (message.getContent().equals("!kafarji join")) {
			active = true;
			sendMessage("Kafarji joined the chat");
		}
		if (message.getContent().equals("!kafarji leave")) {
			active = false;
			sendMessage("Kafarji left the chat");
		}

		if (!active) {
			return;
		}
		
		Collections.shuffle(templates);
		Collections.shuffle(tya7a);
		sendMessage(
				templates.get(0)
					.replace("$name", message.getSender().split(" ")[0])
					.replace("$cuss", tya7a.get(0)));
	}

}
