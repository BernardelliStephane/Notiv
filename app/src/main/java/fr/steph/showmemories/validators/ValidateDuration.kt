package fr.steph.showmemories.validators

import fr.steph.showmemories.R

class ValidateDuration {

    fun execute(duration: String): ValidationResult {
        if(duration.isEmpty()) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.mandatory_field_error
            )
        }

        return ValidationResult(
            successful = true
        )
    }
}