package com.example.myapplication.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import extension.*
import update.UpdateAppUtils
import util.FileDownloadUtil
import util.SPUtil
import util.SignMd5Util
import java.io.File

/**
 * Created by Teprinciple on 2016/12/13.
 */
internal object DownloadAppUtils {

    const val KEY_OF_SP_APK_PATH = "KEY_OF_SP_APK_PATH"

    /**
     * apk 下载后本地文件路径
     */
    var downloadUpdateApkFilePath: String = ""

    /**
     * 更新信息
     */
    private val updateInfo by lazy { UpdateAppUtils.updateInfo }

    /**
     * context
     */
    private val context by lazy { globalContext()!! }

    /**
     * 是否在下载中
     */
    var isDownloading = false

    /**
     * 下载是否完成
     */
    var isDownloadComplete = false

    /**
     *下载进度回调
     */
    var onProgress: (Int) -> Unit = {}

    /**
     * 下载出错回调
     */
    var onError: () -> Unit = {}

    /**
     * 出错，点击重试回调
     */
    var onReDownload: () -> Unit = {}

    /**
     * 通过浏览器下载APK包
     */
    fun downloadForWebView(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 出错后，点击重试
     */
    fun reDownload() {
        //onReDownload.invoke()
        download()
    }

    /**
     * App下载APK包，下载完成后安装
     */
    fun download() {

        (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED).no {
            log("没有SD卡")
            //onError.invoke()
            return
        }

        var filePath = ""
        (updateInfo.config.apkSavePath.isNotEmpty()).yes {
            filePath = updateInfo.config.apkSavePath
        }.no {
            // 适配Android10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()){
                filePath = (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: "") + "/apk"
            }else{
                val packageName = context.packageName
                filePath = Environment.getExternalStorageDirectory().absolutePath + "/" + packageName
            }
        }


        // apk 保存名称
        val apkName = if (updateInfo.config.apkSaveName.isNotEmpty()) {
            updateInfo.config.apkSaveName
        } else {
            context.appName
        }


        val apkLocalPath = "$filePath/$apkName"

        log("package Path: $apkLocalPath");
        log("package Url: ${updateInfo.apkUrl}")

        downloadUpdateApkFilePath = apkLocalPath

        SPUtil.putBase(KEY_OF_SP_APK_PATH, downloadUpdateApkFilePath)

        downloadUpdateApkFilePath.deleteFile()
        "$downloadUpdateApkFilePath.temp".deleteFile()
        downloadByHttpUrlConnection(filePath, apkName)

    }

    /**
     * 使用 HttpUrlConnection 下载
     */
    private fun downloadByHttpUrlConnection(filePath: String, apkName: String?) {
        FileDownloadUtil.download(
            updateInfo.apkUrl,
            filePath,
            "$apkName",
            onStart = { downloadStart() },
            onProgress = { current, total -> downloading(current, total) },
            onComplete = { downloadComplete() },
            onError = { downloadError(it) }
        )
    }

    /**
     * 开始下载逻辑
     */
    private fun downloadStart() {
        isDownloading = true
        isDownloadComplete = false
        UpdateAppUtils.downloadListener?.onStart()
        log("download start")
    }

    /**
     * 下载中逻辑
     */
    private fun downloading(soFarBytes: Long, totalBytes: Long) {
//        log("soFarBytes:$soFarBytes--totalBytes:$totalBytes")
        isDownloading = true
        var progress = (soFarBytes * 100.0 / totalBytes).toInt()
        if (progress < 0) progress = 0
        log("progress:$progress")
        this@DownloadAppUtils.onProgress.invoke(progress)
        UpdateAppUtils.downloadListener?.onDownload(progress)
    }

    /**
     * 下载完成处理逻辑
     */
    private fun downloadComplete() {
        isDownloading = false
        isDownloadComplete = true
        log("download completed")
        this@DownloadAppUtils.onProgress.invoke(100)
        UpdateAppUtils.downloadListener?.onFinish()
        // 校验md5
        (updateInfo.config.needCheckMd5).yes {
            checkMd5(context)
        }
    }

    /**
     * 下载失败处理逻辑
     */
    private fun downloadError(e: Throwable) {
        isDownloading = false
        isDownloadComplete = false
        log("error:${e.message}")
        downloadUpdateApkFilePath.deleteFile()
        this@DownloadAppUtils.onError.invoke()
        UpdateAppUtils.downloadListener?.onError(e)
    }

    /**
     * 校验Md5
     *  先获取本应用的MD5值，获取未安装应用的MD5.进行对比
     */
    private fun checkMd5(context: Context) {
        // 当前应用md5
        val localMd5 = SignMd5Util.getAppSignatureMD5()

        // 下载的apk 签名md5
        val apkMd5 = SignMd5Util.getSignMD5FromApk(File(downloadUpdateApkFilePath))
        log("当前应用签名md5：$localMd5")
        log("下载apk签名md5：$apkMd5")

        // 校验结果回调
        UpdateAppUtils.md5CheckResultListener?.onResult(localMd5.equals(apkMd5, true))

        (localMd5.equals(apkMd5, true)).yes {
            log("md5校验成功")
            //UpdateAppReceiver.send(context, 100)
        }.no {
            log("md5校验失败")
        }
    }
}