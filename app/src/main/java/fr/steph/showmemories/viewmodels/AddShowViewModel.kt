package fr.steph.showmemories.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.steph.showmemories.R
import fr.steph.showmemories.forms_presentation.SeasonAdditionFormEvent
import fr.steph.showmemories.forms_presentation.ShowAdditionFormEvent
import fr.steph.showmemories.forms_presentation.ShowAdditionFormState
import fr.steph.showmemories.models.SeasonModel
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.validators.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class AddShowViewModel(
    private val validateImage: ValidateImage = ValidateImage(),
    private val validateName: ValidateName = ValidateName(),
    private val validateNote: ValidateNote = ValidateNote()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowAdditionFormState())
    val uiState = _uiState.asStateFlow()

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    fun onEvent(event: ShowAdditionFormEvent) {
        when(event) {
            is ShowAdditionFormEvent.ImageChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(imageUrl = event.imageUrl, imageErrorRes = null)
                }
            }
            is ShowAdditionFormEvent.NameChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(name = event.name, nameErrorRes = null)
                }
            }
            is ShowAdditionFormEvent.NoteChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(note = event.note, noteErrorRes = null)
                }
            }
            is ShowAdditionFormEvent.WatchDateChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(watchDate = event.watchDate)
                }
            }
            is ShowAdditionFormEvent.SynopsisChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(synopsis = event.synopsis)
                }
            }
            is ShowAdditionFormEvent.ReviewChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(review = event.review)
                }
            }
            is ShowAdditionFormEvent.AlternateNameAdded -> {
                return _uiState.update { currentUiState ->
                    val names = currentUiState.alternateNames
                    names[event.index] = event.alternateName
                    currentUiState.copy(alternateNames = names)
                }
            }
            is ShowAdditionFormEvent.AlternateNameChanged -> {
                return _uiState.update { currentUiState ->
                    val names = currentUiState.alternateNames
                    names[event.index] = event.alternateName
                    currentUiState.copy(alternateNames = names)
                }
            }
            is ShowAdditionFormEvent.AlternateNameRemoved -> {
                return _uiState.update { currentUiState ->
                    val names = currentUiState.alternateNames
                    names.remove(event.index)
                    currentUiState.copy(alternateNames = names)
                }
            }
            is ShowAdditionFormEvent.SeasonAdded -> {
                return _uiState.update { currentUiState ->
                    val seasons = currentUiState.seasons
                    seasons[event.index] = event.addSeasonViewModel
                    currentUiState.copy(seasons = seasons)
                }
            }
            is ShowAdditionFormEvent.SeasonRemoved -> {
                return _uiState.update { currentUiState ->
                    val seasons = currentUiState.seasons
                    seasons.remove(event.index)
                    currentUiState.copy(seasons = seasons)
                }
            }
            is ShowAdditionFormEvent.Submit -> {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSubmitting = true)
                }
                uiState.value.seasons.forEach { (_, addSeasonViewModel) ->
                    addSeasonViewModel.onEvent(SeasonAdditionFormEvent.Submit)
                }
                return submitData()
            }
        }
    }

    private fun submitData() {
        var hasError = false

        val imageResult = validateImage.execute(uiState.value.imageUrl)
        val nameResult = validateName.execute(uiState.value.name)
        val noteResult = validateNote.execute(uiState.value.note)
        val seasonsResult = mutableMapOf<Int, Boolean>()

        uiState.value.seasons.forEach { (index, addSeasonViewModel) ->
            val seasonValue = addSeasonViewModel.uiState.value
            val seasonHasError = listOf(seasonValue.nameErrorRes, seasonValue.noteErrorRes, seasonValue.durationErrorRes, seasonValue.episodeCountErrorRes).any { it != null }
            if(seasonHasError) hasError = true
            seasonsResult[index] = seasonHasError
        }

        _uiState.update { currentUiState ->
            currentUiState.copy(
                imageErrorRes = imageResult.errorMessageRes,
                nameErrorRes = nameResult.errorMessageRes,
                noteErrorRes = noteResult.errorMessageRes,
                seasonsErrors = seasonsResult
            )
        }

        if(!hasError) hasError = listOf(imageResult, nameResult, noteResult).any { !it.successful }


        if(hasError) viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isSubmitting = false)
            }
            validationEventChannel.send(ValidationEvent.Failure(R.string.form_incorrect_filling))
        }

        else viewModelScope.launch {
            val show = createShowFromUiState()
            validationEventChannel.send(ValidationEvent.Success(show))
        }
    }

    fun buildUiStateFromShow(show: ShowModel){
        viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    imageUrl = show.imageUrl,
                    name = show.name,
                    note = show.note.toString(),
                    watchDate = show.watchDate,
                    synopsis = show.synopsis,
                    review = show.review
                )
            }
        }
    }

    private fun createShowFromUiState(): ShowModel {
        val seasons = mutableMapOf<Int, SeasonModel>()
        val uiStateValue = uiState.value
        val alts = uiStateValue.alternateNames.values.filter { it.isNotEmpty() }

        uiStateValue.seasons.forEach { (i, addSeasonViewModel) ->
            seasons[i] = addSeasonViewModel.createSeasonFromUiState()
        }

        return ShowModel(
            id = UUID.randomUUID().toString(),
            imageUrl = uiStateValue.imageUrl,
            name = uiStateValue.name,
            watchDate = uiStateValue.watchDate,
            note = uiStateValue.note.toInt(),
            synopsis = uiStateValue.synopsis,
            review = uiStateValue.review,
            alternateNames = ArrayList(alts),
            seasons = ArrayList(seasons.values)
        )
    }

    sealed class ValidationEvent {
        data class Success(val show: ShowModel): ValidationEvent()
        data class Failure(val failureMessage: Int): ValidationEvent()
    }
}