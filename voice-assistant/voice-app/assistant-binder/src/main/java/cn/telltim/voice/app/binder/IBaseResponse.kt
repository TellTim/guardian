//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : IBaseResponse
// Author     : Tim.WJ
// Create At  : 2022/4/11 0:56
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//

package cn.telltim.voice.app.binder

/**
 * @Author     : Tim.WJ
 * @Description: 统一跨进程回调类
 */
interface IBaseResponse<T> {

    fun onResponse(code:Int, data:T? = null)

    companion object {
        //数据KEY
        const val EXTRA_VALUE = "extra_value"

        //数据KEY1
        const val EXTRA_VALUE_1 = "extra_value_1"

        //服务尚未启动
        const val SERVICE_UNREADY = -1

        //初始化申请
        const val INIT_SDK_SETUP: Int = 100

        //申请recognizer
        const val RECOGNIZER_SETUP: Int = 101

        //申请拦截器
        const val INTERCEPTOR_SETUP: Int = 102

        //申请Communicator
        const val COMMUNICATOR_SETUP: Int = 103

        //开启语音唤醒
        const val WAKE_UP_OPEN: Int = 104

        //关闭语音唤醒
        const val WAKE_UP_CLOSE: Int = 105

        //播放TTS
        const val TTS_PLAY: Int = 106

        //停止播放TTS
        const val TTS_STOP: Int = 107

        //暂停播放TTS
        const val TTS_PAUSE: Int = 108

        //恢复播放TTS
        const val TTS_RESUME: Int = 109


        //成功
        const val SUCCESS = 0

        //泛化错误(没有指定具体错误)
        const val ERROR = 1

        //无网络
        const val NO_INTERNET_ERROR: Int = 2001
    }
}