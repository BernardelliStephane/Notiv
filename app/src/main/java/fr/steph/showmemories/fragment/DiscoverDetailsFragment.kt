package fr.steph.showmemories.fragment

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.FragmentDiscoverDetailsBinding
import fr.steph.showmemories.viewmodel.DiscoverViewModel

class DiscoverDetailsFragment : Fragment(R.layout.fragment_discover_details) {

    // ViewModel
    private val discoverViewModel: DiscoverViewModel by activityViewModels()

    // Binding
    private var _binding: FragmentDiscoverDetailsBinding? = null
    private val binding get() = _binding!!

    // Show ID
    private var showId: Int = 0

    // Navigation args
    private val args: DiscoverDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDiscoverDetailsBinding.bind(view)

        discoverViewModel.resetShowValue()

        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).setDuration(300)
        sharedElementEnterTransition = animation

        showId = args.showId
        discoverViewModel.getShowInfo(showId)

        initializeComponents(view)
        initializeObservers(view)
    }

    private fun initializeComponents(view: View) {
        binding.apply {
            detailsToolbar.setNavigationOnClickListener {
                Navigation.findNavController(view).navigateUp()
            }
        }
    }

    private fun initializeObservers(view: View) {
            discoverViewModel.tmdbTv.observe(viewLifecycleOwner) {
                binding.discoverDetailsProgressBar.isVisible = it is DiscoverViewModel.Resource.Loading
                when(it) {
                    is DiscoverViewModel.Resource.Error -> {
                        Snackbar.make(view, it.message, BaseTransientBottomBar.LENGTH_INDEFINITE).setAction(R.string.retry) {
                            discoverViewModel.getShowInfo(showId)
                        }.show()
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
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}