package fr.steph.showmemories.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import fr.steph.showmemories.R
import fr.steph.showmemories.databinding.*
import fr.steph.showmemories.forms_presentation.SeasonAdditionFormEvent
import fr.steph.showmemories.forms_presentation.ShowAdditionFormEvent
import fr.steph.showmemories.models.SeasonModel
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.utils.UpdateDatePicker
import fr.steph.showmemories.viewmodels.AddSeasonViewModel
import fr.steph.showmemories.viewmodels.AddShowViewModel
import fr.steph.showmemories.viewmodels.ShowsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddShowFragment : Fragment(R.layout.fragment_add_show) {

    // Current Show
    private var currentShow: ShowModel? = null

    // ViewModels
    private val showsViewModel: ShowsViewModel by activityViewModels()
    private val addShowViewModel: AddShowViewModel by viewModels()

    // Bindings
    private var _binding: FragmentAddShowBinding? = null
    private val binding get() = _binding!!
    private lateinit var seasonBinding: ItemAddSeasonBinding
    private lateinit var alternateNameBinding: ItemAddNameBinding

    // Navigation args
    private val args: AddShowFragmentArgs by navArgs()

    // Indexes
    private var alternateNamesIndex = 0
    private var seasonsIndex = 0

    // DatePicker Updaters
    private lateinit var showUpdateDatePicker: UpdateDatePicker
    private lateinit var seasonUpdateDatePicker: UpdateDatePicker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddShowBinding.bind(view)

        binding.viewModel = addShowViewModel
        binding.lifecycleOwner = this

        currentShow = args.show

        initializeComponents()
        initializeObservers(view)
    }

    @SuppressLint("NewApi", "ClickableViewAccessibility")
    private fun initializeComponents() {
        var date = LocalDate.now()

        currentShow?.let {
            addShowViewModel.buildUiStateFromShow(it)

            date = LocalDate.ofEpochDay(it.watchDate)
            binding.addShowWatchDateInput.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)

            for (name in it.alternateNames) { addNameView(name) }
            for (season in it.seasons) { addSeasonView(season) }
        }

        showUpdateDatePicker = UpdateDatePicker(date.dayOfMonth, date.monthValue - 1, date.year)
        addShowViewModel.onEvent(ShowAdditionFormEvent.WatchDateChanged(LocalDate.of(date.year, date.month, date.dayOfMonth).toEpochDay()))

        binding.apply {
            addShowPreviewImage.setOnClickListener {
                pickupImage()
            }

            addShowNameInput.doAfterTextChanged {
                addShowViewModel.onEvent(ShowAdditionFormEvent.NameChanged(it.toString()))
            }

            addShowNoteInput.doAfterTextChanged {
                addShowViewModel.onEvent(ShowAdditionFormEvent.NoteChanged(it.toString()))
            }

            addShowSynopsisInput.doAfterTextChanged {
                addShowViewModel.onEvent(ShowAdditionFormEvent.SynopsisChanged(it.toString()))
            }

            addShowReviewInput.doAfterTextChanged {
                addShowViewModel.onEvent(ShowAdditionFormEvent.ReviewChanged(it.toString()))
            }

            addShowWatchDateInput.setOnDateChangedListener { it, year, month, day ->
                showUpdateDatePicker(it, day, month, year)
                addShowViewModel.onEvent(ShowAdditionFormEvent.WatchDateChanged(LocalDate.of(it.year, it.month + 1, it.dayOfMonth).toEpochDay()))
            }

            addShowConfirmButton.setOnClickListener {
                addShowScroll.setOnTouchListener { _, _ -> true }
                addShowViewModel.onEvent(ShowAdditionFormEvent.Submit)
            }

            addShowAddSeasonLayout.setOnClickListener {
                addSeasonView(null)
            }

            addShowAddNameLayout.setOnClickListener {
                addNameView("")
            }
        }
    }

    @SuppressLint("NewApi", "SetTextI18n")
    private fun initializeSeasonComponents(season: SeasonModel?, seasonView: View, index: Int, addSeasonViewModel: AddSeasonViewModel) {
        var date = LocalDate.now()

        season?.let {
            addSeasonViewModel.buildUiStateFromSeason(it)
            date = LocalDate.ofEpochDay(it.watchDate)
            seasonBinding.addShowSeasonWatchDateInput.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)
        }

        seasonUpdateDatePicker = UpdateDatePicker(date.dayOfMonth, date.monthValue - 1, date.year)
        addSeasonViewModel.onEvent(SeasonAdditionFormEvent.WatchDateChanged(LocalDate.of(date.year, date.month, date.dayOfMonth).toEpochDay()))

        seasonBinding.apply {
            addShowSeasonTitleInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.NameChanged(it.toString()))
            }

            addShowSeasonNoteInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.NoteChanged(it.toString()))
            }

            addShowSeasonMovieCheckbox.setOnCheckedChangeListener { _, isChecked ->
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.IsMovieChanged(isChecked))
            }

            addShowSeasonSummaryInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.SummaryChanged(it.toString()))
            }

            addShowSeasonReviewInput.doAfterTextChanged {
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.ReviewChanged(it.toString()))
            }

            addShowSeasonDurationInput.doAfterTextChanged {
                if (addSeasonViewModel.uiState.value.movie) addSeasonViewModel.onEvent(
                    SeasonAdditionFormEvent.DurationChanged(it.toString()))
                else addSeasonViewModel.onEvent(SeasonAdditionFormEvent.EpisodeCountChanged(it.toString()))
            }

            addShowSeasonWatchDateInput.setOnDateChangedListener { it, year, month, day ->
                seasonUpdateDatePicker(it, day, month, year)
                addSeasonViewModel.onEvent(SeasonAdditionFormEvent.WatchDateChanged(LocalDate.of(it.year, it.month + 1, it.dayOfMonth).toEpochDay()))
            }

            addShowSeasonLayout.setOnHeadingImageClickListener {
                binding.addShowSeasonsList.removeView(seasonView)
                addShowViewModel.onEvent(ShowAdditionFormEvent.SeasonRemoved(index))
            }
        }
    }

    private fun addSeasonView(season: SeasonModel?) {
        val seasonView = LayoutInflater.from(context).inflate(R.layout.item_add_season, null, false)
        seasonBinding = DataBindingUtil.bind(seasonView)!!
        val addSeasonViewModel = AddSeasonViewModel()

        val index = seasonsIndex++

        seasonBinding.viewModel = addSeasonViewModel
        seasonBinding.isNew = season == null
        seasonBinding.lifecycleOwner = this

        initializeSeasonComponents(season, seasonView, index, addSeasonViewModel)

        binding.addShowSeasonsList.addView(seasonView)
        addShowViewModel.onEvent(ShowAdditionFormEvent.SeasonAdded(index, addSeasonViewModel))
    }

    @SuppressLint("InflateParams")
    private fun addNameView(name: String) {
        val alternateNameView = LayoutInflater.from(context).inflate(R.layout.item_add_name, null, false)
        val index = alternateNamesIndex++

        alternateNameBinding = DataBindingUtil.bind(alternateNameView)!!

        alternateNameBinding.viewModel = addShowViewModel
        alternateNameBinding.index = index
        alternateNameBinding.lifecycleOwner = this

        alternateNameBinding.alternateNameInput.doAfterTextChanged {
            addShowViewModel.onEvent(ShowAdditionFormEvent.AlternateNameChanged(index, it.toString()))
        }

        alternateNameBinding.alternateNameLayout.setEndIconOnClickListener {
            binding.addShowNamesList.removeView(alternateNameView)
            addShowViewModel.onEvent(ShowAdditionFormEvent.AlternateNameRemoved(index))
        }

        binding.addShowNamesList.addView(alternateNameView)
        addShowViewModel.onEvent(ShowAdditionFormEvent.AlternateNameAdded(index, name))
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
            addShowViewModel.validationEvents.collectLatest { event ->
                when (event) {
                    is AddShowViewModel.ValidationEvent.Success -> {
                        if(currentShow == null) showsViewModel.addShow(event.show)
                        else event.show.copy(id = currentShow!!.id).apply {
                            showsViewModel.updateShow(this, currentShow!!)
                        }
                    }
                    is AddShowViewModel.ValidationEvent.Failure -> {
                        binding.addShowScroll.setOnTouchListener { _, _ -> false }
                        binding.addShowScroll.fullScroll(ScrollView.FOCUS_UP)
                        Snackbar.make(view, event.failureMessage, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Image selectors
    private val imageSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data == null || result.data?.data == null) return@registerForActivityResult
            addShowViewModel.onEvent(ShowAdditionFormEvent.ImageChanged(result.data!!.data.toString()))
        }
    }

    private val newImageSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            addShowViewModel.onEvent(ShowAdditionFormEvent.ImageChanged(it.toString()))
        }
    }

    private fun pickupImage() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable())
            newImageSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        else Intent().apply {
            type = "image/"
            action = Intent.ACTION_PICK
            imageSelector.launch(Intent.createChooser(this, "Select Picture"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}