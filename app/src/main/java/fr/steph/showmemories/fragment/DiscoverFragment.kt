package fr.steph.showmemories.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.LoadState
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import fr.steph.showmemories.R
import fr.steph.showmemories.adapter.ShowLoadStateAdapter
import fr.steph.showmemories.adapter.TmdbShowAdapter
import fr.steph.showmemories.databinding.FragmentDiscoverBinding
import fr.steph.showmemories.utils.ConnectivityChecker
import fr.steph.showmemories.utils.extension.safeNavigate
import fr.steph.showmemories.viewmodel.DiscoverViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment(R.layout.fragment_discover) {

    // Adapter
    private lateinit var tmdbShowsAdapter: TmdbShowAdapter

    // ViewModel
    private val discoverViewModel: DiscoverViewModel by activityViewModels()

    // Binding
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    // Connectivity Checker
    private lateinit var connectivityChecker: ConnectivityChecker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDiscoverBinding.bind(view)

        initializeComponents(view)
        initializeObservers(view)
    }

    private fun initializeComponents(view: View) {
        val currentLanguage = this.resources.configuration.locales[0]
        discoverViewModel.setLanguage(currentLanguage)

        connectivityChecker = ConnectivityChecker(requireContext())

        discoverViewModel.setConnectivityChecker(connectivityChecker)

        tmdbShowsAdapter = TmdbShowAdapter().apply {
            itemClickedListener = { show, image ->
                val extras = FragmentNavigatorExtras(image to getString(R.string.tmdbtv_details_image_transition))
                val action = DiscoverFragmentDirections.actionDiscoverFragmentToDiscoverDetailsFragment(show.id)
                safeNavigate(action, extras)
            }
        }

        binding.apply {
            viewModel = discoverViewModel
            lifecycleOwner = this@DiscoverFragment

            discoverRecyclerView.apply {
                itemAnimator = null
                postponeEnterTransition()
                adapter = tmdbShowsAdapter.withLoadStateHeaderAndFooter(
                    header = ShowLoadStateAdapter(requireContext()) { tmdbShowsAdapter.retry() },
                    footer = ShowLoadStateAdapter(requireContext()) { tmdbShowsAdapter.retry() }
                )
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
            }

            var job: Job? = null
            discoverSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean { return false }

                override fun onQueryTextChange(newText: String): Boolean {
                    job?.cancel()
                    job = MainScope().launch {
                        delay(500L)
                        if(newText != discoverViewModel.currentQuery.value) binding.discoverRecyclerView.scrollToPosition(0)
                        discoverViewModel.queryChanged(newText)
                    }
                    return false
                }
            })
        }
    }

    private fun initializeObservers(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            discoverViewModel.shows.collectLatest {
                tmdbShowsAdapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            tmdbShowsAdapter.loadStateFlow.collectLatest {
                binding.apply {
                    discoverRecyclerView.isVisible = it.refresh is LoadState.NotLoading && tmdbShowsAdapter.itemCount != 0
                    showsLoadingProgressBar.isVisible = it.refresh is LoadState.Loading
                    textNoResult.isVisible = it.refresh is LoadState.NotLoading && tmdbShowsAdapter.itemCount == 0
                }

                if (it.refresh is LoadState.Error) {
                    val message = if (connectivityChecker.invoke()) R.string.show_fetching_failure
                        else R.string.no_internet_connexion

                    Snackbar.make(view, message, LENGTH_INDEFINITE)
                        .setAction(R.string.retry) { tmdbShowsAdapter.retry() }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}