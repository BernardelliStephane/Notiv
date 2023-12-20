package fr.steph.showmemories.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.FragmentProfileBinding
import fr.steph.showmemories.viewmodel.ShowsViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // Binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val showsViewModel: ShowsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        binding.shows = showsViewModel.shows.value
        binding.lifecycleOwner = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}