package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentFirstBinding
import com.google.android.material.snackbar.Snackbar
import extension.log
import extension.yes
import listener.UpdateDownloadListener
import model.UpdateConfig
import update.UpdateAppUtils

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val apkUrl = "http://10.11.10.167/update.zip"
    //private val apkUrl = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/QQ9.5.3.28008.exe"

    private val updateTitle = "发现新版本V2.0.0"
    private val updateContent = "1、Kotlin重构版\n2、支持自定义UI\n3、增加md5校验\n4、更多功能等你探索"
    private var newFeatureInfo: EditText? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        log("this is first fragment onCreateView.")


        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            // 更新配置
            val updateConfig = UpdateConfig().apply {
                force = false
                alwaysShowDownLoadDialog= true
                isDebug = true
                checkWifi = false
                isShowNotification = true
                //notifyImgRes = R.drawable.ic_logo
                apkSavePath = Environment.getExternalStorageDirectory().absolutePath + "/Download"
                apkSaveName = "update.zip"
            }

            UpdateAppUtils
                .getInstance()
                .apkUrl(apkUrl)
                .updateTitle(updateTitle)
                .updateContent(updateContent)
                .updateConfig(updateConfig)
                //.uiConfig(uiConfig)
                .setUpdateDownloadListener(object : UpdateDownloadListener {
                    override fun onStart() {
                    }

                    override fun onDownload(progress: Int) {
                    }

                    override fun onFinish() {
                    }

                    override fun onError(e: Throwable) {
                    }
                })

            when {
                this.context?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                } == PackageManager.PERMISSION_GRANTED -> {
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    //initView()
                }
                else -> {
                    Snackbar.make(view, "No ExStorage Write Permission.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * 更新信息
     */
    private val updateInfo by lazy { UpdateAppUtils.updateInfo }

    private fun initView() {
        val textView: TextView? = view?.findViewById(R.id.button_first) as? TextView
        textView?.text = getString(R.string.app_name)

        val str: String = textView?.text.toString()

        println("the value is $str")

    }

}