package com.uniroad.kiduck

import android.bluetooth.*
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Looper.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.uniroad.kiduck.Constants.Companion.CHARACTERISTIC_COMMAND_STRING
import com.uniroad.kiduck.Constants.Companion.CHARACTERISTIC_RESPONSE_STRING
import com.uniroad.kiduck.Constants.Companion.SERVICE_STRING
import com.uniroad.kiduck.databinding.ActivitySummaryBinding
import kotlinx.coroutines.experimental.Delay
import org.jetbrains.anko.bluetoothManager
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.lang.Float.max
import java.util.*
import java.util.Collections.max
import kotlin.collections.ArrayList


class SummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummaryBinding

    private lateinit var device: BluetoothDevice
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private lateinit var bluetoothGatt: BluetoothGatt

    private var readKiDuckData = mutableListOf<String>()
    private val TAG = "BLE_GATT"
    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if( status == BluetoothGatt.GATT_FAILURE ) {
                disconnectGattServer()
                return
            } else if( status != BluetoothGatt.GATT_SUCCESS ) {
                disconnectGattServer()
                return
            }
            if( newState == BluetoothProfile.STATE_CONNECTED ) {
                // update the connection status message
                Log.d(TAG, "Connected to the GATT server")
                gatt.discoverServices()
            } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)


            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service discovery failed, status: $status")
                return
            }
            // log for successful discovery
            Log.d(TAG, "Services discovery is successful")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            //Log.d(TAG, "characteristic changed: " + characteristic.uuid.toString())
            readCharacteristic(characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully")
            } else {
                Log.e(TAG, "Characteristic write unsuccessful, status: $status")
                disconnectGattServer()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: $status")
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }



        /**
         * Log the value of the characteristic
         * @param characteristic
         */
        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
            val msg = characteristic.getStringValue(0)
            readKiDuckData.add(msg)
            Log.d(TAG, "read: $msg")
        }
    }

    fun disconnectGattServer() {
        Log.d(TAG, "Closing Gatt connection")
        // disconnect and close the gatt
        if (bluetoothGatt != null) {
            bluetoothGatt!!.disconnect()
            bluetoothGatt!!.close()
        }
    }

    fun read(){
        val respCharacteristic = bluetoothGatt?.let { BluetoothUtils.findResponseCharacteristic(it) }
        // disconnect if the characteristic is not found
        if( respCharacteristic == null ) {
            Log.e(TAG, "Unable to find cmd characteristic")
            disconnectGattServer()
            return
        }

        bluetoothGatt.setCharacteristicNotification(respCharacteristic, true)
        // UUID for notification
        val descriptor:BluetoothGattDescriptor = respCharacteristic.getDescriptor(
            UUID.fromString(Constants.CLIENT_CHARACTERISTIC_CONFIG)
        )
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt.writeDescriptor(descriptor)
    }

    // write 사용시 delay 추가 해야함
    fun write(msg:String){
        val cmdCharacteristic = BluetoothUtils.findResponseCharacteristic(bluetoothGatt!!)
        // disconnect if the characteristic is not found
        if (cmdCharacteristic == null) {
            Log.e(TAG, "Unable to find cmd characteristic")
            disconnectGattServer()
            return
        }
        cmdCharacteristic.setValue(msg)
        cmdCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val success: Boolean = bluetoothGatt!!.writeCharacteristic(cmdCharacteristic)
        // check the result
        if( !success ) {
            Log.e(TAG, "Failed to write command")
        }
    }

    private var deviceAddress:String?= null
    /*
    키덕으로부터 받아야 하는 데이터
        이름
        데이터의 갯수

        걸음수
        음수량

        통신 횟수

        성장 조건별 기준
            하루당 걸음수 기준
            하루당 음수량 기준
            하루당 통신 횟수 기준

        암호

        응급 알람 활성화 상태
     */
    private lateinit var kiduckName: String
    private lateinit var dataCount: String
    private lateinit var numOfSteps: ArrayList<Float>
    private lateinit var amountOfDrink: ArrayList<Float>
    private lateinit var numOfCommunication: ArrayList<Float>
    private lateinit var criteriaOfSteps: String
    private lateinit var criteriaOfDrink: String
    private lateinit var criteriaOfCommunication: String
    private lateinit var kiduckPassword: String
    private lateinit var emergencyAlarm_isEnabled: String

    private var DATA_COUNT = 0
    private var MAX_STEPS = 0f
    private var MAX_DRINK = 0f
    private var MAX_COMMUNICATION = 0f
    private var SUM_STEPS = 0
    private var SUM_DRINK = 0
    private var SUM_COMMUNICATION = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 블루투스 통신으로 KiDuck 데이터 수신
        deviceAddress = intent.getStringExtra("address")
        if(deviceAddress == null)
            Log.d("DongSu", "null")

        if (deviceAddress == null || deviceAddress == "") {
            toast("device address 획득 실패")
            this.finish()
            return
        }

        device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        bluetoothGatt = device.connectGatt(applicationContext, false, gattClientCallback)
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        var isConnected = false
        Handler(Looper.getMainLooper()).postDelayed({
            val list = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
            for(connectedDevice in list){
                if(connectedDevice.name == "KIDUCK"){
                    isConnected = true
                    break;
                }
            }
            if(!isConnected){
                toast("BLE 연결 실패, 다시 연결을 시도하세요.")
                loadingDialog.dismiss()
                finish()
            } else {
                read()// 통신 설정
                Handler(Looper.getMainLooper()).postDelayed({
                    write("AppConnected")
                    Handler(Looper.getMainLooper()).postDelayed({
                        if(readKiDuckData.isEmpty()) {
                            loadingDialog.dismiss()
                            finish()
                        } else {
                            if(readKiDuckData.size < 6){
                                toast("KiDuck 데이터 불러오기 실패, 다시 연결을 시도하세요.")
                                loadingDialog.dismiss()
                                finish()
                            } else {
                                var curi = 0
                                kiduckName = readKiDuckData[curi++]
                                for(i in 0 until kiduckName.length){
                                    if(kiduckName[i] == ' '){
                                        kiduckName = kiduckName.substring(0,i)
                                        break
                                    }
                                }

                                dataCount = readKiDuckData[curi++]
                                for(i in 0 until dataCount.length){
                                    if(dataCount[i] == ' '){
                                        dataCount = dataCount.substring(0,i)
                                        break
                                    }
                                }
                                DATA_COUNT = dataCount.toInt()
                                MAX_STEPS = 0f
                                MAX_DRINK = 0f
                                MAX_COMMUNICATION = 0f
                                SUM_STEPS = 0
                                SUM_DRINK = 0
                                SUM_COMMUNICATION = 0

                                numOfSteps = ArrayList<Float>()
                                amountOfDrink = ArrayList<Float>()
                                numOfCommunication = ArrayList<Float>()
                                for(i in 0 until DATA_COUNT){
                                    val dayData = readKiDuckData[curi++].split(' ')
                                    numOfSteps.add(dayData[0].toFloat())
                                    MAX_STEPS = max(MAX_STEPS, dayData[0].toFloat())
                                    SUM_STEPS += dayData[0].toInt()
                                    amountOfDrink.add(dayData[1].toFloat())
                                    MAX_DRINK = max(MAX_DRINK, dayData[1].toFloat())
                                    SUM_DRINK += dayData[1].toInt()
                                    numOfCommunication.add(dayData[2].toFloat())
                                    MAX_COMMUNICATION = max(MAX_COMMUNICATION, dayData[2].toFloat())
                                    SUM_COMMUNICATION += dayData[2].toInt()
                                }
                                Log.d(TAG, MAX_STEPS.toString())
                                val thresholdData = readKiDuckData[curi++].split(' ')
                                criteriaOfSteps = thresholdData[0]
                                criteriaOfDrink = thresholdData[1]
                                criteriaOfCommunication = thresholdData[2]
                                kiduckPassword = readKiDuckData[curi++]
                                emergencyAlarm_isEnabled = readKiDuckData[curi]

                                // 요약 내용 업데이트
                                binding.name.text = kiduckName + "의 기기"
                                binding.time.text = "사용기간 : " + DATA_COUNT + " 일"
                                binding.walk.text = "총 걸음수 : " + SUM_STEPS + " 보"
                                binding.drink.text = "총 음수량 : " + SUM_DRINK + " mL"
                                binding.communication.text = "타 기기와 통신 : " + SUM_COMMUNICATION + " 회"

                                // 차트 설정
                                setupStepChart()
                                setupDrinkChart()
                                setupCommunicationChart()

                                // 로딩 종료
                                loadingDialog.dismiss()
                            }
                        }
                    }, 1000)
                },500) // 통신 준비 완료를 알림
            }
        }, 2000)

    }
    override fun onResume() {
        super.onResume()
        binding.chartWalk.setOnClickListener {
            startActivity<StaticsActivity>(
                "type" to "walk",
                "data" to numOfSteps
            )
        }

        binding.chartDrink.setOnClickListener {
            startActivity<StaticsActivity>(
                "type" to "drink",
                "data" to amountOfDrink
            )
        }

        binding.chartCommunication.setOnClickListener {
            startActivity<StaticsActivity>(
                "type" to "communication",
                "data" to numOfCommunication
            )
        }

        binding.settingButton.setOnClickListener {
            startActivity<SettingsActivity>()
        }

        binding.backButton.setOnClickListener { finish() } // 현재 activity 종료
    }
    override fun onDestroy() {
        super.onDestroy()
        if (deviceAddress == null || deviceAddress == ""){
            return
        }
        disconnectGattServer()
    }

    private fun setupStepChart() {
        var stepChart: BarChart = binding.chartWalk

        val step_entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            step_entries.add(BarEntry(i.toFloat(), numOfSteps[i-1]))
        }

        var step_set = BarDataSet(step_entries, "걸음수")
            .apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                color = Color.parseColor("#EB592A")
                setDrawIcons(false)
                setDrawValues(true)
                valueFormatter = CustomDecimalFormatter()
                valueTextColor = Color.BLACK
            }

        var step_dataSet = ArrayList<IBarDataSet>()
        step_dataSet.add(step_set)

        var step_data = BarData(step_dataSet)
            .apply {
                barWidth = 0.8f
                setValueTextSize(10f)
            }

        stepChart.data = step_data

        stepChart.apply {
            description.isEnabled = false
            background = getDrawable(R.drawable.chart_body_background)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 12f
                textColor = Color.parseColor("#A5A4A8")
                valueFormatter = MyXAxisFormatter()
            }

            axisLeft.isEnabled = false

            axisRight.apply {
                axisMaximum = MAX_STEPS + 1f
                axisMinimum = 0f
                granularity = 1000f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#A5A4A8") // 축 색깔 설정
                gridColor = Color.parseColor("#A5A4A8") // 축 아닌 격자 색깔 설정
                textColor = Color.parseColor("#A5A4A8") // 라벨 텍스트 컬러 설정
                textSize = 13f //라벨 텍스트 크기
            }

            setTouchEnabled(true)
            animateY(1000)
            legend.isEnabled = false

            data = step_data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setupDrinkChart() {
        var drinkChart: BarChart = binding.chartDrink

        val drink_entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            drink_entries.add(BarEntry(i.toFloat(), amountOfDrink[i-1]))
        }

        var drink_set = BarDataSet(drink_entries, "걸음수")
            .apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                color = Color.parseColor("#EB592A")
                setDrawIcons(false)
                setDrawValues(true)
                valueFormatter = CustomDecimalFormatter()
                valueTextColor = Color.BLACK
            }

        var drink_dataSet = ArrayList<IBarDataSet>()
        drink_dataSet.add(drink_set)

        var drink_data = BarData(drink_dataSet)
            .apply {
                barWidth = 0.8f
                setValueTextSize(10f)
            }

        drinkChart.data = drink_data

        drinkChart.apply {
            description.isEnabled = false
            background = getDrawable(R.drawable.chart_body_background)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 12f
                textColor = Color.parseColor("#A5A4A8")
                valueFormatter = MyXAxisFormatter()
            }

            axisLeft.isEnabled = false

            axisRight.apply {
                axisMaximum = MAX_DRINK + 1f
                axisMinimum = 0f
                granularity = 500f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#A5A4A8") // 축 색깔 설정
                gridColor = Color.parseColor("#A5A4A8") // 축 아닌 격자 색깔 설정
                textColor = Color.parseColor("#A5A4A8") // 라벨 텍스트 컬러 설정
                textSize = 13f //라벨 텍스트 크기
            }

            setTouchEnabled(true)
            animateY(1000)
            legend.isEnabled = false

            data = drink_data
            setFitBars(true)
            invalidate()
        }
    }

    private fun setupCommunicationChart() {
        var commChart: BarChart = binding.chartCommunication

        val comm_entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            comm_entries.add(BarEntry(i.toFloat(), numOfCommunication[i-1]))
        }

        var comm_set = BarDataSet(comm_entries, "걸음수")
            .apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                color = Color.parseColor("#EB592A")
                setDrawIcons(false)
                setDrawValues(true)
                valueFormatter = CustomDecimalFormatter()
                valueTextColor = Color.BLACK
            }

        var comm_dataSet = ArrayList<IBarDataSet>()
        comm_dataSet.add(comm_set)

        var comm_data = BarData(comm_dataSet)
            .apply {
                barWidth = 0.8f
                setValueTextSize(10f)
            }

        commChart.data = comm_data

        commChart.apply {
            description.isEnabled = false
            background = getDrawable(R.drawable.chart_body_background)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 12f
                textColor = Color.parseColor("#A5A4A8")
                valueFormatter = MyXAxisFormatter()
            }

            axisLeft.isEnabled = false

            axisRight.apply {
                axisMaximum = MAX_COMMUNICATION + 1f
                axisMinimum = 0f
                granularity = 2f
                setDrawLabels(true)
                setDrawGridLines(true)
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#A5A4A8") // 축 색깔 설정
                gridColor = Color.parseColor("#A5A4A8") // 축 아닌 격자 색깔 설정
                textColor = Color.parseColor("#A5A4A8") // 라벨 텍스트 컬러 설정
                textSize = 13f //라벨 텍스트 크기
            }

            setTouchEnabled(true)
            animateY(1000)
            legend.isEnabled = false

            data = comm_data
            setFitBars(true)
            invalidate()
        }
    }

    inner class MyXAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return value.toString().split(".")[0] + "일차"
        }
    }

    inner class CustomDecimalFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val score = value.toString().split(".")
            return score[0]
        }
    }
}

