package fr.steph.showmemories.adapters

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
import fr.steph.showmemories.models.ShowModel

class ShowAdapter(private val context: Context) : ListAdapter<ShowModel, ShowAdapter.ViewHolder>(ShowDiffUtil()) {
    var itemClickedListener: ((ShowModel, ImageView) -> Unit)? = null
    var itemEditedListener: ((ShowModel) -> Unit)? = null
    var itemDeletedListener: ((ShowModel) -> Unit)? = null

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentShow = getItem(position)
        holder.bind(currentShow)

        holder.itemView.setOnClickListener{ itemClickedListener?.invoke(currentShow, holder.binding.showImage) }

        holder.binding.menuIcon.setOnClickListener{ menuIcon ->
            val popupMenu = PopupMenu(context, menuIcon)
            popupMenu.menuInflater.inflate(R.menu.show_management_menu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.edit -> {
                        itemEditedListener?.invoke(currentShow)
                        return@setOnMenuItemClickListener true
                    }
                    R.id.delete -> {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle(context.getString(R.string.show_suppression_confirmation))
                        builder.setCancelable(true)
                        builder.setPositiveButton(context.getString(R.string.confirm_button)) { _, _ -> itemDeletedListener?.invoke(currentShow)}
                        builder.setNegativeButton(context.getString(R.string.cancel_button)) { _, _ -> }
                        builder.show()
                        return@setOnMenuItemClickListener true
                    }
                    else -> false
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