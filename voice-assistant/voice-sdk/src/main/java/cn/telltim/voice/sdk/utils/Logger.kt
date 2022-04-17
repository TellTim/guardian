package cn.telltim.voice.sdk.utils

import android.util.Log

/**
 * Logger
 *
 * @author Tell.Tim
 * @date 2020/4/25 19:45
 */
class Logger private constructor(private val subTag: String) {
    fun dFormat(format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        Log.d(
            MAIN_TAG,
                "$subTag: --> $msg")
    }

    fun d(vararg objects: Any?) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }
        Log.d(
            MAIN_TAG,
                "$subTag:$builder")
    }

    fun iFormat(format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        Log.i(
            MAIN_TAG,
                "$subTag: --> $msg")
    }

    fun i(vararg objects: Any?) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }
        Log.i(
            MAIN_TAG,
                "$subTag:$builder")
    }

    fun wFormat(format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        Log.w(
            MAIN_TAG,
                "$subTag: --> $msg")
    }

    fun w(vararg objects: Any?) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }
        Log.w(
            MAIN_TAG,
                "$subTag:$builder")
    }

    fun eFormat(format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        Log.e(
            MAIN_TAG,
                "$subTag: --> $msg")
    }

    fun e(vararg objects: Any?) {
        val builder = StringBuilder()
        for (o in objects) {
            builder.append(" --> ").append(o)
        }
        Log.e(
            MAIN_TAG,
                "$subTag:$builder")
    }

    companion object {
        private const val MAIN_TAG = "VoiceAssistantSdk"
        fun getLogger(subTag: String): Logger {
            return Logger(subTag)
        }
    }
}