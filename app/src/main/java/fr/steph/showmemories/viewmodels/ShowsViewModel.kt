package fr.steph.showmemories.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.steph.showmemories.R
import fr.steph.showmemories.models.SeasonModel
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.repository.ShowRepository
import kotlinx.coroutines.launch


class ShowsViewModel(private val repo: ShowRepository = ShowRepository()) : ViewModel() {

    val shows = repo.shows

    private val _messageId = MutableLiveData<Int?>()
    val messageId: LiveData<Int?> = _messageId

    private val _completed = MutableLiveData(false)
    val completed: LiveData<Boolean> = _completed


    init {
        addDatabaseListener()
    }


    fun addShow(show: ShowModel) {
        uploadImage(Uri.parse(show.imageUrl)) {
            viewModelScope.launch {
                val successMessageId = R.string.show_addition_success
                val failureMessageId = R.string.show_addition_failure
                repo.insertShow(show.copy(imageUrl = it.toString()),
                    {
                        _messageId.postValue(successMessageId)
                        _completed.postValue(true)
                    },
                    {
                        _messageId.postValue(failureMessageId)
                        _completed.postValue(true)
                    }
                )
            }
        }
    }


    fun updateShow(show: ShowModel, oldShow: ShowModel) {
        updateShowImage(show, oldShow) {
            viewModelScope.launch {
                val successMessageId = R.string.show_update_success
                val failureMessageId = R.string.show_update_failure
                repo.insertShow(show,
                    {
                        _messageId.postValue(successMessageId)
                        _completed.postValue(true)
                    },
                    {
                        _messageId.postValue(failureMessageId)
                        _completed.postValue(true)
                    }
                )
            }
        }
    }


    fun deleteShow(show: ShowModel) = viewModelScope.launch {
        val successMessageId = R.string.show_deletion_success
        val failureMessageId = R.string.show_deletion_failure
        repo.deleteShow(show, { _messageId.postValue(successMessageId) }, { _messageId.postValue(failureMessageId) })
    }


    fun addSeason(show: ShowModel, season: SeasonModel) = viewModelScope.launch {
        val (successMessageId, failureMessageId) = when (season.movie) {
            true -> listOf(R.string.movie_addition_success, R.string.movie_addition_failure)
            false -> listOf(R.string.season_addition_success, R.string.season_addition_failure)
        }
        show.addSeason(season)
        repo.insertShow(show,
            {
                _messageId.postValue(successMessageId)
                _completed.postValue(true)
            },
            {
                _messageId.postValue(failureMessageId)
                _completed.postValue(true)
            })
    }


    fun updateSeason(show: ShowModel, season: SeasonModel) = viewModelScope.launch {
        val (successMessageId, failureMessageId) = when (season.movie) {
            true -> listOf(R.string.movie_update_success, R.string.movie_update_failure)
            false -> listOf(R.string.season_update_success, R.string.season_update_failure)
        }
        show.updateSeason(season)
        repo.insertShow(show,
            {
                _messageId.postValue(successMessageId)
                _completed.postValue(true)
            },
            {
                _messageId.postValue(failureMessageId)
                _completed.postValue(true)
            })
    }


    fun deleteSeason(show: ShowModel, season: SeasonModel) = viewModelScope.launch {
        val (successMessageId, failureMessageId) = when (season.movie) {
            true -> listOf(R.string.movie_deletion_success, R.string.movie_deletion_failure)
            false -> listOf(R.string.season_deletion_success, R.string.season_deletion_failure)
        }
        show.seasons.remove(season)
        repo.insertShow(show, { _messageId.value = successMessageId }, { _messageId.value = failureMessageId })
    }


    private fun uploadImage(file: Uri, callback: (Uri) -> Unit) = viewModelScope.launch {
        repo.uploadImage(file, callback) {
            _messageId.value = R.string.image_upload_failure
        }
    }


    private fun updateShowImage(show: ShowModel, oldShow: ShowModel, callback: () -> Unit) = viewModelScope.launch {
        if (show.imageUrl == oldShow.imageUrl) callback()
        else deleteImage(oldShow.imageUrl)
        uploadImage(Uri.parse(show.imageUrl)) {
            show.imageUrl = it.toString()
            callback()
        }
    }


    private fun deleteImage(imageUrl: String) = viewModelScope.launch {
        repo.deleteImage(imageUrl)
    }


    private fun addDatabaseListener() = viewModelScope.launch {
        repo.addDatabaseListener()
    }


    fun resetMessageIdValue() {
        _messageId.value = null
    }


    fun resetCompletedValue() {
        _completed.value = false
    }

    fun getLastFiveShows() = shows.value?.sortedBy { it.watchDate }?.take(5)
}