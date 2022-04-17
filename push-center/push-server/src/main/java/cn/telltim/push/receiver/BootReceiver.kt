//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : BootReceiver
// Author     : Tim.WJ
// Create At  : 2022/4/17 0:45
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.push.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @Author     : Tim.WJ
 * @Description:
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // 开机启动核心服务
        context?.startService(
            Intent("cn.telltim.push.action.CALL").setPackage(
                context
                    .packageName
            )
        )
    }
}