package org.trackerlounge;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.opencv.core.Mat;

public class HogInOpenCV {
	public static void main(String[] args) {
		System.out.println("Calculate Hog Descriptor In OpenCV");
		
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);
		String path = s+"\\resources\\";

		ImageUtil cv = new ImageUtil(path);
		HogTrackingXY hxy = new HogTrackingXY(cv);
//		String fileName = "LF_20in_Stride_Wet_Sand_ColoredZAxis_Small.jpg";
		String fileName = "LF_20in_Stride_Wet_Sand_Binary_Small.jpg";
//		String fileName = "uniformWhiteToBlackDown24x24.png";
//		String fileName = "whiteToBlackDiagonal24x24.png";
//		String fileName = "testpatchHorizontal16x16.png";
//		String fileName = "testpatchVertical16x16.png";
//		String fileName = "testpatchDiagonal16x16.png";
//		String fileName = "testpatchReverseDiagonal16x16.png";
		
		
//		String fileName = "small-hog.jpg";
//		String fileName = "small-hog24x24.jpg";
//		String fileName = "small-hog26x26.jpg";
//		String fileName = "small-hog24x32.jpg";
		
		Mat image = cv.loadImage(fileName);
		cv.displayImage(image, fileName);
		hxy.calculateHog(image);
	}
}
