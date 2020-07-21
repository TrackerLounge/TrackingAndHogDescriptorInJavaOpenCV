[Home Page](https://github.com/TrackerLounge/Home)

# Tracking and Hog Descriptor in Java OpenCV
Experiment to better understand how to create image HOG Descriptors  or Histogram of Oriented Gradients in Java OpenCV.
It was instructive to work thru the problem of implementing. 
This code is not ment to be production ready. 
It was written quickly with an eye to understand the process rather than for performance.
I referenced [Histogram of Oriented Gradients](https://www.learnopencv.com/histogram-of-oriented-gradients/)

Note: After I had written the code, I discovered that OpenCV has a Java implementation of [HogDescriptor](https://docs.opencv.org/3.4/javadoc/org/opencv/objdetect/HOGDescriptor.html)

# Coding Environment
- Windows 10 Operating System
- Eclipse IDE (Version: 2019-06 (4.12.0) Build id: 20190614-1200)
- Java 8 version 101
- OpenCV 4.3.0
    - There are several odd steps required to run openCV in Eclipse. I suspect this is because is wrapping C++ code within the jars???
    - [Installing OpenCV for Java](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html)
    - [Running OpenCV in Eclipse](https://opencv-java-tutorials.readthedocs.io/en/latest/02-first-java-application-with-opencv.html)

# Code Sample
You can download the code at:
[HogInOpenCV](https://github.com/TrackerLounge/TrackingAndHogDescriptorInJavaOpenCV/tree/master/HogInOpenCV)

# Sample Input/Output

Original Image

<img src='/HogInOpenCV/resources/LF_20in_Stride_Wet_Sand_Binary_Small.jpg' width=800>

Note: For a dicussion on how the original image was created see [Tracking and Edge Detection](https://github.com/TrackerLounge/TrackingAndEdgeDetection) and look at the FFT process.

Image with HOG Descriptors

<img src='/HogInOpenCV/resources/arrowsDraw.png' width=800>

# Observations
I originally wrote all HOG logic as double[] hog array lists.
I found that row shifting on a single dimentional array was fast to compute but error prone.
Ultimately I abandoned it in favor of a slower but easier to reason about set of Value Object classes, containing the X,Y coordinates and the double[] hog value. 
This ran slightly slower but was much easier to see errors. 
This performance it did not matter in my case but in a production setting, this could be costly.

I have not added image padding to the x or y axis. As a result, we can have partial blocks. 
You may want to add padding to the edge of the image to ensure multiples of 16 block widths.
I chose to ignore these partial blocks.

In my implementation, I used a HOG of 9 Bins.
I used degrees: 
0, 20, 40, 60, 80, 100, 120, 140, 160

I proportionally divided values that fell between two bins based on nearness.
This resulted in some unwanted behaviors.
Specifically, if the degree exactly equals 90 degrees, I don't have a bin for that. As a result, that result is devided in half. Half of the value is put into the bin: 80 degrees and the other half is put into the bin: 100 degrees.
This is fine but when you display the HOG on an image it can look weird.

Here is an 10 x enlarged image to visuallize this:

<img src='/HogInOpenCV/resources/arrowsDrawAt90degreesEnlarged.png' width=160>

I found it very helpful to create small patch images to debug the process.
There are lots of ways to get a partially correct but wrong answer.
Without test patches, it would have been very hard to identify all the small errors I was making.

<img src='/HogInOpenCV/resources/testpatchReverseDiagonal16x16.png' width=16>
<img src='/HogInOpenCV/resources/testpatchVertical16x16.png' width=16>
<img src='/HogInOpenCV/resources/testpatchHorizontal16x16.png' width=16>
<img src='/HogInOpenCV/resources/testpatchDiagonal16x16.png' width=16>
<img src='/HogInOpenCV/resources/whiteToBlackDiagonal24x24.png' width=24>
<img src='/HogInOpenCV/resources/whiteToBlackDown24x24.png' width=24>

Note: because I ignore partial block sizes (see above), small blocks can cause index-out-of-bounds exceptions in my code.
While debugging with these small images, I would temporarily change the for-loop max size to be the patch size (8) rather than the window size (16).
I found this annoying but not enough so to redirect my efforts to fix that bit. 
Perhaps if I spent a little longer with this code, I'd become annoyed enough by it to fix the problem.

For example, I followed a openCV tutorial on using:

Core.cartToPolar(tempSobelX, tempSobelY, magnitude, angle, angleInDegrees);

As part of that tutorial the sobelX and sobelY images were normalized before running cartToPolar.
In my application this resulted in all lines running at 135 degree angle across the image being treated as lines running at 45 degrees. 
As a result my 9 bin histogram was only filling bins: 0, 20, 40, 60, and 80. 

I thought the normalizing of sobelx and sobely would allow me to ignore angles 181 degrees to 360 degrees. 
This turned out to be not the case. I ended up having to detect and flip those degrees into the appropriate 0-180 bin range. 

Without these little sample patches, I would not have discovered these error in my code.

I found that drawing all 9 historgrams on the image at every point was not helpful. It was too noisy. In order to see the lines I had to add a scalar to them but this can increase the noise when lines bleed into other patches. To reduce the noise, I draw the two strongest gradients only.

I found that if I output the image with 9 historgram lines draw per patch as a JPG image, the lines were heavily blurred.
JPEG is a lossy compression algoritm and this resulted in the blurred lines. This is much more apparent on color images when all 9 bins are drawn per patch.
To preserve clean pixel lines, I had to output the results as .png files.

The HOG lines when draw looked different than the examples I was seeing on-line. 
It turns out that when HOG lines are draw, they are rotated by -90 degrees before being draw. 
