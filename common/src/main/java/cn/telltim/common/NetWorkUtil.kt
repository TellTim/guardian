//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : NetWorkUtil
// Author     : Tim.WJ
// Create At  : 2022/4/10 21:40
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//
package cn.telltim.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.text.isDigitsOnly
import com.orhanobut.logger.Logger
import com.telltim.xtask.XThreadTaskManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.regex.Pattern

/**
 * @Author     : Tim.WJ
 * @Description:
 */
object NetWorkUtil {

    private const val MIUI_PING_URL = "https://connect.rom.miui.com/generate_204"

    /**
     * 获取内网IP
     * @param context 上下文
     * @return 判断是否有分配的内网IP
     */
    fun getIpV4Address(context: Context): String? {
        var ipAddress: String? = null
        val nis = NetworkInterface.getNetworkInterfaces()
        var ia: InetAddress? = null
        while (nis.hasMoreElements()) {
            val ni = nis.nextElement() as NetworkInterface
            val ias = ni.inetAddresses
            while (ias.hasMoreElements()) {
                ia = ias.nextElement()
                if (ia is Inet6Address) {
                    // 局域网内就算存在IPv6地址，也一定会分配ipV4地址，使用ipV4地址判断即可
                    continue
                }
                val ip = ia.hostAddress
                if (ip != "127.0.0.1") {
                    ipAddress = ia.hostAddress
                    break
                }
            }
        }
        return ipAddress
    }

    /**
     * 网络是否已配置好
     * @param context 上下文
     */
    fun isNetConfigured(context: Context): Boolean {
        val ipAddress = getIpV4Address(context)
        return !ipAddress.isNullOrEmpty() && ipAddress != "127.0.0.1"
    }

    /**
     * 子线程判断是否是良好的网络环境
     * @param context 上下文
     * @阿里DNS 223.6.6.6
     */
    fun isGoodInternet(context: Context, onSuccess: () -> Unit, onFalse: () -> Unit) {
        XThreadTaskManager.instance.addNetTask(Runnable {
            val runtime = Runtime.getRuntime()
            var ipProcess: Process? = null
            try {
                ipProcess = runtime.exec("ping -c 3 -i 0.2 223.6.6.6")
                ipProcess?.let {
                    val input: InputStream = it.inputStream
                    val readIn = BufferedReader(InputStreamReader(input))
                    var content = ""
                    while (true) {
                        val tempString = readIn.readLine() ?: break
                        content += tempString
                    }
                    val exitValue = it.waitFor()
                    if (exitValue == 0) {
                        onSuccess.invoke()
                        return@Runnable
                    } else {
                        //根据正则表达式获取 0% packet loss 中的0部分并转换未数值
                        val pattern = Pattern.compile("\\d+(?=% packet loss)")
                        val matcher = pattern.matcher(content)
                        if (matcher.find()) {
                            val badNumber = matcher.group(0)
                            if (badNumber != null && badNumber.isDigitsOnly()) {
                                if (badNumber.toInt() <= 10) {
                                    onSuccess.invoke()
                                    return@Runnable
                                } else {
                                    Logger.t("NetWorkUtil").w(
                                        "net packet loss ${
                                            badNumber.toInt
                                                ()
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Logger.t("NetWorkUtil").e("isGoodInternet exception ${e.message}")
            } catch (e: InterruptedException) {
                Logger.t("NetWorkUtil").e("isGoodInternet exception ${e.message}")
            } finally {
                ipProcess?.destroy();
                runtime.gc();
            }
            onFalse.invoke()
        })
    }

}