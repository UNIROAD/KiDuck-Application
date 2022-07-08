package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityGrowthSettingBinding

class GrowthSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGrowthSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrowthSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.finishGrowthSettingButton.setOnClickListener { finish() }
    }
}