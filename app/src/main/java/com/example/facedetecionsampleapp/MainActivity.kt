package com.example.facedetecionsampleapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.facedetecionsampleapp.databinding.ActivityMainBinding
import com.example.facedetecionsampleapp.ui.DetectionActivity

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG : String = MainActivity::class.java.simpleName
    }

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.btSample1.setOnClickListener {
            val intent = Intent(this, DetectionActivity::class.java)
            startActivity(intent)
        }
    }

}