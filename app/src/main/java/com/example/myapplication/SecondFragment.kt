package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.myapplication.databinding.FragmentSecondBinding
import com.example.myapplication.update.DownloadAppUtils
import com.google.android.material.snackbar.Snackbar
import extension.*
import update.UpdateAppUtils

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private var updateButton: Button? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateButton = view?.findViewById(R.id.button_second) as? Button

        binding.buttonSecond.setOnClickListener {

            DownloadAppUtils.isDownloading.no {
                if (updateButton is Button) {
                    (updateButton as? Button)?.text = string(R.string.downloading)
                }
                log("click on ${updateButton?.text.toString()}..")

                DownloadAppUtils.isDownloadComplete.no {
                    startDownload()
                }.yes{
                    context?.installApk(DownloadAppUtils.downloadUpdateApkFilePath)
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startDownload() {
        DownloadAppUtils.onError = {
            (updateButton as? Button)?.text = uiConfig.downloadFailText
                view?.let {
                    Snackbar.make(it, "下载出错,点击重试", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
                }
        }

        DownloadAppUtils.onReDownload = {
            (updateButton as? Button)?.text = uiConfig.updateBtnText
        }

        DownloadAppUtils.onProgress = {
            (it == 100).yes {
                (updateButton as? Button)?.text = string(R.string.install)
                view?.let {
                    Snackbar.make(it, "下载完成", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
                }
            }.no {
                (updateButton as? Button)?.text  = "${uiConfig.downloadingBtnText}$it%"

            }
        }

        DownloadAppUtils.download()

    }

    /**
     * 更新信息
     */
    private val updateInfo by lazy { UpdateAppUtils.updateInfo }

    /**
     * 更新配置
     */
    private val updateConfig by lazy { updateInfo.config }

    /**
     * ui 配置
     */
    private val uiConfig by lazy { updateInfo.uiConfig }
}