package com.partnership.bjbdocumenttrackerreader.ui.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partnership.bjbdocumenttrackerreader.R
import com.partnership.bjbdocumenttrackerreader.data.model.TutorialVideo
import androidx.core.net.toUri

class VideoTutorialAdapter(
    private val onItemClick: ((TutorialVideo) -> Unit)? = null
) : ListAdapter<TutorialVideo, VideoTutorialAdapter.VideoVH>(Diff) {

    object Diff : DiffUtil.ItemCallback<TutorialVideo>() {
        override fun areItemsTheSame(oldItem: TutorialVideo, newItem: TutorialVideo): Boolean {
            // anggap link unik per video
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(oldItem: TutorialVideo, newItem: TutorialVideo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_list_tutorial, parent, false)
        return VideoVH(view)
    }

    override fun onBindViewHolder(holder: VideoVH, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class VideoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val arrow: ImageView = itemView.findViewById(R.id.imageView3)

        fun bind(item: TutorialVideo, onItemClick: ((TutorialVideo) -> Unit)?) {
            title.text = item.title

            val clicker = View.OnClickListener {
                if (onItemClick != null) {
                    onItemClick(item)
                } else {
                    // default behavior: buka link dengan intent
                    openLink(itemView.context, item.link)
                }
            }

            itemView.setOnClickListener(clicker)
            arrow.setOnClickListener(clicker)
        }

        private fun openLink(context: Context, rawLink: String) {
            val uri = ensureHttpScheme(rawLink).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // fallback: coba buka melalui chooser
                val chooser = Intent.createChooser(intent, "Buka dengan")
                context.startActivity(chooser)
            }
        }

        private fun ensureHttpScheme(link: String): String {
            val trimmed = link.trim()
            return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "https://$trimmed"
            }
        }
    }
}