package fr.steph.showmemories.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.steph.showmemories.R
import fr.steph.showmemories.forms_presentation.*
import fr.steph.showmemories.models.SeasonModel
import fr.steph.showmemories.validators.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class AddSeasonViewModel(
    private val validateName: ValidateName = ValidateName(),
    private val validateNote: ValidateNote = ValidateNote(),
    private val validateDuration: ValidateDuration = ValidateDuration(),
    private val validateEpisodeCount: ValidateEpisodeCount = ValidateEpisodeCount()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonAdditionFormState())
    val uiState = _uiState.asStateFlow()

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    fun onEvent(event: SeasonAdditionFormEvent) {
        when(event) {
            is SeasonAdditionFormEvent.NameChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(name = event.name, nameErrorRes = null)
                }
            }
            is SeasonAdditionFormEvent.NoteChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(note = event.note, noteErrorRes = null)
                }
            }
            is SeasonAdditionFormEvent.WatchDateChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(watchDate = event.watchDate)
                }
            }
            is SeasonAdditionFormEvent.SummaryChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(summary = event.summary)
                }
            }
            is SeasonAdditionFormEvent.ReviewChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(review = event.review)
                }
            }
            is SeasonAdditionFormEvent.IsMovieChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(movie = event.isMovie)
                }
            }
            is SeasonAdditionFormEvent.DurationChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(movieDuration = event.duration, durationErrorRes = null)
                }
            }
            is SeasonAdditionFormEvent.EpisodeCountChanged -> {
                return _uiState.update { currentUiState ->
                    currentUiState.copy(episodeCount = event.episodeCount, episodeCountErrorRes = null)
                }
            }
            is SeasonAdditionFormEvent.Submit -> {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isSubmitting = true)
                }
                return submitData()
            }
        }
    }

    private fun submitData() {
        val nameResult = validateName.execute(uiState.value.name)
        val noteResult = validateNote.execute(uiState.value.note)
        val durationResult = if (uiState.value.movie) validateDuration.execute(uiState.value.movieDuration)
            else ValidationResult(successful = true)
        val episodeCountResult = if (uiState.value.movie) ValidationResult(successful = true)
            else validateEpisodeCount.execute(uiState.value.episodeCount)

        _uiState.update { currentUiState ->
            currentUiState.copy(
                nameErrorRes = nameResult.errorMessageRes,
                noteErrorRes = noteResult.errorMessageRes,
                durationErrorRes = durationResult.errorMessageRes,
                episodeCountErrorRes = episodeCountResult.errorMessageRes
            )
        }

        val hasError = listOf(nameResult, noteResult, durationResult, episodeCountResult).any { !it.successful }

        if(hasError) viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isSubmitting = false)
            }
            validationEventChannel.send(ValidationEvent.Failure(R.string.form_incorrect_filling))
        }

        else viewModelScope.launch {
            val season = createSeasonFromUiState()
            validationEventChannel.send(ValidationEvent.Success(season))
        }
    }

    fun buildUiStateFromSeason(season: SeasonModel){
        _uiState.update { currentUiState ->
            currentUiState.copy(
                name = if(season.movie) season.name else season.name.split(" ")[1],
                note = season.note.toString(),
                watchDate = season.watchDate,
                summary = season.summary,
                review = season.review,
                movie = season.movie,
                movieDuration = if(season.movieDuration == 0) "" else season.movieDuration.toString(),
                episodeCount = if(season.episodeCount == 0) "" else season.episodeCount.toString()
            )
        }
    }

    fun createSeasonFromUiState(): SeasonModel{
        val uiStateValue = uiState.value
        return SeasonModel(
            id = UUID.randomUUID().toString(),
            name = if (uiStateValue.movie) uiStateValue.name else "Saison ${uiStateValue.name}",
            watchDate = uiStateValue.watchDate,
            note = uiStateValue.note.toInt(),
            summary = uiStateValue.summary,
            review = uiStateValue.review,
            movie = uiStateValue.movie,
            movieDuration = if(uiStateValue.movieDuration.isEmpty()) 0 else uiStateValue.movieDuration.toInt(),
            episodeCount = if(uiStateValue.episodeCount.isEmpty()) 0 else uiStateValue.episodeCount.toInt()
        )
    }

    sealed class ValidationEvent {
        data class Success (val season: SeasonModel): ValidationEvent()
        data class Failure (val failureMessage: Int): ValidationEvent()
    }
}