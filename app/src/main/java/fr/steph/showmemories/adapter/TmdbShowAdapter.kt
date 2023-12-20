package fr.steph.showmemories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.steph.showmemories.databinding.ItemTmdbShowBinding
import fr.steph.showmemories.model.tmdbmodels.TmdbTv

class TmdbShowAdapter : PagingDataAdapter<TmdbTv, TmdbShowAdapter.ViewHolder>(TmdbShowDiffUtil()) {
    var itemClickedCallback: ((TmdbTv, ImageView) -> Unit) = { _, _ -> }

    class ViewHolder (val binding : ItemTmdbShowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(show: TmdbTv) {
            binding.show = show
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemTmdbShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentShow = getItem(position)
        currentShow?.let { show ->
            holder.bind(show)
            holder.itemView.setOnClickListener{ itemClickedCallback(show, holder.binding.tmdbShowImage) }
        }
    }

    class TmdbShowDiffUtil: DiffUtil.ItemCallback<TmdbTv>(){
        override fun areItemsTheSame(oldItem: TmdbTv, newItem: TmdbTv): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(oldItem: TmdbTv, newItem: TmdbTv): Boolean {
            return newItem == oldItem
        }
    }
}