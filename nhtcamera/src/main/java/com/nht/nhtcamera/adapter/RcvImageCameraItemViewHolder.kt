package com.nht.nhtcamera.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.nht.nhtcamera.model.ImageSaved
import com.bumptech.glide.Glide
import com.nht.nhtcamera.databinding.ItemRcvImagePreviewBinding

class RcvImageCameraItemViewHolder(
    private val binding: ItemRcvImagePreviewBinding,
    private val context: Context,
    private val onItemImageClickListener: OnItemImageClickListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bindView(imageSaved: ImageSaved, position: Int) {
        Glide.with(context)
            .load(imageSaved.imgBitmap)
            .into(binding.imgPreview)

        binding.layoutPreview.setOnClickListener {
            onItemImageClickListener?.onItemImageClick(position)
        }
    }

    interface OnItemImageClickListener {
        fun onItemImageClick(position: Int)
    }
}