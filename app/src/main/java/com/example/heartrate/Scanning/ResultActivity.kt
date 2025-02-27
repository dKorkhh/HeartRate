package com.example.heartrate.Scanning

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.heartrate.Homepage
import com.example.heartrate.R

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bpm = intent.getStringExtra("PULSE")
        val date = intent.getStringExtra("DATETIME")

        val bpmTextView: TextView = findViewById(R.id.name_result)
        val dateTextView: TextView = findViewById(R.id.data_time)

        bpmTextView.text = bpm
        dateTextView.text = date

        val btnDone : AppCompatButton = findViewById(R.id.btn_done)
        btnDone.setOnClickListener(){
            val intent = Intent(this, Homepage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}