package fr.steph.showmemories.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.steph.showmemories.*
import fr.steph.showmemories.databinding.ItemShowBinding
import fr.steph.showmemories.model.ShowModel

class ShowAdapter(private val context: Context) : ListAdapter<ShowModel, ShowAdapter.ViewHolder>(ShowDiffUtil()) {
    var itemClickedCallback: ((ShowModel, ImageView) -> Unit) = { _, _ -> }
    var itemEditedCallback: ((ShowModel) -> Unit) = {}
    var itemDeletedCallback: ((ShowModel) -> Unit) = {}

    class ViewHolder (val binding : ItemShowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(show: ShowModel) {
            binding.show = show
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ItemShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentShow = getItem(position)
        holder.apply {
            bind(currentShow)
            itemView.setOnClickListener{ itemClickedCallback(currentShow, binding.showImage) }

            binding.menuIcon.setOnClickListener{ menuIcon ->
                PopupMenu(context, menuIcon).apply {
                    menuInflater.inflate(R.menu.show_management_menu, menu)
                    show()
                    setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.edit -> {
                                itemEditedCallback(currentShow)
                                return@setOnMenuItemClickListener true
                            }
                            R.id.delete -> {
                                AlertDialog.Builder(context).apply {
                                    setTitle(context.getString(R.string.show_suppression_confirmation))
                                    setCancelable(true)
                                    setPositiveButton(context.getString(R.string.confirm_button)) { _, _ -> itemDeletedCallback(currentShow)}
                                    setNegativeButton(context.getString(R.string.cancel_button)) { _, _ -> }
                                    show()
                                }
                                return@setOnMenuItemClickListener true
                            }
                            else -> false
                        }
                    }
                }
            }
        }
    }

    class ShowDiffUtil: DiffUtil.ItemCallback<ShowModel>(){
        override fun areItemsTheSame(oldItem: ShowModel, newItem: ShowModel): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(oldItem: ShowModel, newItem: ShowModel): Boolean {
            return newItem == oldItem
        }
    }
}