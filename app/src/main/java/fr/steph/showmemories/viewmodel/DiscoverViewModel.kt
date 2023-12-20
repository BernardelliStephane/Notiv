package fr.steph.showmemories.viewmodel

import androidx.lifecycle.*
import androidx.paging.cachedIn
import fr.steph.showmemories.R
import fr.steph.showmemories.model.tmdbmodels.TmdbTv
import fr.steph.showmemories.repository.TmdbShowRepository
import fr.steph.showmemories.utils.ConnectivityChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*

class DiscoverViewModel(private val repo: TmdbShowRepository = TmdbShowRepository()) : ViewModel() {

    private lateinit var connectivityChecker: ConnectivityChecker
    private var currentLanguage = "en-US"

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery

    private val _tmdbTv: MutableLiveData<Resource?> = MutableLiveData()
    val tmdbTv: LiveData<Resource?> = _tmdbTv

    @OptIn(ExperimentalCoroutinesApi::class)
    val shows = currentQuery.flatMapLatest {
        if (it.isBlank()) getTrendingShows(currentLanguage)
        else searchShows(it.trim(), currentLanguage)
    }.cachedIn(viewModelScope)

    private fun getTrendingShows(language: String) =
        repo.getTrendingShows(language)

    private fun searchShows(query: String, language: String) =
        repo.searchShows(query, language)

    fun getShowInfo(id: Int) = viewModelScope.launch {
        _tmdbTv.postValue(Resource.Loading)
        try {
            if(connectivityChecker()) {
                val response = repo.getShowInfo(id)
                _tmdbTv.postValue(handleResponse(response))
            }
            else _tmdbTv.postValue(Resource.Error(R.string.no_internet_connexion))
        } catch (t: Throwable) {
            when(t) {
                is SocketTimeoutException -> _tmdbTv.postValue(Resource.Error(R.string.connexion_timeout))
                is IOException -> _tmdbTv.postValue(Resource.Error(R.string.network_failure))
                else -> _tmdbTv.postValue(Resource.Error(R.string.conversion_error))
            }
        }
    }

    private fun handleResponse(response: Response<TmdbTv>) : Resource {
        return if(response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
        else Resource.Error(R.string.network_failure)
    }

    fun queryChanged(query: String) = viewModelScope.launch {
        _currentQuery.emit(query)
    }

    fun resetShowValue() {
        _tmdbTv.value = null
    }

    fun setConnectivityChecker(connectivityChecker: ConnectivityChecker) {
        this.connectivityChecker = connectivityChecker
    }

    fun setLanguage(language: Locale) {
        currentLanguage = language.language
    }

    sealed class Resource {
        object Loading: Resource()
        data class Error(val message: Int): Resource()
        data class Success(val data: TmdbTv): Resource()
    }
}