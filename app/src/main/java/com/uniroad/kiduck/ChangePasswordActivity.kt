package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.finishPasswordButton.setOnClickListener { finish() }
    }
}