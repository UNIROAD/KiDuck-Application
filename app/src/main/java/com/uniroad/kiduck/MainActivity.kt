package com.uniroad.kiduck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.uniroad.kiduck.databinding.ActivityMainBinding

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {
    private val PERMISSION_RESULT_CODE = 1334
    private lateinit var binding: ActivityMainBinding // activitiy_main.xml의 layout 요소에 접근을 위한 변수

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

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

        /*packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "해당 기기가 BLE를 지원하지 않아 어플을 사용하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }*/

        if (ContextCompat.checkSelfPermission(this, PackageManager.FEATURE_BLUETOOTH_LE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(PackageManager.FEATURE_BLUETOOTH_LE), PERMISSION_RESULT_CODE)
            Log.d("BS_LOG", "DONE!")
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        mStartForResult.launch(enableBtIntent);

        binding.addDevice.setOnClickListener {
            startActivity<ConnectKiduckActivity>()
        }

        binding.Kiduck1.setOnClickListener {
            startActivity<SummaryActivity>()
        }
    }
}