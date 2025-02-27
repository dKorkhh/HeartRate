package com.example.heartrate

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.heartrate.Scanning.ScanningActivity

class Homepage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMeasure : ImageView = findViewById(R.id.btn_measure)
        val btnHistory : ImageView = findViewById(R.id.btn_history)

        btnMeasure.setOnClickListener(){
            val intent = Intent(this, ScanningActivity::class.java)
            startActivity(intent)
        }

        btnHistory.setOnClickListener(){
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}