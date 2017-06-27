package hz.cameraandmediacodec;

import android.app.Activity;
import android.os.Bundle;

import hz.cameraandmediacodec.Camera.CameraHelper;
import hz.cameraandmediacodec.OpenglEs.VideoViewSurfaceView;

public class MainActivity extends Activity {

    private CameraHelper mCameraHelper;
    private VideoViewSurfaceView mCameraView, mCodecView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = (VideoViewSurfaceView) findViewById(R.id.video1);
        mCodecView  = (VideoViewSurfaceView) findViewById(R.id.video2);

        mCameraView.setOnViewPreparedListener(new VideoViewSurfaceView.OnViewPreparedListener() {
            @Override
            public void onPrepared() {
                mCameraHelper = new CameraHelper();
                mCameraHelper.startPreview(mCameraView);
            }
        });
    }
}
