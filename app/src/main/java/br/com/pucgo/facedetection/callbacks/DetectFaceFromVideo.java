package br.com.pucgo.facedetection.callbacks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import br.com.pucgo.facedetection.controller.CreateMP4Video;
import br.com.pucgo.facedetection.controller.FramePackage;

/**
 * PUC GO
 * Created by rafaela
 * on 30/06/2016.
 */
public class DetectFaceFromVideo {

    public static final int FILE_SELECT_CODE = 5;
    //    private Button btnCreateVideo;
    private Activity activity;

    private ProgressDialog mProgress;
    private FaceDetect faceDetectController;

    private FramePackage framePackage;
    private boolean carregandoVideo = false;


    public DetectFaceFromVideo(Activity activity) {
        this.activity = activity;
        framePackage = new FramePackage();
        faceDetectController = new FaceDetect(activity);

        Log.i("TAG", "Trying to load OpenCV library");
        LoaderCallbackInterface mOpenCVCallBack = new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                System.loadLibrary("detection_based_tracker");
                faceDetectController.iniciaFaceDetection(false);
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {

            }
        };
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, activity, mOpenCVCallBack)) {
            Log.e("TAG", "Cannot connect to OpenCV Manager");
        }

        createDialog();
    }

    private void initAnalysisVideo(String mChosenFile) {
        try {
            if (!carregandoVideo) {
                carregandoVideo = true;

                File videoFile = new File(mChosenFile);
                Uri videoFileUri = Uri.parse(videoFile.toString());

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                FileInputStream inputStream = new FileInputStream(videoFile.getAbsolutePath());
                retriever.setDataSource(inputStream.getFD());
                //Create a new Media Player
                ProgressDialog pd = new ProgressDialog(activity);
                pd.setMessage("aguarde!");
                pd.show();
                MediaPlayer mp = MediaPlayer.create(activity, videoFileUri);
                int millis = mp.getDuration();

                for (int i = 0; i < millis; i += 100) {
                    Bitmap bitmap = retriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                    Mat mat = new Mat();
                    Utils.bitmapToMat(bitmap, mat);
                    Utils.matToBitmap(faceDetectController.detectface(mat), bitmap);

                    framePackage.addImage(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3, false));
                }

                new MyVideoProgressBar(activity, framePackage, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/out.mp4", "MOBILE").execute();
                pd.dismiss();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void createDialog() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            activity.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(activity, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }


    //TODO: IMPLEMENTADO NA PARTE DO CLIENTE
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case FILE_SELECT_CODE:
//                if (resultCode == RESULT_OK) {
//
    //TODO: RESULT OK CHAMAR METODO
//                    openVideoFromUri(data);
//
//                }
//
//                break;
//        }
//    }

    public void openVideoFromUri(Intent data) {
        if (data != null) {
            final Uri uri = data.getData();
            try {
                initAnalysisVideo(getFilePath(uri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(activity, "Caminho do video inválido", Toast.LENGTH_LONG).show();
        }
    }

    public void openVideoFromPath(String path) {
        if (path != null && path.equals("")) {
            initAnalysisVideo(path);
        } else {
            Toast.makeText(activity, "Caminho do video inválido", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get file path from uri object
     *
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public String getFilePath(Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(activity.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private class MyVideoProgressBar extends AsyncTask<Void, String, String> {
        private Context context;
        private String output;
        private String quality;
        private FramePackage framePackage;

        CreateMP4Video createMP4Video;

        public MyVideoProgressBar(Context context, FramePackage framePackage, String output, String quality) {
            this.context = context;
            this.output = output;
            this.quality = quality;
            this.framePackage = framePackage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create Video Maker
            createMP4Video = new CreateMP4Video(context, framePackage, output, quality);
            // Create progress
            mProgress = new ProgressDialog(context);
            mProgress.setTitle("Please Wait..");
            mProgress.setMessage("Creating Video...");
            mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgress.setProgress(0);
            mProgress.setMax(100);
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            createMP4Video.verifyQuality();
            publishProgress("Verifying Quality...");
            mProgress.incrementProgressBy(5);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            try {
                createMP4Video.PrepareForEncoder();
                publishProgress("Perparing for encoder quality...");
                mProgress.incrementProgressBy(5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int totalFrameNumber = framePackage.getCount();
            for (int frameNum = 0; frameNum < totalFrameNumber; ++frameNum) {
                try {
                    createMP4Video.addFrameToVideo(frameNum);
                    publishProgress("Adding frame...");
                    mProgress.incrementProgressBy(80 / totalFrameNumber);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                createMP4Video.Completing();
                publishProgress("Completing...");
                mProgress.setProgress(100);
                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            mProgress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
            if (mProgress != null)
                mProgress.dismiss();
            carregandoVideo = false;
            mProgress = null;

        }
    }
}
