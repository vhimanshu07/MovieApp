package com.example.inshortsmovieapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inshortsmovieapp.data.repository.MovieRepo
import com.example.inshortsmovieapp.ui.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: MovieRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            repository.getBookmarkedMovies().collect { movies ->
                _uiState.update {
                    it.copy(
                        movies = movies,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun removeBookmark(movie: Movie) {
        viewModelScope.launch {
            repository.toggleBookmark(movie)
        }
    }
}