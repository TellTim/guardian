//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : InterceptorResponse
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:08
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
open abstract class InterceptorResponse : IBaseResponse<String> {
    /**
     * 数据拦截处理
     */
    override fun onResponse(code: Int, data: String?) {
        if(code == 0 && !data.isNullOrEmpty()){
            onIntercept(data)
        }
    }

    /**
     * 技能回调
     */
    abstract fun onIntercept(data: String)
}