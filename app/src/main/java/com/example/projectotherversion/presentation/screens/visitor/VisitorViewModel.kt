package com.example.projectotherversion.presentation.screens.visitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.usecase.post.GetAllPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VisitorViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VisitorState())
    val state = _state.asStateFlow()

    init {
        observePosts()
    }

    private fun observePosts() {
        getAllPostsUseCase()
            .onEach { posts ->
                _state.update { it.copy(posts = posts) }
            }
            .catch { e ->
                _state.update { it.copy(error = e.message) }
            }
            .launchIn(viewModelScope)
    }
}

data class VisitorState(
    val posts: List<Post> = emptyList(),
    val error: String? = null
)
