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

    private MediaCodec mMediaCodec = null;

    public MediaCodecHelper() {
    }

    public void initEncoder(Surface surface, int w, int h) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType("Video/avc");

            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mMediaCodec.configure(mediaFormat, surface, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initDecoder(Surface surface, int w, int h) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType("Video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            mMediaCodec.configure(mediaFormat, surface, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encode(byte[] buf) {
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, 0, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    public void decode(byte[] buf) {
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, 0, 0);
        }

        // TO DO
    }

}
