package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            if (item.noRef == null){
                binding.tvNoRef.visibility = View.GONE
            }else{
                binding.tvNoRef.visibility = View.VISIBLE
                binding.tvNoRef.text = "No.Ref : ${item.noRef}"
            }
            binding.tvRfid.text = "RFID : ${item.rfid}"
            if (item.cif == null){
                binding.tvNoCif.visibility = View.GONE
            }else{
                binding.tvNoCif.visibility = View.VISIBLE
                binding.tvNoCif.text = "CIF : ${item.cif}"
            }
            binding.tvNoDoc.text = "No.Doc : ${item.noDoc}"
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
            if (item.isBorrowed){
                binding.lyStateLending.visibility = View.VISIBLE
                binding.tvStateLending.text = "Dipinjam"
                binding.tvStateLending.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_background))
                binding.lyStateLending.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_yellow))
            }else{
                binding.lyStateLending.visibility = View.GONE
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
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
                return oldItem == newItem
            }
        }
    }
}


