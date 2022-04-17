//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : VoiceAssistantResponse
// Author     : Tim.WJ
// Create At  : 2022/4/11 1:05
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.voice.app.binder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * @Author     : Tim.WJ
 * @Description:
 */
abstract class VoiceAssistantResponse:IResponse.Stub(),Handler.Callback {

    companion object{
        private const val MSG_RESPONSE:Int = 1
    }

    private lateinit var mHandler: Handler
    init {
        Looper.myLooper()?.let {
            mHandler = Handler(Looper.myLooper()!!, this@VoiceAssistantResponse)
        }?:run {
            throw RuntimeException("voiceAssistantResponse init error!")
        }
    }

    /**
     * 跨进程回调
     */
    override fun onResponse(code: Int, data: Bundle?) {
        mHandler.obtainMessage(MSG_RESPONSE,code,0, data).sendToTarget();
    }

    /**
     * 工作线程处理回调
     */
    protected abstract fun onAsyncResponse(code: Int, data: Bundle?)

    /**
     * 工作线程处理
     */
    override fun handleMessage(message: Message): Boolean {
        when(message.what){
            MSG_RESPONSE->{
                onAsyncResponse(message.arg1, message.obj as Bundle?);
            }
        }
        return true
    }
}