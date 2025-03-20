package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.ItemStatus
import com.partnership.bjbdocumenttrackerreader.databinding.RvEpcLostBinding

class EpcLostAdapter : ListAdapter<ItemStatus, EpcLostAdapter.EpcViewHolder>(DiffCallback()) {

    inner class EpcViewHolder(private val binding: RvEpcLostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemStatus) {
            binding.tvEpc.text = item.epc
            if (item.isThere) {
                binding.ivStatus.setImageResource(R.drawable.check_24px)
                binding.ivStatus.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.md_theme_green))
            } else {
                binding.ivStatus.setImageResource(R.drawable.close_24px)
                binding.ivStatus.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.md_theme_error))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpcViewHolder {
        val binding = RvEpcLostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpcViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpcViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemStatus>() {
        override fun areItemsTheSame(oldItem: ItemStatus, newItem: ItemStatus): Boolean {
            return oldItem.epc == newItem.epc
        }

        override fun areContentsTheSame(oldItem: ItemStatus, newItem: ItemStatus): Boolean {
            return oldItem == newItem
        }
    }
}

