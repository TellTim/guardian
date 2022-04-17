package cn.telltim.voice.client

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import cn.telltim.voice.sdk.VoiceAssistantClient
import cn.telltim.voice.sdk.response.CommunicatorResponse
import cn.telltim.voice.sdk.response.InterceptorResponse
import cn.telltim.voice.sdk.response.RecognizerResponse
import cn.telltim.voice.sdk.response.SDKResponseInit
import com.alibaba.fastjson.JSONObject

class VoiceAssistantClientActivity : AppCompatActivity() {

    private val initBtn: AppCompatButton? = null
    private val killBtn: AppCompatButton? = null
    private val wakeUpOnBtn: AppCompatButton? = null
    private val wakeUpOffBtn: AppCompatButton? = null
    private val sayBtn: AppCompatButton? = null
    private val stopSayBtn: AppCompatButton? = null
    private val resumeBtn: AppCompatButton? = null
    private val exitBtn: AppCompatButton? = null
    private val wifiBtn: AppCompatButton? = null
    private val pauseBtn: AppCompatButton? = null
    private val editView: AppCompatEditText? = null
    private val progress: ProgressBar? = null
    private val initCall: AppCompatTextView? = null
    private val interceptorTxt:AppCompatTextView? = null
    private val recognitionTxt:AppCompatTextView? = null
    private val communicateTxt1:AppCompatTextView? = null
    private val communicateTxt2:AppCompatTextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant_client)
        initView()
        initAssistant()
        killAssistantAction()
        wakeUpAction()
        initTTsAction()
        wifiBtn?.setOnClickListener {
            VoiceAssistantClient.instance.openWifi(this@VoiceAssistantClientActivity)
        }
        exitBtn?.setOnClickListener {
            finish()
        }

    }

    private fun initView() {
        // todo set UI
    }

    //初始化助手
    private fun initAssistant() {
        initBtn?.setOnClickListener {
            progress?.visibility = View.VISIBLE
            VoiceAssistantClient.instance.init(
                this.application,
                response = object : SDKResponseInit() {
                    /**
                     * @param code 初始化回调
                     */
                    override fun onInitSdkCallBack(code: Int) {
                        //回调在子线程！
                        initCall?.post {
                            initCall.text = "初始化回调:$code"
                            progress?.visibility = View.GONE
                            if (code == 0) {
                                interceptorAction()
                                recognitionAction()
                                initCommunicateAction()
                            }
                        }
                    }
                }
            )
        }
    }

    //销毁语音助手
    private fun killAssistantAction() {
        killBtn?.setOnClickListener {
            VoiceAssistantClient.instance.destroy()
            initCall?.text = "销毁服务"
        }
    }

    //开启或者关闭唤醒
    private fun wakeUpAction() {
        wakeUpOnBtn?.setOnClickListener {
            VoiceAssistantClient.instance.start()
            initCall?.text = "开启唤醒"
        }

        wakeUpOffBtn?.setOnClickListener {
            VoiceAssistantClient.instance.stop()
            initCall?.text = "关闭唤醒"
        }
    }

    //TTS相关
    private fun initTTsAction() {
        sayBtn?.setOnClickListener {
            val says: String? = editView?.text.toString().trim()
            if (says.isNullOrEmpty()) {
                initCall?.text = "无播放内容"
                return@setOnClickListener
            }
            VoiceAssistantClient.instance.startPlayText(says)
            initCall?.text = "播放TTS:$says"
        }
        stopSayBtn?.setOnClickListener {
            VoiceAssistantClient.instance.stopPlayText()
            initCall?.text = "停止播放TTS"
        }
        pauseBtn?.setOnClickListener {
            VoiceAssistantClient.instance.pausePlayText()
            initCall?.text = "暂停播放TTS"
        }
        resumeBtn?.setOnClickListener {
            VoiceAssistantClient.instance.resumePlayText()
            initCall?.text = "恢复播放TTS"
        }
    }

    //前置拦截器
    private fun interceptorAction(){
        VoiceAssistantClient.instance.setInterceptorListener(
            BuildConfig.CUSTOM_PREFIX,
            object: InterceptorResponse() {
                override fun onIntercept(data: String) {
                    val payload: JSONObject = JSONObject.parseObject(data)
                    try {
                        if (payload.contains("headerName")) {
                            val headerName = payload["headerName"]
                            val data = payload.getJSONObject("data")
                            if (headerName !is String) return
                            when (headerName) {
                                "test.open.app" -> {
                                    interceptorTxt?.post {
                                        interceptorTxt.text = String.format(getString(R.string.str_matching_simple), headerName)
                                    }
                                }
                                "test.query.app" -> {
                                    val paramItem = data[("item")]
                                    interceptorTxt?.post {
                                        interceptorTxt.text = String.format(
                                            getString(R.string.str_matching),
                                            headerName,
                                            paramItem)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Demo", "transmit object exception ${e.message}")
                    }
                }
            })
    }

    //识别监听器
    fun recognitionAction(){
        VoiceAssistantClient.instance.setRecognitionListener(
            response = object: RecognizerResponse() {
                override fun onRecognizer(data: String) {
                    recognitionTxt?.post {
                        recognitionTxt.text = "识别:$data"
                    }
                }
            })
    }

    // 交互监听器
    fun initCommunicateAction(){
        VoiceAssistantClient.instance.setCommunicateListener(
            object: CommunicatorResponse() {
                override fun onRecognition(recognizeText: String) {
                    communicateTxt1?.post {
                        communicateTxt1.text = "实时语句:$recognizeText"
                    }
                }
                override fun onReply(replyText: String) {
                    communicateTxt2?.post {
                        communicateTxt2.text = "云回答:$replyText"
                    }
                }
            })
    }

}