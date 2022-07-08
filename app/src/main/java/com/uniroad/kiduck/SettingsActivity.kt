package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivitySettingsBinding
import org.jetbrains.anko.startActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gotoGrowthSetting.setOnClickListener {
            startActivity<GrowthSettingActivity>()
        }

        binding.gotoNameSetting.setOnClickListener {
            startActivity<ChangeNameActivity>()
        }

        binding.gotoPasswordSetting.setOnClickListener {
            startActivity<ChangePasswordActivity>()
        }

        binding.gotoEmergencyAlarm.setOnClickListener {
            startActivity<EmergencyAlarmActivity>()
        }


    }
}