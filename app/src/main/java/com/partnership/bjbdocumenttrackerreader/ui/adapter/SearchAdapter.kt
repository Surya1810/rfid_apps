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
            binding.tvEpc.text = item.rfid
            binding.tvCIF.text = item.noDoc
            binding.tvName.text = item.name
            binding.linearLayout3.apply {
                if (item.segment == null){
                    binding.tvSegment.visibility = View.GONE
                    binding.tvLocation.visibility = View.GONE
                }else{
                    getChildAt(0)?.let { (it as TextView).text = "Segmen : ${item.segment ?: "-"}" }

                    val lokasi = item.location?.let {
                        "Lokasi : Ruangan ${it.room}, baris ${it.row}, rak ${it.rack}, box ${it.box}"
                    } ?: "Lokasi : -"
                    getChildAt(1)?.let { (it as TextView).text = lokasi }
                }
                val context = binding.root.context
                val status: String

                if (item.isThere) {
                    status = "Status : Ditemukan"
                    binding.cardDocument.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_primary))
                } else {
                    status = "Status : Tidak Ditemukan"
                    binding.cardDocument.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_outline))
                }

                getChildAt(2)?.let { (it as TextView).text = status }
            }
            binding.cardDocument.setOnClickListener {
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


