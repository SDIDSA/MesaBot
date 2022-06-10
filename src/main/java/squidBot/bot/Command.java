package squidBot.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Command {
	private String[] command;

	private LineHandler inputHandler;
	private LineHandler errorHandler;

	public Command(LineHandler inputHandler, LineHandler errorHandler, String... command) {
		this.command = command;
		this.inputHandler = inputHandler;
		this.errorHandler = errorHandler;
	}

	public Command(LineHandler inputHandler, String... command) {
		this(inputHandler, inputHandler, command);
	}

	public Command(String... command) {
		this(null, null, command);
	}

	private Process p;
	public Process execute(File root, Runnable...periodicals) {
		System.out.println(Arrays.toString(command));
		try {
			p = Runtime.getRuntime().exec(command, null, root);

			new Thread(() -> {
				InputStream is = p.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				try {
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						if (inputHandler != null && !inputHandler.accept(line)) {
							p.children().forEach(ProcessHandle::destroyForcibly);
							p.destroyForcibly();
							Thread.currentThread().interrupt();
							return;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

			new Thread(() -> {
				InputStream is = p.getErrorStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				try {
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						if (errorHandler != null && !errorHandler.accept(line)) {
							p.children().forEach(ProcessHandle::destroyForcibly);
							p.destroyForcibly();
							Thread.currentThread().interrupt();
							return;
						}
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
			
			new Thread(()-> {
				while(p.isAlive()) {
					for(Runnable periodical : periodicals) {
						periodical.run();
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
				}
				
			}).start();
			return p;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
}
