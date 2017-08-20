package com.ecjtu.sharebox.ui.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.R
import tv.danmaku.ijk.media.exo.video.AndroidMediaController
import tv.danmaku.ijk.media.exo.video.AsusMediaController
import tv.danmaku.ijk.media.exo.video.IjkVideoView
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * Created by Ethan_Xiang on 2017/8/18.
 */
class IjkVideoFragment:Fragment(){

    private var mMediaController: AndroidMediaController? = null

    private var mVideoView: IjkVideoView? =null

    private var mIsPlaying: Boolean =false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.activity_video_player,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(mMediaController==null)
            mMediaController = AndroidMediaController(context)
        mMediaController?.setMediaPlayerCallback(mCallback)

        mVideoView = view?.findViewById(R.id.video_view) as IjkVideoView
        mVideoView?.setMediaController(mMediaController)

        mVideoView?.setVideoPath("http://wvideo.spriteapp.cn/video/2016/1119/e9a47928-ae5c-11e6-a7e5-d4ae5296039d_wpc.mp4")
        mVideoView?.start()
    }

    private val mCallback = AsusMediaController.MediaPlayerCallback {
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
        }
    }

    override fun onResume() {
        super.onResume()
        if(mIsPlaying == true){
            mVideoView?.resume()
        }
    }

    override fun onStop() {
        super.onStop()
        mIsPlaying=mVideoView?.isPlaying ?: mIsPlaying
        if(mIsPlaying){
            mVideoView?.pause()
        }
    }
}