package hz.cameraandmediacodec.MediaCodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2017-06-27.
 */

public class MediaCodecHelper {

    private MediaCodec mEncoder = null;
    private MediaCodec mDecoder = null;

    private boolean mEncoderIsInited = false;
    private boolean mDecoderIsInited = false;

    public MediaCodecHelper() {
    }

    public boolean initEncoder(int w, int h) {
        try {
            mEncoder = MediaCodec.createEncoderByType("Video/avc");

            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();
            mEncoderIsInited = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initDecoder(Surface surface, int w, int h) {
        try {
            mDecoder = MediaCodec.createDecoderByType("Video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            mDecoder.configure(mediaFormat, surface, null, 0);
            mDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encode(byte[] buf) {
        if (!mEncoderIsInited) return;

        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
        int inputBufferIndex = mEncoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            mEncoder.queueInputBuffer(inputBufferIndex, 0, buf.length, 0, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

            mEncoder.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    public void decode(byte[] buf) {
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
        int inputBufferIndex = mDecoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            mDecoder.queueInputBuffer(inputBufferIndex, 0, buf.length, 0, 0);
        }

        // TO DO
    }

}
