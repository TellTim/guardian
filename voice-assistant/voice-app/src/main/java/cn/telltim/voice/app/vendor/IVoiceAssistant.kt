//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : IVoiceAssistant
// Author     : Tim.WJ
// Create At  : 2022/4/11 8:00
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//

package cn.telltim.voice.app.vendor

import android.os.Bundle
import cn.telltim.voice.app.binder.IBaseResponse

/**
 * @Author     : Tim.WJ
 * @Description:
 */
interface IVoiceAssistant {

    /**
     * 初始化SDK
     */
    fun setupSDK(bundle: Bundle, response:IBaseResponse<Bundle>)

    /**
     * 注册私有业务
     */
    fun setupRecognizer(response: IBaseResponse<Bundle>)

    /**
     * 注册拦截器
     */
    fun setupInterceptor(name:String,response: IBaseResponse<Bundle>)

    /**
     * 注册交互
     */
    fun setupCommunicator(response: IBaseResponse<Bundle>)

    /**
     * 开启获取关闭唤醒功能
     */
    fun wakeUpSwitch(boolean: Boolean)

    /**
     * 开启语音播报
     */
    fun playTTS(tts:String?)

    /**
     * 停止语音播报
     */
    fun stopTTS()

    /**
     * 暂停语音播报
     */
    fun pauseTTS()

    /**
     * 恢复语音播报
     */
    fun resumeTTS()

    /**
     * 是否初始化过
     */
    fun isInitialized():Boolean

    /**
     * 重新授权
     */
    fun reAuth()

    /**
     * 添加日志
     */
    fun addLog(response:IBaseResponse<Bundle>)


    interface INetworkAvailable{
        //网络可用
        fun onNetEnabled()
        //网络信号差
        fun onNetDisabled()
        //网络未连接
        fun onNetDisconnected()
    }
}