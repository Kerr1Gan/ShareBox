package com.ecjtu.sharebox.ui.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
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

    companion object {
        @JvmField
        val EXTRA_URI_PATH = "extra_uri_path"
    }

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

        var arg=arguments
        if(arg!=null){
            val uri=arg.getString("extra_uri_path","")
            if(!TextUtils.isEmpty(uri)){
                mVideoView?.setVideoPath(uri)
                mVideoView?.start()
            }else{
                activity.finish()
            }
        }else{
            activity.finish()
        }
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