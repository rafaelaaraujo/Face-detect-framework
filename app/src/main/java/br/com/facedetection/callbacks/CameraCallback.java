package br.com.facedetection.callbacks;

import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.CameraBridgeViewBase;

import br.com.facedetection.custom.CustomJavaCameraView;

/**
 * Created by rafaela araujo on 10/04/2016.
 */
public abstract class CameraCallback implements CameraBridgeViewBase.CvCameraViewListener2{

    public CustomJavaCameraView cameraView;

    /**
     * set camera that being used in the application
     *
     * @param cameraView
     */

    public void setCameraView(CustomJavaCameraView cameraView) {
    }

    /**
     * Loads and initializes OpenCV library using OpenCV Engine service.
     */
    public void initAsyncOpenCv() {
    }

    /**
     * receive intent with camera resolution
     */
    public void setResolution(int width, int height) {
        cameraView.setMaxFrameSize(width, height);
    }

    /**
     * change camera effect according to the item selected in the menu
     * @param item selected in menu options
     */
    public void menuSelected(MenuItem item) {
    }

    /**
     * creates menu with options for switching between front and back camera
     */
    public void initMenuCameraChoice(Menu menu) {
    }

    /**
     * define if camera image will be visible to user
     * @param show
     */
    public void showPreviewImage(boolean show) {
    }

    /**
     * desable camera preview
     */
    public void disableCameraView() {
    }
}
