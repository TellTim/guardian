//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : VoiceAssistantClient
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:13
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.voice.sdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import cn.telltim.common.ContextWrapper
import cn.telltim.voice.app.binder.IBaseResponse
import cn.telltim.voice.app.binder.IVoiceAssistantService
import cn.telltim.voice.app.binder.VoiceAssistantResponse
import cn.telltim.voice.sdk.response.CommunicatorResponse
import cn.telltim.voice.sdk.response.InterceptorResponse
import cn.telltim.voice.sdk.response.RecognizerResponse
import cn.telltim.voice.sdk.response.SDKResponseInit
import cn.telltim.voice.sdk.utils.Logger
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class VoiceAssistantClient private constructor() : IVoiceAssistant {

    companion object {
        const val TAG = "VoiceAssistantClient"
        val instance = VoiceAssistantClientHolder.holder

        //模块意图类标志
        private const val ROOT_ACTION_PREFIX = "cn.telltim.voice.assistant.module"

        //设置类Activity
        private const val ACTION_MODULE_SETTING = "$ROOT_ACTION_PREFIX.setting"

        //WIFI设置意图
        const val ACTION_WIFI_SETTING = "$ACTION_MODULE_SETTING.wifi"

        private const val ACTION_PERMISSION =
            "cn.telltim.voice.assistant.permission.ACCESS_VOICE_ASSISTANT"

        private val targetVoiceAssistantReceiverComponent = ComponentName(
            "cn.telltim.voice.assistant",
            "cn.telltim.voice.assistant.android.receiver.VoiceAssistantReceiver"
        )
        private val targetVoiceAssistantServiceComponent = ComponentName(
            "cn.telltim.voice.assistant",
            "cn.telltim.voice.assistant.android.service.VoiceAssistantService"
        )
    }

    private val logger = Logger.getLogger("client")
    private var mContext: WeakReference<Context>? = null

    //工作线程,外部调用的方式全部通过子线程调用跨进程
    private val mWorkerThread: HandlerThread = HandlerThread("VoiceAssistantClientWorker")

    //工作线程派发Handle
    private val mWorkerHandler: Handler = Handler(mWorkerThread.looper)

    //等待器
    private var mCountDownLatch: CountDownLatch? = null

    //语音助手服务
    private var voiceAssistantService: IVoiceAssistantService? = null

    //服务链接
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            voiceAssistantService = IVoiceAssistantService.Stub.asInterface(service)
            logger.d("onServiceConnected:$voiceAssistantService")
            notifyServiceReady()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            voiceAssistantService = null
            logger.d("onServiceDisconnected:$voiceAssistantService")
        }
    }

    private object VoiceAssistantClientHolder {
        val holder = VoiceAssistantClient()
    }

    init {
        mWorkerThread.start()
    }

    /**
     * 服务准备就绪可以使用
     */
    private fun notifyServiceReady() {
        mCountDownLatch?.countDown()
        mCountDownLatch = null
    }

    /**
     * 阻塞等待服务绑定
     */
    private fun waitServiceReady() {
        try {
            mCountDownLatch?.await()
        } catch (e: InterruptedException) {
            logger.e("waitServiceReady exception ${e.message}")
        }
    }

    /**
     * 语音助手初始化
     */
    override fun init(context: Context, response: SDKResponseInit?) {
        this.mContext = WeakReference(context)
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.INIT_SDK_SETUP, Bundle(), WeakReference(
                object :
                    VoiceAssistantResponse() {
                    override fun onAsyncResponse(code: Int, data: Bundle?) {
                        response?.onResponse(code, data?.getString(IBaseResponse.EXTRA_VALUE))
                    }
                }
            ))
        }
    }

    override fun destroy() {
        voiceAssistantService?.let {
            try {
                mContext?.get()?.applicationContext?.unbindService(mConnection)
            } catch (e: RuntimeException) {
                logger.e("destroy voiceAssistant exception ${e.message}")
            }
        }
        mContext = null
        voiceAssistantService = null
    }

    /**
     * 开启唤醒识别
     */
    override fun start() {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.WAKE_UP_OPEN)
        }
    }

    /**
     * 停止唤醒识别
     */
    override fun stop() {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.WAKE_UP_CLOSE)
        }
    }

    /**
     * 设置业务拦截器
     */
    override fun setInterceptorListener(name: String, response: InterceptorResponse) {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.INTERCEPTOR_SETUP,
                Bundle().apply { putString(IBaseResponse.EXTRA_VALUE, name) },
                WeakReference(
                    object : VoiceAssistantResponse() {
                        override fun onAsyncResponse(code: Int, data: Bundle?) {
                            response.onResponse(code, data?.getString(IBaseResponse.EXTRA_VALUE))
                        }
                    }
                ))
        }
    }

    /**
     * 设置识别器
     */
    override fun setRecognitionListener(response: RecognizerResponse) {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.RECOGNIZER_SETUP, Bundle(), WeakReference(
                object :
                    VoiceAssistantResponse() {
                    override fun onAsyncResponse(code: Int, data: Bundle?) {
                        response.onResponse(code, data?.getString(IBaseResponse.EXTRA_VALUE))
                    }
                }
            ))
        }
    }

    /**
     * 注册交互监听器
     */
    override fun setCommunicateListener(response: CommunicatorResponse) {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.COMMUNICATOR_SETUP, Bundle(), WeakReference(
                object : VoiceAssistantResponse() {
                    override fun onAsyncResponse(code: Int, data: Bundle?) {
                        response.onResponse(code, data)
                    }
                }
            ))
        }
    }

    /**
     * tts播报
     */
    override fun startPlayText(ttsText: String?) {
        ttsText?.let {
            mWorkerHandler.post {
                voiceAssistantApiByBinder(IBaseResponse.TTS_PLAY, Bundle().apply {
                    putString(
                        IBaseResponse.EXTRA_VALUE,
                        it
                    )
                })
            }
        }
    }

    /**
     * 停止播报
     */
    override fun stopPlayText() {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.TTS_STOP)
        }
    }

    /**
     * tts播报暂停
     */
    override fun pausePlayText() {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.TTS_PAUSE)
        }
    }

    /**
     * tts播报恢复
     */
    override fun resumePlayText() {
        mWorkerHandler.post {
            voiceAssistantApiByBinder(IBaseResponse.TTS_RESUME)
        }
    }

    override fun openWifi(context: Context) {
        ContextWrapper.sendBroadcastAsUser(
            context,
            Intent().apply {
                component = targetVoiceAssistantReceiverComponent
                action = "cn.telltim.voice.assistant.action.HIDE_ACTIVITY"
                putExtra(IBaseResponse.EXTRA_VALUE, ACTION_WIFI_SETTING)
            },
            "CURRENT",
            ACTION_PERMISSION
        )
    }

    /**
     * 开始跨进程调用服务
     * @param args
     * @param response
     */
    private fun voiceAssistantApiByBinder(
        code: Int, args: Bundle? = null, response:
        WeakReference<VoiceAssistantResponse>? = null
    ) {
        if (code == IBaseResponse.INIT_SDK_SETUP) {
            voiceAssistantService ?: kotlin.run { bindServiceSync() }
        }
        val service = voiceAssistantService
        service?.let {
            try {
                service.voiceAssistantApi(code, args, response?.get())
            } catch (e: Throwable) {
                logger.e("voiceAssistantApi exception ${e.message}")
                response?.get()?.onResponse(IBaseResponse.ERROR, null)
            }
        } ?: run {
            //service为空
            response?.get()?.onResponse(IBaseResponse.SERVICE_UNREADY, null)
        }
    }

    /**
     * 绑定服务
     */
    private fun bindServiceSync() {
        mContext?.get()?.let {
            mCountDownLatch = CountDownLatch(1)
            val intent = Intent()
            intent.component = targetVoiceAssistantServiceComponent
            intent.setPackage("cn.telltim.voice.assistant")
            intent.type = it.packageName
            if (it.applicationContext?.bindService(
                    intent,
                    mConnection,
                    Context.BIND_AUTO_CREATE
                ) == true
            ) {
                waitServiceReady()
            } else {
                logger.e("canNotBind")
            }
        } ?: kotlin.run {
            logger.e("canNotBind")
        }
    }
}