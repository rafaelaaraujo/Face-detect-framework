package br.com.facedetection.callbacks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import br.com.facedetection.controller.ShakeEventManager;
import br.com.facedetection.custom.CustomJavaCameraView;
import br.com.facedetection.enumerator.ViewModeEnum;

/**
 * Created by Rafaela
 * on 06/04/2016.
 */
public class Zoom extends CameraCallback implements View.OnTouchListener, RecognitionListener {

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private ShakeEventManager sd;
    private boolean stillOnCreate = true;
    private Context context;
    private Mat mSepiaKernel;

    public Mat mIntermediateMat;

    public float zoomX = 0.1f;
    public float zoomY = 0.1f;
    public float zoomR = 0.2f;
    public int midR = 11;

    public static float alpha = 1f;
    public int beta = 0;
    public ViewModeEnum viewModeEnum = ViewModeEnum.RGBA;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @SuppressWarnings("null")
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("zoom", "OpenCV loaded successfully");
                    cameraView.setOnTouchListener(Zoom.this);
                    cameraView.enableView();

                    viewModeEnum = ViewModeEnum.ZOOM_2;
                    cameraView.setEffect("sepia");
                    break;

                default:
                    super.onManagerConnected(status);

                    break;
            }
        }
    };

    public Zoom(Context context) {
        this.context = context;

        zoomX = zoomR / 2;
        zoomY = zoomR / 2;

        speech = SpeechRecognizer.createSpeechRecognizer(context);
        speech.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        sd = new ShakeEventManager();
        sd.init(context);
        sd.setOnShakeListener(new ShakeEventManager.ShakeListener() {

            @Override
            public void onShake() {
                if (!stillOnCreate)
                    speech.startListening(recognizerIntent);

                stillOnCreate = false;
            }
        });
    }

    public void disableCameraView() {
        if (cameraView != null)
            cameraView.disableView();

        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
        sd.unregister();
    }

    public void initAsyncOpenCv() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, mLoaderCallback);
        sd.register();
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return getMat(inputFrame);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i("zoom", "onTouch event");

        int xpos, ypos;
        xpos = (int) event.getX();
        ypos = (int) event.getY();

        if (xpos >= 0 && xpos <= cameraView.getWidth() && ypos >= 0 && ypos <= cameraView.getHeight()) {
            repositionCorner(xpos, ypos);
        }
        return true;
    }

    private void repositionCorner(int xpos, int ypos) {
        repositConer(xpos, ypos);
    }

    public void setCameraView(CustomJavaCameraView cameraView) {
        this.cameraView = cameraView;
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        speech.stopListening();
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        if (matches != null) {
            for (String result : matches)
                text += result + "\n";
        }

        if (text.contains("ello")) {
            alpha = 2.5f;
        }
        if (text.equals("hello")) alpha = 2f;
        if (text.equals("6")) alpha = 1f;
        if (text.contains("ex")) {
            alpha = 1f;
        }
    }


    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Didn't understand, please try again.";
        }
    }

    public void showPreviewImage(boolean show) {
        cameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void initMenuCameraChoice(Menu menu) {
        List<String> effects = cameraView.getEffectList();

        if (effects == null) {
            Log.e("zoom", "Color effects are not supported by device!");
            return;
        }

        SubMenu mColorEffectsMenu = menu.addSubMenu("Color Effect");
        MenuItem[] mEffectMenuItems = new MenuItem[effects.size()];
        Log.i("zoom", "After Add Color Effect");
        int idx = 0;
        for (String element : effects) {
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }
        SubMenu mAlphaMenu = menu.addSubMenu("Alpha");
        MenuItem[] mAlphaItems = new MenuItem[6];
        mAlphaItems[0] = mAlphaMenu.add(3, 0, Menu.NONE, "1");
        mAlphaItems[1] = mAlphaMenu.add(3, 0, Menu.NONE, "1.2");
        mAlphaItems[2] = mAlphaMenu.add(3, 0, Menu.NONE, "1.3");
        mAlphaItems[3] = mAlphaMenu.add(3, 0, Menu.NONE, "1.5");
        mAlphaItems[4] = mAlphaMenu.add(3, 0, Menu.NONE, "1.7");
        mAlphaItems[5] = mAlphaMenu.add(3, 0, Menu.NONE, "2");

        SubMenu mBetaMenu = menu.addSubMenu("Beta");
        MenuItem[] mBetaItems = new MenuItem[5];
        mBetaItems[0] = mBetaMenu.add(4, 0, Menu.NONE, "-50");
        mBetaItems[1] = mBetaMenu.add(4, 0, Menu.NONE, "-25");
        mBetaItems[2] = mBetaMenu.add(4, 0, Menu.NONE, "0");
        mBetaItems[3] = mBetaMenu.add(4, 0, Menu.NONE, "25");
        mBetaItems[4] = mBetaMenu.add(4, 0, Menu.NONE, "50");

        SubMenu mZoomMenu = menu.addSubMenu("ZoomR");
        MenuItem[] mZoomItems = new MenuItem[5];
        mZoomItems[0] = mZoomMenu.add(5, 0, Menu.NONE, "0.1");
        mZoomItems[1] = mZoomMenu.add(5, 0, Menu.NONE, "0.15");
        mZoomItems[2] = mZoomMenu.add(5, 0, Menu.NONE, "0.2");
        mZoomItems[3] = mZoomMenu.add(5, 0, Menu.NONE, "0.25");
        mZoomItems[4] = mZoomMenu.add(5, 0, Menu.NONE, "0.3");
    }

    public void menuSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case 1:
                cameraView.setEffect((String) item.getTitle());
                Log.i("zoom", "The String is:" + item.getTitle());
                Toast.makeText(context, cameraView.getEffect(), Toast.LENGTH_SHORT).show();
                break;
            case 3:
                alpha = Float.parseFloat(item.getTitle().toString());
                break;
            case 4:
                beta = Integer.parseInt(item.getTitle().toString());
                break;
            case 5:
                zoomR = Float.parseFloat(item.getTitle().toString());
                repositionCorner((int) (cameraView.getWidth() * zoomR * .5), (int) (cameraView.getHeight() * zoomR * .5));
                break;
        }
    }

    public void keyEvent(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:

                if (midR < 19)
                    midR += 1;
            case KeyEvent.KEYCODE_VOLUME_DOWN:

                if (midR > 7)
                    midR -= 1;
        }

    }

    public void repositConer(float xpos, float ypos) {
        float aux = zoomR / 2;
        float pointFactorX = xpos / cameraView.getWidth();
        if (pointFactorX >= (aux) && pointFactorX <= (1 - (aux))) {
            zoomX = pointFactorX;
        }
        if (pointFactorX < (aux)) {
            zoomX = aux;
        } else if (pointFactorX > (1 - aux)) {
            zoomX = 1 - aux;
        }

        float pointFactorY = ypos / cameraView.getHeight();
        if (pointFactorY >= (aux) && pointFactorY <= (1 - (aux))) {
            zoomY = pointFactorY;
        }
        if (pointFactorY < (aux)) {
            zoomY = aux;
        } else if (pointFactorY > (1 - aux)) {
            zoomY = 1 - aux;
        }
    }

    @NonNull
    public Mat getMat(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        Mat rgbaInnerWindow;

        Mat output = rgba.clone();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        int L_1, L_2, R_1, R_2, T_1, T_2, B_1, B_2;
        L_1 = (int) (cols * (zoomX - zoomR / 2));
        L_2 = cols / 2 - midR * cols / 100;
        R_1 = (int) (cols * (zoomX + zoomR / 2));
        R_2 = cols / 2 + midR * cols / 100;
        T_1 = (int) (rows * (zoomY - zoomR / 2));
        T_2 = rows / 2 - midR * rows / 100;
        B_1 = (int) (rows * (zoomY + zoomR / 2));
        B_2 = rows / 2 + midR * rows / 100;
        Mat zoomCorner = output.submat(T_1, B_1, L_1, R_1);

        Point pt1_1 = new Point();
        Point pt1_2 = new Point();
        Point pt2_1 = new Point();
        Point pt2_2 = new Point();
        Point pt3_1 = new Point();
        Point pt3_2 = new Point();
        Point pt4_1 = new Point();
        Point pt4_2 = new Point();

        pt1_1.x = R_1;
        pt1_1.y = T_1;
        pt1_2.x = R_2;
        pt1_2.y = T_2;

        pt2_1.x = R_1;
        pt2_1.y = B_1;
        pt2_2.x = R_2;
        pt2_2.y = B_2;

        pt3_1.x = L_1;
        pt3_1.y = B_1;
        pt3_2.x = L_2;
        pt3_2.y = B_2;

        pt4_1.x = L_1;
        pt4_1.y = T_1;
        pt4_2.x = L_2;
        pt4_2.y = T_2;

        Mat mZoomWindow = rgba.submat(T_2, B_2, L_2, R_2);

        switch (viewModeEnum) {
            case RGBA:
                break;

            case CANNY:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                rgbaInnerWindow.release();
                break;

            case SOBEL:
                Mat gray = inputFrame.gray();
                Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                rgbaInnerWindow.release();
                break;

            case SEPIA:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
                rgbaInnerWindow.release();
                break;

            case ZOOM_2:
                Imgproc.resize(zoomCorner, mZoomWindow, mZoomWindow.size());

                rgba.convertTo(rgba, -1, alpha, beta);

                Core.rectangle(rgba, pt4_1, pt2_1, new Scalar(255, 0, 0, 255), 3);
                Core.rectangle(rgba, pt4_2, pt2_2, new Scalar(255, 255, 255, 255), 2);
                output.release();
                zoomCorner.release();
                mZoomWindow.release();
                break;

            case ZOOM:

                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
                Size wsize = mZoomWindow.size();
                Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
                Core.putText(rgba, "Captured: " + mZoomWindow.size(), new Point(rgba.cols() / 3 * 2, rgba.rows() * 0.5),
                        Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
                zoomCorner.release();
                mZoomWindow.release();
                break;
        }
        return rgba;
    }
}
