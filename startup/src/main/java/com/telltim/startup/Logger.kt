package com.telltim.startup

import android.util.Log

interface Logger {

    fun e(tag: String?, msg: String?, e: Throwable?)

    fun d(tag: String?, msg: String?)


    class DefaultLogger : Logger {
        override fun e(tag: String?, msg: String?, e: Throwable?) {
            Log.e(tag, msg!!)
        }

        override fun d(tag: String?, msg: String?) {
            Log.d(tag, msg!!)
        }
    }
}