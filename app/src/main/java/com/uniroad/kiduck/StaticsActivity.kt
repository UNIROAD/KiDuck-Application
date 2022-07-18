package com.uniroad.kiduck

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.uniroad.kiduck.databinding.ActivityStaticsBinding

class StaticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}