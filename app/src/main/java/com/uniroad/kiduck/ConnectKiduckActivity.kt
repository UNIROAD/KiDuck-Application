package com.uniroad.kiduck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.uniroad.kiduck.databinding.ActivityConnectKiduckBinding

class ConnectKiduckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectKiduckBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectKiduckBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}