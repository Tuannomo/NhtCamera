package com.nht.nhtcamera.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nht.nhtcamera.model.ImageSaved
import com.nht.nhtcamera.R
import com.nht.nhtcamera.adapter.RcvImageCameraItemViewHolder.*
import com.nht.nhtcamera.databinding.ItemRcvImagePreviewBinding

class RcvImageCameraPreviewAdapter(
    private val context: Context
) : RecyclerView.Adapter<RcvImageCameraItemViewHolder>() {

    private var viewModels = mutableListOf<ImageSaved>()
    private var onItemImageClickListener: OnItemImageClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RcvImageCameraItemViewHolder {
        val binding: ItemRcvImagePreviewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_rcv_image_preview,
            parent,
            false
        )

        return RcvImageCameraItemViewHolder(
            binding = binding,
            context = context,
            onItemImageClickListener = onItemImageClickListener
        )
    }

    override fun onBindViewHolder(holder: RcvImageCameraItemViewHolder, position: Int) {
        holder.bindView(viewModels[position], position)
    }

    override fun getItemCount(): Int {
        return viewModels.size
    }

    fun setViewModels(viewModels: List<ImageSaved>) {
        this.viewModels = viewModels.toMutableList()
        notifyDataSetChanged()
    }

    fun setItemImageClickCallBack(onItemImageClickListener: OnItemImageClickListener) {
        this.onItemImageClickListener = onItemImageClickListener
    }
}