package content;

import java.util.ArrayList;
import java.util.Iterator;

public class LinedText implements Iterable<Line>{
	private ArrayList<Line> lines;
	
	public LinedText(String input) {
		lines = new ArrayList<>();
		
		StringBuilder builder = new StringBuilder();
		
		boolean akTsal = false;
		
		for (int i = 0; i < input.length(); i++) {
			builder.append(input.charAt(i));
			
			if (builder.length() == 40) {
				akTsal = true;
			}
			
			if(input.charAt(i) == ('\n')) {
				lines.add(new Line(builder.toString()));
				builder = new StringBuilder();
				akTsal = false;
			}

			if (akTsal) {
				if (input.charAt(i) == ' ') {
					lines.add(new Line(builder.toString()));
					builder = new StringBuilder();
					akTsal = false;
				}
			}
		}

		if (builder.length() != 0) {
			lines.add(new Line(builder.toString()));
		}
	}
	
	public int size() {
		return lines.size();
	}
	
	public Line get(int i) {
		return lines.get(i);
	}

	@Override
	public Iterator<Line> iterator() {
		return lines.iterator();
	}
}
