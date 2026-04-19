package com.emdez.pixeldarts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(
    private val list: List<PlayerStats>
) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pos: TextView = view.findViewById(R.id.tvPosition)
        val name: TextView = view.findViewById(R.id.tvName)
        val wins: TextView = view.findViewById(R.id.tvWins)
        val played: TextView = view.findViewById(R.id.tvPlayed)
        val winrate: TextView = view.findViewById(R.id.tvWinrate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_rank, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val p = list[position]

        holder.pos.text = "${position + 1}."
        holder.name.text = p.name
        holder.wins.text = "W:${p.wins}"
        holder.played.text = "P:${p.played}"
        holder.winrate.text = "${"%.1f".format(p.winRate)}%"
    }

    override fun getItemCount(): Int = list.size
}