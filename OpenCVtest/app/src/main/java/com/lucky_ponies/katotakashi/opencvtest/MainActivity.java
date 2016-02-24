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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        RECT_COLOR = new Scalar(0, 0, 255);

        Mat rgba = inputFrame.rgba();
        if (mFaceDetector != null) {
            MatOfRect faces = new MatOfRect();
            mFaceDetector.detectMultiScale(inputFrame.gray(), faces, 1.1, 2, 2, mMinFaceSize,
                    new Size());
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), RECT_COLOR, 3);
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
}
