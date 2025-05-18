package com.example.mykotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoresAdapter(
    private val items: List<ScoreEntry>,
    private val clickListener: (ScoreEntry) -> Unit
) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvRank  : TextView = view.findViewById(R.id.tvRank)
        private val tvName  : TextView = view.findViewById(R.id.tvName)
        private val tvScore : TextView = view.findViewById(R.id.tvScore)

        fun bind(entry: ScoreEntry, position: Int) {
            tvRank.text  = "${position + 1}."
            tvName.text  = entry.name
            tvScore.text = entry.score.toString()
            itemView.setOnClickListener { clickListener(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(items[pos], pos)
    }
}
