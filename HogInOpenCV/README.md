
# HOG 
I referenced serveral tutorials on HOG including:
- https://www.youtube.com/watch?v=0Zib1YEE4LU
    - Gives a calculation on how to split votes between the 9 degree bins 
- https://www.youtube.com/watch?v=4ESLTAd3IOM
- https://www.youtube.com/watch?v=Y1tm5Eo-cz4
    - this has a calculation that to convert a degree into one of our 9 degree bins - 
- https://www.youtube.com/watch?v=7S5qXET179I
    - This is a long presentation - 1:30 hours on detecting humans in images programmatically.
    - He used 3000 positive training images as well as other false images.
    - He talks about feeding these HOGS into a Featore Vector list and using that in a Learn algorithm
    - All training images start with same sized images (fixed scale - 64x128) with the person in the center of the image. The size of the block (8x8) was big enough to hold 1-2 limbs of a human body. Pick your block size based on your data. Pick your fixed scale size based on your data. The images need to be big enough for you to tell what they are.
    - Then train on negative images at every location and every scale
    - He talks about testing and training sets. He doubled his data by flipping images along y axis.
    - In 10200 images would be created and sampled in a scale-space pyramid for a 640x640 pixel image.
    - You want a detector that has 1 false positive in 100,000 images
    - Don't smooth the images
    - Use 9 degree bins 
    - Normalizing is important - he compares different ways of normalizing and how the performed.
    - Linear SVM learning algorithm
        - https://en.wikipedia.org/wiki/Support_vector_machine
        - Simple algorithm used to allow them to tune the inputs and know that the input changes were driving better results.
    - In 3 KB of training data, they have a system that is over 90% accurate and has an error rate of less than 1 false positive for every 100,000 images.
    - Pyramid scaled images are different in size by 1.03-1.05
- https://www.learnopencv.com/histogram-of-oriented-gradients/
- hog implementation in CPP:
    - https://github.com/opencv/opencv/blob/master/modules/objdetect/src/hog.cpp
- https://scikit-image.org/docs/dev/api/skimage.feature.html#skimage.feature.hog

# Useful OpenCV links
- https://docs.opencv.org/3.4/javadoc/org/opencv/objdetect/HOGDescriptor.html
- https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html
- https://opencv-java-tutorials.readthedocs.io/en/latest/02-first-java-application-with-opencv.html
- https://www.tutorialspoint.com/opencv/opencv_reading_images.htm
- https://www.tutorialspoint.com/opencv/opencv_writing_image.htm
- https://www.tutorialspoint.com/opencv/opencv_gui.htm
- https://docs.opencv.org/3.4/javadoc/org/opencv/imgcodecs/Imgcodecs.html
- https://stackoverflow.com/questions/17035005/using-get-and-put-to-access-pixel-values-in-opencv-for-java
- https://www.tutorialspoint.com/how-to-get-pixels-rgb-values-of-an-image-using-java-opencv-library
- https://opencv-java-tutorials.readthedocs.io/en/latest/07-image-segmentation.html
- https://stackoverflow.com/questions/14539498/change-type-of-mat-object-from-cv-32f-to-cv-8u
- https://docs.opencv.org/3.4/d2/d2c/tutorial_sobel_derivatives.html
- https://www.codota.com/code/java/methods/org.opencv.core.Core/cartToPolar_1
- https://stackoverflow.com/questions/32592950/python-opencv-template-matching-error

# Useful Math links
- Dot Product Refresher
    - https://www.youtube.com/watch?v=LyGKycYT2v0
    - [1,2]dot[3,-4] = 1x3+2*-4 = -5
    - if the result is positive the two vectors are generally laying on the same line
    - if the result is close to 0, then the two vectors are generally perpendicular
    - if the result is negative, the two vectors are generally going in opposite directions on the same line
