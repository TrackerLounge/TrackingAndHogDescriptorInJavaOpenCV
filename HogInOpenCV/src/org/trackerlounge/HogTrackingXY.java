package org.trackerlounge;

import java.util.HashMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class HogTrackingXY {
	ImageUtil cv;

	public HogTrackingXY(ImageUtil cv) {
		this.cv = cv;
	}

	public void calculateHog(Mat orig) {
		Mat src = cv.convertImageToGrayscale(orig);
		cv.displayImage(src, "Original In Grayscale");

		Pair<Mat, Mat> sobelXAndY = createSobelInXAndY(src);

//		combineSobelXAndY(sobelXAndY);

		Pair<Mat, Mat> angleAndMagnitude = createAngleAndMagnitude(sobelXAndY);
		
		int patch = 8;
		int hogSize = 9;

		int w = src.width();
		int h = src.height();
		
		HashMap<Key, HogVO> wholeHog = calculateSmallHog(w, h, patch, hogSize, angleAndMagnitude);
	
		HashMap<Key, HogVO> groupedAndNormalizedHog = groupAndNormalizeMegaHogs(w, h, patch, hogSize, wholeHog);
		
		drawHogByPatch(orig, groupedAndNormalizedHog);
	}
	
	public Pair<Mat, Mat> createSobelInXAndY(Mat src) {

		int scale = 1;
		int delta = 0;
		int ddepth = CvType.CV_16S;

		Mat grad_x = new Mat(), grad_y = new Mat();
		Imgproc.Sobel(src, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
		Imgproc.Sobel(src, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT);
		cv.displayImage(grad_x, "sobel_grad_x");
		cv.displayImage(grad_y, "sobel_grad_y");
		//We do not want to calculate absolute gradient because that would restrict our magnitude and angle later to 0-90. 
		//We wouldn't be able to tell a line running at 45 degree angle from a line running at 135 degreee angle.
		Pair<Mat, Mat> result = new Pair<Mat, Mat>(grad_x, grad_y);
		return result;
	}

	public void combineSobelXAndY(Pair<Mat, Mat> sobelXAndY) {
		Mat grad = new Mat();
		Core.addWeighted(sobelXAndY.a, 0.5, sobelXAndY.b, 0.5, 0, grad);
		cv.displayImage(grad, "grad");
	}

	public Pair<Mat, Mat> createAngleAndMagnitude(Pair<Mat, Mat> sobelXAndY) {
		Mat magnitude = new Mat();
		Mat angle = new Mat();
		Mat tempSobelX = new Mat();
		Mat tempSobelY = new Mat();
		// https://www.codota.com/code/java/methods/org.opencv.core.Core/cartToPolar_1
		// https://stackoverflow.com/questions/32592950/python-opencv-template-matching-error

		sobelXAndY.a.convertTo(tempSobelX, CvType.CV_32F, 1);
		sobelXAndY.b.convertTo(tempSobelY, CvType.CV_32F, 1);
		
//		https://docs.opencv.org/2.4/modules/core/doc/operations_on_arrays.html#carttopolar
		boolean angleInDegrees = true;
		Core.cartToPolar(tempSobelX, tempSobelY, magnitude, angle, angleInDegrees);

		cv.displayImage(magnitude, "magnitude");
		cv.displayImage(angle, "angle");

		cv.saveImage("magnitude.png", magnitude);
		cv.saveImage("angle.png", angle);
		
		Pair<Mat, Mat> result = new Pair<Mat, Mat>(angle, magnitude);
		return result;
	}
	
	public HashMap<Key, HogVO> calculateSmallHog(int w, int h, int patch, int hogSize, Pair<Mat, Mat> angleAndMagnitude) {
		HashMap<Key, HogVO> hogs = new HashMap<>();
		double[] hog;
		HogVO hogVO;
		for (int y = 0; y <= h - patch; y = y + patch) {
			for (int x = 0; x <= w - patch; x = x + patch) {
				hog = calculateHogHistogram(angleAndMagnitude, patch, x, y);
				hogVO = new HogVO();
				hogVO.x1 = x;
				hogVO.y1 = y;
				hogVO.x2 = x+patch-1;
				hogVO.y2 = y+patch-1;
				hogVO.hog = hog;
				hogs.put(new Key(x, y), hogVO);
			}
		}
//		System.out.println("Whole Hog Size: " + hogs.size());
//		System.out.println("Printing Whole Hog:");	
//		hogs.entrySet().forEach(entry -> {
//			System.out.println(entry.getValue().toString());
//		});
				
		return hogs;
	}
	
	public double[] calculateHogHistogram(Pair<Mat, Mat> angleAndMagnitude, int patch, int offsetX, int offsetY) {
		double[] hog = new double[9];
		double[] m;
		double[] a;
		double M;
		int A;
		int posX;
		int posY;
		boolean print = false;
		for (int x = 0; x < patch; x++) {
			for (int y = 0; y < patch; y++) {
				posX = x + offsetX;
				posY = y + offsetY;
				m = angleAndMagnitude.b.get(posY, posX);
				a = angleAndMagnitude.a.get(posY, posX);
				M = (double) Math.round(m[0]);
				A = (int) Math.round(a[0]);
				
				hog = addElementToHog(hog, A, M);
				int bob = 0;
			}
		}
		return hog;
	}

	/*
	 * each bucket has 5 internal slots. If we land on exactly bucket 160, it gets
	 * it all If we land on exactly bucket 165, it gets it 3/4th of total, and
	 * bucket 0 gets 1/4 of total magnitude
	 */
	public double[] addElementToHog(double[] hog, int A, double M) {
		int bucket1 = -1;
		int bucket2 = -1;
		double p = 0;
		double p1 = 0;
		double p2 = 0;
		A = Math.abs(A);
		M = Math.abs(M);
		if(A>180) {
			A = 180-(360-A);// We want to capture degrees greater than 180 and we wan them to keep the same slope in the 0-180 range.
		}
		if (0 == A) {
			bucket1 = 0;
		} else if (A > 0 && A < 20) {
			bucket1 = 0;
			bucket2 = 1;
			p = (double) 1F - A / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (20 == A) {
			bucket1 = 1;
		} else if (A > 20 && A < 40) {
			bucket1 = 1;
			bucket2 = 2;
			p = (double) 1F - (A - 20F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (40 == A) {
			bucket1 = 2;
		} else if (A > 40 && A < 60) {
			bucket1 = 2;
			bucket2 = 3;
			p = (double) 1F - (A - 40F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (60 == A) {
			bucket1 = 3;
		} else if (A > 60 && A < 80) {
			bucket1 = 3;
			bucket2 = 4;
			p = (double) 1F - (A - 60F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (80 == A) {
			bucket1 = 4;
		} else if (A > 80 && A < 100) {
			bucket1 = 4;
			bucket2 = 5;
			p = (double) 1F - (A - 80F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (100 == A) {
			bucket1 = 5;
		} else if (A > 100 && A < 120) {
			bucket1 = 5;
			bucket2 = 6;
			p = (double) 1F - (A - 100F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (120 == A) {
			bucket1 = 6;
		} else if (A > 120 && A < 140) {
			bucket1 = 6;
			bucket2 = 7;
			p = (double) 1F - (A - 120F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (140 == A) {
			bucket1 = 7;
		} else if (A > 140 && A < 160) {
			bucket1 = 7;
			bucket2 = 8;
			p = (double) 1F - (A - 140F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (160 == A) {
			bucket1 = 8;
		} else if (A > 160 && A < 180) {
			bucket1 = 8;
			bucket2 = 0;
			p = (double) 1F - (A - 160F) / 20F;
			p1 = (double) M * p;
			p2 = (double) M - p1;
		} else if (180 == A) {
			bucket1 = 0;
		}

		if (bucket1 > -1 && -1 == bucket2) {
			hog[bucket1] = (double) hog[bucket1] + M;
		} else if (bucket1 > -1 && bucket2 > -1) {
			hog[bucket1] = (double) hog[bucket1] + p1;
			hog[bucket2] = (double) hog[bucket2] + p2;
		}

		return hog;
	}
	
	public void printWholeHog(double[] wholeHog, int hogSize) {
		for (int i = 0; i < wholeHog.length; i++) {
			if (i % hogSize == 0) {
				System.out.print("i = " + i + " [ ");
			}
			System.out.print(wholeHog[i] + " ");
			int end = i % hogSize;
			if (i % hogSize == (hogSize - 1)) {
				System.out.println("]");
			}
		}
	}

	public double[] insertHogIntoWholeHog(double[] wholeHog, double[] hog, int wholeHogOffset) {
		for (int i = 0; i < hog.length; i++) {
			wholeHog[wholeHogOffset + i] = hog[i];
		}
		return wholeHog;
	}
	
	public double[] combineFourHogs(double[] hog1, double[] hog2, double[] hog3, double[] hog4) {
		int size = hog1.length;
		int megaSize = 4*size;
		double[] megaHog = new double[megaSize];
		for(int i=0; i<size; i++) {
			megaHog[i] = hog1[i];
			megaHog[size+i] = hog2[i];
			megaHog[(2*size)+i] = hog3[i];
			megaHog[(3*size)+i] = hog4[i];
		}

		return megaHog;
	}
	
	
	public HashMap<Key, HogVO> groupAndNormalizeMegaHogs(int w, int h, int patch, int hogSize, HashMap<Key, HogVO> wholeHog) {
		int windowWidth = patch * 2;
		HashMap<Key, HogVO> groupAndHormalizedMegaHogs = new HashMap<Key, HogVO>();
		/*
		 * If we use x<w-windowWidth than we cut off a usable hog on the right edge
		 * But if we use x<w-patch than we get a null pointer on the last hog, 
		 * when we do double[] hog2 = wholeHog.get(new Key(x+8, y)).hog;
		 * because we don't have a hog at that location.
		 * 
		 * For now I will leave off the last hog - but we could try adding padding to the right edge to get two blocks. 
		 */
		for(int y = 0; y<h-windowWidth; y=y+patch) {
			for(int x = 0; x<w-windowWidth; x=x+patch) {
				double[] hog1 = wholeHog.get(new Key(x, y)).hog;
				double[] hog2 = wholeHog.get(new Key(x+8, y)).hog;
				double[] hog3 = wholeHog.get(new Key(x, y+8)).hog;
				double[] hog4 = wholeHog.get(new Key(x+8, y+8)).hog;
				
				double[] megaHog = combineFourHogs(hog1, hog2, hog3, hog4);
				megaHog = normalizeHog(megaHog);
				HogVO hogVO = new HogVO();
				hogVO.x1 = x;
				hogVO.y1 = y;
				hogVO.x2 = x+8+8-1;
				hogVO.y2 = y+8+8-1;
				hogVO.hog = megaHog;
				groupAndHormalizedMegaHogs.put(new Key(x, y), hogVO);
				
			}
		}
		
//		groupAndHormalizedMegaHogs.entrySet().forEach(entry -> {
//			System.out.println(entry.getValue().toString());
//		});
		
		return groupAndHormalizedMegaHogs;
	}
	
	public double[] normalizeHog(double[] hog) {
		// We want to normalize the hog so that each value is between 0 and 1.
		// To do that we will calculate the len = sqrt(hog[0]^2 + hog[1]^2 + ...)
		// We will then divide all elements in the len.
		double len = 0;
		for (int i = 0; i < hog.length; i++) {
			len += hog[i] * hog[i];
		}
		len = Math.sqrt(len);

		// We want to have 5 digits of precision - I picked this randomly - to get rid
		// of the 0.2E-9 values that are basically zero
//		https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
		for (int i = 0; i < hog.length; i++) {
			hog[i] = (double) Math.round((hog[i] / len) * 100000d) / 100000d;
		}

		return hog;
	}
	
	/*
	 * https://stackoverflow.com/questions/22252438/draw-a-line-using-an-angle-and-a
	 * -point-in-opencv
	 * 
	 * A 9 bucket vectors represents the following angles Pos 0 = Holds All values
	 * of Angle 0 Pos 1 = Holds All values of Angle 20 Pos 2 = Holds All values of
	 * Angle 40 Pos 3 = Holds All values of Angle 60 Pos 4 = Holds All values of
	 * Angle 80 Pos 5 = Holds All values of Angle 100 Pos 6 = Holds All values of
	 * Angle 120 Pos 7 = Holds All values of Angle 140 Pos 8 = Holds All values of
	 * Angle 160
	 */	
	public void drawHogByPatch(Mat src, HashMap<Key, HogVO> groupedAndNormalizedHog) {
		int w = src.width();
		int h = src.height();
		int patch = 8;
		int windowWidth = 2*patch;
		int hogSize = 9;
		int vectorSize = 36;
		int offset = 0;
		int start = offset * vectorSize; 
		double angle;
		double length0;
		double length1;
		double length2;
		double length3;
		
		double scalar = 10; //We need to make it big enough to see;
		double[] angles = { -90F, -70F, -50F, -30F, -10F, 10F, 30F, 50F, 70F }; //"displayed direction = actual direction - 90 degree".
//		double[] angles = { 0F, 20F, 40F, 60F, 80F, 100F, 120F, 140F, 160F };
		HogVO hogVO;
		Point p11;
		Point p12;
		Point p13;
		Point p14;
		Point p21;
		Point p22;
		Point p23;
		Point p24;
		for(int y = 0; y<h-windowWidth; y=y+patch) {
			for(int x = 0; x<w-windowWidth; x=x+patch) {
				
				hogVO = groupedAndNormalizedHog.get(new Key(x, y));
				p11 = new Point(hogVO.x1, hogVO.y1);
				p12 = new Point(hogVO.x1+patch, hogVO.y1);
				p13 = new Point(hogVO.x1, hogVO.y1+patch);
				p14 = new Point(hogVO.x1+patch, hogVO.y1+patch);
				p21 = new Point();
				p22 = new Point();
				p23 = new Point();
				p24 = new Point();
				double[] hogSubMax = findHogSubMax(hogVO.hog, hogSize);
				for(int i = 0; i<9; i++) {
					angle = angles[i];
					length0 = hogVO.hog[i]*scalar;
					length1 = hogVO.hog[9 + i]*scalar;
					length2 = hogVO.hog[18 + i]*scalar;
					length3 = hogVO.hog[27 + i]*scalar;
					
					//We only want to show the two strongest signals in each hog
				    //If we try to show all of the hog lines, it gets too messy in the image. I can't tell what the slopes are	
					if(hogVO.hog[i]> 0 && hogVO.hog[i]>=hogSubMax[0]) {
						p21.x = (int)(Math.round(p11.x + length0 * Math.cos(angle * Math.PI / 180.0)));
						p21.y = (int)(Math.round(p11.y + length0 * Math.sin(angle * Math.PI / 180.0)));
						src = cv.drawLine(src, shiftLineToCenter(p11, p21, patch));
					}
					
					if(hogVO.hog[9 + i] >0 && hogVO.hog[9 + i]>=hogSubMax[1]) {
						p22.x = (int) (Math.round(p12.x + length1 * Math.cos(angle * Math.PI / 180.0)));
						p22.y = (int) (Math.round(p12.y + length1 * Math.sin(angle * Math.PI / 180.0)));
						src = cv.drawLine(src, shiftLineToCenter(p12, p22, patch));
					}
					
					if(hogVO.hog[18 + i]>0 && hogVO.hog[18 + i]>=hogSubMax[2]) {
						p23.x = (int) (Math.round(p13.x + length2 * Math.cos(angle * Math.PI / 180.0)));
						p23.y = (int) (Math.round(p13.y + length2 * Math.sin(angle * Math.PI / 180.0)));
						src = cv.drawLine(src, shiftLineToCenter(p13, p23, patch));
					}
					
					if(hogVO.hog[27 + i] > 0 && hogVO.hog[27 + i]>=hogSubMax[3]) {
						p24.x = (int) (Math.round(p14.x + length3 * Math.cos(angle * Math.PI / 180.0)));
						p24.y = (int) (Math.round(p14.y + length3 * Math.sin(angle * Math.PI / 180.0)));
						src = cv.drawLine(src, shiftLineToCenter(p14, p24, patch));
					}
					
				}
				
			}
		}
		cv.displayImage(src, "Orig in Gray with Hog");
		cv.saveImage("arrowsDraw.png", src);

	}
	
	/*
	 * We want to find the two strongest signals in each 9 bin hog.
	 * We will draw those two strongest signals later on the image to illustrate what the HOG thinks the gradient signature is at each 8x8 pixel block.
	 */
	public double[] findHogSubMax(double[] hog, int hogSize) {
		double[] allSubMax = new double[4];
		
		double h1;
		double max=0;
		double subMax=0;
		for(int j=0; j<hog.length/hogSize; j++) {
			for(int i=0; i<hogSize-1; i++) {
				h1 = hog[i+j*hogSize];
				if(h1>max) {
					if(max!=0 && max>subMax) {
						subMax = max;
					}
					max = h1;
				}else if(h1<max && h1>subMax) {
					subMax=h1;
				}
			}
			if(max>0 && 0==subMax)subMax=max;
			allSubMax[j]=subMax;
			max = 0;
			subMax = 0;
			
		}
		return allSubMax;	
	}
	
	public Pair<Point, Point> shiftLineToCenter(Point p1, Point p2, int patch){
		Point tP1 = new Point(p1.x, p1.y);
		Point tP2 = new Point(p2.x, p2.y);
		int halfPatch = patch/2;
		double centerX = (double)(tP2.x - tP1.x)/2F;
		double centerY = (double)(tP2.y - tP1.y)/2F;
		tP1.x = tP1.x - centerX + halfPatch;
		tP1.y = tP1.y - centerY + halfPatch;
		tP2.x = tP2.x - centerX + halfPatch;
		tP2.y = tP2.y - centerY + halfPatch;
		Pair<Point, Point> line = new Pair<Point, Point>(tP1, tP2);
		return line;
	}
	
}//End of Class
