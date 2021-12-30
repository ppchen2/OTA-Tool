package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import extension.log
import extension.no
import extension.yes
import util.GlobalContextProvider

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalContextProvider.mContext == null){
            GlobalContextProvider.mContext = this.applicationContext
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val writePermission = ContextCompat.checkSelfPermission(this,
            MainActivity.permission
        )
        (writePermission == PackageManager.PERMISSION_GRANTED).yes {
            // findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }.no {
            // 申请权限
            ActivityCompat.requestPermissions(this, arrayOf(MainActivity.permission),
                MainActivity.PERMISSION_CODE
            )
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Hello...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * 权限请求结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_CODE -> (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED).yes {
                log("Get Permission OK.")
            }.no {
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission).no {
                    // 显示无权限弹窗
//                    AlertDialogUtil.show(this, getString(R.string.no_storage_permission), onSureClick = {
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        intent.data = Uri.parse("package:$packageName") // 根据包名打开对应的设置界面
//                        startActivity(intent)
//                    })
                    log("Get Permission Failed.")
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    companion object {

//        fun launch() = globalContext()?.let {
//            val intent = Intent(it, UpdateAppActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            it.startActivity(intent)
//        }

        private const val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        // private const val permission = Manifest.permission.REBOOT
        private const val PERMISSION_CODE = 1001
    }

    }