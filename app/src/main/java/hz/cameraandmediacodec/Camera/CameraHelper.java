package hz.cameraandmediacodec.Camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Administrator on 2017-06-27.
 */

public class CameraHelper implements Camera.PreviewCallback{

    private Camera mCamera;
    private Camera.Parameters mParameters = null;
    private int mBufferSize = -1;
    private byte[] mBuffer;

    public CameraHelper() {
    }

    public void startPreview(SurfaceView v) {
        try {
            mCamera = Camera.open();

            mParameters = mCamera.getParameters();
            mParameters.setPreviewFormat(ImageFormat.NV21);
            /*int[] previewFpsRange = mParameters.getSupportedPreviewFpsRange().get(0);
            mParameters.setPreviewFpsRange(previewFpsRange[0], previewFpsRange[1]);
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);*/
            mCamera.setParameters(mParameters);
            mCamera.setPreviewDisplay(v.getHolder());
            mCamera.setDisplayOrientation(90);

            mBufferSize = mParameters.getPreviewSize().width * mParameters.getPreviewSize().height;
            mBufferSize *= ImageFormat.getBitsPerPixel(mParameters.getPreviewFormat()) / 8;
            mBuffer = new byte[mBufferSize];
            mCamera.setPreviewCallbackWithBuffer(this);

            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(mBuffer);
    }
}
