package com.uniroad.kiduck

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
                    }
                }
            }
        }
    }

    fun checkPermission() {
        var permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        var arrayPermission = ArrayList<String>()
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            arrayPermission.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (arrayPermission.size > 0) {
            ActivityCompat.requestPermissions(this, arrayPermission.toTypedArray(), PERMISSION_RESULT_CODE)
        }
    }

}