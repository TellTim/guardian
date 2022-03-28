//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : XThreadFactory
// Author     : Tim.WJ
// Create At  : 2022/3/23 23:54
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package com.smartwasp.xtask.factory

import android.os.Process
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author     : Tim.WJ
 * @Description: 可设置前缀和优先级的线程工厂
 */
class XThreadFactory(private val threadName: String,private val priority:Int = Process.THREAD_PRIORITY_DEFAULT) :ThreadFactory {

    private val mThreadId = AtomicInteger(0)

    override fun newThread(p0: Runnable?): Thread {
        return object : Thread(p0, "${threadName}-" + mThreadId.getAndIncrement()) {
            override fun run() {
                Process.setThreadPriority(priority)
                super.run()
            }
        }
    }
}