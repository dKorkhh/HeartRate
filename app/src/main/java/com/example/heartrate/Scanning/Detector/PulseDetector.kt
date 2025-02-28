package com.example.heartrate.Scanning.Detector

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Log
import com.example.heartrate.Adapter.ResultHistoryCard
import com.google.gson.Gson
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.jtransforms.fft.DoubleFFT_1D
import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.global.opencv_imgproc.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class PulseDetector {
    private val FRAME_RATE = 30  // Кадров в секунду
    private val ANALYSIS_DURATION = 10 // Длительность анализа (сек)
    private val MAX_FRAMES = FRAME_RATE * ANALYSIS_DURATION

    private val redValues = mutableListOf<Double>()

    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun processVideo(videoPath: String) {
        val grabber = FFmpegFrameGrabber(videoPath)
        grabber.start()

        var frameCount = 0
        while (frameCount < MAX_FRAMES) {
            val frame = grabber.grabImage() ?: break
            val mat = OpenCVFrameConverter.ToMat().convert(frame) ?: continue

            val redValue = extractAverageRed(mat)
            redValues.add(redValue)

            frameCount++
        }
        grabber.stop()

        val pulse = calculatePulse()
        println("Определенный пульс: $pulse BPM")

        val result = ResultHistoryCard(pulse.toString(), getCurrentTime() + "\n" + getCurrentDate())
        val json = Gson().toJson(result)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "user_data.json")
        file.writeText(json)
    }

    private fun extractAverageRed(frame: Mat): Double {
        val indexer = frame.createIndexer<UByteIndexer>()
        var totalRed = 0
        var count = 0

        // Усредняем красный цвет по всей центральной области
        val startX = frame.cols() / 3
        val endX = frame.cols() * 2 / 3
        val startY = frame.rows() / 3
        val endY = frame.rows() * 2 / 3

        for (y in startY until endY) {
            for (x in startX until endX) {
//                totalRed += indexer.get(y, x, 2).toInt() // 2 - красный канал
                count++
            }
        }
        return totalRed / count.toDouble()
    }

    private fun calculatePulse(): Int {
        if (redValues.size < FRAME_RATE * 5) {
            println("Недостаточно данных для вычисления пульса")
            return -1
        }

        // Применяем сглаживание (скользящее среднее)
        val smoothed = movingAverage(redValues, 5)

        // Выполняем FFT для определения частоты пульса
        return fftAnalysis(smoothed)
    }

    private fun movingAverage(values: List<Double>, windowSize: Int): List<Double> {
        val result = mutableListOf<Double>()
        for (i in 0 until values.size - windowSize + 1) {
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
        val signal = DoubleArray(n * 2) // Двойной размер для FFT
        for (i in values.indices) {
            signal[i] = values[i]
        }

        val fft = DoubleFFT_1D(n.toLong())
        fft.realForward(signal)

        var bestIndex = 0
        var bestValue = 0.0
        for (i in (0.6 * n / FRAME_RATE).toInt() until (3.0 * n / FRAME_RATE).toInt()) {
            val magnitude = sqrt(signal[2 * i] * signal[2 * i] + signal[2 * i + 1] * signal[2 * i + 1])
            if (magnitude > bestValue) {
                bestValue = magnitude
                bestIndex = i
            }
        }

        // Переводим частоту в BPM
        return (bestIndex * FRAME_RATE * 60.0 / n).toInt()
    }
}

fun main() {
    val videoPath = "D:\\Java\\untitled6\\image.mp4"  // Путь к видео
    val detector = PulseDetector()
    detector.processVideo(videoPath)
}