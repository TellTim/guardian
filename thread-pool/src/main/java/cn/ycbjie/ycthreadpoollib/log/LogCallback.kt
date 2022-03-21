//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : LogCallback
// Author     : Tim.WJ
// Create At  : 2022/3/20 22:40
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.ycbjie.ycthreadpoollib.log

import cn.ycbjie.ycthreadpoollib.callback.ThreadCallback
import com.orhanobut.logger.Logger

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class LogCallback:ThreadCallback {

    private val _tag = "LogCallback"

    override fun onError(name: String, t: Throwable) {
        Logger.t(_tag).e("onError :$name ${t.message}")
    }

    override fun onCompleted(name: String) {
        Logger.t(_tag).i("onCompleted :$name")
    }

    override fun onStart(name: String) {
        Logger.t(_tag).i("onStart :$name")
    }
}