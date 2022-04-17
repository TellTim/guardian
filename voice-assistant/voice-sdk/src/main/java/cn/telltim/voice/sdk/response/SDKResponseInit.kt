//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : SDKInitResponse
// Author     : Tim.WJ
// Create At  : 2022/4/17 22:06
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
open abstract class SDKResponseInit : IBaseResponse<String?> {
    /**
     * 分拆数据，根据需求暴露给调用者
     */
    final override fun onResponse(code: Int, data: String?) {
        onInitSdkCallBack(code)
    }

    /**
     * 暴露给调用者
     */
    abstract fun onInitSdkCallBack(code: Int)
}