package fr.steph.showmemories.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.FragmentAddSeasonBinding
import fr.steph.showmemories.form_presentation.SeasonAdditionFormEvent
import fr.steph.showmemories.model.SeasonModel
import fr.steph.showmemories.model.ShowModel
import fr.steph.showmemories.utils.UpdateDatePicker
import fr.steph.showmemories.viewmodel.AddSeasonViewModel
import fr.steph.showmemories.viewmodel.ShowsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddSeasonFragment : Fragment(R.layout.fragment_add_season) {

    // Current season/show
    private lateinit var currentShow: ShowModel
    private var currentSeason: SeasonModel? = null

    // View Models
    private val showsViewModel: ShowsViewModel by activityViewModels()
    private val addSeasonViewModel: AddSeasonViewModel by viewModels()

    // Binding
    private var _binding: FragmentAddSeasonBinding? = null
    private val binding get() = _binding!!

    // Navigation args
    private val args: AddSeasonFragmentArgs by navArgs()

    // DatePicker Updater
    private lateinit var updateDatePicker: UpdateDatePicker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddSeasonBinding.bind(view)

        binding.viewModel = addSeasonViewModel
        binding.lifecycleOwner = this

        currentShow = args.show
        currentSeason = args.season

        initializeComponents(view)
        initializeObservers(view)
    }

    @SuppressLint("NewApi", "SetTextI18n", "ClickableViewAccessibility")
    private fun initializeComponents(view: View) {
        var date = LocalDate.now()

        currentSeason?.let {
            addSeasonViewModel.buildUiStateFromSeason(currentSeason!!)
            date = LocalDate.ofEpochDay(currentSeason!!.watchDate)
            binding.popupSeasonWatchDateInput.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)
        }

        updateDatePicker = UpdateDatePicker(date.dayOfMonth, date.monthValue - 1, date.year)
        addSeasonViewModel.onEvent(SeasonAdditionFormEvent.WatchDateChanged(LocalDate.of(date.year, date.month, date.dayOfMonth).toEpochDay()))

        binding.apply {
            popupSeasonTitleInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.NameChanged(it.toString()))
            }

            popupSeasonNoteInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.NoteChanged(it.toString()))
            }

            popupMovieCheckbox.setOnCheckedChangeListener { _, isChecked ->
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.IsMovieChanged(isChecked))
            }

            popupSeasonSummaryInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.SummaryChanged(it.toString()))
            }

            popupSeasonReviewInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.ReviewChanged(it.toString()))
            }

            popupSeasonDurationInput.doAfterTextChanged {
                if (addSeasonViewModel.uiState.value.movie) addSeasonViewModel.onEvent(
                    SeasonAdditionFormEvent.DurationChanged(it.toString()))
                else addSeasonViewModel.onEvent(SeasonAdditionFormEvent.EpisodeCountChanged(it.toString()))
            }

            popupSeasonWatchDateInput.setOnDateChangedListener { it, year, month, day ->
                updateDatePicker(it, day, month, year)
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.WatchDateChanged(LocalDate.of(it.year, it.month + 1, it.dayOfMonth).toEpochDay()))
            }

            popupConfirmButton.setOnClickListener {
                binding.popupSeasonScrollView.setOnTouchListener { _, _ -> true }
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.Submit)
            }

            popupCloseIcon.setOnClickListener {
                Navigation.findNavController(view).navigateUp()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeObservers(view: View) {
        showsViewModel.completed.observe(viewLifecycleOwner) {
            if(it) {
                showsViewModel.resetCompletedValue()
                Navigation.findNavController(view).navigateUp()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            addSeasonViewModel.validationEvents.collectLatest { event ->
                when (event) {
                    is AddSeasonViewModel.ValidationEvent.Success -> {
                        if(currentSeason == null) showsViewModel.addSeason(currentShow, event.season)
                        else
                            event.season.copy(id = currentSeason!!.id).apply {
                                showsViewModel.updateSeason(currentShow, this)
                            }
                    }
                    is AddSeasonViewModel.ValidationEvent.Failure -> {
                        binding.popupSeasonScrollView.setOnTouchListener { _, _ -> false }
                        binding.popupSeasonScrollView.fullScroll(ScrollView.FOCUS_UP)
                        Snackbar.make(view, event.failureMessage, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}