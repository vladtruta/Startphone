package com.vladtruta.startphone.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vladtruta.startphone.databinding.ListItemTutorialPageBinding
import com.vladtruta.startphone.model.local.Tutorial

class TutorialPageAdapter :
    ListAdapter<List<Tutorial>, TutorialPageAdapter.ViewHolder>(TutorialPageDiffCallback()),
    TutorialAdapter.TutorialListener {

    var listener: TutorialPageListener? = null

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemTutorialPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemTutorialPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tutorials: List<Tutorial>) {
            binding.tutorialsRv.adapter = TutorialAdapter().apply {
                submitList(tutorials)
                listener = this@TutorialPageAdapter
            }

            binding.tutorialsRv.setRecycledViewPool(viewPool)
            binding.tutorialsRv.suppressLayout(true)
        }
    }

    override fun onTutorialClicked(tutorial: Tutorial) {
        listener?.onTutorialClicked(tutorial)
    }

    interface TutorialPageListener {
        fun onTutorialClicked(tutorial: Tutorial)
    }
}

private class TutorialPageDiffCallback : DiffUtil.ItemCallback<List<Tutorial>>() {
    override fun areItemsTheSame(oldItem: List<Tutorial>, newItem: List<Tutorial>): Boolean {
        return oldItem.map { it.packageName } == newItem.map { it.packageName }
    }

    override fun areContentsTheSame(oldItem: List<Tutorial>, newItem: List<Tutorial>): Boolean {
        return oldItem == newItem
    }
}