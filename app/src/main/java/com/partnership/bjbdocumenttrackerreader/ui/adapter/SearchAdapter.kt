package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.data.model.DocumentDetail
import com.partnership.bjbdocumenttrackerreader.databinding.RvListSearchBinding

class SearchAdapter(
    private val onItemClick: (DocumentDetail) -> Unit
) : ListAdapter<DocumentDetail, SearchAdapter.SearchViewHolder>(DiffCallback) {

    inner class SearchViewHolder(val binding: RvListSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DocumentDetail) {
            binding.tvEpc.text = item.rfidNumber
            binding.tvCIF.text = item.cif
            binding.tvName.text = item.namaNasabah

            binding.cardDocument.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = RvListSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<DocumentDetail>() {
            override fun areItemsTheSame(oldItem: DocumentDetail, newItem: DocumentDetail): Boolean {
                // Bandingkan ID unik jika ada, misalnya epc sebagai identifier
                return oldItem.rfidNumber == newItem.rfidNumber
            }

            override fun areContentsTheSame(oldItem: DocumentDetail, newItem: DocumentDetail): Boolean {
                return oldItem == newItem
            }
        }
    }
}


