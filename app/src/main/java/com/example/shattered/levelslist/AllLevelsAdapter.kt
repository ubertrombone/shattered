package com.example.shattered.levelslist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shattered.MainActivity
import com.example.shattered.R
import com.example.shattered.leaderboards.LeaderboardFragment
import com.example.shattered.model.AllLevelsItem

class AllLevelsAdapter(private val context: Context):
    ListAdapter<AllLevelsItem, AllLevelsAdapter.AllLevelsViewHolder>(LevelsCardsDiffCallback()) {

    class AllLevelsViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var card: CardView = view.findViewById(R.id.all_levels_card)
        var level: TextView = view.findViewById(R.id.level_number)
        var scoreValue: TextView = view.findViewById(R.id.score_value)
        var firstStar: ImageView = view.findViewById(R.id.first_star)
        var secondStar: ImageView = view.findViewById(R.id.second_star)
        var thirdStar: ImageView = view.findViewById(R.id.third_star)
        val activity =  itemView.context as? MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllLevelsViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.all_levels_cards, parent, false)
        return AllLevelsViewHolder(layout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllLevelsViewHolder, position: Int) {
        val item = getItem(position)
        holder.level.text = context.getString(R.string.level, item.level.toString())
        holder.scoreValue.text = item.score.toString()
        holder.firstStar.setImageResource(
            if (item.numberOfStars >= 1) R.drawable.on_star_24dp else R.drawable.off_star_24dp)
        holder.secondStar.setImageResource(
            if (item.numberOfStars >= 2) R.drawable.on_star_24dp else R.drawable.off_star_24dp)
        holder.thirdStar.setImageResource(
            if (item.numberOfStars >= 3) R.drawable.on_star_24dp else R.drawable.off_star_24dp)
        holder.card.setOnClickListener {
            LeaderboardFragment.newInstance(item.level.toString())
                .show(holder.activity?.supportFragmentManager!!, LeaderboardFragment.TAG)
        }
    }
}

class LevelsCardsDiffCallback: DiffUtil.ItemCallback<AllLevelsItem>() {
    override fun areItemsTheSame(oldItem: AllLevelsItem, newItem: AllLevelsItem) = oldItem == newItem
    override fun areContentsTheSame(oldItem: AllLevelsItem, newItem: AllLevelsItem) = oldItem == newItem
}