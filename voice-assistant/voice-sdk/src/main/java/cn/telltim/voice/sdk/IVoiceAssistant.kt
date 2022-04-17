//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : IVoiceAssistant
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:04
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//

package cn.telltim.voice.sdk

import android.content.Context
import cn.telltim.voice.sdk.response.CommunicatorResponse
import cn.telltim.voice.sdk.response.InterceptorResponse
import cn.telltim.voice.sdk.response.RecognizerResponse
import cn.telltim.voice.sdk.response.SDKResponseInit

/**
 * @Author     : Tim.WJ
 * @Description:
 */
interface IVoiceAssistant {
    /**
     * 初始化语音助手
     */
    fun init(context: Context, response: SDKResponseInit? = null)

    /**
     * 销毁语音助手
     */
    fun destroy()

    /**
     * 开启唤醒识别
     */
    fun start()

    /**
     * 停止唤醒识别
     */
    fun stop()

    /**
     *  拦截监听器
     */
    fun setInterceptorListener(name: String, response: InterceptorResponse)

    /**
     * 识别监听器
     */
    fun setRecognitionListener(response: RecognizerResponse)

    /**
     * 交互监听器
     */
    fun setCommunicateListener(response: CommunicatorResponse)

    /**
     * tts播报
     */
    fun startPlayText(ttsText: String?)

    /**
     * 停止播报
     */
    fun stopPlayText()

    /**
     * tts播报暂停
     */
    fun pausePlayText()

    /**
     * tts播报恢复
     */
    fun resumePlayText()

    /**
     * 开启wifi设置
     */
    fun openWifi(context: Context)
}