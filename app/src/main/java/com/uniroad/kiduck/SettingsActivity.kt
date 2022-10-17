package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivitySettingsBinding
import org.jetbrains.anko.startActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private var deviceAddress: String? = null
    private var criteriaOfSteps: String? = null
    private var criteriaOfDrink: String? = null
    private var criteriaOfCommunication: String? = null
    private var kiduckName: String? = null
    private var kiduckPassword: String? = null
    private var emergencyAlarm_isEnabled: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceAddress = intent.getStringExtra("deviceAddress")
        criteriaOfSteps = intent.getStringExtra("criteriaOfSteps")
        criteriaOfDrink = intent.getStringExtra("criteriaOfDrink")
        criteriaOfCommunication = intent.getStringExtra("criteriaOfCommunication")
        kiduckName = intent.getStringExtra("kiduckName")
        kiduckPassword = intent.getStringExtra("kiduckPassword")
        emergencyAlarm_isEnabled = intent.getStringExtra("emergencyAlarm_isEnabled")

        binding.gotoGrowthSetting.setOnClickListener {
            startActivity<GrowthSettingActivity>(
                "deviceAddress" to deviceAddress,
                "criteriaOfSteps" to criteriaOfSteps,
                "criteriaOfDrink" to criteriaOfDrink,
                "criteriaOfCommunication" to criteriaOfCommunication
            )
        }

        binding.gotoNameSetting.setOnClickListener {
            startActivity<ChangeNameActivity>(
                "deviceAddress" to deviceAddress,
                "kiduckName" to kiduckName
            )
        }

        binding.gotoPasswordSetting.setOnClickListener {
            startActivity<ChangePasswordActivity>()
        }

        binding.gotoEmergencyAlarm.setOnClickListener {
            startActivity<EmergencyAlarmActivity>()
        }

        binding.finishSettingsButton.setOnClickListener { finish() }
    }
}