package hz.cameraandmediacodec;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import hz.cameraandmediacodec.Camera.CameraHelper;
import hz.cameraandmediacodec.MediaCodec.MediaCodecHelper;
import hz.cameraandmediacodec.View.CameraTextrueView;

public class MainActivity extends Activity implements SensorEventListener {

    private CameraHelper mCameraHelper;
    private CameraTextrueView mCameraView, mCodecView;
    private Button mLuZhi;
    private boolean mIsLuZhi = false;
    private MediaCodecHelper mMediaCodecHelper;
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaCodecHelper = new MediaCodecHelper();

        mCameraView = (CameraTextrueView) findViewById(R.id.video1);
//        mCodecView  = (VideoViewSurfaceView) findViewById(R.id.video2);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mLuZhi = (Button) findViewById(R.id.luzhi);
        mLuZhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsLuZhi) {
                    mIsLuZhi = true;
                    mMediaCodecHelper.initEncoder(640, 480);
                    mCameraHelper.setMediaCodec(mMediaCodecHelper);
                } else {
                    mIsLuZhi = false;
                    mMediaCodecHelper.stop();
                }
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


    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null && mSensor != null)
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager != null && mSensor != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (Math.abs(x*x + y*y + z*z - 9.8*9.8) - 13 > 0.01f){
            mCameraHelper.autoFocus();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
