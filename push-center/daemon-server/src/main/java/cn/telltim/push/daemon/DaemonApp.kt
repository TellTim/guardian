//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : DaemonApp
// Author     : Tim.WJ
// Create At  : 2022/4/17 10:13
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.push.daemon

import android.app.Application
import android.util.Log
import cn.telltim.common.ProcessUtil
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import java.util.logging.Level

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class DaemonApp:Application() {
    override fun onCreate() {
        super.onCreate()
        val processName = ProcessUtil.getCurrentProcessName(this)
        java.util.logging.Logger.getLogger("Push").log(Level.INFO, "应用启动 $processName")
        if (BuildConfig.APPLICATION_ID == processName) {
            bootApp()
        }
    }

    private fun bootApp() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .tag("Push")
            .methodCount(2)
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return if (priority > Log.DEBUG) {
                    true
                } else {
                    BuildConfig.DEBUG
                }
            }
        })
    }
}