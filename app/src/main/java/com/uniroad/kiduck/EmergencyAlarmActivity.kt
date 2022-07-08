package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityEmergencyAlarmBinding

class EmergencyAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmergencyAlarmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.finishEmergencyAlarmButton.setOnClickListener { finish() }
    }
}