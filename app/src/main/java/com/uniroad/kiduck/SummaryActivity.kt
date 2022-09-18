package com.uniroad.kiduck

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.uniroad.kiduck.databinding.ActivitySummaryBinding
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class SummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummaryBinding

    private var deviceAddress = intent.getStringExtra("address")
    private var bluetoothService: BluetoothLeService? = null
    var connected: Boolean = false
    val gattUpdateReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> connected = true
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    toast("BLE 기기와 연결 끊어짐")
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothService?.let {
                        if (it.getSupportedGattServices() == null)
                            return

                        SelectCharacteristicData(it.getSupportedGattServices()!!)
                    }
                }
                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                    val resp: String? = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                }
            }

        }
    }
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
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
    private lateinit var numOfSteps: ArrayList<String>
    private lateinit var amountOfDrink: ArrayList<String>
    private lateinit var numOfCommunication: ArrayList<String>
    private lateinit var criteriaOfSteps: String
    private lateinit var criteriaOfDrink: String
    private lateinit var criteriaOfCommunication: String
    private lateinit var kiduckPassword: String
    private lateinit var emergencyAlarm_isEnabled: String

    private fun SelectCharacteristicData(gattServices: List<BluetoothGattService>) {
        for (gattService in gattServices) {
            var gattCharacteristics: List<BluetoothGattCharacteristic> = gattService.characteristics

            for (gattCharacteristic in gattCharacteristics) {
                when (gattCharacteristic.uuid) {
                    BluetoothLeService.UUID_DATA_WRITE -> writeCharacteristic = gattCharacteristic
                    BluetoothLeService.UUID_DATA_NOTIFY -> notifyCharacteristic = gattCharacteristic
                }
            }
        }
    }

    private fun SendData(data: String) {
        writeCharacteristic?.let {
            if (it.properties or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                bluetoothService?.writeCharacteristic(it, data)
            }
        }

        notifyCharacteristic?.let {
            if (it.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                bluetoothService?.setCharacteristicNotification(it, true)

            }
        }
    }

    private fun ReadData() {
        readCharacteristic?.let {
            if (it.properties or BluetoothGattCharacteristic.PROPERTY_READ > 0) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (deviceAddress == null) {
            toast("장치 MAC 주소 획득 실패")
            finish()
        }
        bluetoothService = BluetoothLeService()
        bluetoothService?.connect(deviceAddress!!)



        // 블루투스 통신으로 KiDuck 데이터 수신
        kiduckName = "DONGSU"
        dataCount = "7"

        numOfSteps = ArrayList<String>()
        numOfSteps.add("1000")
        numOfSteps.add("424")
        numOfSteps.add("5959")
        numOfSteps.add("5894")
        numOfSteps.add("7233")
        numOfSteps.add("1294")
        numOfSteps.add("12399")
        amountOfDrink = ArrayList<String>()

        numOfCommunication = ArrayList<String>()

        val DATA_COUNT = dataCount.toInt()
        val MAX_STEPS = 12399f

        // 요약 내용 없데이트
        binding.name.text = kiduckName + "의 기기"

        // 차트 설정
        var stepChart: BarChart = binding.chartWalk

        val step_entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            step_entries.add(BarEntry(i.toFloat(), numOfSteps[i-1].toFloat()))
        }

        var step_set = BarDataSet(step_entries, "걸음수")
            .apply {
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




        binding.chartWalk.setOnClickListener {
            startActivity<StaticsActivity>(
                "type" to "walk",
                "data" to numOfSteps
            )
        }


        binding.settingButton.setOnClickListener {
            startActivity<SettingsActivity>()
        }

        binding.backButton.setOnClickListener { finish() } // 현재 activity 종료
    }

    inner class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("1차","2차","3차","4차","5차","6차","7차")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    inner class CustomDecimalFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val score = value.toString().split(".")
            return score[0]
        }
    }
}