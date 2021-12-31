package com.example.shattered.leaderboards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shattered.R
import com.example.shattered.model.AllLevelsItem

class LeaderboardAdapter:
    ListAdapter<AllLevelsItem, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardCardsDiffCallback()) {

    class LeaderboardViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var username: TextView = view.findViewById(R.id.username_leaderboard)
        var scoreValue: TextView = view.findViewById(R.id.score_value_leaderboard)
        var firstStar: ImageView = view.findViewById(R.id.first_star_leaderboard)
        var secondStar: ImageView = view.findViewById(R.id.second_star_leaderboard)
        var thirdStar: ImageView = view.findViewById(R.id.third_star_leaderboard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_card, parent, false)
        return LeaderboardViewHolder(layout)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.username.text = item.username
        holder.thirdStar.setImageResource(
            if (item.numberOfStars >= 3) R.drawable.on_star_20dp else R.drawable.off_star_20dp)
        holder.secondStar.setImageResource(
            if (item.numberOfStars >= 2) R.drawable.on_star_20dp else R.drawable.off_star_20dp)
        holder.firstStar.setImageResource(
            if (item.numberOfStars >= 1) R.drawable.on_star_20dp else R.drawable.off_star_20dp)
        holder.scoreValue.text = item.score.toString()
    }
}

class LeaderboardCardsDiffCallback: DiffUtil.ItemCallback<AllLevelsItem>() {
    override fun areItemsTheSame(oldItem: AllLevelsItem, newItem: AllLevelsItem) = oldItem == newItem
    override fun areContentsTheSame(oldItem: AllLevelsItem, newItem: AllLevelsItem) = oldItem == newItem
}