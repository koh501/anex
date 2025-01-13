package com.example.imgshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.camera.view.PreviewView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.example.imgshot.createFile

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private val imageList = mutableListOf<Bitmap>()
    private val imagePaths = mutableListOf<String>()
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var continuousShootingCheckbox: CheckBox

    // 연속 촬영 모드 변수 선언
    private var isContinuousShooting = false

    // MediaPlayer 선언
    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        val button: Button = findViewById(R.id.button)
        continuousShootingCheckbox = findViewById(R.id.continuous_shooting_checkbox)
        recyclerView = findViewById(R.id.recyclerView)
        adapter = ImageAdapter(imageList, { position ->
            // 이미지 삭제 로직
            imageList.removeAt(position)
            imagePaths.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "이미지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }, { bitmap, position ->
            // 전체 화면 이미지 표시
            val intent = Intent(this, FullscreenImageActivity::class.java)
            intent.putExtra("images", ArrayList(imageList))
            intent.putExtra("position", position)
            startActivity(intent)
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(this, R.raw.shutter)

        // 권한 확인
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        button.setOnClickListener {
            isContinuousShooting = continuousShootingCheckbox.isChecked
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraXApp", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = createFile(this) // createFile 함수를 사용합니다
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXApp", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val bitmap = BitmapFactory.decodeFile(savedUri.path)
                    imageList.add(bitmap)
                    imagePaths.add(savedUri.path ?: return) // Non-nullable String으로 변환
                    adapter.notifyDataSetChanged()

                    // 셔터 소리 재생
                    mediaPlayer.start()

                    // 이미지 리스트 표시 액티비티로 전환
                    val intent = Intent(this@MainActivity, DisplayImageListActivity::class.java)
                    intent.putStringArrayListExtra("image_paths", ArrayList(imagePaths))
                    startActivity(intent)

                    // 연속 촬영 모드가 활성화된 경우 다음 촬영 호출
                    if (isContinuousShooting) {
                        handler.postDelayed({ takePhoto() }, 1000) // 1초 간격으로 촬영
                    }
                }
            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                // 권한을 거부한 경우 앱을 종료하지 않고 권한 요청을 다시 하도록 합니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isContinuousShooting = false
        handler.removeCallbacksAndMessages(null)
        cameraExecutor.shutdown()
    }
}
