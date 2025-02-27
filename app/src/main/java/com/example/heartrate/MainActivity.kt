package com.example.heartrate

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var arrImageView: Array<ImageView>
    private lateinit var llPagerDots: LinearLayout
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.imageView)
        val btnStart = findViewById<AppCompatButton>(R.id.btn_start)
        llPagerDots = findViewById(R.id.pager_dots)
        setupPagerIndicatorDots()


        arrImageView[0].setImageResource(R.drawable.selected_dot)

        var j = 0

        btnStart.setOnClickListener {
            if (j == 2) {
                val intent = Intent(this, Homepage::class.java)
                startActivity(intent)
            }

            for (i in arrImageView.indices) {
                arrImageView[i].setImageResource(R.drawable.unselected_dot)
            }
            arrImageView[j + 1].setImageResource(R.drawable.selected_dot)

            imageView.setImageResource(
                when (j) {
                    1 -> R.drawable.onboarding2
                    else -> R.drawable.onboarding3
                }
            )

            j++
        }

    }

    private fun setupPagerIndicatorDots() {
        arrImageView = Array(3) { ImageView(this) }
        for (i in arrImageView.indices) {
            arrImageView[i] = ImageView(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 0, 5, 0)
            arrImageView[i].layoutParams = params
            arrImageView[i].setImageResource(R.drawable.unselected_dot)
            arrImageView[i].setOnClickListener { view ->
                view.alpha = 1f
            }
            llPagerDots.addView(arrImageView[i])
            llPagerDots.bringToFront()
        }
    }
}