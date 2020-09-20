[Home Page](https://github.com/TrackerLounge/Home)


# Youtube Video
[![Alt text](https://github.com/TrackerLounge/TrackingAndHogDescriptorInJavaOpenCV/blob/master/HOGCompare/resources/splashSceen.jpg)](https://www.youtube.com/watch?v=dXSyIhgNh3M)

# Description

I experimented with OpenCV HOGDescriptor - compute(), detect() and detectMultiScale() to see how will it will work when comparing a footprint (track) to a shoe sole. It appears that HOGs are too lenient for this purpose, finding matches on too dissimilar tracks and shoe soles. 

Note: this is not production quality code. It was written in haste to quickly experiment. It has not been optimized. It is ugly rough-cut code.

# Library Requirements

In order to run this code, you will need to:
* download opencv precompiled code or build opencv locally and include the result in build path. 
    * See https://github.com/TrackerLounge/OpenCVSURF/blob/master/CompilingOpenCV/CompilingOpenCV.md


# Potential Useful Links:

https://www.learnopencv.com/histogram-of-oriented-gradients/

https://www.youtube.com/watch?v=oFVexhcltzE

https://www.learnopencv.com/handwritten-digits-classification-an-opencv-c-python-tutorial/

https://stackoverflow.com/questions/27343614/opencv-hogdescriptor-compute-error

https://www.pyimagesearch.com/2015/11/16/hog-detectmultiscale-parameters-explained/ 
