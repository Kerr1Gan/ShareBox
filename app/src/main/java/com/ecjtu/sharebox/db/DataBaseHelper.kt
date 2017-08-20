package com.ecjtu.sharebox.db

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Ethan_Xiang on 2017/8/14.
 */
class DataBaseHelper:SQLiteOpenHelper{

    constructor(context: Context, name: String, factory: SQLiteDatabase.CursorFactory, version: Int):super(context, name, factory, version){
    }

    constructor(context: Context, name: String, factory: SQLiteDatabase.CursorFactory, version: Int,
                         errorHandler: DatabaseErrorHandler?):super(context, name, factory, version,errorHandler){
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}