package cn.telltim.guardian.app

import android.util.Log
import androidx.multidex.MultiDexApplication
import cn.telltim.common.ProcessUtil
import cn.telltim.guardian.BuildConfig
import cn.telltim.guardian.app.init.AppInitTask
import cn.telltim.guardian.global.AppConst
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.telltim.startup.AppBootUp
import com.telltim.startup.AppBootUpTaskListener
import com.telltim.startup.Config
import com.telltim.startup.OnProjectListener
import com.telltim.xtask.XThreadTaskManager

import xcrash.ICrashCallback
import xcrash.TombstoneManager
import xcrash.XCrash
import xcrash.XCrash.InitParameters
import java.util.concurrent.ThreadPoolExecutor
import java.util.logging.Level

/**
 * @author :Tim.WJ
 * Created on 2022/3/16.
 */
class GuardianApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        val processName = ProcessUtil.getCurrentProcessName(this)
        java.util.logging.Logger.getLogger(AppConst.TAG).log(Level.INFO, "应用启动 $processName")
        if (BuildConfig.APPLICATION_ID == processName) {
            bootApp()
        }
    }

    private fun bootApp() {
        initLogger()
        initCrash()
        initAppTask()
    }

    private fun initCrash() {

        // callback for java crash, native crash and ANR
        val callback = ICrashCallback { logPath, emergency ->
            java.util.logging.Logger.getLogger(AppConst.TAG).log(
                Level.WARNING, "log path: " + (logPath ?: "(null)") + ", emergency: " + (emergency
                    ?: "(null)")
            )
            if (emergency != null) {
                //debug(logPath, emergency)
                // Disk is exhausted, send crash report immediately.
                //sendThenDeleteCrashLog(logPath, emergency)
            } else {
                // Add some expanded sections. Send crash report at the next time APP startup.
                // OK
                TombstoneManager.appendSection(logPath, "expanded_key_1", "expanded_content")
                TombstoneManager.appendSection(
                    logPath,
                    "expanded_key_2",
                    "expanded_content_row_1\nexpanded_content_row_2"
                )
                // Invalid. (Do NOT include multiple consecutive newline characters ("\n\n") in the content string.)
                // TombstoneManager.appendSection(logPath, "expanded_key_3", "expanded_content_row_1\n\nexpanded_content_row_2");
                //debug(logPath, null)
            }
        }

        val anrFastCallback =
            ICrashCallback { logPath, emergency ->
                java.util.logging.Logger.getLogger(AppConst.TAG)
                    .log(Level.WARNING, "anrFastCallback is called")
            }

        java.util.logging.Logger.getLogger(AppConst.TAG).log(Level.INFO, "xCrash SDK init: start")

        // Initialize xCrash.
        XCrash.init(
            this, InitParameters()
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setJavaRethrow(true)
                .setJavaLogCountMax(10)
                .setJavaDumpAllThreadsWhiteList(arrayOf("^main$", "^Binder:.*", ".*Finalizer.*"))
                .setJavaDumpAllThreadsCountMax(10)
                .setJavaCallback(callback)
                .setNativeRethrow(true)
                .setNativeLogCountMax(10)
                .setNativeDumpAllThreadsWhiteList(
                    arrayOf(
                        "^xcrash\\.sample$",
                        "^Signal Catcher$",
                        "^Jit thread pool$",
                        ".*(R|r)ender.*",
                        ".*Chrome.*"
                    )
                )
                .setNativeDumpAllThreadsCountMax(10)
                .setNativeCallback(callback) //          .setAnrCheckProcessState(false)
                .setAnrRethrow(true)
                .setAnrLogCountMax(10)
                .setAnrCallback(callback)
                .setAnrFastCallback(anrFastCallback)
                .setPlaceholderCountMax(3)
                .setPlaceholderSizeKb(512) //          .setLogDir(getExternalFilesDir("xcrash").toString())
                .setLogFileMaintainDelayMs(1000)
        )
        java.util.logging.Logger.getLogger(AppConst.TAG).log(Level.INFO, "xCrash SDK init: end")
    }

    private fun initAppTask() {
        AppBootUp.Builder()
            .add(AppInitTask())
            .setConfig(Config(AppConst.isStrictMode))
            .addTaskListener(AppBootUpTaskListener(AppConst.TAG, true))
            .setExecutorService(
                XThreadTaskManager.instance.cpuThreadPoolExecutor
            )
            .addOnProjectExecuteListener(object : OnProjectListener {
                override fun onProjectStart() {
                    Logger.t("AppBootUp").i("开始启动应用")
                }

                override fun onProjectFinish() {
                    Logger.t("AppBootUp").i("应用启动完成")
                }

                override fun onStageFinish() {
                    //Logger.d( "onStageFinish")
                }
            })
            .create()
            .start()
            .await()
    }

    private fun initLogger() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .tag(AppConst.TAG)
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