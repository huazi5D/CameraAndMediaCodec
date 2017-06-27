package hz.cameraandmediacodec.OpenglEs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import hz.cameraandmediacodec.Camera.CameraHelper;

/**
 * Created by Administrator on 2017-06-27.
 */

public class VideoViewSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private OnViewPreparedListener mViewPreparedListener = null;
    public interface OnViewPreparedListener {
        void onPrepared();
    }

    public VideoViewSurfaceView(Context context) {
        this(context, null);
    }

    public VideoViewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void setOnViewPreparedListener(OnViewPreparedListener l) {
        this.mViewPreparedListener = l;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mViewPreparedListener != null)
            mViewPreparedListener.onPrepared();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
