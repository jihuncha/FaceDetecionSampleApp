package com.example.facedetecionsampleapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.facedetecionsampleapp.databinding.ActivityCameraBinding
import com.google.android.gms.common.util.concurrent.HandlerExecutor


class CameraActivity : AppCompatActivity() {
    companion object {
        val TAG: String = CameraActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityCameraBinding

    //카메라 매니저
    private lateinit var cameraManager: CameraManager

    private lateinit var previewSurface: Surface

    private lateinit var cameraDevice: CameraDevice

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

            this@CameraActivity.cameraDevice = cameraDevice

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
                    //일단 임시로 yuv중에서 가장 마지막걸로 사용.
                    val previewSize = yuvSizes.last()

                    val displayRotation = windowManager.defaultDisplay.rotation

                    val swappedDimensions = checkDimensions(displayRotation, cameraCharacteristics)
                    Log.d(TAG, "swappedDimensions - $swappedDimensions")
                    // swap width and height if needed
                    val rotatedPreviewWidth =
                        if (swappedDimensions) previewSize.height else previewSize.width
                    val rotatedPreviewHeight =
                        if (swappedDimensions) previewSize.width else previewSize.height

                    Log.d(TAG, "width - $rotatedPreviewWidth, height - $rotatedPreviewHeight")

                    //surface view의 사이즈를 설정해준다.
                    binding.svCamera.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)
                }
            }

//            val sessionConfiguration = SessionConfiguration(
//                SessionConfiguration.SESSION_REGULAR,
//                Collections.singletonList(outputConfiguration),
//                HandlerExecutor(mCameraHandler.getLooper()),
//                mCameraSessionListener
//            )
//            cameraDevice.createCaptureSession(sessionConfiguration)

            //카메라 캡쳐
            cameraDevice.createCaptureSession(mutableListOf(previewSurface),
                captureCallback,
                Handler(Looper.getMainLooper()))
        }
    }

    //CameraCaptureSessionCallback
    val captureCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            Log.d(TAG, "onConfigured")
            // session configured
            val previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                .apply {
                    addTarget(previewSurface)
                }
            session.setRepeatingRequest(
                previewRequestBuilder.build(),
                object: CameraCaptureSession.CaptureCallback(){},
                Handler(Looper.getMainLooper())
            )
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "onConfigureFailed - $session")
        }

    }

    //surface 준비됨을 파악
    private val surfaceReadyCallback = object: SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            startCameraSession()
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //단말 하드웨어 카메라 여부 체크
        if (!checkCameraHardware(this)) {
            Log.e(TAG, "onCreate/checkCameraHardware - NO Camera!!")
            Toast.makeText(this, "No Available Camera!!", Toast.LENGTH_SHORT).show()
            return
        }

        //매니저를 설정한다.
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        previewSurface = binding.svCamera.holder.surface
        binding.svCamera.holder.addCallback(surfaceReadyCallback)
    }

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun startCameraSession() {
        Log.d(TAG, "startCameraSession")

        if (cameraManager.cameraIdList.isEmpty()) {
            Log.e(TAG, "startCameraSession - No Camera!!")
            return
        }

        val firstCamera = cameraManager.cameraIdList[0]
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //권한이 없는 경우..
            return
        }

        //카메라를 open한다.
        //TODO 예외처리필요
        cameraManager.openCamera(firstCamera, openCameraCallback, Handler(Looper.getMainLooper()))
    }

    private fun checkDimensions(
        displayRotation: Int,
        cameraCharacteristics: CameraCharacteristics
    ): Boolean {
        Log.d(TAG, "checkDimensions")

        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_ORIENTATION
                    ) == 270
                ) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_ORIENTATION
                    ) == 180
                ) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
                Log.e(TAG, "checkDimensions/invalid display rotation!!")
            }
        }
        return swappedDimensions
    }
}