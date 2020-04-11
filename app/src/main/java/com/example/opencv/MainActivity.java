package com.example.opencv;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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


    JavaCameraView cameraManager;
    Mat mRgba;
    CascadeClassifier cascadeClassifier;
    int absoluteFaceSize;


    private BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            if (status == BaseLoaderCallback.SUCCESS) {
                initializeOpenCVDependencies();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private void initializeOpenCVDependencies() {

        try {
            // Yuz algılama işlemi yapan xml dosyasının ice aktarımı yapılmaktadır
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "yukleme hatası cascade", e);
        }

        // Kamera calsıp dısarıdan görüntü almaya baslar
        cameraManager.enableView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraManager=findViewById(R.id.camera);
        cameraManager.setVisibility(View.VISIBLE);
        cameraManager.setCvCameraViewListener(this);
        // Ön yada Arka olmak üzere kamera secimi yapılmaktadır
        cameraManager.setCameraIndex(0);

        // Kameranın ekrana yansıttıgı görüntü alanı
        cameraManager.setMaxFrameSize(500,400);
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
        if (!OpenCVLoader.initDebug())
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, baseLoaderCallback);

        }
        else
        {
            try {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba=new Mat(height,width, CvType.CV_8UC4);

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
        Imgproc.cvtColor(mRgbaT,mRgbaT,Imgproc.COLOR_RGB2GRAY);
        MatOfRect faces = new MatOfRect();

        // Yüz algılama işlemi yapılıyor

        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mRgbaT, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // Bulunan yuzler kare içine alınıyor
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(mRgbaT, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return mRgbaT;
    }
}
