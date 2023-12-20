package fr.steph.showmemories.form_presentation

import fr.steph.showmemories.viewmodel.AddSeasonViewModel

data class ShowAdditionFormState(
        val imageUrl: String = "",
        val imageErrorRes: Int? = null,
        val name: String = "",
        val nameErrorRes: Int? = null,
        val alternateNames: MutableMap<Int, String> = mutableMapOf(),
        val note: String = "",
        val noteErrorRes: Int? = null,
        val watchDate: Long = 0,
        val synopsis: String = "",
        val review: String = "",
        val seasons: MutableMap<Int, AddSeasonViewModel> = mutableMapOf(),
        val seasonsErrors: MutableMap<Int, Boolean> = mutableMapOf(),
        var isSubmitting: Boolean = false
)