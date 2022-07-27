package com.uniroad.kiduck

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.material.datepicker.MaterialDatePicker
import com.uniroad.kiduck.databinding.ActivityStaticsBinding

class StaticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")
        val dataList = intent.getStringArrayListExtra("data")
        val DATA_COUNT = dataList!!.size

        var barChart: BarChart = binding.barchart

        val entries = ArrayList<BarEntry>()
        for(i in 1..DATA_COUNT) {
            entries.add(BarEntry(i.toFloat(), dataList[i-1].toFloat()))
        }

        var set = BarDataSet(entries, "걸음수")
            .apply {
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
                axisMaximum = 12399f + 1f
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

            data = data
            setFitBars(true)
            invalidate()
        }

        binding.finishStatics.setOnClickListener { finish() }

        binding.setDateRangeButton.setOnClickListener {
            //참고 url : https://www.geeksforgeeks.org/material-design-date-range-picker-in-android-using-kotlin/
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("조회할 기간 선택")
                .build()
            dateRangePicker.show(supportFragmentManager, "DatePicker")

            dateRangePicker.addOnPositiveButtonClickListener {
                binding.dateRangeText.text = dateRangePicker.headerText
                // 통계치 수정 코드 여기 작성

            }

            dateRangePicker.addOnNegativeButtonClickListener {
                Toast.makeText(this, "기간 선택이 취소되었습니다.", Toast.LENGTH_LONG).show()
            }

            dateRangePicker.addOnCancelListener {
                Toast.makeText(this, "기간 선택이 취소되었습니다.", Toast.LENGTH_LONG).show()
            }

        }
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