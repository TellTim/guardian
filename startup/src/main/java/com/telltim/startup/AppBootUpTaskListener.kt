package com.telltim.startup

import android.os.Process
import android.os.Trace
import cn.telltim.common.ThreadManager
import com.orhanobut.logger.Logger

/**
 *
 * @ProjectName:    Guardian
 * @Package:        com.telltim.startup
 * @ClassName:      AppBootUpTaskListener
 * @Description:    应用启动任务监听器
 * @Author:         Tim.WJ
 * @CreateDate:     2022/3/16 16:54
 * @UpdateUser:     Tim.WJ
 * @UpdateDate:     2022/3/16 16:54
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class AppBootUpTaskListener (private val tag: String, private val isLog: Boolean) : TaskListener {
    override fun onWaitRunning(task: AppBootUpTask?) {}
    override fun onStart(task: AppBootUpTask?) {
        if (isLog) {
            Logger.t(task!!.taskName).d("任务启动 :" + task.taskName)
        }
        Trace.beginSection(task!!.taskName)
        if (task.isWaitOnMainThread) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        }
    }

    override fun onFinish(task: AppBootUpTask?, dw: Long, df: Long) {
        if (task!!.isWaitOnMainThread) {
            Process.setThreadPriority(ThreadManager.DEFAULT_PRIORITY)
        }
        Trace.endSection()
        if (isLog) {
            Logger.t(task.taskName).i("任务结束 :" + task.taskName + " wait " + dw + " cost " + df)
        }
    }
}