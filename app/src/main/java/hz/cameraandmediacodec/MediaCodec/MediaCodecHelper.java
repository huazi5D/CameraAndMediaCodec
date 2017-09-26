package hz.cameraandmediacodec.MediaCodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import hz.cameraandmediacodec.MediaMuxer.MediaMuxerHelp;

/**
 * Created by Administrator on 2017-06-27.
 */

public class MediaCodecHelper {

    private MediaCodec mEncoder = null;
    private MediaCodec mDecoder = null;

    private boolean mEncoderStarted = false;
    private MediaMuxerHelp mediaMuxer;
    private int trackIndex = -1;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(10);

    public MediaCodecHelper() {
        mediaMuxer = MediaMuxerHelp.getInstanse();
    }

    public boolean initEncoder(int w, int h) {
        try {
            mEncoder = MediaCodec.createEncoderByType("Video/avc");

            MediaFormat mediaFormat = MediaFormat.createVideoFormat("Video/avc", w, h);
            // 通过参数设置高中低码率
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
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {// 请求超时，没有数据就直接跳过

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {// 必须在此处启用混合器，否则报错
                mediaMuxer.addTrack(mEncoder.getOutputFormat(), MediaMuxerHelp.TrackType.VIDEO_TRACK);
                mediaMuxer.start();
            } else { // 正常编码数据
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                if (bufferInfo.size != 0) {
                    bufferInfo.presentationTimeUs = System.nanoTime() / 1000; // 设定时间戳 否则无法播放
                    mediaMuxer.writeSampleData(MediaMuxerHelp.TrackType.VIDEO_TRACK, outputBuffer, bufferInfo);
                }
                mEncoder.releaseOutputBuffer(outputBufferIndex, false);
            }
        }while (outputBufferIndex >= 0);
        if (!mEncoderStarted) {
            mediaMuxer.stop(MediaMuxerHelp.TrackType.VIDEO_TRACK);
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
