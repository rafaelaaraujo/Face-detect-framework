package br.com.pucgo.facedetection.callbacks;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

/**
 * Created by Rafaela
 * on 04/04/2016.
 */
public class MultipleZoom extends CameraCallback {

    private Context context;
    private Mat mIntermediateMat;

    private float alpha;
    private int beta;
    private AudioManager audio;
    private float resizefactor = 0.25f;

    public ViewModeEnum viewModeEnum = ViewModeEnum.RGBA;

    public MultipleZoom(Context context) {
        this.context = context;
        alpha = 1f;
        beta = 0;
        audio = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    cameraView.enableView();

                    viewModeEnum = ViewModeEnum.MULTIPLE;
                    cameraView.setEffect("sepia");

                    break;
                default:
                    super.onManagerConnected(status);

                    break;
            }
        }
    };

    public void setCameraView(CustomJavaCameraView openCvCameraView) {
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);
        this.cameraView = openCvCameraView;
    }


    /**
     * Evento keyDown botoes de volume
     *
     * @param keyCode
     */
    public void keydownEvent(int keyCode) {
        switch (keyCode) {
            case KEYCODE_VOLUME_UP:

                if (resizefactor < 0.35)
                    resizefactor += 0.025;

                int volume_level = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.i("multipleZoom", "volume level: " + volume_level + "resizeFactor:" + resizefactor);
                break;
            case KEYCODE_VOLUME_DOWN:
                if (resizefactor > 0.15)
                    resizefactor -= 0.025;


                volume_level = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.i("multipleZoom", "volume level: " + volume_level + "resizeFactor:" + resizefactor);
                break;
        }

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

    /**
     * configura frames
     *
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        Mat output = Mat.zeros(rows, cols, rgba.type());

        int X_1, X_2, X_3, X_4, X_5, X_6, Y_1, Y_2, Y_3, Y_4, Y_5, Y_6, X_7, Y_7;
        float lr, lr2, tp, tp2, shrinkedX;
        lr = 0.00f;
        lr2 = 0.50f;
        tp = 0.00f;
        tp2 = 0.5f;
        shrinkedX = 0.13f;
        X_1 = (int) (cols * lr);
        X_2 = (int) (cols * lr2);
        X_3 = (int) (cols * (1 - lr2));
        X_4 = (int) (cols * (1 - lr));

        X_5 = (int) (cols * (lr2 - resizefactor));   //middles        50% of the image. (.25 - .75)%
        X_6 = (int) (cols * ((1 - lr2) + resizefactor)); //middles

        Y_1 = (int) (rows * tp);
        Y_2 = (int) (rows * tp2);
        Y_3 = (int) (rows * (1 - tp2));
        Y_4 = (int) (rows * (1 - tp));

        Y_5 = (int) (rows * (tp2 - resizefactor));           //middles  50% of the image. (.25 - .75)%
        Y_6 = (int) (rows * ((1 - tp2) + resizefactor));         //middles

        Point v1 = new Point(X_1, Y_1);
        Point v4 = new Point(X_2, Y_2);                            //v3------------------|-v4                 v11-|-------------v12         --- Y2
        //                                                |                        |
        Point v5 = new Point(X_1, Y_3);                            //v5------------------|-v6                 v13-|-------------v14         --- Y3
        Point v8 = new Point(X_2, Y_4);                            //v7--------------------v8                 v15---------------v16         --- Y4

        Point v9 = new Point(X_3, Y_1);//							  x1                 x5  x2                x3  x6             x4
        Point v12 = new Point(X_4, Y_2);

        Point v13 = new Point(X_3, Y_3);
        Point v16 = new Point(X_4, Y_4);

        Mat corner1 = rgba.submat(Y_1, Y_2, X_1, X_2);  //ROWS and then columns  X- governs cols and Y- Rows
        Mat corner2 = rgba.submat(Y_3, Y_4, X_1, X_2);
        Mat corner3 = rgba.submat(Y_1, Y_2, X_3, X_4);
        Mat corner4 = rgba.submat(Y_3, Y_4, X_3, X_4);
        Mat middle = rgba.submat(Y_5, Y_6, X_5, X_6);

        Core.rectangle(rgba, v1, v4, new Scalar(0, 155, 0, 255), 1);
        Core.rectangle(rgba, v5, v8, new Scalar(255, 0, 0, 255), 1);
        Core.rectangle(rgba, v9, v12, new Scalar(5, 0, 255, 255), 1);
        Core.rectangle(rgba, v13, v16, new Scalar(100, 100, 100, 255), 1);

        Mat corner1_Shrinked = output.submat((int) ((rows * .15)), (int) (rows * .42), (int) (cols * shrinkedX), (int) (cols * .37));                           //Submats of the corners shrinked  1,2,3,4
        Mat corner2_Shrinked = output.submat((int) (rows * .58), (int) (rows * .85), (int) (cols * shrinkedX), (int) (cols * .37));
        Mat corner3_Shrinked = output.submat((int) ((rows * .15)), (int) (rows * .42), (int) (cols * .63), (int) (cols * (1 - shrinkedX)));
        Mat corner4_Shrinked = output.submat((int) (rows * .58), (int) (rows * .85), (int) (cols * .63), (int) (cols * (1 - shrinkedX)));
        Point mid1, mid2;
        mid1 = new Point((int) (cols * (.34)), (int) (rows * (.34)));
        mid2 = new Point((int) (cols * (1 - (.34))), (int) (rows * (1 - (.34))));

        Mat middle_Shrinked = output.submat((int) mid1.y, (int) mid2.y, (int) mid1.x, (int) mid2.x);

        switch (viewModeEnum) {
            case RGBA:
                break;

            case CANNY:
                break;

            case SEPIA:
                break;

            case ZOOM:
                break;

            case MULTIPLE:
                Imgproc.resize(corner1, corner1_Shrinked, corner1_Shrinked.size());
                Imgproc.resize(corner2, corner2_Shrinked, corner2_Shrinked.size());
                Imgproc.resize(corner3, corner3_Shrinked, corner3_Shrinked.size());
                Imgproc.resize(corner4, corner4_Shrinked, corner4_Shrinked.size());

                Imgproc.resize(middle, middle_Shrinked, middle_Shrinked.size());
                Core.rectangle(output, mid1, mid2, new Scalar(255, 255, 255, 255), 3);
                output.convertTo(rgba, -1, alpha, beta);

                corner1_Shrinked.release();
                corner1.release();
                corner2_Shrinked.release();
                corner3_Shrinked.release();
                corner4_Shrinked.release();
                corner2.release();
                corner3.release();
                corner4.release();
                middle_Shrinked.release();
                middle.release();
                output.release();
                break;
        }

        return rgba;
    }

    public void initAsyncOpenCv() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, mLoaderCallback);
    }

    public void disableCameraView() {
        cameraView.disableView();
    }

    public void initMenuCameraChoice(Menu menu) {
        List<String> effects = cameraView.getEffectList();

        if (effects == null) {
            Log.e("multipleZoom", "Color effects are not supported by device!");
            return;
        }

        SubMenu mColorEffectsMenu = menu.addSubMenu("Color Effect");
        MenuItem[] mEffectMenuItems = new MenuItem[effects.size()];
        Log.i("multipleZoom", "After Add Color Effect");
        int idx = 0;
        for (String element : effects) {
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }
        Log.i("multipleZoom", "Before Camera Add Submenu resolution");

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


    public void menuSelected(MenuItem item) {
        Log.i("multipleZoom", "called onOptionsItemSelected; selected item: " + item);

        switch (item.getGroupId()) {
            case 1:
                cameraView.setEffect((String) item.getTitle());
                Log.i("multipleZoom", "The String is:" + item.getTitle());
                Toast.makeText(context, cameraView.getEffect(), Toast.LENGTH_SHORT).show();
                break;
            case 3:
                alpha = Float.parseFloat(item.getTitle().toString());
                break;
            case 4:
                beta = Integer.parseInt(item.getTitle().toString());
                break;
        }
    }

    public void showPreviewImage(boolean show) {
        cameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
