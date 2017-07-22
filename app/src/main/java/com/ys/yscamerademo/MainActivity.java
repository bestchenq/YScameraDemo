package com.ys.yscamerademo;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private Button mOpenBtn, mCaptureBtn, mSwitchBtn;

    private SurfaceView mPreviewSurface;

    private SurfaceHolder mHolder;

    private Camera mCamera;

    // 0表示后置，1表示前置
    private int mCameraPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initHolder();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initHolder() {
        mHolder = mPreviewSurface.getHolder();
        mHolder.addCallback(this);
    }

    private void initViews() {
        mOpenBtn = (Button) findViewById(R.id.open_btn);
        mOpenBtn.setOnClickListener(this);

        mCaptureBtn = (Button) findViewById(R.id.capture_btn);
        mCaptureBtn.setOnClickListener(this);

        mSwitchBtn = (Button) findViewById(R.id.switch_btn);
        mSwitchBtn.setOnClickListener(this);

        mPreviewSurface = (SurfaceView) findViewById(R.id.preview_suf);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_btn:
                openCamera();
                break;
            case R.id.capture_btn:
                takePicture();
                break;
            case R.id.switch_btn:
                switchCamera();
                break;
            default:
                break;
        }
    }

    // 打开摄像头预览
    private void openCamera() {
        if (checkCameraHardware() && mCamera == null) {
            // 打开camera
            mCamera = getCamera();
            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);
            }
        }
    }

    // 获取摄像头实例
    private Camera getCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    // 检测是否有可用摄像头
    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    // 开启预览
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {

        }
    }

    private void switchCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

        if (cameraCount < 2) {
            return;
        }

        if (cameraCount == 2) {
            releaseCamera();
            // 打开当前选中的摄像头
            mCamera = Camera.open((mCameraPosition + 1) % 2);
            // 通过surfaceview显示取景画面
            setStartPreview(mCamera, mHolder);
            mCameraPosition = (mCameraPosition + 1) % 2;
        }
    }

    private void takePicture() {
        // 拍照,设置相关参数
        // Camera.Parameters params = mCamera.getParameters();
        // params.setPictureFormat(ImageFormat.JPEG);
        // params.setPreviewSize(800, 400);
        // 自动对焦
        // params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        // mCamera.setParameters(params);
        mCamera.takePicture(null, null, picture);
        setStartPreview(mCamera, mHolder);
    }

    /**
     * 创建png图片回调数据对象
     */
    Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) return;
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    };

    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".png");
        } else {
            return null;
        }
        return mediaFile;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = mPreviewSurface.getHolder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        if (mHolder.getSurface() == null)
            return;

        try {
            mCamera.stopPreview();
        } catch (Exception e) {

        }

        // setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
        mPreviewSurface = null;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
}
