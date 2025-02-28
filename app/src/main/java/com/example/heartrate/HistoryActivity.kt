package com.example.heartrate

import android.content.Context
import android.os.Bundle
import android.os.Environment
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException

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

        /*val result = ResultHistoryCard("80 BPM", "11:07 \n30/01/2024")
        val json = Gson().toJson(result)*/

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "user_data.json")
        //file.writeText(json)

        val userList = readJsonFromDownloads()
        val items = mutableListOf<ResultHistoryCard>()

        userList?.let {
            items.addAll(listOf(it))
        }

        val adapter = ResultHistoryAdapter(items, this)
        recyclerView.adapter = adapter

        val btnBack : ImageView = findViewById(R.id.btn_arrow_back)
        btnBack.setOnClickListener(){
            Toast.makeText(this, "Back to scanning", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnClear.setOnClickListener {
            // Clear the items list
            items.clear()
            adapter.notifyDataSetChanged()

            // Delete the JSON file
            val file = File(downloadsDir, "user_data.json")
            if (file.exists()) {
                file.delete()
            }

            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
        }


        Toast.makeText(this, "User data: $userList", Toast.LENGTH_SHORT).show()

    }

    fun readJsonFromDownloads(): ResultHistoryCard? {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "user_data.json")

            if (file.exists()) {
                val json = file.readText() // Read the content of the file
                return Gson().fromJson(json, ResultHistoryCard::class.java) // Convert JSON to User object
            } else {
                println("File does not exist")
                return null
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }
}