package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo
import com.partnership.bjbdocumenttrackerreader.databinding.RvTagBinding

class TagAdapter : ListAdapter<TagInfo, TagAdapter.TagViewHolder>(DIFF_CALLBACK) {

    inner class TagViewHolder(private val binding: RvTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagInfo) {
            binding.tvTag.text = tag.epc
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = RvTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TagInfo>() {
            override fun areItemsTheSame(oldItem: TagInfo, newItem: TagInfo): Boolean {
                // Bisa pakai EPC sebagai ID unik
                return oldItem.epc == newItem.epc
            }

            override fun areContentsTheSame(oldItem: TagInfo, newItem: TagInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
