package com.ecjtu.sharebox.db.table

import android.database.sqlite.SQLiteDatabase

/**
 * Created by Ethan_Xiang on 2017/8/14.
 */
interface BaseTable {
    val _id: String
        get() = "_id"

    fun createTable(sqLiteDatabase: SQLiteDatabase)
    fun deleteTable(sqLiteDatabase: SQLiteDatabase)
}