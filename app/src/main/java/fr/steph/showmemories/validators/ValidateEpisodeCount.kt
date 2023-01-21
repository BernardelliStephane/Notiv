package fr.steph.showmemories.validators

import fr.steph.showmemories.R

class ValidateEpisodeCount {

    fun execute(episodeCount: String): ValidationResult {
        if(episodeCount.isEmpty()) {
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