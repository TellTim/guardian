//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : RecognizerResponse
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:11
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.voice.sdk.response

import cn.telltim.voice.app.binder.IBaseResponse

/**
 * @Author     : Tim.WJ
 * @Description:
 */
open abstract class RecognizerResponse: IBaseResponse<String> {
    /**
     * 数据拦截处理
     */
    final override fun onResponse(code: Int, data: String?) {
        if(code == 0 && !data.isNullOrEmpty()){
            onRecognizer(data)
        }
    }

    /**
     * 回调
     */
    abstract fun onRecognizer(data:String)
}