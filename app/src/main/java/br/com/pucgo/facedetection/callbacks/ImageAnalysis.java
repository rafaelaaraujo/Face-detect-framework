package br.com.pucgo.facedetection.callbacks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.LinkedList;

import br.com.pucgo.facedetection.controller.FaceDrawing;

/**
 * Created by rafaela on 30/06/2016.
 */
public class ImageAnalysis {

    private Activity activity;

    private LinkedList<FaceDrawing> trackedFaceDrawings = new LinkedList<>();
    //Constantes
    private final Scalar RED = new Scalar(255, 0, 0, 255);
    private final Scalar ORANGE = new Scalar(255, 103, 0, 255);
    private final Scalar MAGENTA = new Scalar(255, 0, 255, 255);
    private final Scalar GREEN = new Scalar(0, 255, 0, 255);
    private final Scalar BLUE = new Scalar(0, 0, 255, 255);
    private final Scalar YELLOW = new Scalar(255, 255, 0, 255);

    public ImageAnalysis(Activity activity) {
        this.activity = activity;
    }

    public Bitmap analysePicture(Uri selectedImageUri) {
        Bitmap bitmap = null;
        Mat imageMat = new Mat(100, 100, CvType.CV_8U, new Scalar(4));
        try {
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImageUri);
            Utils.bitmapToMat(bitmap, imageMat);
            Utils.matToBitmap(detectface(imageMat), bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Mat detectface(Mat inputFrame) {

        MatOfRect faces = new MatOfRect();
        MatOfRect facesFliped = new MatOfRect();

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
                        f.updateFace(aFacesArray.width, aFacesArray.height, (int) pt1.x, (int) pt1.y);
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
                Core.rectangle(inputFrame, lnpt1, lnpt2, RED, 10, 8, 0);

                //drawing face
                Core.rectangle(inputFrame, pt1, pt2, getColor(mf), 3, 8, 0);

                //drawing eyes
                Core.rectangle(inputFrame, mf.eyeLeft1, mf.eyeLeft2, MAGENTA, 3, 8, 0);
                Core.rectangle(inputFrame, mf.eyeRight1, mf.eyeRight2, MAGENTA, 3, 8, 0);

                //drawing mouth
                Core.rectangle(inputFrame, mf.mouthTopLeft, mf.mouthBotRight, ORANGE, 3, 8, 0);
            }
        }

        return inputFrame;
    }

    private Scalar getColor(FaceDrawing mf) {
        if (mf.isNodding()) return GREEN;
        else if (mf.isShaking()) return RED;
        else if (mf.isStill()) return BLUE;
        else return YELLOW;
    }
}
