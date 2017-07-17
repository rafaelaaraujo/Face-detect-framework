package br.com.facedetection.callbacks;

import android.content.Context;
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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import br.com.facedetection.custom.CustomJavaCameraView;
import br.com.facedetection.enumerator.ViewModeEnum;

/**
 * Created by Rafaela
 * on 06/04/2016.
 */
public class MultipleZoomCanny extends CameraCallback {

    private  final String TAG = "Zoom:View";
    private Context context;

    public static ViewModeEnum viewModeEnum = ViewModeEnum.RGBA;
    public float alpha = 1f;
    public int beta = 0;
    public Mat mIntermediateMat;


    public MultipleZoomCanny(Context context) {
        this.context = context;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");

                    cameraView.enableView();
                    viewModeEnum = ViewModeEnum.MULTIPLE;
                    cameraView.setEffect("sepia");

                    break;

                default:
                    super.onManagerConnected(status);
            }
        }
    };

    public void setCameraView(CustomJavaCameraView openCvCameraView) {
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);
        this.cameraView = openCvCameraView;
    }

    public void showPreviewImage(boolean show) {
        cameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void initAsyncOpenCv() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, mLoaderCallback);
    }

    public void disableCameraView() {
        cameraView.disableView();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        Mat output = Mat.zeros(rows, cols, rgba.type());
        int X_1, X_2, X_3, X_4, X_5, X_6, Y_1, Y_2, Y_3, Y_4, Y_5, Y_6;
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
        X_5 = (int) (cols * (lr2 - 0.25));
        X_6 = (int) (cols * ((1 - lr2) + 0.25));

        Y_1 = (int) (rows * tp);
        Y_2 = (int) (rows * tp2);
        Y_3 = (int) (rows * (1 - tp2));
        Y_4 = (int) (rows * (1 - tp));
        Y_5 = (int) (rows * (tp2 - 0.24));
        Y_6 = (int) (rows * ((1 - tp2) + 0.24));

        Mat corner1 = rgba.submat(Y_1, Y_2, X_1, X_2);  //ROWS and then columns  X- governs cols and Y- Rows
        Mat corner2 = rgba.submat(Y_3, Y_4, X_1, X_2);
        Mat corner3 = rgba.submat(Y_1, Y_2, X_3, X_4);
        Mat corner4 = rgba.submat(Y_3, Y_4, X_3, X_4);
        Mat middle = rgba.submat(Y_5, Y_6, X_5, X_6);
        Mat teste = middle.clone();


        Mat corner1_Shrinked = output.submat((int) ((rows * .15)), (int) (rows * .42), (int) (cols * shrinkedX), (int) (cols * .37));                           //Submats of the corners shrinked  1,2,3,4
        Mat corner2_Shrinked = output.submat((int) (rows * .58), (int) (rows * .85), (int) (cols * shrinkedX), (int) (cols * .37));
        Mat corner3_Shrinked = output.submat((int) ((rows * .15)), (int) (rows * .42), (int) (cols * .63), (int) (cols * (1 - shrinkedX)));
        Mat corner4_Shrinked = output.submat((int) (rows * .58), (int) (rows * .85), (int) (cols * .63), (int) (cols * (1 - shrinkedX)));

        Point mid1, mid2;
        mid1 = new Point((int) (cols * (.34)), (int) (rows * (.34)));
        mid2 = new Point((int) (cols * (1 - (.34))), (int) (rows * (1 - (.34))));
        Mat middle_Shrinked = output.submat((int) mid1.y, (int) mid2.y, (int) mid1.x, (int) mid2.x);


        switch (MultipleZoomCanny.viewModeEnum) {
            case MULTIPLE:

                Imgproc.Canny(corner1, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, corner1, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.Canny(corner2, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, corner2, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.Canny(corner3, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, corner3, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.Canny(corner4, mIntermediateMat, 50, 200);
                Imgproc.cvtColor(mIntermediateMat, corner4, Imgproc.COLOR_GRAY2BGRA, 4);

                Imgproc.resize(corner1, corner1_Shrinked, corner1_Shrinked.size());
                Imgproc.resize(corner2, corner2_Shrinked, corner2_Shrinked.size());
                Imgproc.resize(corner3, corner3_Shrinked, corner3_Shrinked.size());
                Imgproc.resize(corner4, corner4_Shrinked, corner4_Shrinked.size());

                Imgproc.resize(teste, middle_Shrinked, middle_Shrinked.size());
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
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
}
