package fr.steph.showmemories.form_presentation

data class SeasonAdditionFormState(
    var name: String = "",
    var nameErrorRes: Int? = null,
    var note: String = "",
    var noteErrorRes: Int? = null,
    var watchDate: Long = 0,
    var summary: String = "",
    var review: String = "",
    var movie: Boolean = false,
    var movieDuration: String = "",
    var durationErrorRes: Int? = null,
    var episodeCount: String = "",
    var episodeCountErrorRes: Int? = null,
    var isSubmitting: Boolean = false
)