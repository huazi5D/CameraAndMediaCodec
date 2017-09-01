package hz.cameraandmediacodec.AudioRecoderHelp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import hz.cameraandmediacodec.MediaMuxer.MediaMuxerHelp;

/**
 * Created by Administrator on 2017-09-01.
 */

public class AudioRecoderHelp {

    private AudioRecord mAudioRecod;
    private int mMinBufferSize;
    private byte[] mAudioData;
    private boolean mIsRecoding = false;
    private MediaCodec mEncoder;
    private boolean mEncoderStarted = false;
    public ArrayBlockingQueue<byte[]> mAudioQueue = new ArrayBlockingQueue<byte[]>(10);
    private MediaMuxerHelp mMediaMuxer = MediaMuxerHelp.getInstanse();

    public AudioRecoderHelp() {
        mMinBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecod = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
        mAudioData = new byte[mMinBufferSize];
    }

    public void start() {
        mAudioRecod.startRecording();
        mIsRecoding = true;
        while (mIsRecoding) {
            mAudioRecod.read(mAudioData, 0, mMinBufferSize);
            if (mAudioQueue.size() >= 10)
                mAudioQueue.poll();
            mAudioQueue.add(mAudioData);
        }
    }

    public boolean initEncoder(int w, int h) {
        try {
            mEncoder = MediaCodec.createEncoderByType("Audio/mp4a-latm");

            MediaFormat mediaFormat = MediaFormat.createAudioFormat("Audio/mp4a-latm", 48000, h);
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

    byte[] buf;

    public void encode() {

        try {
            buf = mAudioQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
                mMediaMuxer.addTrack(mEncoder.getOutputFormat(), MediaMuxerHelp.TrackType.AUDIO_TRACK);
                mMediaMuxer.start();
            } else { // 正常编码数据
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                if (bufferInfo.size != 0) {
                    bufferInfo.presentationTimeUs = System.nanoTime() / 1000; // 设定时间戳 否则无法播放
                    mMediaMuxer.writeSampleData(MediaMuxerHelp.TrackType.AUDIO_TRACK, outputBuffer, bufferInfo);
                }
                mEncoder.releaseOutputBuffer(outputBufferIndex, false);
            }
        }while (outputBufferIndex >= 0);
    }


}
