package com.derzapp.myfacedetection;

/**
 * Created by rafaela on 22/08/2016.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;


import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.pucgo.facedetection.controller.ClassifierDatabase;
import br.com.pucgo.facedetection.enumerator.EmotionEnum;


public class FisherFaceRecognizer {

    static {
        System.loadLibrary("facerec");
    }

    private final Context context;
    private final String modelPath;
    private Map<String, Integer> labelMap;
    private ClassifierDatabase classifierDatabase;

    private boolean classifierLoaded;

    public FisherFaceRecognizer(Context c) {
        classifierDatabase = new ClassifierDatabase(c);
        context = c;
        labelMap = new HashMap<>();
        classifierLoaded = false;
        modelPath = ClassifierDatabase.IMAGES_LIST_FILE + "/" + ClassifierDatabase.MODEL_FILE;
        init();
    }

    public void trainClassifier() {
        if (fileExists()) {
            loadClassifier();
        } else {
            classifierDatabase.load();
            List<Mat> images = classifierDatabase.getImages();
            List<String> labels = classifierDatabase.getLabels();

            long[] imagesAddrArray = new long[images.size()];
            for (int i = 0; i < images.size(); i++) {
                imagesAddrArray[i] = images.get(i).getNativeObjAddr();
            }

            labelMap = prepareLabels(labels);
            int[] numericLabels = new int[labels.size()];
            for (int i = 0; i < labels.size(); i++) {
                numericLabels[i] = labelMap.get(labels.get(i));
            }

            train(imagesAddrArray, numericLabels, ClassifierDatabase.IMAGES_LIST_FILE + "/" + ClassifierDatabase.MODEL_FILE);
            storeLabelsMap(labelMap);
            classifierDatabase.deleteExamples();
            classifierLoaded = true;
            loadClassifier();
        }
    }

    public void loadClassifier() {
        if (fileExists()) {
            load(modelPath);
            classifierLoaded = true;
        }
    }

    private boolean fileExists() {
        File file = new File(ClassifierDatabase.IMAGES_LIST_FILE + "/" + ClassifierDatabase.MODEL_FILE);
        return file.exists();
    }

    private void storeLabelsMap(Map<String, Integer> labelsMap) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(ClassifierDatabase.LABELS_FILE, Context.MODE_PRIVATE);
            for (Map.Entry<String, Integer> entry : labelsMap.entrySet()) {
                String strLabel = entry.getKey();
                int intLabel = entry.getValue();
                String line = intLabel + ";" + strLabel + System.getProperty("line.separator");
                fos.write(line.getBytes());
            }
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> prepareLabels(List<String> labelsList) {
        Map<String, Integer> labels = new HashMap<>();
        int counter = 0;
        for (String l : labelsList) {
            if (!labels.containsKey(l)) {
                labels.put(l, counter++);
            }
        }
        return labels;
    }

    public String recognizeFace(Mat faceFrame) {
        if (classifierLoaded) {
            int predicted = predict(faceFrame.getNativeObjAddr());
            EmotionEnum emotionEnum = EmotionEnum.getEnumFromValue(predicted);
            if (emotionEnum != null) {
                return emotionEnum.toString();
            }
        }
        return "";
    }


    private void showClassifierStatus() {
        classifierDatabase.load();
        int examplesNumber = classifierDatabase.examplesNumber();
        int classesNumber = classifierDatabase.classesNumber();
        String text;
        if (classifierDatabase.isTrained()) {
            text = "Classifier already trained.";
        } else if (examplesNumber == 0) {
            text = "Classifier database is empty.";
        } else {
            text = "Classifier ready to train with " + examplesNumber + " examples representing " + classesNumber + " classes.";
        }
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void resetClassifier() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    classifierDatabase.clear();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure want to permanently delete all examples stored in database?");
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);
        builder.show();
    }


    public static native void init();


    /**
     * Loads a FaceRecognizer and its model state.
     * *
     * C++: void FaceRecognizer::load(const string& filename)
     * C++: void FaceRecognizer::load(const FileStorage& fs) = 0
     * Loads a persisted model and state from a given XML or YAML file .
     * Every FaceRecognizer has to overwrite FaceRecognizer::load(FileStorage& fs) to enable loading the model state.
     * FaceRecognizer::load(FileStorage& fs) in turn gets called by FaceRecognizer::load(const string& filename),
     * to ease saving a model.
     *
     * @param path
     */
    public static native void load(String path);


    /**
     * Trains a FaceRecognizer with given data and associated labels.
     * <p/>
     * C++: void FaceRecognizer::train(InputArrayOfArrays src, InputArray labels) = 0
     * Parameters:
     * src – The training images, that means the faces you want to learn. The data has to be given as a vector<Mat>.
     * labels – The labels corresponding to the images have to be given either as a vector<int> or a
     * Think of the label as the subject (the person) this image belongs to,
     * so same subjects (persons) should have the same label. For the available FaceRecognizer you don’t have to pay any attention to the order of the labels,
     * just make sure same persons have the same label:
     *
     * @param images
     * @param labels
     * @param path
     */
    public static native void train(long[] images, int[] labels, String path);


    /**
     * C++: int FaceRecognizer::predict(InputArray src) const = 0
     * C++: void FaceRecognizer::predict(InputArray src, int& label, double& confidence) const = 0
     * Predicts a label and associated confidence (e.g. distance) for a given input image.
     * Parameters:
     * src – Sample image to get a prediction from.
     * label – The predicted label for the given image.
     * confidence – Associated confidence (e.g. distance) for the predicted label.
     * The suffix const means that prediction does not affect the internal model state, so the method can be safely
     * called from within different threads.
     * <p/>
     * The following example shows how to get a prediction from a trained model:
     *
     * @param image
     * @return
     */
    public static native int predict(long image);
}