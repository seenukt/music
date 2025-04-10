package com.example.mymusicapplication.adapter.recyclerview

import android.graphics.BitmapFactory
import android.graphics.DiscretePathEffect
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.R
import com.example.mymusicapplication.constant.getImage
import com.example.mymusicapplication.databinding.MusicItemBinding
import com.example.mymusicapplication.model.MusicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicAdapter(private val innerClick:(file:MusicFile, position : Int)->Unit ): ListAdapter<MusicFile, MusicAdapter.ItemHolder>(DiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ItemHolder {
        return ItemHolder(MusicItemBinding.inflate(LayoutInflater.from(parent.context),parent,false),innerClick)
    }

    override fun onBindViewHolder(holder:ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemHolder(private val binding: MusicItemBinding, val innerClick: (data:MusicFile, positions:Int) -> Unit): RecyclerView.ViewHolder(binding.root){
        private var data :MusicFile? = null
        init {
            binding.root.setOnClickListener { data?.let{innerClick(it, position)} }
        }

        fun  bind(d :MusicFile){
            data = d
            binding.tvTitle.text = data!!.title
            CoroutineScope(Dispatchers.IO).launch {
                getImage(data!!.path)?.let {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    withContext(Dispatchers.Main){
                        binding.ivImage.setImageBitmap(bitmap)
                    }

                } ?: run {
                    // If no image is found, explicitly clear or set a default placeholder
                    withContext(Dispatchers.Main){
                        binding.ivImage.setImageResource(R.drawable.ic_launcher_background)
                    }
                }
            }

        }
    }

    private class DiffUtils : DiffUtil.ItemCallback<MusicFile>(){
        override fun areItemsTheSame(oldItem: MusicFile, newItem: MusicFile): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: MusicFile, newItem:MusicFile): Boolean {
            return oldItem == newItem
        }

    }


}