package org.trackerlounge;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

public class Main {

	public static String getCurrentPath() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);
		return s;
	}

	public static String getResourcePath() {
		String s = getCurrentPath();
		String path = s + "\\resources\\";
		return path;
	}

	public static Mat loadImage(String file) {
		Imgcodecs imageCodecs = new Imgcodecs();
		Mat image = imageCodecs.imread(file);
		return image;
	}

	public static Mat loadGrayscaleImage(String file) {
		Mat image = Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE);
		return image;
	}

	public static void showImageType(Mat src) {
		// https://stackoverflow.com/questions/10167534/how-to-find-out-what-type-of-a-mat-object-is-with-mattype-in-opencv
		System.out.println("Image Type: " + src.type() + " and channels:" + src.channels());
		/*
		 * +--------+----+----+----+----+------+------+------+------+ | | C1 | C2 | C3 |
		 * C4 | C(5) | C(6) | C(7) | C(8) |
		 * +--------+----+----+----+----+------+------+------+------+ | CV_8U | 0 | 8 |
		 * 16 | 24 | 32 | 40 | 48 | 56 | | CV_8S | 1 | 9 | 17 | 25 | 33 | 41 | 49 | 57 |
		 * | CV_16U | 2 | 10 | 18 | 26 | 34 | 42 | 50 | 58 | | CV_16S | 3 | 11 | 19 | 27
		 * | 35 | 43 | 51 | 59 | | CV_32S | 4 | 12 | 20 | 28 | 36 | 44 | 52 | 60 | |
		 * CV_32F | 5 | 13 | 21 | 29 | 37 | 45 | 53 | 61 | | CV_64F | 6 | 14 | 22 | 30 |
		 * 38 | 46 | 54 | 62 |
		 * +--------+----+----+----+----+------+------+------+------+
		 */
	}

	//https://www.geeksforgeeks.org/program-to-find-whether-a-no-is-power-of-two/
	static boolean isPowerOfTwo(int n) {
		if (n == 0)
			return false;

		return (int) (Math.ceil((Math.log(n) / Math.log(2)))) == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
	}

	static int getClosestPowerOfTwo(int val) {
		if (!isPowerOfTwo(val)) {
			int base = 2;
			int pow = 1;
			double temp = Math.pow(base, pow);
			while (val > temp) {
				pow++;
				temp = Math.pow(base, pow);
			}
			val = (int)temp;
		}
		return val;
	}
	
	/*
	 * HOGs are made of 8x8 pixel blocks that are then combined into 16x16 blocks.
	 * At the edge, if we don't add padding, part of a block would hang off the edge
	 * and result in null point exceptions.
	 */
	public static Mat enlargeImageBoader(Mat src) {
		int winSize = 16;// 16
		int w = src.width() % winSize;
		int h = src.height() % winSize;
		if (w > 0 || h > 0) {
			System.out.println("Orig width: " + src.width() + " and height: " + src.height());
			System.out.println("Enlarge width by: " + (winSize - w) + " and height by: " + (winSize - h));
			int newWidth = src.width() + (winSize - w);
			int newHeigth = src.height() + (winSize - h);
			newWidth = getClosestPowerOfTwo(newWidth);
			newHeigth = getClosestPowerOfTwo(newHeigth);
			Mat dest = new Mat(new Size(newWidth, newHeigth), src.type(), new Scalar(0, 0, 0));
			int rowStart = 0;
			int rowEnd = src.rows();
			int colStart = 0;
			int colEnd = src.cols();
			src.copyTo(dest.submat(rowStart, rowEnd, colStart, colEnd));
			System.out.println("Dest width: " + dest.width() + " and height: " + dest.height());
			return dest;
		}
		return src;

	}

	/*
	 * https://javadoc.io/doc/org.bytedeco.javacpp-presets/opencv/latest/org/
	 * bytedeco/javacpp/opencv_objdetect.HOGDescriptor.html
	 * https://stackoverflow.com/questions/23967297/opencv-hogdescriptor-returning-
	 * wrong-result-in-static-image
	 * https://www.javatips.net/api/org.opencv.objdetect.hogdescriptor
	 * http://www.java2s.com/Open-Source/Android_Free_Code/Development/opencv/
	 * org_opencv_objdetectHOGDescriptor_java.htm
	 * https://software.intel.com/content/www/us/en/develop/documentation/ipp-dev-
	 * reference/top/volume-2-image-processing/computer-vision/feature-detection-
	 * functions/histogram-of-oriented-gradients-hog-descriptor/hog.html
	 * http://www.learnopencv.com/histogram-of-oriented-gradients
	 * 
	 * https://www.learnopencv.com/handwritten-digits-classification-an-opencv-c-
	 * python-tutorial/
	 * 
	 * https://www.pyimagesearch.com/2015/11/16/hog-detectmultiscale-parameters-explained/
	 * https://answers.opencv.org/question/95042/hog-detectmultiscale-weight-scale-explanation/?sort=votes
	 * https://www.pyimagesearch.com/2015/02/16/faster-non-maximum-suppression-python/
	 */

	// https://www.learnopencv.com/handwritten-digits-classification-an-opencv-c-python-tutorial/
	// https://stackoverflow.com/questions/27343614/opencv-hogdescriptor-compute-error
	public static HOGDescriptor createHOGDescriptor() {
		Size winSize = new Size(32, 32);//new Size(64, 64);//new Size(20, 20);
		Size blockSize = new Size(16, 16); //new Size(10, 10);
		Size blockStride = new Size(8, 8);//new Size(8, 8); // new Size(5, 5);
		Size cellSize = new Size(8, 8); // new Size(10, 10);
		int nbins = 9;
		int derivAperture = 1;
		double winSigma = -1.0;
		int histogramNormType = 0;
		double L2HysThreshold = 0.2;
		boolean gammaCorrection = true;
		int nlevels = 64;
		boolean useSignedGradients = true;
		HOGDescriptor mHOGDescriptor = new HOGDescriptor(winSize, blockSize, blockStride, cellSize, nbins,
				derivAperture, winSigma, histogramNormType, L2HysThreshold, gammaCorrection, nlevels,
				useSignedGradients);

		return mHOGDescriptor;
	}

	public static MatOfFloat getHogDescriptor(Mat img) {
		HOGDescriptor mHOGDescriptor = createHOGDescriptor();
		MatOfFloat descriptors = new MatOfFloat(0f, 256f);
		System.out
				.println("Descriptor width: " + descriptors.size().width + " -- height: " + descriptors.size().height);
		mHOGDescriptor.compute(img, descriptors);
		System.out
				.println("Descriptor width: " + descriptors.size().width + " -- height: " + descriptors.size().height);
		return descriptors;
	}

	public static void doHog(Mat img, MatOfFloat descriptors) {
		HOGDescriptor mHOGDescriptor = createHOGDescriptor();// = new HOGDescriptor();
		System.out
				.println("Descriptor width: " + descriptors.size().width + " -- height: " + descriptors.size().height);
		mHOGDescriptor.setSVMDetector(descriptors);

		/*
		final MatOfPoint foundLocations = new MatOfPoint();
		final MatOfDouble foundWeights = new MatOfDouble();
		final Size winStride = mHOGDescriptor.get_blockStride();//new Size(8, 8);
		final Size padding = new Size(32, 32);//new Size(32, 32);

//		mHOGDescriptor.detect(img, foundLocations, foundWeights, 100.0, winStride, padding);
//		mHOGDescriptor.detect(img, foundLocations, foundWeights, 165.0, winStride, padding);
		mHOGDescriptor.detect(img, foundLocations, foundWeights, 0.0, winStride, padding);
		Point[] array = foundLocations.toArray();
		double[] weights = foundWeights.toArray();
		int maxIndex = -1;
		double maxWeight = -1;
		double maxX=-1;
		double maxY=-1;
		for (int j = 0; j < array.length; j++) {
			Point rect = array[j];
			double weight = weights[j];
			if(weight>maxWeight) {
				maxWeight = weight;
				maxX = rect.x;
				maxY = rect.y;
				maxIndex = j;
			}
			System.out.println("X: "+rect.x+" -- Y: "+rect.y+" -- Height " + 64 + ", Width " + 64+" --- weight: "+weight);
//			Imgproc.rectangle(img, new Point(rect.x, rect.y),
//					new Point(rect.x + 64, rect.y + 64), new Scalar(0, 255, 0));
			//If we are in grayscale so Scalar only  response to change in the first value in  new Scalar(255, 255, 255)
		}
		System.out.println("Max index: "+maxIndex+ " -- x: "+maxX+" -- y: "+maxY+" -- weight: "+maxWeight);
		Point rect = array[maxIndex];
		Imgproc.rectangle(img, new Point(rect.x, rect.y),
				new Point(rect.x + 64, rect.y + 64), new Scalar(0, 255, 0));
				*/

		
		final MatOfRect foundLocations = new MatOfRect();
		final MatOfDouble foundWeights = new MatOfDouble();
		final Size winStride = mHOGDescriptor.get_blockStride();//new Size(8, 8);
		final Size padding = new Size(32, 32);//new Size(32, 32);
		
		
		
		



mHOGDescriptor.detectMultiScale(img, foundLocations, foundWeights, 0.7, winStride, padding, 1.05, 2.0, true);//0.7
		
		
		
		
		
//		mHOGDescriptor.detectMultiScale(img, foundLocations, foundWeights, 0.0, winStride, padding);
//		mHOGDescriptor.detectMultiScale(img, foundLocations, foundWeights, 1.0, winStride, padding, 1.0, 31.0, true);//Best one so far for Size blockStride = new Size(8, 8);
//		mHOGDescriptor.detectMultiScale(img, foundLocations, foundWeights, 0.0, winStride, padding, 1.05, 2.0, false);
		Rect[] array = foundLocations.toArray();
		double[] weights = foundWeights.toArray();
		int maxIndex = -1;
		double maxWeight = -1;
		int maxX=-1;
		int maxY=-1;
		for (int j = 0; j < array.length; j++) {
			Rect rect = array[j];
			double weight = weights[j];
			if(weight>maxWeight) {
				maxWeight = weight;
				maxX = rect.x;
				maxY = rect.y;
				maxIndex = j;
			}
			System.out.println("Count: "+j+" -- X: "+rect.x+" -- Y: "+rect.y+" -- Height " + rect.height + ", Width " + rect.width+" --- weight: "+weight);
//			Imgproc.rectangle(img, new Point(rect.x, rect.y),
//					new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
			//If we are in grayscale so Scalar only  response to change in the first value in  new Scalar(255, 255, 255)
		}
		System.out.println("Max index: "+maxIndex+ " -- x: "+maxX+" -- y: "+maxY+" -- weight: "+maxWeight);
		Rect rect = array[maxIndex];
		Imgproc.rectangle(img, new Point(rect.x, rect.y),
				new Point(rect.x + 64, rect.y + 64), new Scalar(0, 255, 0));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("HOG Compare");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		String filename1 = getResourcePath() + "testHorizontal20x20.png";
//		String filename1 = getResourcePath() + "testPatch64x64.png";
		String filename1 = getResourcePath() + "track_tread_patch_32_for_200pixelImage.png";
//		String filename1 = getResourcePath() + "track_tread_patch_64_for_200pixelImage.png";
//		String filename1 = getResourcePath() + "RF_Merrill_WetSand_VerySlow_Colored_Z_axis_small.jpg";
//		String filename1 = getResourcePath() + "my_merrill_small.JPG";
//		String filename1 = getResourcePath() + "my_merrill_small_edge.jpg";
//		String filename1 = getResourcePath() + "merrill_shiver_moc_shoe.jpg";
//		String filename2 = getResourcePath() + "testPatch512x512.png";
//		String filename2 = getResourcePath() + "testPatch512x512_8Cases.png";
//		String filename2 = getResourcePath() + "RF_Merrill_WetSand_VerySlow_Colored_Z_axis_small_200.jpg";
//		String filename2 = getResourcePath() + "my_merrill_small_200.PNG";
//		String filename2 = getResourcePath() + "merrill_shiver_moc_shoe_200.png";
//		String filename2 = getResourcePath() + "LF_20in_Stride_Wet_Sand_Binary_Small_200.jpg";
//		String filename2 = getResourcePath() + "LF_crock_200.jpg";
//		String filename2 = getResourcePath() + "original_raw_track_small_200.png";
		String filename2 = getResourcePath() + "convers_shoe_sole_200.png";
		Mat src = Main.loadImage(filename1);
		Mat imageToSearch = Main.loadImage(filename2);
//		src = enlargeImageBoader(src);
//		imageToSearch = enlargeImageBoader(imageToSearch);

		MatOfFloat descriptors = getHogDescriptor(src);
		doHog(imageToSearch, descriptors);
		
		Mat result = imageToSearch.clone();
		
		HighGui.imshow("Result", result);
		HighGui.imshow("template", src);
		HighGui.waitKey(0);
		System.exit(0);
	}

}
