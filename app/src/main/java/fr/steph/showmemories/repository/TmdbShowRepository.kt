package fr.steph.showmemories.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import fr.steph.showmemories.api.RetrofitInstance
import fr.steph.showmemories.api.ShowsAPI.Companion.NETWORK_PAGE_SIZE
import fr.steph.showmemories.api.ShowsPagingSource
import fr.steph.showmemories.models.tmdbmodels.TmdbTv
import kotlinx.coroutines.flow.Flow

class TmdbShowRepository {

    fun getTrendingShows(language: String): Flow<PagingData<TmdbTv>> =
        Pager(config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, maxSize = 200, enablePlaceholders = false),
            pagingSourceFactory = { ShowsPagingSource(null, language) }
        ).flow

    fun searchShows(query: String, language: String): Flow<PagingData<TmdbTv>> =
        Pager(config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, maxSize = 200, enablePlaceholders = false),
            pagingSourceFactory = { ShowsPagingSource(query, language) }
        ).flow

    suspend fun getShowInfo(id: Int) =
        RetrofitInstance.api.getShowInfo(id)
}