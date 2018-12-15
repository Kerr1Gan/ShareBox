package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ecjtu.netcore.RequestManager
import com.ecjtu.netcore.network.IRequestCallback
import com.ecjtu.qrcode.QRCodeScannerActivity
import com.ecjtu.qrcode.QrUtils
import com.ecjtu.sharebox.Constants
import com.ecjtu.sharebox.R
import org.ecjtu.channellibrary.wifiutil.NetworkUtil
import org.ecjtu.channellibrary.wifiutil.WifiUtil
import java.net.HttpURLConnection
import kotlin.concurrent.thread


/**
 * Created by KerriGan on 2017/6/10.
 */

class ApDataDialog(activity: Activity) : BaseBottomSheetDialog(activity, activity) {

    companion object {
        const val ACTION_UPDATE_DEVICE = "update_device_action"
        const val EXTRA_IP = "extra_ip"
        const val EXTRA_JSON = "extra_json"
    }


    private val mFormat = "%s %s"

    private var mOthers = false

    private var mPort = 8000

    private var mIp = ""

    override fun onCreateView(): View? {
        var vg = layoutInflater.inflate(R.layout.dialog_ap_data, null)

        fullScreenLayout(vg)

        initView(vg as ViewGroup)
        return vg
    }

    override fun onViewCreated(view: View?): Boolean {
        var behavior = BottomSheetBehavior.from(findViewById<View>(android.support.design.R.id.design_bottom_sheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return true
    }

    private fun initView(vg: ViewGroup) {
        var ip = if (mOthers) mIp else ""
        var port = if (mOthers) mPort else PreferenceManager.getDefaultSharedPreferences(ownerActivity).getInt(Constants.PREF_SERVER_PORT, mPort)

        var ap = vg.findViewById<View>(R.id.text_ap) as TextView
        var name = vg.findViewById<View>(R.id.text_name) as TextView
        var pwd = vg.findViewById<View>(R.id.text_pwd) as TextView
        val textIp = vg.findViewById<View>(R.id.text_ip) as TextView

        if (NetworkUtil.isWifi(context)) {
            if (TextUtils.isEmpty(ip)) {
                val ips = NetworkUtil.getLocalWLANIps()
                if (ips.isNotEmpty()) {
                    ip = ips[0]
                }
            }
            textIp.text = String.format(mFormat, textIp.text.toString(), "$ip:$port")
            ap.text = context.getString(R.string.wifi)
            var wifiInfo = NetworkUtil.getConnectWifiInfo(context)
            var ssid = wifiInfo.ssid.drop(1)
            ssid = ssid.dropLast(1)
            name.setText(String.format(mFormat, name.text.toString(), ssid))
            pwd.visibility = View.GONE

            vg.findViewById<View>(R.id.qr_container)?.visibility = View.GONE
        } else if (NetworkUtil.isHotSpot(context)) {
            var ips = NetworkUtil.getLocalApIps()
            if (TextUtils.isEmpty(ip) && ips.isNotEmpty())
                ip = ips[0]
            else {
                ips = NetworkUtil.getLocalWLANIps()
                if (ips.isNotEmpty()) {
                    ip = ips[0]
                }
            }
            textIp.text = String.format(mFormat, textIp.text.toString(), "$ip:$port")
            ap.text = context.getString(R.string.hotspot)
            var config = NetworkUtil.getHotSpotConfiguration(context)
            var ssid = config.SSID
            var preSharedKey = config.preSharedKey
            name.setText(String.format(mFormat, name.text.toString(), ssid))
            pwd.setText(String.format(mFormat, pwd.text.toString(), preSharedKey))
            thread {
                var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, context.resources.displayMetrics)
                val qr = QrUtils.createQRImage(WifiUtil.setupWifiDataProtocol(ssid, preSharedKey), px.toInt(), px.toInt())
                ownerActivity.runOnUiThread {
                    darkImageView(vg.findViewById<View>(R.id.image_qr) as ImageView)
                            .setImageBitmap(qr)
                }
            }
        }

        var url = "http://$ip:$port"
        (vg.findViewById<View>(R.id.text_url) as TextView).apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setText(url)
            setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                val u = getText().toString()
                val uri = Uri.parse(u)
                i.data = uri
                context.startActivity(i)
            }
        }
        thread {
            var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, context.resources.displayMetrics)
            val qr = QrUtils.createQRImage(url, px.toInt(), px.toInt())
            ownerActivity.runOnUiThread {
                darkImageView(vg.findViewById<View>(R.id.image_url) as ImageView)
                        .setImageBitmap(qr)
            }
        }

        vg.findViewById<View>(R.id.enter).setOnClickListener {
            val dlg = IPSearchDialog(ownerActivity)
            dlg.setCallback { ip ->
                RequestManager.requestDeviceInfo(ip, object : IRequestCallback {
                    override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent().apply {
                            setAction(ACTION_UPDATE_DEVICE).putExtra(EXTRA_IP, ip)
                            putExtra(EXTRA_JSON, response)
                        })
                    }
                })
            }
            dlg.show()
        }

        vg.findViewById<View>(R.id.qr_code).setOnClickListener {
            getFragmentHost()?.startActivityForResult(Intent(context, QRCodeScannerActivity::class.java), 100)
        }
    }

    private fun darkImageView(img: ImageView): ImageView {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)//饱和度 0灰色 100过度彩色，50正常
        val filter = ColorMatrixColorFilter(matrix)
        img.setColorFilter(filter)
        return img
    }

    fun setup(ip: String, port: Int) {
        mOthers = true
        mPort = port
        mIp = ip
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 100) {
            return
        }
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val text = data.getStringExtra(QRCodeScannerActivity.EXTRA)
                RequestManager.requestDeviceInfo(text, object : IRequestCallback {
                    override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent().apply {
                            setAction(ACTION_UPDATE_DEVICE).putExtra(EXTRA_IP, text)
                            putExtra(EXTRA_JSON, response)
                        })
                    }
                })
            }
        }
    }
}