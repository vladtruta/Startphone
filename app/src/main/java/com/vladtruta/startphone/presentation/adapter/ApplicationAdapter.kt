package com.vladtruta.startphone.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ListItemApplicationBinding
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.util.UIUtils

class ApplicationAdapter :
    ListAdapter<ApplicationInfo, ApplicationAdapter.ViewHolder>(ApplicationsDiffCallback()) {

    var listener: ApplicationsListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemApplicationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemApplicationBinding) :
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

                val clickedItem = getItem(position)
                if (clickedItem.packageName.isNotEmpty()) {
                    listener?.onApplicationClicked(clickedItem)
                } else {
                    listener?.onNeedHelpClicked()
                }
            }
        }

        fun bind(applicationInfo: ApplicationInfo) {
            binding.applicationTv.text = applicationInfo.label
            binding.applicationIv.setImageDrawable(applicationInfo.icon)

            if (applicationInfo.packageName.isNotEmpty()) {
                binding.applicationIv.setBackgroundColor(Color.WHITE)
                binding.applicationTv.setBackgroundColor(UIUtils.getColor(android.R.color.holo_orange_dark))
                binding.applicationTv.setTextColor(Color.BLACK)
            } else {
                // This means it's the help menu
                binding.applicationIv.setBackgroundColor(UIUtils.getColor(R.color.help_red))
                binding.applicationTv.setBackgroundColor(UIUtils.getColor(R.color.help_red))
                binding.applicationTv.setTextColor(Color.WHITE)
            }
        }
    }

    interface ApplicationsListener {
        fun onApplicationClicked(applicationInfo: ApplicationInfo)
        fun onNeedHelpClicked()
    }
}

private class ApplicationsDiffCallback : DiffUtil.ItemCallback<ApplicationInfo>() {
    override fun areItemsTheSame(oldItem: ApplicationInfo, newItem: ApplicationInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: ApplicationInfo, newItem: ApplicationInfo): Boolean {
        return oldItem == newItem
    }
}