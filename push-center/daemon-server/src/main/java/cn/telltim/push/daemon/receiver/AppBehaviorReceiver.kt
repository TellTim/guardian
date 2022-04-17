//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : AppBehaviorReceiver
// Author     : Tim.WJ
// Create At  : 2022/4/17 2:07
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.push.daemon.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class AppBehaviorReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        context?.startService(
            Intent("cn.telltim.push.daemon.action.LAUNCH").setPackage(
                context.packageName
            )
        )
    }
}