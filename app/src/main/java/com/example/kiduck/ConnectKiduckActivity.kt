package com.example.kiduck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kiduck.databinding.ActivityConnectKiduckBinding

class ConnectKiduckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectKiduckBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectKiduckBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}