package com.uniroad.kiduck

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityGrowthSettingBinding
import org.jetbrains.anko.toast

class GrowthSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGrowthSettingBinding

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

    private var criteriaOfSteps: String? = null
    private var criteriaOfDrink: String? = null
    private var criteriaOfCommunication: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrowthSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceAddress = intent.getStringExtra("deviceAddress")
        criteriaOfSteps = intent.getStringExtra("criteriaOfSteps")
        criteriaOfDrink = intent.getStringExtra("criteriaOfDrink")
        criteriaOfCommunication = intent.getStringExtra("criteriaOfCommunication")

        val INIT_CRITERIA_STEP = criteriaOfSteps!!.trim().toIntOrNull()
        val INIT_CRITERIA_DRINK = criteriaOfDrink!!.trim().toIntOrNull()
        val INIT_CRITERIA_COMMUNICATION = criteriaOfCommunication!!.trim().toIntOrNull()

        if(INIT_CRITERIA_STEP == null || INIT_CRITERIA_DRINK == null || INIT_CRITERIA_COMMUNICATION == null) {
            toast("기존 성장 기준 불러오기 실패, BLE 연결을 다시 시도하세요.")
            finish()
            return
        }

        binding.editStep.setText(INIT_CRITERIA_STEP.toString())
        binding.editDrink.setText(INIT_CRITERIA_DRINK.toString())
        binding.editCommunication.setText(INIT_CRITERIA_COMMUNICATION.toString())

        binding.growthPerStep.text = (24f/INIT_CRITERIA_STEP!!.toFloat()).toString() + " h/걸음"
        binding.growthPerDrink.text = (24f/INIT_CRITERIA_DRINK!!.toFloat()).toString() + " h/mL"
        binding.growthPerCommunication.text = (24f/INIT_CRITERIA_COMMUNICATION!!.toFloat()).toString() + " h/통신"

        binding.editStep.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
                val step = binding.editStep.text.toString().toFloatOrNull()
                if(step != null){
                    binding.growthPerStep.text = (24f/step!!.toFloat()).toString() + " h/걸음"
                }
            }
        })

        binding.editDrink.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
                val drink = binding.editDrink.text.toString().toFloatOrNull()
                if(drink != null){
                    binding.growthPerDrink.text = (24f/drink!!.toFloat()).toString() + " h/mL"
                }
            }
        })

        binding.editCommunication.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
                val comm = binding.editCommunication.text.toString().toFloatOrNull()
                if(comm != null){
                    binding.growthPerCommunication.text = (24f/comm!!.toFloat()).toString() + " h/통신"
                }
            }
        })

        val gattServiceIntent = Intent(this, BLEService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())

        binding.setGrowthButton.setOnClickListener {
            val step = binding.editStep.text.toString().toIntOrNull()
            val drink = binding.editDrink.text.toString().toIntOrNull()
            val communication = binding.editCommunication.text.toString().toIntOrNull()

            if (step == null) {
                toast("걸음수 조건을 입력하세요.")
            } else if(drink == null){
                toast("음수량 조건을 입력하세요.")
            } else if(communication == null){
                toast("통신 횟수 조건을 입력하세요.")
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
                        bleService!!.write("SetGrowth")
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (readKiDuckData.isEmpty()) {
                                toast("KIDUCK과 통신 불량, 다시 시도하세요.")
                                loadingDialog!!.dismiss()
                            } else {
                                if (readKiDuckData[0] == "ACK") {
                                    readKiDuckData.clear()
                                    bleService!!.write(String.format("%d %d %d", step!!, drink!!, communication!!))
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (readKiDuckData.isEmpty()) {
                                            toast("KIDUCK과 통신 불량, 다시 시도하세요.")
                                            loadingDialog!!.dismiss()
                                        } else {
                                            if (readKiDuckData[0] == "SUCCESS") {
                                                readKiDuckData.clear()
                                                toast("성장 조건 업데이트 완료")
                                                loadingDialog!!.dismiss()
                                            } else {
                                                readKiDuckData.clear()
                                                toast("성장 조건 업데이트 실패, 다시 시도하세요.")
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

        binding.finishGrowthSettingButton.setOnClickListener { finish() }
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