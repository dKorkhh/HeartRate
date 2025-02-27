package com.example.heartrate

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heartrate.Adapter.ResultHistoryAdapter
import com.example.heartrate.Adapter.ResultHistoryCard

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val btnClear: AppCompatButton = findViewById(R.id.clearHistoryButton)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val items = listOf(
                ResultHistoryCard("80 BPM", "11:07 \n30/01/2024"),
            ResultHistoryCard("75 BPM", "10:45 \n29/01/2024"),
        // Add more items here
        )

        val adapter = ResultHistoryAdapter(items, this)
        recyclerView.adapter = adapter

        val btnBack : ImageView = findViewById(R.id.btn_arrow_back)
        btnBack.setOnClickListener(){
            Toast.makeText(this, "Back to scanning", Toast.LENGTH_SHORT).show()
            finish()
        }


    }
}