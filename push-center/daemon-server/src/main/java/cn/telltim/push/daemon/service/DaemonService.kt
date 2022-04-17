//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : DaemonService
// Author     : Tim.WJ
// Create At  : 2022/4/17 1:38
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.push.daemon.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import cn.telltim.puhs.daemin.binder.IDaemonBinderService
import cn.telltim.push.daemon.receiver.AppBehaviorReceiver
import com.orhanobut.logger.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * @Author     : Tim.WJ
 * @Description:
 *  守护进程:
 *  通过以下手段进行保活
 *  1 前台服务
 *  2 监听应用的安装,卸载,启动服务
 *  3 心跳包
 *  4 双进程互相绑定服务
 */
class DaemonService : Service() {

    companion object {
        const val TAG = "DaemonService"
    }

    //跨进程binder
    private val binder = ServiceBinder()

    private val appBehaviorReceiver = AppBehaviorReceiver()

    private val heartbeatHandler: HeartbeatHandler =
        HeartbeatHandler()

    /**
     * 返回绑定器
     */
    override fun onBind(intent: Intent?): IBinder {
        Logger.t(TAG).d("onBind ${intent?.action}")
        return binder
    }

    inner class ServiceBinder : IDaemonBinderService.Stub()

    /**
     * 服务连接
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Logger.t(TAG).d("onServiceDisconnected")
            stopSelf()
            GlobalScope.launch {
                delay(2 * 1000)
                bindCoreService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.t(TAG).d("onServiceConnected")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.t(TAG).d("onCreate");
        bindCoreService()
        setForeground()
        registerReceiver(appBehaviorReceiver, IntentFilter().apply {
            addAction("android.intent.action.PACKAGE_INSTALL")
            addAction("android.intent.action.PACKAGE_ADDED")
            addAction("android.intent.action.PACKAGE_REPLACED")
        })

        heartbeatHandler.setOnDeadCallback(object : OnDeadCallback {
            override fun onDead() {
                Logger.t(TAG).d("unReceiver the target heartbeat,then relaunch it")
                val intent =
                    packageManager.getLaunchIntentForPackage("cn.telltim.push")

                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }?: kotlin.run {
                    val launcherIntent = Intent()
                    launcherIntent.setClassName(
                        "cn.telltim.push",
                        "cn.telltim.push.MainActivity"
                    )
                    launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launcherIntent)
                }
            }
        })

    }

    /**
     * 服务销毁
     */
    override fun onDestroy() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(100)
        try {
            unbindService(mServiceConnection)
        } catch (e: Throwable) {
            Logger.t(TAG).d("unbindService exception ${e.message}")
        }
        try {
            unregisterReceiver(appBehaviorReceiver)
        } catch (e: Throwable) {
            Logger.t(TAG).d("unregisterReceiver exception ${e.message}")
        }
        heartbeatHandler.setOnDeadCallback(null)
        Log.d(TAG, "DaemonService onDestroy")
        super.onDestroy()
    }

    /**
     * 启动前台服务，保活处理
     */
    private fun setForeground() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "channel_id")
        with(builder) {
            setSmallIcon(android.R.drawable.presence_online)
            setAutoCancel(false)
            setSound(null)
            setOnlyAlertOnce(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId("notification_id")
                val channel = NotificationChannel(
                    "notification_id",
                    "support_service",
                    NotificationManager.IMPORTANCE_MIN
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
        startForeground(100, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.t(TAG).d("onStartCommand ${intent?.action}")
        intent?.let {
            if (it.action == "cn.telltim.push.daemon.action.KEEP_ALIVE_ACTION") {
                heartbeatHandler.checkHeartbeat()
            }
        }

        return START_STICKY
    }

    /**
     * 绑定主应用服务
     */
    private fun bindCoreService() {
        if (!applicationContext.bindService(Intent().apply {
                component = ComponentName(
                    "cn.telltim.push",
                    "cn.telltim.push.service.CoreService"
                )
                setPackage(packageName)
                type = packageName
            }, mServiceConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "绑定失败")
        }
        startService(Intent().apply {
            action = "cn.telltim.push.action.CALL"
            component = ComponentName(
                "cn.telltim.push",
                "cn.telltim.push.service.CoreService"
            )
            setPackage(packageName)
        })
    }

    internal class HeartbeatHandler : Handler() {
        private var current = 0L
        private var onDeadCallback: OnDeadCallback? = null

        fun setOnDeadCallback(onDeadCallback: OnDeadCallback?) {
            this.onDeadCallback = onDeadCallback
        }

        fun checkHeartbeat() {
            val msg = Message.obtain()
            val current = System.currentTimeMillis()
            msg.obj = current
            this.current = current
            sendMessageDelayed(msg, TimeUnit.SECONDS.toMillis(15))
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) {
                Logger.t(TAG).d("msg.obj is ${msg.obj},current is $current")
                if (msg.obj == current) {
                    // 每一个 keep-alive 的消息都应该被下一个的置换掉，若未置换则说明主程序发送 keep-alive 超时
                    onDeadCallback?.onDead() ?: kotlin.run {
                        Logger.t(TAG).e("onDeadCallback is empty")
                    }
                }
            }
        }
    }

    internal interface OnDeadCallback {
        fun onDead()
    }

}