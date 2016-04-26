package com.derzapp.myfacedetection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker {

    private long mNativeObj = 0;

    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private native long nativeCreateObject(String cascadeName, int minFaceSize);

    private native void nativeDestroyObject(long thiz);

    private native void nativeStart(long thiz);

    private native void nativeStop(long thiz);

    private native void nativeSetFaceSize(long thiz, int size);

    private native void nativeDetect(long thiz, long inputImage, long faces);
}
