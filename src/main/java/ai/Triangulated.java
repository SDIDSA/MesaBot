package ai;

import java.io.File;

public class Triangulated {
	private File video;
	private File image;
	public Triangulated(File video, File image) {
		this.video = video;
		this.image = image;
	}
	
	public File getVideo() {
		return video;
	}
	
	public File getImage() {
		return image;
	}
}
