package ai;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class FaceDetector {
	private FaceDetector() {}
	
	private static CascadeClassifier cfccc;

	static {
		try {
			File cpuFaceDetectionData = new File(URLDecoder.decode(FaceDetector.class.getResource("/cpu_face_detect.xml").getFile(), "utf-8"));
			cfccc = new CascadeClassifier(cpuFaceDetectionData.getAbsolutePath());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static RectVector detect(Mat input) {
		Mat gray = new Mat();
		opencv_imgproc.cvtColor(input, gray, opencv_imgproc.COLOR_BGR2GRAY);
		RectVector frects = new RectVector();
		cfccc.detectMultiScale(gray, frects);
		return frects;
	}
}
