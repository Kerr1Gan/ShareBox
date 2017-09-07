package com.ecjtu.sharebox.db

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import com.ecjtu.sharebox.db.table.impl.BaseTableImpl

/**
 * Created by Ethan_Xiang on 2017/8/15.
 */
class DatabaseManager(context: Context? = null) {

    private var mContext: Context? = null

    private var mDatabaseHelper: DataBaseHelper? = null

    init {
        if (context != null) {
            mContext = context
        }
    }

    fun withHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory, version: Int,
                   errorHandler: DatabaseErrorHandler? = null): DataBaseHelper? {
        mDatabaseHelper = DataBaseHelper(context, name, factory, version, errorHandler)
        return mDatabaseHelper
    }

    companion object {
        fun getInstance(context: Context? = null): DatabaseManager? {
            return DatabaseManager(context)
        }

        //C


        //R
        fun <T : BaseTableImpl> getById(dataBaseHelper: DataBaseHelper, obj: T) {

        }


        //U


        //D
    }
}