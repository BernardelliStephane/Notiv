package fr.steph.showmemories.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.steph.showmemories.*
import fr.steph.showmemories.adapter.ShowAdapter
import fr.steph.showmemories.databinding.FragmentCollectionBinding
import fr.steph.showmemories.model.ShowModel
import fr.steph.showmemories.utils.extension.safeNavigate
import fr.steph.showmemories.viewmodel.FilterViewModel
import fr.steph.showmemories.viewmodel.ShowsViewModel

class CollectionFragment : Fragment(R.layout.fragment_collection) {

    // Adapter
    private lateinit var showsAdapter: ShowAdapter

    // Binding
    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    // ViewModels
    private val showsViewModel: ShowsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCollectionBinding.bind(view)

        binding.viewModel = filterViewModel
        binding.lifecycleOwner = this

        initializeComponents(view)
        initializeObservers()
    }

    private fun initializeComponents(view: View) {
        showsAdapter = ShowAdapter(requireContext())

        binding.collectionRecyclerView.apply {
            adapter = showsAdapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        showsAdapter.itemDeletedCallback = {
            showsViewModel.deleteShow(it)
        }

        showsAdapter.itemEditedCallback = {
            val action = CollectionFragmentDirections.actionNavigationCollectionToAddShowFragment(it)
            safeNavigate(action)
        }

        showsAdapter.itemClickedCallback = { show, image ->
            val extras = FragmentNavigatorExtras(image to getString(R.string.show_details_image_transition))
            val action = CollectionFragmentDirections.actionNavigationCollectionToDetailsFragment(show)
            safeNavigate(action, extras)
        }

        // Hide Navigation Bar on Soft Keyboard Presence
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val navBar: BottomNavigationView = activity?.findViewById(R.id.navigation_bar_view)!!
            navBar.isVisible = !insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
    }

    private fun initializeObservers() {
        var isShowsUpdate = false
        showsViewModel.shows.observe(viewLifecycleOwner) {
            isShowsUpdate = true
            filterViewModel.setList(it)
        }

        filterViewModel.displayList.observe(viewLifecycleOwner) {
            val showList: ArrayList<ShowModel> = arrayListOf()
            showList.addAll(it)
            showsAdapter.submitList(showList) {
                if(isShowsUpdate) isShowsUpdate = false
                else binding.collectionRecyclerView.scrollToPosition(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}