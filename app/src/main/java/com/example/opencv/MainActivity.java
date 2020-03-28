package com.example.opencv;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    JavaCameraView cameraManager;
    Mat mRgba,imgGray,imgCanny;


    private BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            if (status == BaseLoaderCallback.SUCCESS) {
                cameraManager.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraManager=findViewById(R.id.camera);
        cameraManager.setVisibility(View.VISIBLE);
        cameraManager.setCvCameraViewListener(this);
        cameraManager.setCameraIndex(0);
        cameraManager.setMaxFrameSize(640,640);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        if (cameraManager != null)
            cameraManager.disableView();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (cameraManager != null)
            cameraManager.disableView();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (OpenCVLoader.initDebug())
        {
            try {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, baseLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba=new Mat(height,width, CvType.CV_8UC1);
        imgGray=new Mat(height,width, CvType.CV_8UC4);
        imgCanny=new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba= inputFrame.rgba();
        Mat mRgbaT = mRgba.t();
        Core.flip(mRgba.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        Imgproc.cvtColor(mRgbaT,imgGray,Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(mRgbaT,imgCanny,50,150);
        return imgCanny;
    }
}
