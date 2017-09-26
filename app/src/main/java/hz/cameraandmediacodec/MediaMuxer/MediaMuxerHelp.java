package hz.cameraandmediacodec.MediaMuxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import hz.cameraandmediacodec.utils.SDCardUtils;

/**
 * Created by Administrator on 2017-09-01.
 */

public class MediaMuxerHelp {

    private static MediaMuxerHelp instance;
    private MediaMuxer mMediaMuxer;
    private int mAudioTrackIndex = -1;
    private int mVideoTrackIndex = -1;
    private boolean mIsStarted = false;

    public enum TrackType {
        AUDIO_TRACK, VIDEO_TRACK;
    }

    public MediaMuxerHelp() {
        try {
            mMediaMuxer = new MediaMuxer(SDCardUtils.getSDCardPath() + ".123/out.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MediaMuxerHelp getInstanse() {
        if (instance == null)
            instance = new MediaMuxerHelp();
        return instance;
    }

    public void addTrack(MediaFormat format, TrackType trackType) {
        if (trackType == TrackType.AUDIO_TRACK) {
            mAudioTrackIndex = mMediaMuxer.addTrack(format);
        } else if (trackType == TrackType.VIDEO_TRACK) {
            mVideoTrackIndex = mMediaMuxer.addTrack(format);
        }

    }

    public void start() {
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
            mMediaMuxer.start();
            mIsStarted = true;
        }
    }

    public void stop(TrackType type) {
        if (type == TrackType.VIDEO_TRACK)
            mVideoTrackIndex = -1;
        if (type == TrackType.AUDIO_TRACK)
            mAudioTrackIndex = -1;
        if (mVideoTrackIndex == -1 && mAudioTrackIndex == -1) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
        }
    }

    public void writeSampleData(TrackType type, ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (!mIsStarted) return;
        int trackIndex = type == TrackType.AUDIO_TRACK ? mAudioTrackIndex : mVideoTrackIndex;
        mMediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo);
    }

}
