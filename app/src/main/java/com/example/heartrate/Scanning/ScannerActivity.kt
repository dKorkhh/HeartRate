package com.example.heartrate.Scanning

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.heartrate.R
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import java.io.IOException
import java.util.concurrent.Executors
import android.os.Environment
import java.io.File
import java.util.concurrent.TimeUnit


class ScannerActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        surfaceView = findViewById(R.id.surface_view)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || true) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            camera?.unlock()
            Toast.makeText(this@ScannerActivity, "Запись началась", Toast.LENGTH_SHORT).show()
            startRecordingWithDelay()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        camera?.stopPreview()
        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRecording()
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun startRecordingWithDelay() {
        Executors.newSingleThreadScheduledExecutor().schedule({
            runOnUiThread { startRecording() }
        }, 1, TimeUnit.SECONDS)
    }

    private fun startRecording() {
        Toast.makeText(this@ScannerActivity, "З555ь", Toast.LENGTH_SHORT).show()
        camera?.unlock()
        mediaRecorder = MediaRecorder().apply {
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setOrientationHint(90)

            val videoFile = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "recorded_video.mp4")
            setOutputFile(videoFile.absolutePath)
            setPreviewDisplay(surfaceHolder.surface)
            Toast.makeText(this@ScannerActivity, "Запись началась", Toast.LENGTH_SHORT).show()

            try {
                prepare()
                start()
                Toast.makeText(this@ScannerActivity, "Запись началась", Toast.LENGTH_SHORT).show()
                Executors.newSingleThreadScheduledExecutor().schedule({
                    runOnUiThread { stopRecording() }
                }, 5, TimeUnit.SECONDS)
            } catch (e: IOException) {
                Toast.makeText(this@ScannerActivity, "Ошибка записи видео", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        camera?.lock()
        Toast.makeText(this, "Видео сохранено", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startRecordingWithDelay()
            }
        }
    }

}