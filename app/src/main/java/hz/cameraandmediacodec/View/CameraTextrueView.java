package hz.cameraandmediacodec.View;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by Administrator on 2017-07-05.
 */

public class CameraTextrueView extends TextureView implements TextureView.SurfaceTextureListener{

    private static final String TAG = CameraTextrueView.class.getSimpleName();

    private Context mContext;
    private SurfaceTexture mSurfaceTexture;

    private OnViewPreparedListener mViewPreparedListener = null;
    public interface OnViewPreparedListener {
        void onPrepared();
    }


    public CameraTextrueView(Context context) {
        this(context, null);
    }

    public CameraTextrueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setSurfaceTextureListener(this);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        if (mViewPreparedListener != null)
            mViewPreparedListener.onPrepared();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureUpdated: ");
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setOnViewPreparedListener(OnViewPreparedListener l) {
        this.mViewPreparedListener = l;
    }

}
