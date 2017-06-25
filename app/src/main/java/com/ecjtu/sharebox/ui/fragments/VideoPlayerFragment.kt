package com.ecjtu.sharebox.ui.fragments

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecjtu.sharebox.R
import android.widget.VideoView
import android.widget.MediaController



/**
 * Created by KerriGan on 2017/6/18.
 */
class VideoPlayerFragment:Fragment(){

    companion object{
        val EXTRA_URI_PATH="extra_uri_path"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_video_player,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var videoView=view?.findViewById(R.id.video_view) as VideoView
        var bundle=arguments
        if(bundle!=null){
            var path=bundle.getString(EXTRA_URI_PATH)
            val uri = Uri.parse(path)

            videoView.setOnPreparedListener { mediaPlayer->
                var width=mediaPlayer.videoWidth
                var height=mediaPlayer.videoHeight

                if(width>height){
                    //横屏
                    if(activity.requestedOrientation!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                        videoView.stopPlayback()
                    }
                }else{
                    //竖屏
                    if(activity.requestedOrientation!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        videoView.stopPlayback()
                    }
                }
            }

            videoView.setMediaController(MediaController(context))
            videoView.setVideoURI(uri)
            videoView.start()
            videoView.requestFocus()
            return
        }
        activity.finish()
    }


}
