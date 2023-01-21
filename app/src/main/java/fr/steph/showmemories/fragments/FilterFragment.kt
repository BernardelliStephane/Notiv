package fr.steph.showmemories.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.FragmentFilterBinding
import fr.steph.showmemories.forms_presentation.FilterFormEvent
import fr.steph.showmemories.viewmodels.FilterViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FilterFragment: Fragment(R.layout.fragment_filter) {

    // Binding
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilterBinding.bind(view)

        binding.viewModel = filterViewModel
        binding.lifecycleOwner = this

        initializeComponents()
    }

    private fun initializeComponents() {
        var job: Job? = null
        binding.apply {
            filterSearchView.setQuery(filterViewModel.query.value, false)

            iconAlphabeticalSort.setOnClickListener {
                filterViewModel.onEvent(FilterFormEvent.AlphabeticalIconClicked)
            }

            iconWatchDateSort.setOnClickListener {
                filterViewModel.onEvent(FilterFormEvent.WatchDateIconClicked)
            }

            iconNoteSort.setOnClickListener {
                filterViewModel.onEvent(FilterFormEvent.NoteIconClicked)
            }

            filterSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean { return false }

                override fun onQueryTextChange(newText: String): Boolean {
                    job?.cancel()
                    job = MainScope().launch {
                        delay(300L)
                        filterViewModel.onEvent(FilterFormEvent.QueryChangedEvent(newText))
                    }
                    return false
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}