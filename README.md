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

# Code Sample
You can download the code at:
[HogInOpenCV](https://github.com/TrackerLounge/TrackingAndHogDescriptorInJavaOpenCV/tree/master/HogInOpenCV)

# Sample Input/Output

Original Image

<img src='/HogInOpenCV/resources/LF_20in_Stride_Wet_Sand_Binary_Small.jpg' width=800>

Image with HOG Descriptors

<img src='/HogInOpenCV/resources/arrowsDraw.png' width=800>

# Observations
In my implementation, I used a HOG of 9 Bins.
I used degrees: 0, 20, 40, 60, 80, 100, 120, 140, 160
I proportionally devided values that fell between to bins based on nearness.
This resulted in some unwanted behaviors.
Specifically, if the degree exactly equals 90 degrees, I don't have a bin for that. As a result, that result is devided in half. Half of the value is put into the bin: 80 degrees and the other half is put into the bin: 100 degrees.
This is fine but when you display the HOG on an image it can look weird.

Here is an 10 x enlarged image to visuallize this:

<img src='/HogInOpenCV/resources/arrowsDrawAt90degreesEnlarged.png' width=160>
