package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityStaticsBinding

class StaticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.finishStatics.setOnClickListener { finish() }
    }
}