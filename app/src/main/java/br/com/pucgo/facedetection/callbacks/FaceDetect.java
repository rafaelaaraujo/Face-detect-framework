package br.com.pucgo.facedetection.callbacks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.derzapp.myfacedetection.DetectionBasedTracker;
import com.derzapp.myfacedetection.FisherFaceRecognizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import br.com.pucgo.facedetection.R;
import br.com.pucgo.facedetection.controller.Face;
import br.com.pucgo.facedetection.custom.CustomJavaCameraView;

import static org.opencv.imgproc.Imgproc.bilateralFilter;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.equalizeHist;
import static org.opencv.imgproc.Imgproc.resize;


/**
 * Created by Rafaela
 * on 04/04/2016.
 */
public class FaceDetect extends CameraCallback {

    private Activity context;
    private String[] detectorNameCamera;
    private int detectorTypeCamera = 0;
    private MenuItem cameraOrientation;
    private LinkedList<Face> trackedFaces = new LinkedList<>();

    //CONSTANTES
    private final Scalar RED = new Scalar(255, 0, 0, 255);
    private final Scalar ORANGE = new Scalar(255, 103, 0, 255);
    private final Scalar MAGENTA = new Scalar(255, 0, 255, 255);
    private final Scalar GREEN = new Scalar(0, 255, 0, 255);
    private final Scalar BLUE = new Scalar(0, 0, 255, 255);
    private final Scalar YELLOW = new Scalar(255, 255, 0, 255);
    public final int BACK_CAMERA = 0;
    public final int FRONT_CAMERA = 1;
    public NumberProgressBar progressHappy;
    public NumberProgressBar progressAnger;
    public NumberProgressBar progressSurprise;
    public NumberProgressBar progressNeutral;
    public NumberProgressBar progressDisgust;
    public FisherFaceRecognizer fisherFaceRecognizer;

    private CascadeClassifier javaDetector;
    private DetectionBasedTracker nativeDetector;
    private int absoluteFaceSize = 0;
    private Mat mRgba;
    private Mat mGray;
    private int i = 0;



    public FaceDetect(Activity activity) {
        this.context = activity;
        trackedFaces = new LinkedList<>();
        progressHappy = (NumberProgressBar) activity.findViewById(R.id.progress_happy);
        progressAnger = (NumberProgressBar) activity.findViewById(R.id.progress_anger);
        progressSurprise = (NumberProgressBar) activity.findViewById(R.id.progress_surprise);
        progressNeutral = (NumberProgressBar) activity.findViewById(R.id.progress_neutral);
        progressDisgust = (NumberProgressBar) activity.findViewById(R.id.progress_disgust);

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
     * BACK CAMERA 0
     * FRONT CAMERA 1
     * @param camera camera que deseja usar
     */
    public void setCamera(int camera) {
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
                    iniciaFaceDetection(true);
                    fisherFaceRecognizer = new FisherFaceRecognizer(context);
                    fisherFaceRecognizer.trainClassifier();

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

        return getMat(mRgba, mGray, faces, facesFliped);
    }

    private Mat getMat(Mat mRgba, Mat mGray, MatOfRect faces, MatOfRect facesFliped) {

        if (javaDetector != null)
            javaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        Core.flip(faces, facesFliped, 1);
//
        Rect[] facesArray = facesFliped.toArray();
        for (Rect aFacesArray : facesArray) {

            //Ages all trackedFaces
            for (Face f : trackedFaces) {
                f.updateLife();
            }

            //Remove expired faces
            LinkedList<Face> trackedFacesTemp = new LinkedList<>();
            for (Face f : trackedFaces) {
                if (!f.isTooOld()) {
                    trackedFacesTemp.add(f);
                }
            }

            trackedFaces.clear();

            if (trackedFacesTemp.size() > 0) {
                trackedFaces = trackedFacesTemp;
            }

            boolean matchedFace = false;
            Face mf = null;

            Point pt1 = aFacesArray.tl();
            Point pt2 = aFacesArray.br();

            //check if there are trackedFaces
            double IMAGE_SCALE = 1;
            if (trackedFaces.size() > 0) {

                //each face being tracked
                for (Face f : trackedFaces) {
                    //the face is found (small movement)
                    if ((Math.abs(f.xpt - pt1.x) < Face.FACE_MAX_MOVEMENT) && (Math.abs(f.ypt - pt1.y) < Face.FACE_MAX_MOVEMENT)) {
                        matchedFace = true;
                        String label = analizyFace(mRgba, aFacesArray);
                        f.updateFace(aFacesArray.width, aFacesArray.height, (int) pt1.x, (int) pt1.y, analizyFace(mRgba, aFacesArray));
                        mf = f;
                        break;
                    }
                }

                //if face not found, add a new face
                if (!matchedFace) {
                    Face f = new Face(0, (int) (aFacesArray.width * IMAGE_SCALE), (int) (aFacesArray.height * IMAGE_SCALE), (int) pt1.x, (int) pt1.y, 0);
                    trackedFaces.add(f);
                    mf = f;
                }

            } else { //No tracked faces: adding one
                Face f = new Face(0, (int) (aFacesArray.width * IMAGE_SCALE), (int) (aFacesArray.height * IMAGE_SCALE), (int) pt1.x, (int) pt1.y, 0);
                trackedFaces.add(f);
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
            setUIText(mf);

        }

        return mRgba;
    }

    private synchronized void setUIText(final Face face) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressNeutral.setProgress(face.emotion.get("neutral"));
                progressAnger.setProgress(face.emotion.get("anger"));
                progressHappy.setProgress(face.emotion.get("happy"));
                progressSurprise.setProgress(face.emotion.get("surprise"));
                progressDisgust.setProgress(face.emotion.get("disgust"));

            }
        });
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

    public void iniciaFaceDetection(boolean configureCamera) {
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

            if (configureCamera) {
                cameraView.enableView();
                cameraView.enableFpsMeter();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    public void iniciaFaceDetection(CameraBridgeViewBase openCvCameraView, int width, int height) {
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

            openCvCameraView.setMaxFrameSize(width, height);
            openCvCameraView.enableView();
            openCvCameraView.enableFpsMeter();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    private Scalar getColor(Face mf) {
        if (mf.isNodding()) return GREEN;
        else if (mf.isShaking()) return RED;
        else if (mf.isStill()) return BLUE;
        else return YELLOW;
    }


    public Bitmap analysePicture(Uri selectedImageUri) {
        Bitmap bitmap = null;
        Mat imageMat = new Mat(100, 100, CvType.CV_8U, new Scalar(4));
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImageUri);
            Utils.bitmapToMat(bitmap, imageMat);
            Utils.matToBitmap(detectface(imageMat), bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Mat detectface(Mat inputFrame) {

        MatOfRect faces = new MatOfRect();
        MatOfRect facesFliped = new MatOfRect();

        if (absoluteFaceSize == 0) {
            int height = inputFrame.rows();
            float relativeFaceSize = 0.2f;

            if (Math.round(height * relativeFaceSize) > 0) {
                absoluteFaceSize = Math.round(height * relativeFaceSize);
            }
            nativeDetector.setMinFaceSize(absoluteFaceSize);
        }
        return getMat(inputFrame,inputFrame,faces,facesFliped);
    }

    private synchronized String analizyFace(Mat mat, Rect face_i) {
        Mat mIntermediateMat = new Mat();
        cvtColor(mat, mIntermediateMat, Imgproc.COLOR_BGR2GRAY);
        Mat face = new Mat(mIntermediateMat, face_i);
        Mat face_resized = new Mat();

        //resize image
        resize(face, face_resized, new Size(350, 350));

        // histogram equalization
//        face_resized = getImageEqualize(face_resized);
        equalizeHist(face_resized, face_resized);

        Mat filtered = new Mat(face_resized.size(), CvType.CV_8U);

        //smooth
        bilateralFilter(face_resized, filtered, 0, 20.0, 2.0);

        //Mat faceEllipse = ellipseFace(face_resized);
        //save image
       // SaveImage(filtered);

        return fisherFaceRecognizer.recognizeFace(filtered);
    }


}
