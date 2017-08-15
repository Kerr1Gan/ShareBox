package com.ecjtu.sharebox.db.table

/**
 * Created by Ethan_Xiang on 2017/8/14.
 */
interface BaseTable {
    val id: String
        get() = "_id"
}