package com.example.inshortsmovieapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inshortsmovieapp.constants.Result
import com.example.inshortsmovieapp.data.repository.MovieRepo
import com.example.inshortsmovieapp.ui.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class HomeUiState(
    val trendingMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val isLoadingTrending: Boolean = false,
    val isLoadingNowPlaying: Boolean = false,
    val isLoadingMoreTrending: Boolean = false,
    val isLoadingMoreNowPlaying: Boolean = false,
    val trendingError: String? = null,
    val nowPlayingError: String? = null,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val trendingCurrentPage: Int = 1,
    val trendingTotalPages: Int = 1,
    val nowPlayingCurrentPage: Int = 1,
    val nowPlayingTotalPages: Int = 1
) {
    val canLoadMoreTrending: Boolean
        get() = trendingCurrentPage < trendingTotalPages && !isLoadingMoreTrending

    val canLoadMoreNowPlaying: Boolean
        get() = nowPlayingCurrentPage < nowPlayingTotalPages && !isLoadingMoreNowPlaying
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: MovieRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMovies()
    }

    private fun loadMovies() {
        loadTrendingMovies()
        loadNowPlayingMovies()
    }

    fun refresh() {
        _uiState.update {
            it.copy(
                isRefreshing = true,
                trendingCurrentPage = 1,
                nowPlayingCurrentPage = 1
            )
        }
        loadMovies()
    }

    private fun loadTrendingMovies() {
        viewModelScope.launch {
            repository.getTrendingMoviesPaginated(page = 1).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoadingTrending = true,
                                trendingError = null
                            )
                        }
                    }

                    is Result.Success -> {
                        val paginatedResult = result.data
                        _uiState.update {
                            it.copy(
                                trendingMovies = paginatedResult?.data ?: emptyList(),
                                isLoadingTrending = false,
                                trendingError = null,
                                isRefreshing = false,
                                trendingCurrentPage = paginatedResult?.currentPage ?: 1,
                                trendingTotalPages = paginatedResult?.totalPages ?: 1
                            )
                        }
                    }

                    is Result.Error -> {
                        val paginatedResult = result.data
                        _uiState.update {
                            it.copy(
                                trendingMovies = paginatedResult?.data ?: it.trendingMovies,
                                isLoadingTrending = false,
                                trendingError = if (paginatedResult?.data.isNullOrEmpty()) result.message else null,
                                isRefreshing = false,
                                trendingCurrentPage = paginatedResult?.currentPage
                                    ?: it.trendingCurrentPage,
                                trendingTotalPages = paginatedResult?.totalPages
                                    ?: it.trendingTotalPages
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMoreTrendingMovies() {
        val currentState = _uiState.value
        if (!currentState.canLoadMoreTrending || currentState.isLoadingMoreTrending) return

        val nextPage = currentState.trendingCurrentPage + 1
        _uiState.update { it.copy(isLoadingMoreTrending = true) }

        viewModelScope.launch {
            Log.d("CHECK->", "loadMoreTrendingMovies: $nextPage")
            repository.getTrendingMoviesPaginated(page = nextPage).collect { result ->
                when (result) {
                    is Result.Loading -> {
                    }

                    is Result.Success -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val existingIds = state.trendingMovies.map { it.id }.toSet()
                            val newMovies = (paginatedResult?.data ?: emptyList())
                                .filter { it.id !in existingIds }
                            state.copy(
                                trendingMovies = state.trendingMovies + newMovies,
                                isLoadingMoreTrending = false,
                                trendingCurrentPage = paginatedResult?.currentPage
                                    ?: state.trendingCurrentPage,
                                trendingTotalPages = paginatedResult?.totalPages
                                    ?: state.trendingTotalPages
                            )
                        }
                    }

                    is Result.Error -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val existingIds = state.trendingMovies.map { it.id }.toSet()
                            val newMovies = (paginatedResult?.data ?: emptyList())
                                .filter { it.id !in existingIds }

                            state.copy(
                                trendingMovies = state.trendingMovies + newMovies,
                                isLoadingMoreTrending = false,
                                trendingCurrentPage = paginatedResult?.currentPage
                                    ?: state.trendingCurrentPage,
                                trendingTotalPages = paginatedResult?.totalPages
                                    ?: state.trendingTotalPages
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadNowPlayingMovies() {
        viewModelScope.launch {
            repository.getNowPlayingMoviesPaginated(page = 1).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoadingNowPlaying = true,
                                nowPlayingError = null
                            )
                        }
                    }

                    is Result.Success -> {
                        val paginatedResult = result.data
                        _uiState.update {
                            it.copy(
                                nowPlayingMovies = paginatedResult?.data ?: emptyList(),
                                isLoadingNowPlaying = false,
                                nowPlayingError = null,
                                isRefreshing = false,
                                nowPlayingCurrentPage = paginatedResult?.currentPage ?: 1,
                                nowPlayingTotalPages = paginatedResult?.totalPages ?: 1
                            )
                        }
                    }

                    is Result.Error -> {
                        val paginatedResult = result.data
                        _uiState.update {
                            it.copy(
                                nowPlayingMovies = paginatedResult?.data ?: it.nowPlayingMovies,
                                isLoadingNowPlaying = false,
                                nowPlayingError = if (paginatedResult?.data.isNullOrEmpty()) result.message else null,
                                isRefreshing = false,
                                nowPlayingCurrentPage = paginatedResult?.currentPage
                                    ?: it.nowPlayingCurrentPage,
                                nowPlayingTotalPages = paginatedResult?.totalPages
                                    ?: it.nowPlayingTotalPages
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMoreNowPlayingMovies() {
        val currentState = _uiState.value
        if (!currentState.canLoadMoreNowPlaying || currentState.isLoadingMoreNowPlaying) return

        val nextPage = currentState.nowPlayingCurrentPage + 1
        _uiState.update { it.copy(isLoadingMoreNowPlaying = true) }
        viewModelScope.launch {
            repository.getNowPlayingMoviesPaginated(page = nextPage).collect { result ->
                when (result) {
                    is Result.Loading -> {
                    }

                    is Result.Success -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val existingIds = state.nowPlayingMovies.map { it.id }.toSet()
                            val newMovies = (paginatedResult?.data ?: emptyList())
                                .filter { it.id !in existingIds }
                            state.copy(
                                nowPlayingMovies = state.nowPlayingMovies + newMovies,
                                isLoadingMoreNowPlaying = false,
                                nowPlayingCurrentPage = paginatedResult?.currentPage
                                    ?: state.nowPlayingCurrentPage,
                                nowPlayingTotalPages = paginatedResult?.totalPages
                                    ?: state.nowPlayingTotalPages
                            )
                        }
                    }

                    is Result.Error -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val existingIds = state.nowPlayingMovies.map { it.id }.toSet()
                            val newMovies = (paginatedResult?.data ?: emptyList())
                                .filter { it.id !in existingIds }
                            state.copy(
                                nowPlayingMovies = state.nowPlayingMovies + newMovies,
                                isLoadingMoreNowPlaying = false,
                                nowPlayingCurrentPage = paginatedResult?.currentPage
                                    ?: state.nowPlayingCurrentPage,
                                nowPlayingTotalPages = paginatedResult?.totalPages
                                    ?: state.nowPlayingTotalPages
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            repository.toggleBookmark(movie)
            _uiState.update { state ->
                state.copy(
                    trendingMovies = state.trendingMovies.map {
                        if (it.id == movie.id) it.copy(isBookmarked = !it.isBookmarked) else it
                    },
                    nowPlayingMovies = state.nowPlayingMovies.map {
                        if (it.id == movie.id) it.copy(isBookmarked = !it.isBookmarked) else it
                    }
                )
            }
        }
    }

}