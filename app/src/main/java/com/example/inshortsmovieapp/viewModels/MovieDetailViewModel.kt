package com.example.inshortsmovieapp.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inshortsmovieapp.constants.Result
import com.example.inshortsmovieapp.data.repository.MovieRepo
import com.example.inshortsmovieapp.ui.model.MovieDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val movie: MovieDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookmarked: Boolean = false
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val movieId: Int = savedStateHandle.get<Int>("movieId") ?: -1

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        if (movieId != -1) {
            loadMovieDetails()
            observeBookmarkStatus()
        }
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            repository.getMovieDetails(movieId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, error = null)
                        }
                    }

                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                movie = result.data,
                                isLoading = false,
                                error = null,
                                isBookmarked = result.data?.isBookmarked ?: false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                movie = result.data,
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeBookmarkStatus() {
        viewModelScope.launch {
            repository.isMovieBookmarkedFlow(movieId).collect { isBookmarked ->
                _uiState.update { it.copy(isBookmarked = isBookmarked) }
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            _uiState.value.movie?.let { movie ->
                repository.toggleBookmarkFromDetail(movie)
            }
        }
    }

    fun refresh() {
        loadMovieDetails()
    }
}