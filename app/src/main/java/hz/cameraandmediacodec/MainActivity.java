package hz.cameraandmediacodec;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import hz.cameraandmediacodec.Camera.CameraHelper;
import hz.cameraandmediacodec.MediaCodec.MediaCodecHelper;
import hz.cameraandmediacodec.View.CameraTextrueView;

public class MainActivity extends Activity {

    private CameraHelper mCameraHelper;
    private CameraTextrueView mCameraView, mCodecView;
    private Button mLuZhi;
    private MediaCodecHelper mMediaCodecHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaCodecHelper = new MediaCodecHelper();

        mCameraView = (CameraTextrueView) findViewById(R.id.video1);
//        mCodecView  = (VideoViewSurfaceView) findViewById(R.id.video2);
        mLuZhi = (Button) findViewById(R.id.luzhi);
        mLuZhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaCodecHelper.initEncoder(1080, 1920);
                mCameraHelper.setMediaCodec(mMediaCodecHelper);
            }
        });

        mCameraView.setOnViewPreparedListener(new CameraTextrueView.OnViewPreparedListener() {
            @Override
            public void onPrepared() {
                mCameraHelper = new CameraHelper();
                mCameraHelper.startPreview(mCameraView.getSurfaceTexture());
            }
        });
    }
}
