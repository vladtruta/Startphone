package com.vladtruta.startphone.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vladtruta.startphone.databinding.ListItemVisibleApplicationBinding
import com.vladtruta.startphone.model.local.VisibleApplication

class VisibleApplicationAdapter :
    ListAdapter<VisibleApplication, VisibleApplicationAdapter.ViewHolder>(
        VisibleApplicationDiffCallback()
    ) {

    var listener: VisibleApplicationListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemVisibleApplicationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemVisibleApplicationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            initActions()
        }

        private fun initActions() {
            binding.applicationSw.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }

                binding.applicationSw.toggle()

                val visibleApplication = getItem(position).apply {
                    isVisible = binding.applicationSw.isChecked
                }

                listener?.onVisibleApplicationCheckedChanged(visibleApplication)
            }
        }

        fun bind(visibleApplication: VisibleApplication) {
            binding.applicationSw.text = visibleApplication.applicationInfo.packageName
            binding.applicationSw.setCompoundDrawablesRelativeWithIntrinsicBounds(
                visibleApplication.applicationInfo.icon,
                null,
                null,
                null
            )
        }
    }

    interface VisibleApplicationListener {
        fun onVisibleApplicationCheckedChanged(visibleApplication: VisibleApplication)
    }
}

private class VisibleApplicationDiffCallback : DiffUtil.ItemCallback<VisibleApplication>() {
    override fun areItemsTheSame(
        oldItem: VisibleApplication,
        newItem: VisibleApplication
    ): Boolean {
        return oldItem.applicationInfo.packageName == newItem.applicationInfo.packageName
    }

    override fun areContentsTheSame(
        oldItem: VisibleApplication,
        newItem: VisibleApplication
    ): Boolean {
        return oldItem == newItem
    }
}