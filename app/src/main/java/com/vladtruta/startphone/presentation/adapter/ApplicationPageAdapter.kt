package com.vladtruta.startphone.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vladtruta.startphone.databinding.ListItemApplicationPageBinding
import com.vladtruta.startphone.model.local.ApplicationInfo

class ApplicationPageAdapter() :
    ListAdapter<List<ApplicationInfo>, ApplicationPageAdapter.ViewHolder>(
        ApplicationPageDiffCallback()
    ), ApplicationAdapter.ApplicationsListener {

    var listener: ApplicationPageListener? = null

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemApplicationPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemApplicationPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(applications: List<ApplicationInfo>) {
            binding.applicationsRv.adapter = ApplicationAdapter().apply {
                submitList(applications)
                listener = this@ApplicationPageAdapter
            }
            binding.applicationsRv.setRecycledViewPool(viewPool)
            binding.applicationsRv.suppressLayout(true)
        }
    }

    override fun onApplicationClicked(applicationInfo: ApplicationInfo) {
        listener?.onApplicationClicked(applicationInfo)
    }

    override fun onNeedHelpClicked() {
        listener?.onNeedHelpClicked()
    }

    interface ApplicationPageListener {
        fun onApplicationClicked(applicationInfo: ApplicationInfo)
        fun onNeedHelpClicked()
    }
}

private class ApplicationPageDiffCallback : DiffUtil.ItemCallback<List<ApplicationInfo>>() {
    override fun areItemsTheSame(
        oldItem: List<ApplicationInfo>,
        newItem: List<ApplicationInfo>
    ): Boolean {
        return oldItem.map { it.packageName } == newItem.map { it.packageName }
    }

    override fun areContentsTheSame(
        oldItem: List<ApplicationInfo>,
        newItem: List<ApplicationInfo>
    ): Boolean {
        return oldItem == newItem
    }
}