//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : CoreService
// Author     : Tim.WJ
// Create At  : 2022/4/17 0:31
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.push.service

import android.app.Service
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import cn.telltim.puhs.daemin.binder.IDaemonBinderService
import com.orhanobut.logger.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import cn.telltim.common.ContextWrapper

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class CoreService : Service() {

    companion object {
        const val TAG = "CoreService"
    }

    //跨进程binder
    private val binder = ServiceBinder()
    private val handler = Handler()

    /**
     * 服务连接
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            GlobalScope.launch {
                delay(2000)
                bindDaemonService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.t(TAG).d("onServiceConnected")
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        Logger.t(TAG).d("onBind ${intent?.action}")
        return binder
    }

    /**
     * 推送服务启动
     * 启动前台进程
     * 启动极光推送
     */
    override fun onCreate() {
        super.onCreate()
        Logger.t(TAG).d("onCreate")
        bindDaemonService()
        handler.post(object : Runnable {
            override fun run() {
                sendKeepAlive()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5))
            }
        })
    }

    private fun sendKeepAlive() {
        try {
            val intent = Intent()
            intent.action = "cn.telltim.push.daemon.action.KEEP_ALIVE_ACTION"
            intent.setClassName(
                "cn.telltim.push.daemon",
                "cn.telltim.push.daemon.service.DaemonService"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextWrapper.startForegroundServiceAsUser(baseContext, intent,
                    "CURRENT")
            } else {
                ContextWrapper.startServiceAsUser(baseContext, intent, "CURRENT")
            }
            Logger.t(TAG).d("sendKeepAlive")
        } catch (t: Throwable) {
            Logger.t(TAG).e("sendKeepAlive startService exception ${t.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun bindDaemonService() {
        if (!applicationContext.bindService(Intent().apply {
                component = ComponentName(
                    "cn.telltim.push.daemon",
                    "cn.telltim.push.daemon.service.DaemonService"
                )
                setPackage(packageName)
                type = packageName
            }, mServiceConnection, Context.BIND_AUTO_CREATE)) {
            Logger.t(TAG).e( "bindDaemonService failed")
        }
        try {
            startService(Intent().apply {
                action = "cn.telltim.push.daemon.action.DAEMON"
                component = ComponentName(
                    "cn.telltim.push.daemon",
                    "cn.telltim.push.daemon.service.DaemonService"
                )
                setPackage(packageName)
            })
        } catch (e: Exception) {
            Logger.t(TAG).e( "start DaemonService exception ${e.message}")
        }

    }

    /**
     * 服务被销毁
     */
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        try {
            unbindService(mServiceConnection)
        } catch (e: Throwable) {
            Logger.t(TAG).e("unbindService exception ${e.message}")
        }
        Logger.t(TAG).d("onDestroy")
        super.onDestroy()
    }

    inner class ServiceBinder : IDaemonBinderService.Stub()

}