package com.example.facedetecionsampleapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedetecionsampleapp.databinding.ActivityMainBinding
import com.example.facedetecionsampleapp.ui.DetectionActivity

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG : String = MainActivity::class.java.simpleName
        private const val CAMERA_REQUEST_CODE = 1000
    }

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        Log.d(TAG, "initView")

        binding.btSample1.setOnClickListener {
            val cameraPermissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            )

            if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // 권한이 없는 경우
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            } else {
                //권한이 있는 경우
                startDetectionActivity()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //권한이 거부된 경우
                Toast.makeText(this@MainActivity, "No Permission!!", Toast.LENGTH_SHORT).show()
            } else {
                //승인이 된 경우
                startDetectionActivity()
            }
        }
    }

    private fun startDetectionActivity() {
        Log.d(TAG, "startDetectionActivity()")
        val intent = Intent(this, DetectionActivity::class.java)
        startActivity(intent)
    }
}