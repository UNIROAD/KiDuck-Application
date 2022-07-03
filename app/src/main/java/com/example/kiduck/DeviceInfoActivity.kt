package com.example.kiduck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kiduck.databinding.ActivityDeviceInfoBinding

class DeviceInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}