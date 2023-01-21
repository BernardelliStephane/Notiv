package fr.steph.showmemories.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import fr.steph.showmemories.R
import fr.steph.showmemories.adapters.SeasonAdapter
import fr.steph.showmemories.databinding.FragmentDetailsBinding
import fr.steph.showmemories.models.SeasonModel
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.viewmodels.ShowsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetailsFragment : Fragment(R.layout.fragment_details) {

    // Adapter
    private lateinit var seasonAdapter: SeasonAdapter

    // Current show
    private lateinit var currentShow: ShowModel

    // Binding
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val showsViewModel: ShowsViewModel by activityViewModels()

    // Navigation args
    private val args: DetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)

        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).setDuration(300)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        currentShow = args.show

        initializeComponents(view)
        initializeObservers()
    }

    @SuppressLint("NewApi")
    private fun initializeComponents(view: View) {

        seasonAdapter = SeasonAdapter(requireContext()).apply {
            itemDeletedListener = { season ->
                showsViewModel.deleteSeason(currentShow, season)
            }

            itemEditedListener = { season ->
                val action = DetailsFragmentDirections.actionDetailsFragmentToAddSeasonFragment(currentShow, season)
                Navigation.findNavController(view).navigate(action)
            }
        }

        binding.apply {
            seasonsRecyclerView.adapter = seasonAdapter

            detailsToolbar.title = currentShow.name
            detailsShowNote.text = this@DetailsFragment.getString(R.string.note, currentShow.note)
            detailsShowDate.text = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.ofEpochDay(currentShow.watchDate))
            if(currentShow.synopsis.isNotBlank()) detailsShowSynopsis.text = currentShow.synopsis
            if(currentShow.review.isNotBlank()) detailsShowReview.text = currentShow.review

            Glide.with(this@DetailsFragment)
                .load(currentShow.imageUrl)
                .placeholder(R.drawable.default_image)
                .apply(RequestOptions().dontTransform())
                .into(detailsShowImage)

            detailsToolbar.setNavigationOnClickListener {
                Navigation.findNavController(view).navigateUp()
            }

            addSeasonButton.setOnClickListener {
                val action = DetailsFragmentDirections.actionDetailsFragmentToAddSeasonFragment(currentShow, null)
                Navigation.findNavController(view).navigate(action)
            }
        }
    }

    private fun initializeObservers() {
        showsViewModel.shows.observe(viewLifecycleOwner) { shows ->
            val seasonList: ArrayList<SeasonModel> = arrayListOf()
            currentShow = shows.single { it.id == currentShow.id }
            seasonList.addAll(currentShow.seasons)
            seasonAdapter.submitList(seasonList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}