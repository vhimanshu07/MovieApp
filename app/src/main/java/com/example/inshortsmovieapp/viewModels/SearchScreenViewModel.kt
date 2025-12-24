package com.example.inshortsmovieapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inshortsmovieapp.constants.Result
import com.example.inshortsmovieapp.data.repository.MovieRepo
import com.example.inshortsmovieapp.ui.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SearchUiState(
    val query: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalResults: Int = 0
) {
    val canLoadMore: Boolean
        get() = currentPage < totalPages && !isLoadingMore && !isLoading
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val repository: MovieRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query, page = 1)
                    } else {
                        _uiState.update {
                            it.copy(
                                movies = emptyList(),
                                isLoading = false,
                                error = null,
                                hasSearched = false,
                                currentPage = 1,
                                totalPages = 1,
                                totalResults = 0
                            )
                        }
                    }
                }
        }
    }

    private fun performSearch(query: String, page: Int) {
        searchJob?.cancel()
        _uiState.update {
            if (page == 1) {
                it.copy(isLoading = true, error = null)
            } else {
                it.copy(isLoadingMore = true, error = null)
            }
        }
        searchJob = viewModelScope.launch {
            repository.searchMoviesPaginated(query, page).collect { result ->
                when (result) {
                    is Result.Loading -> {
                    }

                    is Result.Success -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val newMovies = if (page == 1) {
                                paginatedResult?.data ?: emptyList()
                            } else {
                                val existingIds = state.movies.map { it.id }.toSet()
                                val uniqueNewMovies = (paginatedResult?.data ?: emptyList())
                                    .filter { it.id !in existingIds }
                                state.movies + uniqueNewMovies
                            }
                            state.copy(
                                movies = newMovies,
                                isLoading = false,
                                isLoadingMore = false,
                                error = null,
                                hasSearched = true,
                                currentPage = paginatedResult?.currentPage ?: 1,
                                totalPages = paginatedResult?.totalPages ?: 1,
                                totalResults = paginatedResult?.totalResults ?: 0
                            )
                        }
                    }

                    is Result.Error -> {
                        val paginatedResult = result.data
                        _uiState.update { state ->
                            val newMovies = if (paginatedResult?.data != null) {
                                if (page == 1) {
                                    paginatedResult.data
                                } else {
                                    val existingIds = state.movies.map { it.id }.toSet()
                                    val uniqueNewMovies = paginatedResult.data
                                        .filter { it.id !in existingIds }
                                    state.movies + uniqueNewMovies
                                }
                            } else {
                                state.movies
                            }
                            state.copy(
                                movies = newMovies,
                                isLoading = false,
                                isLoadingMore = false,
                                error = if (newMovies.isEmpty()) result.message else null,
                                hasSearched = true,
                                currentPage = paginatedResult?.currentPage ?: state.currentPage,
                                totalPages = paginatedResult?.totalPages ?: state.totalPages,
                                totalResults = paginatedResult?.totalResults ?: state.totalResults
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMoreResults() {
        val currentState = _uiState.value
        if (!currentState.canLoadMore || currentState.isLoadingMore) return
        val query = searchQuery.value
        if (query.isNotEmpty()) {
            performSearch(query, currentState.currentPage + 1)
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                currentPage = 1,
                totalPages = 1
            )
        }
        searchQuery.value = query
        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    movies = emptyList(),
                    hasSearched = false,
                    error = null,
                    currentPage = 1,
                    totalPages = 1,
                    totalResults = 0
                )
            }
        }
    }

    fun clearSearch() {
        onQueryChange("")
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            repository.toggleBookmark(movie)
            _uiState.update { state ->
                state.copy(
                    movies = state.movies.map {
                        if (it.id == movie.id) it.copy(isBookmarked = !it.isBookmarked) else it
                    }
                )
            }
        }
    }

}