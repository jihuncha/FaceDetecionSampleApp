package com.example.facedetecionsampleapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.facedetecionsampleapp.databinding.ActivityDetectionBinding

class DetectionActivity : AppCompatActivity() {
    companion object {
        val TAG = DetectionActivity::class.java.simpleName
    }

    private lateinit var binding : ActivityDetectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}