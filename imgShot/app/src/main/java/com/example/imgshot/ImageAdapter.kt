package com.example.imgshot

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(
    private val imageList: List<Bitmap>,
    private val onLongClick: (Int) -> Unit,
    private val onClick: (Bitmap, Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view, onLongClick, onClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageBitmap(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ImageViewHolder(
        itemView: View,
        onLongClick: (Int) -> Unit,
        onClick: (Bitmap, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
            itemView.setOnClickListener {
                onClick(imageView.drawable.toBitmap(), adapterPosition)
            }
        }
    }
}
