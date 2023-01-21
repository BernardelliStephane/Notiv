package fr.steph.showmemories.forms_presentation

sealed class FilterFormEvent {
    object AlphabeticalIconClicked: FilterFormEvent()
    object WatchDateIconClicked: FilterFormEvent()
    object NoteIconClicked: FilterFormEvent()
    data class QueryChangedEvent(val query: String) : FilterFormEvent()
}