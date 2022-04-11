//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : NetWorkUtil
// Author     : Tim.WJ
// Create At  : 2022/4/10 21:40
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import com.telltim.xtask.XThreadTaskManager

/**
 * @Author     : Tim.WJ
 * @Description:
 */
object NetWorkUtil {

    private const val MIUI_PING_URL = "https://connect.rom.miui.com/generate_204"


    /**
     * 检查网络是否可用
     */
    fun checkNetworkAvailable(
        onSuccess: () -> Unit,
        onFailed: ((throwable: Throwable?, responseCode: Int) -> Unit)? = null
    ) {
        XThreadTaskManager.instance.addNetTask(Runnable {
            var code = -1
            // todo 后续实现
            /*try {
                val request = Request.Builder()
                    .url(MIUI_PING_URL)
                    .get()
                    .build()
                val response = client.newCall(request).execute()

                code = response.code()
            } catch (e: Exception) {
                onFailed?.invoke(e, code)
                return@Runnable
            }*/

            if (code in 200 until 300) {
                onSuccess.invoke()
            } else {
                onFailed?.invoke(IllegalStateException("Response not succeed"), code)
            }
        })
    }

    /**
     * 获取连接的wifi名称
     */
    @SuppressLint("MissingPermission")
    fun getConnectedSsid(context: Context?): String? {
        (context?.applicationContext?.getSystemService(Context.WIFI_SERVICE)
                as? WifiManager)?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                val configuredNetworks = it.configuredNetworks
                if (configuredNetworks?.isNotEmpty() == true)
                    for (config in configuredNetworks) {
                        val ssid = config.SSID.substring(1, config.SSID.length - 1)
                        if (config.status == WifiConfiguration.Status.CURRENT) {
                            return ssid
                        }
                    }
            } else {
                val connManager = context.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                if (networkInfo != null && networkInfo.isConnected) {
                    val wifiInfo = it.connectionInfo
                    val ssid = wifiInfo.ssid
                    println("ssid: $ssid")
                    return ssid.substring(1, ssid.length - 1)
                }
            }
        }
        return null
    }

}