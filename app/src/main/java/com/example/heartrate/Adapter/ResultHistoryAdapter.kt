package com.example.heartrate.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heartrate.R
import com.example.heartrate.Scanning.ResultActivity

class ResultHistoryAdapter(private val items: List<ResultHistoryCard>, private val context: Context) :
    RecyclerView.Adapter<ResultHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pulseTextView: TextView = itemView.findViewById(R.id.count_pulse)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.data_time)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    val intent = Intent(context, ResultActivity::class.java).apply {
                        putExtra("PULSE", item.pulse)
                        putExtra("DATETIME", item.dateTime)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.result_history_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.pulseTextView.text = item.pulse
        holder.dateTimeTextView.text = item.dateTime
    }

    override fun getItemCount(): Int = items.size
}