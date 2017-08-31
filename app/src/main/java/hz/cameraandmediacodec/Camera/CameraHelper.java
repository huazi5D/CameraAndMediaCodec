package hz.cameraandmediacodec.Camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import hz.cameraandmediacodec.MediaCodec.MediaCodecHelper;

import static android.content.ContentValues.TAG;
import static hz.cameraandmediacodec.MediaCodec.MediaCodecHelper.YUVQueue;

/**
 * Created by Administrator on 2017-06-27.
 */

public class CameraHelper implements Camera.PreviewCallback{

    private Camera mCamera;
    private Camera.Parameters mParameters = null;
    private int mBufferSize = -1;
    private byte[] mBuffer;
    private MediaCodecHelper mMediaCodecHelper;

    public CameraHelper() {
    }

    public void setMediaCodec(MediaCodecHelper mediaCodecHelper) {
        this.mMediaCodecHelper = mediaCodecHelper;
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        try {
            mCamera = Camera.open();

            mParameters = mCamera.getParameters();
            mParameters.setPreviewFormat(ImageFormat.NV21);
            List<Camera.Size> list = mParameters.getSupportedPreviewSizes();
            mParameters.setPreviewSize(1920, 1080);
            int[] previewFpsRange = mParameters.getSupportedPreviewFpsRange().get(0);
            mParameters.setPreviewFpsRange(previewFpsRange[0], previewFpsRange[1]);
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(mParameters);
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.setDisplayOrientation(90);

            mBufferSize = mParameters.getPreviewSize().width * mParameters.getPreviewSize().height;
            mBufferSize *= ImageFormat.getBitsPerPixel(mParameters.getPreviewFormat()) / 8;
            mBuffer = new byte[mBufferSize];

//            mCamera.addCallbackBuffer(mBuffer);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            Camera.Parameters size = mCamera.getParameters();
            Log.d(TAG, "startPreview: ");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        YUVQueue.add(data);
    }

    public void autoFocus() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success)
                    mCamera.cancelAutoFocus();
            }
        });
    }

}
