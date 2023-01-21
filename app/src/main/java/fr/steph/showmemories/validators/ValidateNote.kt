package fr.steph.showmemories.validators

import fr.steph.showmemories.R

class ValidateNote {

    fun execute(note: String): ValidationResult {
        if(note.isEmpty())
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.mandatory_field_error
            )

        val noteAsString = note.toInt()
        if(noteAsString < 0 || noteAsString > 10)
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.incorrect_note_field_error
            )

        return ValidationResult(
            successful = true
        )
    }
}