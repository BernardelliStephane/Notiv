package fr.steph.showmemories.forms_presentation

sealed class SeasonAdditionFormEvent {
    data class NameChanged(val name: String) : SeasonAdditionFormEvent()
    data class NoteChanged(val note: String) : SeasonAdditionFormEvent()
    data class WatchDateChanged(val watchDate: Long) : SeasonAdditionFormEvent()
    data class SummaryChanged(val summary: String) : SeasonAdditionFormEvent()
    data class ReviewChanged(val review: String) : SeasonAdditionFormEvent()
    data class IsMovieChanged(val isMovie: Boolean) : SeasonAdditionFormEvent()
    data class DurationChanged(val duration: String) : SeasonAdditionFormEvent()
    data class EpisodeCountChanged(val episodeCount: String) : SeasonAdditionFormEvent()

    object Submit: SeasonAdditionFormEvent()
}