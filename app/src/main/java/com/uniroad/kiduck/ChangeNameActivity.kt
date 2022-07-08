package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityChangeNameBinding

class ChangeNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeNameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.finishNameButton.setOnClickListener { finish() }
    }
}