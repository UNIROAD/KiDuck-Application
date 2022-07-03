package com.example.kiduck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kiduck.databinding.ActivityMainBinding
import android.widget.Toast

import androidx.fragment.app.FragmentActivity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {
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

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "해당 기기가 BLE를 지원하지 않아 어플을 사용하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        mStartForResult.launch(enableBtIntent);

        binding.device1.setOnClickListener {
            startActivity<ConnectKiduckActivity>()
        }
    }
}