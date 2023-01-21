package fr.steph.showmemories.validators

import fr.steph.showmemories.R

class ValidateImage {

    fun execute(image: String): ValidationResult {
        if(image.isEmpty())
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.mandatory_field_error
            )

        return ValidationResult(
            successful = true
        )
    }
}