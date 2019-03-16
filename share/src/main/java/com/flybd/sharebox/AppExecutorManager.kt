package com.flybd.sharebox

import com.common.executor.AppExecutors

object AppExecutorManager {

    private val appExecutors: AppExecutors by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        AppExecutors()
    }

    fun getInstance(): AppExecutors {
        return appExecutors
    }
}