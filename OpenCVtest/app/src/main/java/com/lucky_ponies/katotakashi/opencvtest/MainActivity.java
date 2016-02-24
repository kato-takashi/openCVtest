package com.lucky_ponies.katotakashi.opencvtest;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    static{
        if(!OpenCVLoader.initDebug()){
            Log.i("opencv", "initialization failed");
        }else{
            Log.i("opencv", "initialization successful");
        }
    }

    private CameraBridgeViewBase mCameraView;
    private static Context mContext;
    private static CascadeClassifier mFaceDetector;
    private static CascadeClassifier mEyeDetector;
    private Size mMinFaceSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCameraView = null;
    }



    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, MainActivity.this,
                new OpenCVLoaderCallback(MainActivity.this, mCameraView));
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (mMinFaceSize == null) {
            mMinFaceSize = new Size(height / 5, height / 5);
        }
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Scalar RECT_COLOR;
        RECT_COLOR = new Scalar(0, 255, 255);

        Mat rgba = inputFrame.rgba();
        if (mFaceDetector != null) {
            MatOfRect faces = new MatOfRect();
            mFaceDetector.detectMultiScale(inputFrame.gray(), faces, 1.1, 2, 2, mMinFaceSize,
                    new Size());
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), RECT_COLOR, 3);
//                fncDetectEye(mEyeDetector, rgba, inputFrame.gray(),facesArray[i], faces);
            }
        }
        return rgba;
    }

    private static class OpenCVLoaderCallback extends BaseLoaderCallback {
        private final CameraBridgeViewBase mCameraView;
        private OpenCVLoaderCallback(Context context, CameraBridgeViewBase cameraView) {
            super(context);
            mCameraView = cameraView;
            mContext = context;


        }

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    mFaceDetector = setupFaceDetector();
                    mEyeDetector = setupEyeDetector();
                    break;
                default:
                    break;
            }
        }
    }

    //カスケードファイルの作成
    private static File setupCascadeFile() {
        File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
        File cascadeFile = null;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            if (!cascadeFile.exists()) {
                is = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
                os = new FileOutputStream(cascadeFile);
                byte[] buffer = new byte[4096];
                int readLen = 0;
                while ((readLen = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readLen);
                }
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        return cascadeFile;
    }

    private static CascadeClassifier setupFaceDetector() {
        File cascadeFile = setupCascadeFile();
        if (cascadeFile == null) {
            return null;
        }

        CascadeClassifier detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (detector.empty()) {
            return null;
        }
        return detector;
    }

    //////////////
    private static File setupCascadeFileEye() {
        File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
        File cascadeFile = null;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            cascadeFile = new File(cascadeDir, "haarcascade_eye.xml");
            if (!cascadeFile.exists()) {
                is = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
                os = new FileOutputStream(cascadeFile);
                byte[] buffer = new byte[4096];
                int readLen = 0;
                while ((readLen = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readLen);
                }
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        return cascadeFile;
    }


    private static CascadeClassifier setupEyeDetector() {
        File cascadeFile = setupCascadeFileEye();
        if (cascadeFile == null) {
            return null;
        }

        CascadeClassifier detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (detector.empty()) {
            return null;
        }
        return detector;
    }

    //目を描画
    private void fncDetectEye(CascadeClassifier cascade_eye, Mat mat, Mat gray, Rect Rct, MatOfRect matOfRect){
//        String cascade_eye_path=Environment.getExternalStorageDirectory()
//                +"/DCIM/100ANDRO/haarcascade_eye.xml";
//検索用submat切り出し
        Mat sub = new Mat();
        gray.submat(Rct.y, Rct.y + Rct.height, Rct.x, Rct.x + Rct.width).copyTo(sub);
//検索結果格納領域
        List geteyelist = new ArrayList();
//検索処理
        cascade_eye.detectMultiScale(sub, matOfRect, 1.1, 3, Objdetect.CASCADE_SCALE_IMAGE, mMinFaceSize, mMinFaceSize);
//検索結果表示処理
        for (int i=0; i < geteyelist.size(); i++){
            Rect rct = (Rect) geteyelist.get(i);
            Point center = new Point(Rct.x + rct.x + rct.width / 2 ,Rct.y + rct.y + rct.height / 2);
            int radius = rct.width / 2;
//            Core.circle(mat, center, radius, new Scalar(0, 255, 255), 2);
        }
    }
}
