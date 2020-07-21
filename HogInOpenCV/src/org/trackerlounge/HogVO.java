package org.trackerlounge;

import java.util.Arrays;

public class HogVO {
	int x1,y1;//Upper left corner of patch
	int x2,y2;//Lower right corner of patch
	double[] hog;
	@Override
	public String toString() {
		return "HogVO [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + ", hog=" + Arrays.toString(hog) + "]";
	}
	
	
}
