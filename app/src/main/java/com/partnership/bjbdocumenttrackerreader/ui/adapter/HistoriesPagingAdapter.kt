package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.data.model.ScanItem
import com.partnership.bjbdocumenttrackerreader.databinding.RvHistoriesBinding
import com.partnership.bjbdocumenttrackerreader.util.Utils

class HistoriesPagingAdapter :
    PagingDataAdapter<ScanItem, HistoriesPagingAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {
        val binding = RvHistoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    class HistoryViewHolder(
        private val binding: RvHistoriesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanItem) {
            // Title
            binding.tvScanDate.text = Utils.formatDate(item.updatedAt)


            // Found
            binding.tvFoundCount.text = item.totalItems.toString()

            // Not Found
            binding.tvCountNotFound.text =
                item.totalMissing.toString()

            binding.tvScanTotal.text = item.totalItems.toString()

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScanItem>() {
            override fun areItemsTheSame(
                oldItem: ScanItem,
                newItem: ScanItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ScanItem,
                newItem: ScanItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
