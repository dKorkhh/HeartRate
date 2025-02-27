package com.example.heartrate

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InformationActivity : AppCompatActivity() {
    private lateinit var arrImageView: Array<ImageView>
    private lateinit var llPagerDots: LinearLayout
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_information)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.imageView)
        val btnStart = findViewById<AppCompatButton>(R.id.btn_start)
        var textDescrip : TextView = findViewById<AppCompatButton>(R.id.text_description)
        var mainText : TextView = findViewById<AppCompatButton>(R.id.text_main)
        llPagerDots = findViewById(R.id.pager_dots)
        setupPagerIndicatorDots()


        arrImageView[0].setImageResource(R.drawable.selected_dot)

        var j = 0

        btnStart.setOnClickListener {
            j++

            if (j == 3) {
                val intent = Intent(this, Homepage::class.java)
                startActivity(intent)
                finish()
            }
            else{
                for (i in arrImageView.indices) {
                    arrImageView[i].setImageResource(R.drawable.unselected_dot)
                }
                arrImageView[j].setImageResource(R.drawable.selected_dot)


                if (j == 1){
                    textDescrip.setText("Програма надає дієві поради, які допоможуть " +
                            "вам підтримувати оптимальний рівень артеріального тиску та " +
                            "\u2028зменшити фактори ризику серцево-судинних захворювань.")

                    btnStart.setText("Продовжити!")
                    imageView.setImageResource(R.drawable.onboarding1)
                    mainText.setText("Персоналізовані поради")
                }
                else if (j == 2){
                    textDescrip.setText("Не відставайте від графіка контролю артеріального тиску " +
                            "та прийому ліків за допомогою спеціальних нагадувань.")
                    btnStart.setText("Почати!")
                    imageView.setImageResource(R.drawable.onboarding3)
                    mainText.setText("Нагадування")
                }
            }
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