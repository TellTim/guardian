//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : CommunicatorResponse
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:10
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.voice.sdk.response

import android.os.Bundle
import cn.telltim.voice.app.binder.IBaseResponse

/**
 * @Author     : Tim.WJ
 * @Description:
 */
open abstract class CommunicatorResponse: IBaseResponse<Bundle> {

    /**
     * 数据拦截处理
     */
    final override fun onResponse(code: Int, data: Bundle?) {
        val recognizeText = data?.getString(IBaseResponse.EXTRA_VALUE)
        val replyText = data?.getString(IBaseResponse.EXTRA_VALUE_1)
        if (!recognizeText.isNullOrEmpty()) {
            onRecognition(recognizeText!!)
        }
        if (!replyText.isNullOrEmpty()) {
            onReply(replyText!!)
        }
    }

    /**
     * 文字数据回调
     */
    abstract fun onRecognition(recognizeText: String)

    /**
     * JSON数据回调
     */
    abstract fun onReply(replyText: String)
}