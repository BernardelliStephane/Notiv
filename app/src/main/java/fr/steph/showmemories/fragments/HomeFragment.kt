package fr.steph.showmemories.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import fr.steph.showmemories.*
import fr.steph.showmemories.adapters.ShowAdapter
import fr.steph.showmemories.databinding.FragmentHomeBinding
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.viewmodels.ShowsViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Adapter
    private lateinit var showsAdapter: ShowAdapter

    // Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val showsViewModel: ShowsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        initializeComponents(view)
        initializeObservers()
    }

    private fun initializeComponents(view: View) {
        showsAdapter = ShowAdapter(requireContext()).apply {
            itemDeletedListener = {
                showsViewModel.deleteShow(it)
            }

            itemEditedListener = {
                val action = HomeFragmentDirections.actionNavigationHomeToAddShowFragment(it)
                findNavController(view).navigate(action)
            }

           itemClickedListener = { show, image ->
               val extras = FragmentNavigatorExtras(image to getString(R.string.show_details_image_transition))
               val action = HomeFragmentDirections.actionNavigationHomeToDetailsFragment(show)
               findNavController(view).navigate(action, extras)
            }
        }

        binding.homeRecyclerView.apply {
            adapter = showsAdapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        binding.homeButtonAddShow.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToDiscoverFragment()
            findNavController(view).navigate(action)
        }
    }

    private fun initializeObservers() {
        showsViewModel.shows.observe(viewLifecycleOwner) {
            it?.let {
                val showList: ArrayList<ShowModel> = arrayListOf()
                showList.addAll(it)
                showsAdapter.submitList(showList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}