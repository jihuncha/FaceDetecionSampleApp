package com.example.facedetecionsampleapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

import com.example.facedetecionsampleapp.databinding.ActivityDetectionBinding

class DetectionActivity : AppCompatActivity() {
    companion object {
        val TAG = DetectionActivity::class.java.simpleName
    }

    private lateinit var binding : ActivityDetectionBinding
    //카메라 매니저
    private lateinit var cameraManager : CameraManager
    //Open Camera Callback
    private val openCameraCallback = object : CameraDevice.StateCallback() {
        override fun onDisconnected(p0: CameraDevice) {
            //TODO
            Log.d(TAG, "Camera onDisconnected")
        }
        override fun onError(p0: CameraDevice, p1: Int) {
            //TODO
            Log.e(TAG, "Camera onError")
        }

        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "Camera onOpened")

            //카메라 식별자 획득
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
            //카메라의 각종 지원 정보 획득 - streamConfigurationMap

            //출력 유형은 프레임이 인코딩되는 형식을 나타냅니다. 공식문서에 설명 된 가능한 값은 PRIV, YUV, JPEG 및 RAW입니다.
            //PRIV는 애플리케이션에 직접표시되지 않는 형식과 함께 StreamConfigurationMap.getOutputSizes (Class)를 사용하여 사용 가능한 사이즈를 가진 타겟을 참조합니다.
            //YUV는 ImageFormat.YUV_420_888포맷을 사용하는 Surface 타겟을 참조합니다.
            //JPEG는 ImageFormat.JPEG 포맷을 참조합니다.
            //RAW는 ImageFormat.RAW_SENSOR 포맷을 참조합니다.
            //TODO 임시로 잡았지만 추후 해상도에 맞게 사이즈를 찾는 것이 중요하다
            cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                    val previewSize = yuvSizes.last()

                    val displayRotation = windowManager.defaultDisplay.rotation
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //단말 하드웨어 카메라 여부 체크
        if (!checkCameraHardware(this)) {
            Log.e(TAG, "onCreate/checkCameraHardware - NO Camera!!")
        }

        //매니저를 설정한다.
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        startCameraSession()
    }

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun startCameraSession() {
        if (cameraManager.cameraIdList.isEmpty()) {
            Log.e(TAG, "startCameraSession - No Camera!!")
            return
        }

        val firstCamera = cameraManager.cameraIdList[0]
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없는 경우..
            return
        }

        //카메라를 open한다.
        //TODO 예외처리필요
        cameraManager.openCamera(firstCamera, openCameraCallback, Handler(Looper.getMainLooper()))
    }
}