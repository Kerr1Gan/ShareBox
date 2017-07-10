package com.ecjtu.sharebox.domain

import java.io.File

/**
 * Created by Ethan_Xiang on 2017/7/3.
 */
data class DeviceInfo(var name: String,var ip:String ="",var port:Int =0,
                          var icon:String?="",var fileMap:MutableMap<String,List<String>>? = null)