package org.trackerlounge;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageUtil {

	Imgcodecs imageCodecs;
	String path;

	public ImageUtil(String path) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		imageCodecs = new Imgcodecs();
		this.path = path;
	}

	public void displayImage(Mat image, String title) {
//		HighGui.imshow(title, image);
		try {
			// Encoding the image
			MatOfByte matOfByte = new MatOfByte();
			Imgcodecs.imencode(".jpg", image, matOfByte);

			// Storing the encoded Mat in a byte array
			byte[] byteArray = matOfByte.toArray();

			// Preparing the Buffered Image
			InputStream in = new ByteArrayInputStream(byteArray);
			BufferedImage bufImage = ImageIO.read(in);

			// Instantiate JFrame
			JFrame frame = new JFrame(title);

			// Set Content to the JFrame
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
			frame.pack();
			frame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Mat loadImage(String fileName) {
		String file = path + fileName;
		Mat matrix = imageCodecs.imread(file);
		System.out.println("Image Loaded from " + file);
		return matrix;
	}
	
	public void saveImage(String fileName, Mat matrix) {
		String file = path + fileName;
		imageCodecs.imwrite(file, matrix);
		System.out.println("Image saved: "+fileName);
	}

	public Mat convertImageToGrayscale(Mat src) {
		// Creating the empty destination matrix
		Mat dst = new Mat();

		// Converting the image to gray scale and saving it in the dst matrix
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
		return dst;
	}

	public Mat thresholdImage(Mat src) {
		// Creating the destination matrix
		Mat dst = new Mat();

		// Converting to binary image...
		double threshold = 120;// An integer representing the threshold value.
		double maxval = 700; // An integer representing the maximum value to use with the THRESH_BINARY and
								// THRESH_BINARY_INV thresholding types.
		Imgproc.threshold(src, dst, threshold, maxval, Imgproc.THRESH_BINARY);
		return dst;
	}

	public Mat drawBox(Mat image, int x1, int y1, int x2, int y2) {
		image = this.drawBox(image, new Point(x1, y1), new Point(x2, y2));
		return image;
	}

	public Mat drawBox(Mat image, Point p1, Point p2) {
		Imgproc.rectangle(image, // Matrix obj of the image
				p1, // new Point(130, 50), //p1
				p2, // new Point(300, 280), //p2
				new Scalar(0, 0, 255), // Scalar object for color
				1 // Thickness of the line
		);
		return image;
	}

	public Mat drawLine(Mat image, Pair<Point, Point> line) {
		return drawLine(image, line.a, line.b);
	}
	
	// https://www.tutorialspoint.com/opencv/opencv_drawing_line.htm
	public Mat drawLine(Mat image, Point p1, Point p2) {
		Imgproc.line(image, p1, p2, new Scalar(0, 0, 255), // Scalar object for color
				1 // Thickness of the line
		);
		return image;
	}

	public Mat cannyEdgeDetection(Mat src) {
		// Creating an empty matrix to store the result
		Mat gray = new Mat();

		// Converting the image from color to Gray
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
		Mat edges = new Mat();

		// Detecting the edges
		Imgproc.Canny(gray, edges, 60, 60 * 3);
		return edges;
	}

	public Mat scharr(Mat src) {
		// Creating an empty matrix to store the result
		Mat dst = new Mat();
		// Applying Box Filter effect on the Image
		Imgproc.Scharr(src, dst, Imgproc.CV_SCHARR, 0, 1);
		return dst;
	}

	public Mat sobelDX(Mat src) {
		// https://www.tutorialspoint.com/opencv/opencv_sobel_operator.htm
		// https://docs.opencv.org/3.4/d2/d2c/tutorial_sobel_derivatives.html
		// Creating an empty matrix to store the result
		Mat dst = new Mat();
		// Applying sobel on the Image
		int scale = 1;
		int delta = 0;
		int ddepth = CvType.CV_16S;
		Imgproc.Sobel(src, dst, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
		Mat abs_grad_x = new Mat();
		Core.convertScaleAbs(dst, abs_grad_x);
		dst.convertTo(dst, CvType.CV_32F, 1 / 255.0);
		return dst;
	}

	public Mat sobelDY(Mat src) {
		// Creating an empty matrix to store the result
		Mat dst = new Mat();
		// Applying sobel on the Image
		int scale = 1;
		int delta = 0;
		int ddepth = CvType.CV_16S;
		Imgproc.Sobel(src, dst, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
		dst.convertTo(dst, CvType.CV_32F, 1 / 255.0);
		return dst;
	}


	
}//End of Class
