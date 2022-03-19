package cn.telltim.guardian.app

import android.util.Log
import androidx.multidex.MultiDexApplication
import cn.telltim.common.ProcessUtil
import cn.telltim.common.ThreadManager
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
import java.util.logging.Level

/**
 * @author :Tim.WJ
 * Created on 2022/3/16.
 */
class GuardianApp: MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        val processName = ProcessUtil.getCurrentProcessName(this)
        java.util.logging.Logger.getLogger(AppConst.TAG).log(Level.INFO,"应用启动 $processName")
        if (BuildConfig.APPLICATION_ID == processName) {
            bootApp()
        }
    }

    private fun bootApp() {
        initLogger()
        initAppTask()
    }

    private fun initAppTask() {
        AppBootUp.Builder()
            .add(AppInitTask())
            .setConfig(Config(AppConst.isStrictMode))
            .addTaskListener(AppBootUpTaskListener(AppConst.TAG, true))
            .setExecutorService(ThreadManager.getInstance().WORK_EXECUTOR)
            .addOnProjectExecuteListener(object : OnProjectListener {
                override fun onProjectStart() {
                    Logger.t("AppBootUp").i( "开始启动应用")
                }

                override fun onProjectFinish() {
                    Logger.t("AppBootUp").i( "应用启动完成")
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
                return if (priority == Log.DEBUG) {
                    true
                } else {
                    BuildConfig.DEBUG
                }
            }
        })
    }
}