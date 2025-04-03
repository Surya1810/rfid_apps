package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.databinding.RvDocumentLostBinding

class LostDocumentAdapter : PagingDataAdapter<String, LostDocumentAdapter.LostDocumentViewHolder>(DIFF_CALLBACK) {

    inner class LostDocumentViewHolder(private val binding: RvDocumentLostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.tvDocumentName.text = data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostDocumentViewHolder {
        val binding = RvDocumentLostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LostDocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LostDocumentViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}
