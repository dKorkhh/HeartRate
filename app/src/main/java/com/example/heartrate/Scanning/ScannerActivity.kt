package com.example.heartrate.Scanning

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.heartrate.Adapter.ResultHistoryCard
import com.example.heartrate.R
import com.example.heartrate.Scanning.Detector.ConvertingRGB
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import org.jtransforms.fft.DoubleFFT_1D
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException


class ScannerActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CAMERA = 83854
        private const val FRAME_RATE = 30
    }
    private lateinit var textView : TextView
    private lateinit var defineFinger : TextView
    private lateinit var defineFingerDef : TextView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val translator = ConvertingRGB()
    private val redValues = mutableListOf<Double>()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var btnBack : ImageView
    private lateinit var preview: ImageView
    private lateinit var handsScanner : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener(){
            finish()
        }

        preview = findViewById(R.id.preview)
        textView = findViewById(R.id.heart_rate)
        handsScanner = findViewById(R.id.hands_phone)
        defineFinger = findViewById(R.id.define_finger)
        defineFingerDef = findViewById(R.id.define_finger2)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        } else {
            initializeCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1024, 768))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ImageAnalysis.Analyzer { image ->
                    val img = image.image
                    val bitmap = translator.translateYUV(img!!, this)

                    if (isFinger(bitmap)) {
                        processBitmap(bitmap)
                        handsScanner.visibility = ImageView.INVISIBLE
                        defineFinger.setText("Йде Вимірювання.")
                        defineFingerDef.setText("Визначаємо ваш пульс. Утримуйте!")
                    } else {
                        defineFinger.setText("Палець не виявлено")
                        defineFingerDef.setText("Щільно прикладіть палець до камери")
                    }
                    preview.rotation = image.imageInfo.rotationDegrees.toFloat()
                    preview.setImageBitmap(bitmap)
                    image.close()
                })

                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)

            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun isFinger(bitmap: Bitmap): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L

        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            redSum += red
            greenSum += green
            blueSum += blue
        }

        val avgRed = redSum / totalPixels.toDouble()
        val avgGreen = greenSum / totalPixels.toDouble()
        val avgBlue = blueSum / totalPixels.toDouble()

        return avgRed > avgGreen * 1.2 && avgRed > avgBlue * 1.2
    }

    private fun processBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return

        val redValue = extractAverageRed(bitmap)
        redValues.add(redValue)

        val pulse = calculatePulse()
        if (pulse != -1) {
            textView.text = "$pulse"
            startTimerPulse(pulse)
        }
    }

    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun startTimerPulse(pulse: Int) {
        handler.postDelayed({
            val result = ResultHistoryCard(pulse.toString(), getCurrentTime() + "\n" + getCurrentDate())
            val json = Gson().toJson(result)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "user_data.json")
            file.writeText(json)

            intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("PULSE", pulse)
            intent.putExtra("DATETIME", getCurrentTime() + "\n" + getCurrentDate())
            startActivity(intent)

            cameraProviderFuture.cancel(true)
            finish()
        }, 2000)
    }

    private fun calculatePulse(): Int {
        if (redValues.size < FRAME_RATE * 2) {
            return -1
        }

        val smoothed = movingAverage(redValues, 5)
        return fftAnalysis(smoothed)
    }

    private fun movingAverage(values: List<Double>, windowSize: Int): List<Double> {
        val result = mutableListOf<Double>()
        for (i in 0..values.size - windowSize) {
            var sum = 0.0
            for (j in 0 until windowSize) {
                sum += values[i + j]
            }
            result.add(sum / windowSize)
        }
        return result
    }

    private fun fftAnalysis(values: List<Double>): Int {
        val n = values.size
        val signal = DoubleArray(n * 2)

        for (i in values.indices) {
            signal[i] = values[i]
        }

        val fft = DoubleFFT_1D(n.toLong())
        fft.realForward(signal)

        var bestIndex = 0
        var bestValue = 0.0
        for (i in (0.6 * n / FRAME_RATE).toInt() until (3.0 * n / FRAME_RATE).toInt()) {
            val magnitude = Math.sqrt(signal[2 * i] * signal[2 * i] + signal[2 * i + 1] * signal[2 * i + 1])
            if (magnitude > bestValue) {
                bestValue = magnitude
                bestIndex = i
            }
        }

        return (bestIndex * FRAME_RATE * 60.0 / n).toInt()
    }

    private fun extractAverageRed(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height

        var redSum = 0L

        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            val red = (pixel shr 16) and 0xFF
            redSum += red
        }

        return redSum / totalPixels.toDouble()
    }

    private fun turnOnFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

}