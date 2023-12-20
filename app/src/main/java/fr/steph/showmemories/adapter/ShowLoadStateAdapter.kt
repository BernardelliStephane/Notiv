package fr.steph.showmemories.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.ItemShowLoadStateBinding
import fr.steph.showmemories.utils.ConnectivityChecker

class ShowLoadStateAdapter(private val context: Context, private val retry: () -> Unit) :
    LoadStateAdapter<ShowLoadStateAdapter.ShowLoadStateViewHolder>() {

    class ShowLoadStateViewHolder(
        private val binding: ItemShowLoadStateBinding,
        private val context: Context,
        private val retry: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val connectivityChecker = ConnectivityChecker(context)

        init {
            binding.btnRetry.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                if(loadState is LoadState.Error) errorDescription.text = context.getString(
                    if (connectivityChecker.invoke()) R.string.show_fetching_failure
                    else R.string.no_internet_connexion
                )
                progressLoadMore.isVisible = loadState is LoadState.Loading
                btnRetry.isVisible = loadState is LoadState.Error
                errorDescription.isVisible = loadState is LoadState.Error
            }
        }

        companion object {
            fun from(parent: ViewGroup, context: Context, retry: () -> Unit): ShowLoadStateViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_show_load_state, parent, false)
                val binding = ItemShowLoadStateBinding.bind(view)
                return ShowLoadStateViewHolder(binding, context, retry)
            }
        }
    }

    override fun onBindViewHolder(holder: ShowLoadStateViewHolder, loadState: LoadState) =
        holder.bind(loadState)

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ShowLoadStateViewHolder =
        ShowLoadStateViewHolder.from(parent, context, retry)
}