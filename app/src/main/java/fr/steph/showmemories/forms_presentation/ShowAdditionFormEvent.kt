package fr.steph.showmemories.forms_presentation

import fr.steph.showmemories.viewmodels.AddSeasonViewModel

sealed class ShowAdditionFormEvent {
    data class ImageChanged(val imageUrl: String) : ShowAdditionFormEvent()
    data class NameChanged(val name: String) : ShowAdditionFormEvent()
    data class AlternateNameAdded(val index: Int, val alternateName: String) : ShowAdditionFormEvent()
    data class AlternateNameChanged(val index: Int, val alternateName: String) : ShowAdditionFormEvent()
    data class AlternateNameRemoved(val index: Int) : ShowAdditionFormEvent()
    data class NoteChanged(val note: String) : ShowAdditionFormEvent()
    data class WatchDateChanged(val watchDate: Long) : ShowAdditionFormEvent()
    data class SynopsisChanged(val synopsis: String) : ShowAdditionFormEvent()
    data class ReviewChanged(val review: String) : ShowAdditionFormEvent()
    data class SeasonAdded(val index: Int, val addSeasonViewModel: AddSeasonViewModel) : ShowAdditionFormEvent()
    data class SeasonRemoved(val index: Int) : ShowAdditionFormEvent()

    object Submit: ShowAdditionFormEvent()
}