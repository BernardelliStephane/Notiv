package fr.steph.showmemories.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.FragmentDiscoverDetailsBinding
import fr.steph.showmemories.viewmodels.DiscoverViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiscoverDetailsFragment : Fragment(R.layout.fragment_discover_details) {

    // ViewModel
    private val discoverViewModel: DiscoverViewModel by activityViewModels()

    // Binding
    private var _binding: FragmentDiscoverDetailsBinding? = null
    private val binding get() = _binding!!

    // Navigation args
    private val args: DiscoverDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDiscoverDetailsBinding.bind(view)

        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).setDuration(300)
        sharedElementEnterTransition = animation

        val showId = args.showId
        discoverViewModel.getShowInfo(showId)

        initializeComponents(view)
        initializeObservers()
    }

    private fun initializeComponents(view: View) {
        binding.apply {
            detailsToolbar.setNavigationOnClickListener {
                Navigation.findNavController(view).navigateUp()
            }
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            discoverViewModel.tmdbTv.collectLatest {
                when(it) {
                    is DiscoverViewModel.Resource.Error -> {
                        // Handle error
                    }
                    is DiscoverViewModel.Resource.Success -> {
                        binding.detailsToolbar.title = it.data.name
                        Glide.with(this@DiscoverDetailsFragment)
                            .load(this@DiscoverDetailsFragment.getString(R.string.tmdb_image_path, it.data.backdrop_path))
                            .placeholder(R.drawable.default_image)
                            .into(binding.detailsShowImage)

                        // Populate views
                    }
                    else -> {}
                }
                binding.discoverDetailsProgressBar.isVisible = it is DiscoverViewModel.Resource.Loading
            }
        }
        /*discoverViewModel.tmdbTv.observe(viewLifecycleOwner) {
            when(it) {
                is DiscoverViewModel.Resource.Error -> {
                    // Handle error
                }
                is DiscoverViewModel.Resource.Success -> {
                    binding.detailsToolbar.title = it.data.name
                    Glide.with(this)
                        .load(this.getString(R.string.tmdb_image_path, it.data.backdrop_path))
                        .placeholder(R.drawable.default_image)
                        .into(binding.detailsShowImage)

                    // Populate views
                }
                else -> {}
            }
            binding.discoverDetailsProgressBar.isVisible = it is DiscoverViewModel.Resource.Loading
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}