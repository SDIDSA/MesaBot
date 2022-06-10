package ai;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class Video {
	public static File encode(List<File> frames) throws IOException {
		File dest = File.createTempFile("triVid_", ".mp4");

		BufferedImage sample = ImageIO.read(frames.get(0));

		final IMediaWriter writer = ToolFactory.makeWriter(dest.getAbsolutePath());

		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, sample.getWidth(), sample.getHeight());

		writer.encodeVideo(0, sample, 0, TimeUnit.MILLISECONDS);
		for (int i = 1; i < frames.size(); i++) {
			try {
				BufferedImage screen = ImageIO.read(frames.get(i));
				writer.encodeVideo(0, screen, 50l * i + 300, TimeUnit.MILLISECONDS);
			}catch(IIOException x) {
				System.out.println("failed to encode frame " + i);
			}

		}
		writer.close();

		return dest;
	}
}
