package com.ecjtu.sharebox.ui.dialog

import android.app.Activity
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.CENTER_IN_PARENT
import com.ecjtu.sharebox.R
import com.ecjtu.sharebox.domain.PreferenceInfo

/**
 * Created by KerriGan on 2017/6/11.
 */

class EditNameDialog(context: Context,activity: Activity):CloseBottomSheetDialog(context,activity){

    override fun onCreateView(): View? {
        var container=RelativeLayout(context)
        fullScreenLayout(container)

        var parent=super.onCreateView() as ViewGroup
        var vg=layoutInflater.inflate(R.layout.dialog_edit_name,parent,false)
        var parm=LinearLayout.LayoutParams(-1,-2)

        var value=TypedValue()

        context.resources.getValue(R.dimen.edit_name_dialog_margin_bottom,value,true)

        parm.bottomMargin=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value.float,
                context.resources.displayMetrics).toInt()
        parent.addView(vg,parm)

        var params=RelativeLayout.LayoutParams(-1,-2)
        params.addRule(CENTER_IN_PARENT)
        parent.layoutParams=params
        container.addView(parent,params)

        setTitle(context.getString(R.string.edit_name),container)

        init(container)
        return container
    }

    override fun onViewCreated(view: View?): Boolean {
        super.onViewCreated(view)
        return fullScreenBehavior()
    }

    private fun init(vg:ViewGroup){
        val editName=vg.findViewById(R.id.edit_name) as EditText
        var name=PreferenceManager.getDefaultSharedPreferences(context).
                getString(PreferenceInfo.PREF_DEVICE_NAME,Build.MODEL)

        editName.setText(name)

        vg.findViewById(R.id.btn_edit).setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PreferenceInfo.PREF_DEVICE_NAME,editName.text.toString())
                    .commit()
            dismiss()
        }
    }

}