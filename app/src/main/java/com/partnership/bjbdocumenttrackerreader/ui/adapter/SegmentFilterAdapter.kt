package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.GetListSegments

class SegmentFilterAdapter(
    segments: List<GetListSegments>,
    initialSelectedSegment: String?,                         // null = Semua
    private val onItemSelected: (String?) -> Unit            // null = Semua
) : RecyclerView.Adapter<SegmentFilterAdapter.SegmentViewHolder>() {

    // Index 0 = "Semua", sisanya dari API
    private val items: List<String> = listOf("Semua") + segments.map { it.name }

    // Tentukan posisi awal berdasarkan initialSelectedSegment
    private var selectedPosition: Int = when (initialSelectedSegment) {
        null -> 0 // "Semua"
        else -> {
            val index = items.indexOf(initialSelectedSegment)
            if (index >= 0) index else 0
        }
    }

    inner class SegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSegmentName)
        val rbSelected: RadioButton = itemView.findViewById(R.id.rbSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_segment, parent, false)
        return SegmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
        val segmentName = items[position]

        holder.tvName.text = segmentName
        holder.rbSelected.isChecked = position == selectedPosition

        val clickListener = View.OnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.adapterPosition

            if (oldPos != selectedPosition) {
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
            }

            // Mapping: index 0 = "Semua" => null
            val selectedValueForApi: String? =
                if (selectedPosition == 0) null else items[selectedPosition]

            onItemSelected(selectedValueForApi)
        }

        holder.itemView.setOnClickListener(clickListener)
        holder.rbSelected.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedValueForApi(): String? =
        if (selectedPosition == 0) null else items[selectedPosition]
}
