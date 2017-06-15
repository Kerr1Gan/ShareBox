package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.utils.qrimage.QrUtils
import kotlin.concurrent.thread
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix
import android.widget.TextView
import org.ecjtu.channellibrary.wifidirect.WifiDirectManager
import org.ecjtu.channellibrary.wifiutils.NetworkUtil
import org.ecjtu.channellibrary.wifiutils.WifiUtil


/**
 * Created by KerriGan on 2017/6/10.
 */

class ApDataDialog(context: Context,activity: Activity):BaseBottomSheetDialog(context,activity){

    override fun onCreateView(): View? {
        var vg= layoutInflater.inflate(R.layout.dialog_ap_data,null)

        val display = ownerActivity.getWindowManager().getDefaultDisplay()
        val width = display.getWidth()
        val height = display.height/*getScreenHeight(ownerActivity)+getStatusBarHeight(context)*/

        vg.layoutParams= ViewGroup.LayoutParams(width,height)

        initView(vg as ViewGroup)
        return vg
    }

    private val mFormat ="%s %s"


    override fun onViewCreated(view: View?): Boolean {
        var behavior = BottomSheetBehavior.from(findViewById(android.support.design.R.id.design_bottom_sheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed=true
        return true
    }

    private fun initView(vg:ViewGroup){
        var ip=WifiDirectManager.getInstance().localWLANIps
        var port=8000

        var ap=vg.findViewById(R.id.text_ap) as TextView
        var name=vg.findViewById(R.id.text_name) as TextView
        var pwd=vg.findViewById(R.id.text_pwd) as TextView

        if(NetworkUtil.isWifi(context)){
            ap.text="WIFI"
            var wifiInfo=NetworkUtil.getConnectWifiInfo(context)
            var ssid=wifiInfo.ssid.drop(1)
            ssid=ssid.dropLast(1)
            name.setText(String.format(mFormat,name.text.toString(),ssid))
            pwd.visibility=View.INVISIBLE
        }else if(NetworkUtil.isHotSpot(context)){
            ap.text="Hotspot"
            var config=NetworkUtil.getHotSpotConfiguration(context)
            var ssid=config.SSID
            var preSharedKey=config.preSharedKey
            name.setText(String.format(mFormat,name.text.toString(),ssid))
            pwd.setText(String.format(mFormat,pwd.text.toString(),preSharedKey))
            thread {
                var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, context.resources.displayMetrics)
                val qr = QrUtils.createQRImage(WifiUtil.setupWifiDataProtocol(ssid,preSharedKey), px.toInt(), px.toInt())
                ownerActivity.runOnUiThread {
                    darkImageView(vg.findViewById(R.id.image_qr) as ImageView)
                            .setImageBitmap(qr)
                }
            }
        }

        thread {
            var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, context.resources.displayMetrics)
            val qr = QrUtils.createQRImage("http://$ip:$port", px.toInt(), px.toInt())
            ownerActivity.runOnUiThread {
                darkImageView(vg.findViewById(R.id.image_url) as ImageView)
                        .setImageBitmap(qr)
            }
        }

    }

    private fun darkImageView(img:ImageView):ImageView{
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)//饱和度 0灰色 100过度彩色，50正常
        val filter = ColorMatrixColorFilter(matrix)
        img.setColorFilter(filter)
        return img
    }
}