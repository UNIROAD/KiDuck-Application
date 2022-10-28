package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivitySettingsBinding
import org.jetbrains.anko.startActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private var deviceAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceAddress = intent.getStringExtra("deviceAddress")

        binding.gotoGrowthSetting.setOnClickListener {
            startActivity<GrowthSettingActivity>(
                "deviceAddress" to deviceAddress
            )
        }

        binding.gotoNameSetting.setOnClickListener {
            startActivity<ChangeNameActivity>(
                "deviceAddress" to deviceAddress
            )
        }

        binding.finishSettingsButton.setOnClickListener { finish() }
    }
}