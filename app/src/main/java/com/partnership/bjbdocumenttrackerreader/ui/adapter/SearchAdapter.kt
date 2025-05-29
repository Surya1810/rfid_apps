package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.Document
import com.partnership.bjbdocumenttrackerreader.databinding.RvDocumentBinding

class SearchAdapter(
    private val onItemClick: (Document) -> Unit
) : ListAdapter<Document, SearchAdapter.SearchViewHolder>(DiffCallback) {

    inner class SearchViewHolder(val binding: RvDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Document) {
            binding.tvNameDocument.text = item.name
            binding.tvCIF.text = item.cif
            binding.tvRfid.text = item.rfid
            binding.tvBaris.text = item.location?.row
            binding.tvBox.text = item.location?.box
            binding.tvRak.text = item.location?.rack
            if (item.segment.isNullOrEmpty()){
                binding.lySegment.visibility = View.GONE
            }else{
                binding.tvSegment.text = item.segment
            }
            if (item.isThere) {
                binding.tvState.text = "Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_good))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_good))
            } else {
                binding.tvState.text = "Tidak Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_bad))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_bad))
            }
            binding.root.setOnClickListener{
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = RvDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Document>() {
            override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
                // Bandingkan ID unik jika ada, misalnya epc sebagai identifier
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
                return oldItem == newItem
            }
        }
    }
}


