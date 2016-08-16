package br.com.pucgo.facedetection.callbacks;

import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.CameraBridgeViewBase;

import br.com.pucgo.facedetection.custom.CustomJavaCameraView;

/**
 * Created by rafaela araujo on 10/04/2016.
 */
public abstract class CameraCallback implements CameraBridgeViewBase.CvCameraViewListener2{

    public CustomJavaCameraView cameraView;

    /**
     * set Camera que sera usada para capturar as imagens
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
     * receive intent com a resolução
     */
    public void setResolution(int width, int height) {
        cameraView.setMaxFrameSize(width, height);
    }

    /**
     * altera efeito da camera conforme selecionado
     */
    public void menuSelected(MenuItem item) {
    }

    /**
     * adiciona menu para escolher qual camera usar
     */
    public void initMenuCameraChoice(Menu menu) {
    }

    /**
     * define se a imagem da camera sera visivel para o usuario ou no
     *
     * @param show mostrar imagem
     */
    public void showPreviewImage(boolean show) {
    }

    /**
     * define se a imagem capturada pela camera será mostrada ou não
     */
    public void disableCameraView() {
    }
}
