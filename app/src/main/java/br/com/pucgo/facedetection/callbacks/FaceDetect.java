package br.com.pucgo.facedetection.callbacks;

import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.derzapp.myfacedetection.DetectionBasedTracker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import br.com.pucgo.facedetection.R;
import br.com.pucgo.facedetection.controller.FaceDrawing;
import br.com.pucgo.facedetection.custom.CustomJavaCameraView;


/**
 * Created by Rafaela
 * on 04/04/2016.
 */
public class FaceDetect extends CameraCallback {

    private Context context;

    private final int BACK_CAMERA = 0;
    private final int FRONT_CAMERA = 1;

    private String[] detectorNameCamera;
    private int detectorTypeCamera = 0;
    private MenuItem cameraOrientation;

    private CascadeClassifier javaDetector;
    private DetectionBasedTracker nativeDetector;
    private int absoluteFaceSize = 0;
    private LinkedList<FaceDrawing> trackedFaceDrawings = new LinkedList<>();
    private Mat mRgba;
    private Mat mGray;
    //Constantes
    private final Scalar RED = new Scalar(255, 0, 0, 255);
    private final Scalar ORANGE = new Scalar(255, 103, 0, 255);
    private final Scalar MAGENTA = new Scalar(255, 0, 255, 255);
    private final Scalar GREEN = new Scalar(0, 255, 0, 255);
    private final Scalar BLUE = new Scalar(0, 0, 255, 255);
    private final Scalar YELLOW = new Scalar(255, 255, 0, 255);


    public FaceDetect(Context context) {
        this.context = context;
        detectorNameCamera = new String[2];
        detectorNameCamera[FRONT_CAMERA] = "front camera";
        detectorNameCamera[BACK_CAMERA] = "back camera";
    }

    public void setCameraView(CustomJavaCameraView cameraView) {
        this.cameraView = cameraView;
        this.cameraView.setCvCameraViewListener(this);
    }

    /**
     * Altera para camera frontal ou para traseira
     *
     * @param camera camera que deseja usar
     */
    private void setCamera(int camera) {
        if (camera == BACK_CAMERA) {
            cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
            detectorTypeCamera = FRONT_CAMERA;
        } else {
            cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            detectorTypeCamera = BACK_CAMERA;
        }
    }

    public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    System.loadLibrary("detection_based_tracker");
                    iniciaFaceDetection();
                    break;

                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public void initAsyncOpenCv() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, mLoaderCallback);
    }

    public void disableCameraView() {
        cameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (absoluteFaceSize == 0) {
            int height = mGray.rows();
            float relativeFaceSize = 0.2f;

            if (Math.round(height * relativeFaceSize) > 0) {
                absoluteFaceSize = Math.round(height * relativeFaceSize);
            }
            nativeDetector.setMinFaceSize(absoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        MatOfRect facesFliped = new MatOfRect();

        if (javaDetector != null)
            javaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        Core.flip(faces, facesFliped, 1);

        Rect[] facesArray = facesFliped.toArray();
        for (Rect aFacesArray : facesArray) {

            //Ages all trackedFaceDrawings
            for (FaceDrawing f : trackedFaceDrawings) {
                f.updateLife();
            }

            //Remove expired faces
            LinkedList<FaceDrawing> trackedFacesTemp = new LinkedList<>();
            for (FaceDrawing f : trackedFaceDrawings) {
                if (!f.isTooOld()) {
                    trackedFacesTemp.add(f);
                }
            }

            trackedFaceDrawings.clear();

            if (trackedFacesTemp.size() > 0) {
                trackedFaceDrawings = trackedFacesTemp;
            }

            boolean matchedFace = false;
            FaceDrawing mf = null;

            Point pt1 = aFacesArray.tl();
            Point pt2 = aFacesArray.br();

            //check if there are trackedFaceDrawings
            double IMAGE_SCALE = 1;
            if (trackedFaceDrawings.size() > 0) {
                //each face being tracked
                for (FaceDrawing f : trackedFaceDrawings) {
                    //the face is found (small movement)
                    if ((Math.abs(f.xpt - pt1.x) < FaceDrawing.FACE_MAX_MOVEMENT) && (Math.abs(f.ypt - pt1.y) < FaceDrawing.FACE_MAX_MOVEMENT)) {
                        matchedFace = true;
                        f.updateFace( aFacesArray.width , aFacesArray.height, (int) pt1.x, (int) pt1.y);
                        mf = f;
                        break;
                    }
                }
                //if face not found, add a new face
                if (!matchedFace) {
                    FaceDrawing f = new FaceDrawing(0, (int) (aFacesArray.width * IMAGE_SCALE), (int) (aFacesArray.height * IMAGE_SCALE), (int) pt1.x, (int) pt1.y, 0);
                    trackedFaceDrawings.add(f);
                    mf = f;
                }

            } else { //No tracked faces: adding one
                FaceDrawing f = new FaceDrawing(0, (int) (aFacesArray.width * IMAGE_SCALE), (int) (aFacesArray.height * IMAGE_SCALE), (int) pt1.x, (int) pt1.y, 0);
                trackedFaceDrawings.add(f);
                mf = f;
            }
            //where to draw face and properties
            if (mf.age > 5) {
                //draw attention line
                int SCALE = 1;
                Point lnpt1 = new Point(mf.xpt * SCALE, (mf.ypt * SCALE - 5) - 5);
                Point lnpt2;
                if (mf.age > mf.width) {
                    lnpt2 = new Point(mf.xpt * SCALE + mf.width, mf.ypt * SCALE - 5);
                } else {
                    lnpt2 = new Point(mf.xpt * SCALE + mf.age, mf.ypt * SCALE - 5);
                }

                //drawing bold attention line
                Core.rectangle(mRgba, lnpt1, lnpt2, RED, 10, 8, 0);

                //drawing face
                Core.rectangle(mRgba, pt1, pt2, getColor(mf), 3, 8, 0);

                //drawing eyes
                Core.rectangle(mRgba, mf.eyeLeft1, mf.eyeLeft2, MAGENTA, 3, 8, 0);
                Core.rectangle(mRgba, mf.eyeRight1, mf.eyeRight2, MAGENTA, 3, 8, 0);

                //drawing mouth
                Core.rectangle(mRgba, mf.mouthTopLeft, mf.mouthBotRight, ORANGE, 3, 8, 0);
            }
        }
        return mRgba;
    }

    public void setResolution(int width, int height) {
        cameraView.setMaxFrameSize(width, height);
        Toast.makeText(context.getApplicationContext(), Integer.toString(width) + "x" + Integer.toString(height), Toast.LENGTH_LONG).show();
    }

    public void showPreviewImage(boolean show) {
        cameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void menuSelected(MenuItem item) {
        if (item == cameraOrientation) {
            int tmpDetectorType = (detectorTypeCamera + 1) % 2;
            item.setTitle(detectorNameCamera[tmpDetectorType]);
            setCamera(detectorTypeCamera);
            onCameraViewStopped();
            onCameraViewStarted(0, 0);
            disableCameraView();
            cameraView.enableView();
        }
    }

    public void initMenuCameraChoice(Menu menu) {
        cameraOrientation = menu.add(detectorNameCamera[BACK_CAMERA]);
    }

    public void iniciaFaceDetection() {
        String TAG = "OCVSample::Activity";
        try {
            // load cascade file from application resources
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            javaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (javaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                javaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            nativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

            cascadeDir.delete();

            cameraView.enableView();
            cameraView.enableFpsMeter();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    private Scalar getColor(FaceDrawing mf) {
        if (mf.isNodding()) return GREEN;
        else if (mf.isShaking()) return RED;
        else if (mf.isStill()) return BLUE;
        else return YELLOW;
    }

}
