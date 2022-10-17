package com.uniroad.kiduck

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityChangeNameBinding
import org.jetbrains.anko.toast

class ChangeNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeNameBinding

    private var loadingDialog:LoadingDialog? = null

    private val TAG = "BLE_GATT"
    private var bleService: BLEService? = null
    private var deviceAddress:String?= null
    private var readKiDuckData = mutableListOf<String>()
    private var mConnected = false

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bleService = (service as BLEService.LocalBinder).service
            if (!bleService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService!!.connect(deviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bleService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    // ACTION_DATA_WRITTEN: written data to the device.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BLEService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
            } else if (BLEService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED == action) {

            } else if (BLEService.ACTION_DATA_AVAILABLE == action) {
                val msg = intent.getStringExtra(BLEService.EXTRA_DATA)
                readKiDuckData.add(msg!!)
            } else if (BLEService.ACTION_DATA_WRITTEN == action) {

            }
        }
    }

    private var kiduckName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceAddress = intent.getStringExtra("deviceAddress")
        kiduckName = intent.getStringExtra("kiduckName")

        binding.currentName.text = kiduckName!!

        val gattServiceIntent = Intent(this, BLEService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())

        binding.setNameButton.setOnClickListener {
            val nextName = binding.nextName.text.toString().trim()
            if(nextName == ""){
                toast("새로운 이름을 입력하세요.")
            } else {
                loadingDialog = LoadingDialog(this)
                loadingDialog!!.show()

                if (bleService != null) {
                    val result = bleService!!.connect(deviceAddress)
                    Log.d(TAG, "Connect request result=" + result)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    bleService!!.setCharacteristicNotification(true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        bleService!!.write("SetName")
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (readKiDuckData.isEmpty()) {
                                toast("KIDUCK과 통신 불량, 다시 시도하세요.")
                                loadingDialog!!.dismiss()
                            } else {
                                if (readKiDuckData[0] == "ACK") {
                                    readKiDuckData.clear()
                                    bleService!!.write(nextName)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (readKiDuckData.isEmpty()) {
                                            toast("KIDUCK과 통신 불량, 다시 시도하세요.")
                                            loadingDialog!!.dismiss()
                                        } else {
                                            if (readKiDuckData[0] == "SUCCESS") {
                                                readKiDuckData.clear()
                                                toast("이름 업데이트 완료")
                                                loadingDialog!!.dismiss()
                                            } else {
                                                readKiDuckData.clear()
                                                toast("이름 업데이트 실패, 다시 시도하세요.")
                                                loadingDialog!!.dismiss()
                                            }
                                        }
                                    }, 1000)
                                }
                            }
                        }, 1000)
                    }, 500)
                }, 2000)
            }
        }

        binding.finishNameButton.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mGattUpdateReceiver)
        unbindService(mServiceConnection)
        bleService = null
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BLEService.ACTION_DATA_WRITTEN)
        return intentFilter
    }
}