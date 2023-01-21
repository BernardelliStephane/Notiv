package fr.steph.showmemories.validators

import fr.steph.showmemories.R

class ValidateName {

    fun execute(name: String): ValidationResult {
        if(name.isEmpty()) {
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