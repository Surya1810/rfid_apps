package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.TagInfo

class EpcAdapter(private val items: MutableList<TagInfo>) : RecyclerView.Adapter<EpcAdapter.EpcViewHolder>() {

    // ViewHolder untuk item RecyclerView
    class EpcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val epc: TextView = itemView.findViewById(R.id.tvEpc)
    }

    // Menentukan jumlah item yang ada di dalam list
    override fun getItemCount(): Int {
        return items.size
    }

    // Menghubungkan ViewHolder dengan data item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpcViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_epc, parent, false)
        return EpcViewHolder(view)
    }

    // Mengisi data pada item dan menampilkan data ke dalam tampilan
    override fun onBindViewHolder(holder: EpcViewHolder, position: Int) {
        val currentItem = items[position]
        holder.epc.text = currentItem.epc
    }

    // Memperbarui data yang ada dengan DiffUtil untuk efisiensi pembaruan
    fun updateData(newItems: List<TagInfo>) {
        val diffCallback = EpcDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil Callback untuk memeriksa perubahan data
    class EpcDiffCallback(
        private val oldList: List<TagInfo>,
        private val newList: List<TagInfo>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Mengecek apakah item yang dibandingkan adalah item yang sama
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Mengecek apakah isi dari item yang dibandingkan sama
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
