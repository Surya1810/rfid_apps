package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.data.model.DetailAgunan
import com.partnership.bjbdocumenttrackerreader.databinding.RvListSearchAgunanBinding

class SearchAgunanAdapter(
    private val onItemClick: (DetailAgunan) -> Unit
) : ListAdapter<DetailAgunan, SearchAgunanAdapter.SearchAgunanViewHolder>(DiffCallback()) {

    inner class SearchAgunanViewHolder(private val binding: RvListSearchAgunanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailAgunan) {
            binding.tvEpc.text = item.rfidNumber
            binding.tvAgunanNumber.text = item.nomorAgunan
            binding.tvTypeAgunan.text = item.typeAgunan

            binding.cardDocument.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAgunanViewHolder {
        val binding = RvListSearchAgunanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchAgunanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchAgunanViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class DiffCallback : DiffUtil.ItemCallback<DetailAgunan>() {
        override fun areItemsTheSame(oldItem: DetailAgunan, newItem: DetailAgunan): Boolean {
            return oldItem.rfidNumber == newItem.rfidNumber
        }

        override fun areContentsTheSame(oldItem: DetailAgunan, newItem: DetailAgunan): Boolean {
            return oldItem == newItem
        }
    }
}