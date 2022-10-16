package com.uniroad.kiduck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniroad.kiduck.databinding.ActivityChangeNameBinding
import org.jetbrains.anko.toast

class ChangeNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setNameButton.setOnClickListener {
            val nextName = binding.nextName.text.toString().trim()
            if(nextName == ""){
                toast("새로운 이름을 입력하세요.")
            } else {

            }
        }

        binding.finishNameButton.setOnClickListener { finish() }
    }
}