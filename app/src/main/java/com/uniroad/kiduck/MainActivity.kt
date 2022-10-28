package com.uniroad.kiduck

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.uniroad.kiduck.databinding.ActivityMainBinding
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    private val PERMISSION_RESULT_CODE = 1334

    private lateinit var binding: ActivityMainBinding // activitiy_main.xml의 layout 요소에 접근을 위한 변수

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var mStartForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val resultCode = result.resultCode
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater) // activitiy_main.xml의 layout에 연결
        setContentView(binding.root) // layout 보이기

        checkBT()
        checkBLE()

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val registerForResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                }
            }
            registerForResult.launch(enableBtIntent)
        }

        checkPermission()
    }

    override fun onResume() {
        super.onResume()

        binding.addDevice.setOnClickListener {
            Log.d("dongsu","before start connect activity")
            startActivity<ConnectKiduckActivity>()
        }

        binding.Kiduck1.setOnClickListener {
            startActivity<SummaryActivity>(
                "address" to null
            )
        }
    }

    fun checkBT() {
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            toast("기기가 BLE를 지원하지 않습니다.")
            finish()
        }
    }

    fun checkBLE() {
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            toast("기기가 BLE를 지원하지 않습니다.")
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_RESULT_CODE -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    fun checkPermission() {
        var locationPermission = arrayListOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        var arrayBTPermission = arrayListOf<String>(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(!hasPermissions(this, arrayBTPermission.toTypedArray())){
                requestPermissions(arrayBTPermission.toTypedArray(), PERMISSION_RESULT_CODE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(this, locationPermission.toTypedArray())){
                requestPermissions(locationPermission.toTypedArray(), PERMISSION_RESULT_CODE)
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}