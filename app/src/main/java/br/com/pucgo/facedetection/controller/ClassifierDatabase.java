package br.com.pucgo.facedetection.controller;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;


import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.pucgo.facedetection.enumerator.EmotionEnum;

import static org.opencv.highgui.Highgui.imread;

public class ClassifierDatabase {

    public final static String IMAGES_LIST_FILE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/imagem-cortada";
    public final static String MODEL_FILE = "face_recognizer_model";
    public final static String LABELS_FILE = "face_recognizer_labels";

    private Context context;
    private List<String> labels;
    private List<Mat> images;


    public ClassifierDatabase(Context c) {
        context = c;
        images = new ArrayList<>();
        labels = new ArrayList<>();
    }

    public void load() {
        labels.clear();
        images.clear();
        try {

            for (EmotionEnum emotionEnum : EmotionEnum.values()) {

                File root = new File(IMAGES_LIST_FILE + "/00" + (emotionEnum.getValue()+1));
                FilenameFilter imgFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
                    }
                };

                File[] imageFiles = root.listFiles(imgFilter);

//                for (File imageFile : imageFiles) {
                for (int i = 0 ; i < 60 ; i++) {
                    Mat image = readImageFile(imageFiles[i].getAbsolutePath());
                    labels.add(emotionEnum.toString().toLowerCase());
                    images.add(image);
                }
            }

            Toast.makeText(context,"toast",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String label, Mat image) {
        labels.add(label);
        images.add(image);
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(IMAGES_LIST_FILE, Context.MODE_APPEND);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date date = new Date();
            String path = df.format(date);
            while (pathExists(path)) {
                path += '_';
            }
            writeImageFile(path, image);
            String contents = label + ";" + path + System.getProperty("line.separator");
            fos.write(contents.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        String[] files = context.fileList();
        for (String path : files) {
            context.deleteFile(path);
        }
        load();
    }

    public void deleteExamples() {
        String[] files = context.fileList();
        for (String path : files) {
            if (!path.equals(MODEL_FILE) && !path.equals(LABELS_FILE)) {
                context.deleteFile(path);
            }
        }
    }

    public int examplesNumber() {
        return images.size();
    }

    public int classesNumber() {
        Set<String> uniqueLabels = new HashSet<>();
        for (String label : labels) {
            uniqueLabels.add(label);
        }
        return uniqueLabels.size();
    }

    public boolean isTrained() {
        String[] files = context.fileList();
        for (String path : files) {
            if (path.equals(MODEL_FILE)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Mat> getImages() {
        return images;
    }

    private Mat readImageFile(String path) {
//        Mat m = new Mat(IMAGE_HEIGHT, IMAGE_WIDTH, CvType.CV_8UC1);
        return imread(path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
//        try {
//            File dir = context.getFilesDir();
//            File imageFile = new File(dir, path);
//            FileInputStream imageFis = context.openFileInput(path);
//            byte[] buffer = new byte[(int) imageFile.length()];
//            imageFis.read(buffer);
//            imageFis.close();
//            m.put(0, 0, buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return m;
    }

    private void writeImageFile(String path, Mat image) {
        byte[] buffer = new byte[(int) (image.total() * image.channels())];
        image.get(0, 0, buffer);
        try {
            FileOutputStream imageFos = context.openFileOutput(path, Context.MODE_PRIVATE);
            imageFos.write(buffer);
            imageFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean pathExists(String path) {
        try {
            FileInputStream fis = context.openFileInput(path);
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
