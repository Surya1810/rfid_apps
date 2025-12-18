package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.Borrowed
import com.partnership.bjbdocumenttrackerreader.databinding.ItemHistoryBorrowedBinding
import com.partnership.bjbdocumenttrackerreader.util.Utils

class HistoryBorrowedAdapter :
    RecyclerView.Adapter<HistoryBorrowedAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<Borrowed>()

    fun submitList(data: List<Borrowed>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged() // iya ini bukan DiffUtil, kita kejar hidup dulu
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBorrowedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Borrowed) = with(binding) {

            tvBorrowerName.text = item.borrowerName
            val estimatedReturn = if(item.estimatedReturnDate != null) Utils.formatDate(item.estimatedReturnDate) else "-"

            tvBorrowAt.text = "Dipinjam: ${item.borrowedAt ?: "-"}"
            tvEstimatedReturn.text =
                "Estimasi Kembali: $estimatedReturn"
            tvReturnedAt.text =
                "Dikembalikan: ${item.returnedAt ?: "-"}"

            val isReturned = item.returnedAt != null

            if (isReturned) {
                // === SUDAH DIKEMBALIKAN ===
                tvState.text = "Dikembalikan"

                lyState.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.state_good)
                )

                tvState.setTextColor(
                    ContextCompat.getColor(root.context, R.color.accent_good)
                )
            } else {
                // === SEDANG DIPINJAM ===
                tvState.text = "Dipinjam"

                lyState.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.md_theme_yellow)
                )

                tvState.setTextColor(
                    ContextCompat.getColor(root.context, R.color.md_theme_background)
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBorrowedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
