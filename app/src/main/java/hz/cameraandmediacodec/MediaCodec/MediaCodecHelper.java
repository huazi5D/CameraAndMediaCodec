package hz.cameraandmediacodec.MediaCodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import hz.cameraandmediacodec.utils.SDCardUtils;

/**
 * Created by Administrator on 2017-06-27.
 */

public class MediaCodecHelper {

    private MediaCodec mEncoder = null;
    private MediaCodec mDecoder = null;

    private boolean mEncoderStarted = false;
    private boolean mDecoderIsInited = false;
    private MediaMuxer mediaMuxer;
    private int trackIndex = -1;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(10);

    public MediaCodecHelper() {
        try {
            mediaMuxer = new MediaMuxer(SDCardUtils.getSDCardPath() + ".123/out.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean initEncoder(int w, int h) {
        try {
            mEncoder = MediaCodec.createEncoderByType("Video/avc");

            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, w * h * 5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();
            mEncoderStarted = true;
//            trackIndex = mediaMuxer.addTrack(mediaFormat);
//            mediaMuxer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mEncoderStarted)
                        encode();
                }
            }).start();
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

    public void stop() {
        mEncoderStarted = false;
    }

    byte[] yuv420sp = new byte[1920 * 1080 * 3 / 2];
    byte[] buf;
    public void encode() {

        try {
            buf = YUVQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rotateAndToNV12(buf, yuv420sp, 1920, 1080);
        buf = yuv420sp;
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
        int outputBufferIndex;
        do {
            outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = mediaMuxer.addTrack(mEncoder.getOutputFormat());
                mediaMuxer.start();
            } else if (outputBufferIndex < 0) {

            } else {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }

                if (trackIndex == -1) {
                    mediaMuxer.addTrack(mEncoder.getOutputFormat());
                    mediaMuxer.start();
                }

                if (bufferInfo.size != 0) {
                    bufferInfo.presentationTimeUs = System.nanoTime() / 1000;
                    mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo);
                }
                mEncoder.releaseOutputBuffer(outputBufferIndex, false);
            }
        }while (outputBufferIndex >= 0);
        if (!mEncoderStarted) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }

    private void rotateAndToNV12(byte[] nv21,byte[] nv12,int width,int height) {
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i = 0; i < width; i++) {
            for(int j = height - 1; j >= 0; j--)
            {
                nv12[k] = nv21[width * j + i];
                k++;
            }
        }

        for(int i = 0; i < width; i += 2) {
            for(int j = height / 2 - 1; j >= 0; j--)
            {
                nv12[k] = nv21[wh + width * j + i + 1];
                nv12[k+1] = nv21[wh+ width * j + i];
                k+=2;
            }
        }
    }

    private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
        if(nv21 == null || nv12 == null)return;
        int framesize = width*height;
        int i = 0,j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for(i = 0; i < framesize; i++){
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j-1] = nv21[j+framesize];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j] = nv21[j+framesize-1];
        }
    }

    private void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes, int width, int height) {
        final int iSize = width * height;
        System.arraycopy(nv21bytes, 0, i420bytes, 0, iSize);

        for (int iIndex = 0; iIndex < iSize / 2; iIndex += 2) {
            i420bytes[iSize + iIndex / 2 + iSize / 4] = nv21bytes[iSize + iIndex]; // U
            i420bytes[iSize + iIndex / 2] = nv21bytes[iSize + iIndex + 1]; // V
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
