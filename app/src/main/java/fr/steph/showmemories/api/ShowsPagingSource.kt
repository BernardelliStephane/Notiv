package fr.steph.showmemories.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.steph.showmemories.api.ShowsAPI.Companion.STARTING_PAGE_INDEX
import fr.steph.showmemories.models.tmdbmodels.TmdbTv
import retrofit2.HttpException
import java.io.IOException

class ShowsPagingSource(private val query: String? = null, private val language: String) : PagingSource<Int, TmdbTv>() {

    override fun getRefreshKey(state: PagingState<Int, TmdbTv>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TmdbTv> {
        val pageIndex = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = query?.let { RetrofitInstance.api.searchForShows(query, language, pageIndex) }
                ?: RetrofitInstance.api.getWeeklyTrendingShows(language, pageIndex)

            val shows = response.body()?.results

            LoadResult.Page(
                data = shows.orEmpty(),
                prevKey = if (pageIndex == STARTING_PAGE_INDEX) null else pageIndex - 1,
                nextKey = if (shows?.isEmpty() == true) null else pageIndex + 1
            )
        }
        catch (exception: IOException) {
            return LoadResult.Error(exception)
        }
        catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}