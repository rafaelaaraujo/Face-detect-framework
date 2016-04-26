package br.com.pucgo.facedetection.callbacks;

import android.content.Context;
import android.media.AudioManager;
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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import br.com.pucgo.facedetection.custom.CustomJavaCameraView;
import br.com.pucgo.facedetection.enumerator.ViewModeEnum;

/**
 * Created by Rafaela
 * on 06/04/2016.
 */
public class Split extends CameraCallback{

    private final String TAG = "Zoom:View";
    private float lr, lr2, tp, tp2, rr, rr2;
    private float resizefactor = 0.25f;
    private int midWidth;
    public CustomJavaCameraView cameraView;
    public Mat mIntermediateMat;
    public ViewModeEnum viewMode = ViewModeEnum.RGBA;
    //    public int height;
//    public int width;
    private Context context;

    public AudioManager audio;
    private int volume_level;
    public float alpha;
    public int beta;

    public Split(Context context) {
        this.context = context;
        resetVariables();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:

//        cameraView.setMaxFrameSize(width, height);

                    //touch listener
                    cameraView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            int xpos, ypos;
                            xpos = (int) event.getX();
                            ypos = (int) event.getY();

                            if (xpos >= 0 && xpos <= cameraView.getWidth() && ypos >= 0 && ypos <= cameraView.getHeight()) {
                                repositionCorner(xpos);
                            }
                            return false;
                        }
                    });
                    cameraView.enableView();
                    viewMode = ViewModeEnum.SPLIT_FULL_VIEW;
                    cameraView.setEffect("sepia");
                    midWidth = (int) (cameraView.getWidth() / 2.0);
                    break;

                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    /**
     * inicia openCvCallBack
     */
    public void initAsyncOpenCv() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, mLoaderCallback);
    }

    public void disableCameraView() {
        cameraView.disableView();
    }

    /**
     * adiciona a cameraView e seta listner responsavel pela manipulacao das imagens
     *
     * @param openCvCameraView
     */
    public void setCameraView(CustomJavaCameraView openCvCameraView) {
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);
        cameraView = openCvCameraView;
        this.cameraView = openCvCameraView;
    }

    public void showPreviewImage(boolean show) {
        cameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        Mat output = Mat.zeros(rows, cols, rgba.type());

        int X_1, X_2, X_3, X_4, X_5, X_6, Y_1, Y_2, Y_3, Y_4, X_7;

        X_1 = 0;  //0
        X_2 = (int) (cols * 0.5f); // middle
        X_3 = cols;    // end
        X_4 = (int) (cols * lr);   // 1/4 - X
        X_5 = (int) (cols * lr2);  // 1/4 + X
        X_6 = (int) (cols * (rr));  //Opposite of X5   //The subtracting is only for the use of the headset to fix the views
        X_7 = (int) (cols * (rr2));    //Opposite of X4
        Y_1 = 0; //0
        Y_2 = rows;     //end
        Y_3 = (int) (rows * tp);       // 1/2 - X
        Y_4 = (int) (rows * tp2);      // 1/2 + X

        Point v9 = new Point(X_4, Y_3);//							  x1    x4        x5     x2    x6      x7     x3
        Point v10 = new Point(X_5, Y_3);
        Point v11 = new Point(X_4, Y_4);

        Point v13 = new Point(X_6, Y_3);
        Point v14 = new Point(X_7, Y_3);
        Point v15 = new Point(X_6, Y_4);
        Point v17 = new Point(X_1, Y_1);    //**The Whole Screen
        Point v20 = new Point(X_3, Y_2);    //*


        Mat corner1 = rgba.submat(Y_1, Y_2, X_1, X_2);  //ROWS and then columns  X- governs cols and Y- Rows
        Mat corner2 = rgba.submat(Y_1, Y_2, X_2, X_3);
        Mat wholeScreen = rgba.submat(Y_1, Y_2, X_1, X_3);

        Core.rectangle(rgba, v17, v20, new Scalar(255, 255, 255, 255), 3);

        //     rgba.copyTo(output);
        Mat corner1_Shrinked = output.submat((int) v9.y, (int) v11.y, (int) v9.x, (int) v10.x);                           //Submats of the corners shrinked  1,2,3,4
        Mat corner2_Shrinked = output.submat((int) v13.y, (int) v15.y, (int) v13.x, (int) v14.x);


        switch (viewMode) {
            case RGBA:
                break;

            case SPLIT_FULL_VIEW:

                Imgproc.resize(wholeScreen, corner1_Shrinked, corner1_Shrinked.size());
                Imgproc.resize(wholeScreen, corner2_Shrinked, corner2_Shrinked.size());

                output.convertTo(rgba, -1, alpha, beta);

                corner1_Shrinked.release();
                wholeScreen.release();
                corner1.release();
                corner2_Shrinked.release();
                corner2.release();
                output.release();
                break;

            case CANNY:
                Imgproc.resize(wholeScreen, corner1_Shrinked, corner1_Shrinked.size());

                Imgproc.Canny(wholeScreen, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, wholeScreen, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.resize(wholeScreen, corner2_Shrinked, corner2_Shrinked.size());

                corner1_Shrinked.release();
                wholeScreen.release();
                corner1.release();
                corner2_Shrinked.release();
                corner2.release();
                output.copyTo(rgba);
                output.release();
                break;

            case SPLIT_VIEW:

                Imgproc.resize(corner1, corner1_Shrinked, corner1_Shrinked.size());
                Imgproc.resize(corner2, corner2_Shrinked, corner2_Shrinked.size());

                corner1_Shrinked.release();
                wholeScreen.release();
                corner1.release();
                corner2_Shrinked.release();
                corner2.release();
                output.copyTo(rgba);
                output.release();
                break;

            case SPLIT_VIEW_1_CANNY:
                Imgproc.Canny(corner2, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, corner2, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.resize(corner1, corner1_Shrinked, corner1_Shrinked.size());
                Imgproc.resize(corner2, corner2_Shrinked, corner2_Shrinked.size());

                corner1_Shrinked.release();
                wholeScreen.release();
                corner1.release();
                corner2_Shrinked.release();
                corner2.release();
                output.copyTo(rgba);
                output.release();
                break;
        }

        return rgba;
    }

    public void onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:

                if (resizefactor < 0.30)
                    resizefactor += 0.025;

                resizeCorner();
                volume_level = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            case KeyEvent.KEYCODE_VOLUME_DOWN:

                if (resizefactor > 0.15)
                    resizefactor -= 0.025;

                resizeCorner();
                volume_level = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }

    public void initMenuCameraChoice(Menu menu){
        List<String> effects = cameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return;
        }
        SubMenu mColorEffectsMenu = menu.addSubMenu("Color Effect");
        MenuItem[] mEffectMenuItems = new MenuItem[effects.size()];
        Log.i(TAG, "After Add Color Effect");
        int idx = 0;
        for (String element : effects) {
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }
        Log.i(TAG, "Before Camera Add Submenu resolution");
        SubMenu mAlphaMenu = menu.addSubMenu("Alpha");

        MenuItem[] mAlphaItems = new MenuItem[6];
        mAlphaItems[0] = mAlphaMenu.add(3, 0, Menu.NONE, "1");
        mAlphaItems[1] = mAlphaMenu.add(3, 0, Menu.NONE, "1.2");
        mAlphaItems[2] = mAlphaMenu.add(3, 0, Menu.NONE, "1.3");
        mAlphaItems[3] = mAlphaMenu.add(3, 0, Menu.NONE, "1.5");
        mAlphaItems[4] = mAlphaMenu.add(3, 0, Menu.NONE, "1.7");
        mAlphaItems[5] = mAlphaMenu.add(3, 0, Menu.NONE, "2");
        //	mAlphaItems[0]= mAlphaMenu.add(3,0,Menu.NONE,"");

        SubMenu mBetaMenu = menu.addSubMenu("Beta");
        MenuItem[] mBetaItems = new MenuItem[5];
        mBetaItems[0] = mBetaMenu.add(4, 0, Menu.NONE, "-50");
        mBetaItems[1] = mBetaMenu.add(4, 0, Menu.NONE, "-25");
        mBetaItems[2] = mBetaMenu.add(4, 0, Menu.NONE, "0");
        mBetaItems[3] = mBetaMenu.add(4, 0, Menu.NONE, "25");
        mBetaItems[4] = mBetaMenu.add(4, 0, Menu.NONE, "50");
    }

    public void menuSelected(MenuItem item){
        switch (item.getGroupId()) {
            case 1:
                cameraView.setEffect((String) item.getTitle());
                Log.i(TAG, "The String is:" + item.getTitle());
                Toast.makeText(context, cameraView.getEffect(), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                String aux = item.getTitle().toString();
                changeViewMode(aux);
                break;
            case 3:
                alpha = Float.parseFloat(item.getTitle().toString());
                break;
            case 4:
                beta = Integer.parseInt(item.getTitle().toString());
                break;
        }
    }

    public void changeViewMode(String mode) {
        switch (mode) {
            case "SPLIT_VIEW":
                viewMode = ViewModeEnum.SPLIT_VIEW;
                break;
            case "SPLIT_VIEW_1_CANNY":
                viewMode = ViewModeEnum.SPLIT_VIEW_1_CANNY;
                break;
            case "SPLIT_FULL_VIEW":
                viewMode = ViewModeEnum.SPLIT_FULL_VIEW;
                break;
            case "SPLIT_VIEW_FULL_1_CANNY":
                viewMode = ViewModeEnum.CANNY;
                break;
        }
    }

    public void resetVariables() {
        lr = 0.15f;
        lr2 = 0.35f;
        tp = 0.35f;
        tp2 = 0.65f;
        rr = 0.65f;
        rr2 = 0.85f;
        alpha = 1f;
        beta = 0;
    }

    public void repositionCorner(int xpos) {
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume_level = audio.getStreamVolume(AudioManager.STREAM_RING);

        if (xpos <= midWidth) {
            float pointFactor = (float) xpos / cameraView.getWidth();
            if (pointFactor < (0.5 - resizefactor * .5)) {
                lr = pointFactor;
                lr2 = pointFactor + resizefactor;
            } else {
                lr = (float) ((resizefactor * 0.5));
                lr2 = (float) ((resizefactor * 1.5));
            }
        }

        if (xpos >= midWidth) {
            float pointFactor = (float) xpos / cameraView.getWidth();
            if (pointFactor > (0.5 + resizefactor * .5)) {
                rr = pointFactor - resizefactor;
                rr2 = pointFactor;
            } else {
                rr = (float) ((0.5 + resizefactor * 0.5));
                rr2 = (float) ((0.5 + resizefactor * 1.5));
            }

        }
    }

    public void resizeCorner() {
        float aux = resizefactor / 2;
        tp = (float) (0.5 - aux);
        tp2 = (float) (0.5 + aux);
        repositionCorner(midWidth);
    }
}
