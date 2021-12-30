package extension

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.RecoverySystem

import java.io.File

/**
 * desc: context 相关扩展
 * author: teprinciple on 2020/3/27.
 */


/**
 * appName
 */
val Context.appName
    get() = packageManager.getPackageInfo(packageName, 0)?.applicationInfo?.loadLabel(packageManager).toString()

/**
 * 检测wifi是否连接
 */
fun Context.isWifiConnected(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    cm ?: return false
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
}


/**
 * 跳转安装
 */

fun Context.installApk(apkPath: String?) {
    log("install apk:$apkPath")

    val otaPackage = File(apkPath)
    if (!otaPackage.exists()) {
        log("can not found update.zip\n")
        return
    }
    log("start install ...")
    RecoverySystem.installPackage(globalContext(), otaPackage)

}


