package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity
import com.partnership.bjbdocumenttrackerreader.databinding.RvDocumentBinding

class SoDocumentAdapter(
    private val onItemClick: (AssetEntity) -> Unit
) : PagingDataAdapter<AssetEntity, SoDocumentAdapter.SoDocumentViewHolder>(DiffCallback) {

    inner class SoDocumentViewHolder(private val binding: RvDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AssetEntity?) {
            item ?: return
            if (item.isThere) {
                binding.tvState.text = "Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_good))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_good))
            } else {
                binding.tvState.text = "Tidak Ditemukan"
                binding.tvState.setTextColor(ContextCompat.getColor(binding.root.context, R.color.accent_bad))
                binding.lyState.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.state_bad))
            }
            binding.tvNameDocument.text = item.name
            binding.tvCIF.text = item.cif
            binding.tvRfid.text = item.rfid
            binding.tvBaris.text = item.location?.box
            binding.tvBox.text = item.location?.box
            binding.tvRak.text = item.location?.rack
            if (item.segment.isNullOrEmpty()){
                binding.lySegment.visibility = View.GONE
            }else{
                binding.tvSegment.text = item.segment
            }

            binding.root.setOnClickListener{
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoDocumentViewHolder {
        val binding = RvDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoDocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoDocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<AssetEntity>() {
            override fun areItemsTheSame(oldItem: AssetEntity, newItem: AssetEntity): Boolean {
                return oldItem.id == newItem.id && oldItem.rfid == newItem.rfid
            }

            override fun areContentsTheSame(oldItem: AssetEntity, newItem: AssetEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
