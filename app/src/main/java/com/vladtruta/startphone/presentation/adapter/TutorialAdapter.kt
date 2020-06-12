package com.vladtruta.startphone.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ListItemTutorialBinding
import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.util.UIUtils

class TutorialAdapter : ListAdapter<Tutorial, TutorialAdapter.ViewHolder>(TutorialDiffCallback()) {

    var listener: TutorialListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemTutorialBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemTutorialBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            initActions()
        }

        private fun initActions() {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }

                listener?.onTutorialClicked(getItem(position))
            }
        }

        fun bind(tutorial: Tutorial) {
            val position = adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return
            }

            binding.tutorialNumberTv.text = UIUtils.getString(R.string.tutorial_index_placeholder, position + 1)
            binding.tutorialQuestionTv.text = tutorial.title
        }
    }

    interface TutorialListener {
        fun onTutorialClicked(tutorial: Tutorial)
    }
}

private class TutorialDiffCallback : DiffUtil.ItemCallback<Tutorial>() {
    override fun areItemsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean {
        return oldItem == newItem
    }
}