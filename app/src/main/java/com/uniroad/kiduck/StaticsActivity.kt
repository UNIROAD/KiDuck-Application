package com.uniroad.kiduck

import android.bluetooth.BluetoothGatt
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.uniroad.kiduck.databinding.ActivityStaticsBinding
import org.jetbrains.anko.toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.lang.Float.max
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class StaticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaticsBinding

    private var type:String? = null
    private var dataList: ArrayList<Float>? = null
    private var DATA_COUNT = 0

    private var yAxisLineUnit = 0f
    private var LABEL:String? = null

    private val date: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = intent.getStringExtra("type")

        if(type == "walk"){
            binding.staticsTitle.text = "걸음수 통계"
            binding.meanUnit.text = "걸음"
            binding.yAxisUnit.text = "걸음"
            yAxisLineUnit = 1000f
            LABEL = "걸음수"
        } else if(type == "drink"){
            binding.staticsTitle.text = "물 섭취량 통계"
            binding.meanUnit.text = "mL"
            binding.yAxisUnit.text = "mL"
            yAxisLineUnit = 200f
            LABEL = "음수량"
        } else if(type == "communication"){
            binding.staticsTitle.text = "친구 만나기 통계"
            binding.meanUnit.text = "회"
            binding.yAxisUnit.text = "회"
            yAxisLineUnit = 2f
            LABEL = "통신횟수"
        } else {
            toast("통게 액티비티 실행 오류, 다시 시도하세요.")
            finish()
            return
        }

        binding.xAxisText.setText("1")
        binding.yAxisText.setText(yAxisLineUnit.toInt().toString())

        dataList = intent.getSerializableExtra("data") as ArrayList<Float>?
        if(dataList == null){
            Log.d("Kiduck_stat", "dataList is null")
            finish()
            return
        }
        DATA_COUNT = dataList!!.size

        var strCurDate = date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        Log.d("Kdate", strCurDate)
        var strFirstDate = date.minusDays(DATA_COUNT.toLong() - 1).format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        Log.d("Kdate", strFirstDate)

        binding.meanDateRange.text = strFirstDate + "~" + strCurDate
        binding.dateRangeText.text = strFirstDate + "~" + strCurDate

        var maxData = 0f
        var sumData = 0f
        for(i in 0 until DATA_COUNT){
            maxData = max(maxData, dataList!![i])
            sumData += dataList!![i]
        }

        var mean = sumData / DATA_COUNT
        binding.meanText.text = String.format("%.2f", mean)

        var barChart: BarChart = binding.barchart

        val entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            entries.add(BarEntry(i.toFloat(), dataList!![i-1]))
        }

        var set = BarDataSet(entries, LABEL)
            .apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                color = Color.parseColor("#EB592A")
                setDrawIcons(false)
                setDrawValues(true)
                valueFormatter = CustomDecimalFormatter()
                valueTextColor = Color.BLACK
            }

        var dataSet = ArrayList<IBarDataSet>()
        dataSet.add(set)

        var data = BarData(dataSet)
            .apply {
                barWidth = 0.8f
                setValueTextSize(10f)
            }

        barChart.data = data

        barChart.apply {
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
                axisMaximum = maxData + 1f
                axisMinimum = 0f
                granularity = yAxisLineUnit
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

            data = data
            setFitBars(true)
            invalidate()
        }

        binding.setDateRangeButton.setOnClickListener {
            //참고 url : https://www.geeksforgeeks.org/material-design-date-range-picker-in-android-using-kotlin/
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("조회할 기간 선택")
                .setCalendarConstraints(limitRange()?.build())
                .build()
            dateRangePicker.show(supportFragmentManager, "DatePicker")

            dateRangePicker.addOnPositiveButtonClickListener {
                val startDate = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(it.first)
                val endDate = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(it.second)
                binding.dateRangeText.text = startDate + '~' + endDate
            }

            dateRangePicker.addOnNegativeButtonClickListener {
                Toast.makeText(this, "기간 선택이 취소되었습니다.", Toast.LENGTH_LONG).show()
            }

            dateRangePicker.addOnCancelListener {
                Toast.makeText(this, "기간 선택이 취소되었습니다.", Toast.LENGTH_LONG).show()
            }
        }

        binding.setStatics.setOnClickListener {
            val xAxisUnit = binding.xAxisText.text.toString().toIntOrNull()
            val yAxisUnit = binding.yAxisText.text.toString().toIntOrNull()

            if(xAxisUnit == null){
                toast("가로축 단위를 입력하세요.")
            } else if(yAxisUnit == null){
                toast("세로축 단위를 입력하세요.")
            } else {
                val firstDate = date.minusDays(DATA_COUNT.toLong() - 1)
                    .atStartOfDay()
                val inputDateRange = binding.dateRangeText.text.toString().split("~")
                Log.d("DATE_TEST", inputDateRange[0])
                val startDate =
                    LocalDate.parse(inputDateRange[0], DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                        .atStartOfDay()
                val endDate =
                    LocalDate.parse(inputDateRange[1], DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                        .atStartOfDay()
                val startIndex = Duration.between(firstDate, startDate).toDays().toInt()
                val endIndex = Duration.between(firstDate, endDate).toDays().toInt()

                binding.meanDateRange.text = binding.dateRangeText.text
                Log.d("DATE_TEST", startIndex.toString() + ", " + endIndex.toString())

                val dataCount = endIndex - startIndex + 1
                var maxData = 0f
                var sumData = 0f

                var barChart: BarChart = binding.barchart

                val entries = ArrayList<BarEntry>()
                val partCount = dataCount / xAxisUnit
                for(i in 0 until partCount){
                    var partData = 0f
                    for(j in 0 until xAxisUnit){
                        partData += dataList!![startIndex + i*xAxisUnit + j]
                    }
                    entries.add(BarEntry((i+1).toFloat(), partData))
                    maxData = max(maxData, partData)
                }

                if (dataCount % xAxisUnit > 0) {
                    var partData = 0f
                    for (i in 0 until dataCount % xAxisUnit) {
                        partData += dataList!![startIndex + partCount * xAxisUnit + i]
                    }
                    entries.add(BarEntry((partCount+1).toFloat(), partData))
                    maxData = max(maxData, partData)
                }

                for(i in startIndex..endIndex) {
                    sumData += dataList!![i]
                }

                var set = BarDataSet(entries, LABEL)
                    .apply {
                        axisDependency = YAxis.AxisDependency.RIGHT
                        color = Color.parseColor("#EB592A")
                        setDrawIcons(false)
                        setDrawValues(true)
                        valueFormatter = CustomDecimalFormatter()
                        valueTextColor = Color.BLACK
                    }

                var dataSet = ArrayList<IBarDataSet>()
                dataSet.add(set)

                var data = BarData(dataSet)
                    .apply {
                        barWidth = 0.8f
                        setValueTextSize(10f)
                    }

                barChart.data = data

                barChart.apply {
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
                        axisMaximum = maxData + 1f
                        axisMinimum = 0f
                        granularity = yAxisUnit.toFloat()
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

                    data = data
                    setFitBars(true)
                    invalidate()
                }

                val mean = sumData / dataCount
                binding.meanText.text = String.format("%.2f", mean)
            }
        }

        binding.finishStatics.setOnClickListener { finish() }
    }

    private fun updateChart(){

    }

    private fun limitRange(): CalendarConstraints.Builder? {
        val constraintsBuilderRange = CalendarConstraints.Builder()
        val calendarStart: Calendar = Calendar.getInstance()
        val calendarEnd: Calendar = Calendar.getInstance()
        val startDate = date.minusDays(DATA_COUNT.toLong() - 1)
        var startYear = startDate.year
        var startMonth = startDate.month.value
        var startDay = startDate.dayOfMonth
        val endYear = date.year
        val endMonth = date.month.value
        val endDay = date.dayOfMonth
        calendarStart.set(startYear, startMonth - 1, startDay - 1)
        calendarEnd.set(endYear, endMonth - 1, endDay)
        val minDate: Long = calendarStart.timeInMillis
        val maxDate: Long = calendarEnd.timeInMillis
        constraintsBuilderRange.setStart(minDate)
        constraintsBuilderRange.setEnd(maxDate)
        constraintsBuilderRange.setValidator(RangeValidator(minDate, maxDate))
        return constraintsBuilderRange
    }

    inner class MyXAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return value.toString().split(".")[0]
        }
    }

    inner class CustomDecimalFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val score = value.toString().split(".")
            return score[0]
        }
    }
}