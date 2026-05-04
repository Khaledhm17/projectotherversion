package com.example.projectotherversion.presentation.screens.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.usecase.post.DeletePostUseCase
import com.example.projectotherversion.domain.usecase.post.GetAllPostsUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyPostsState())
    val state = _state.asStateFlow()

    init {
        loadMyPosts()
    }

    private fun loadMyPosts() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            _state.update { it.copy(currentUserId = currentUser?.id ?: "") }

            getAllPostsUseCase().collect { posts ->
                // تعديل التصفية لعرض كل منشورات المستخدم الحالي بغض النظر عن النوع
                val myPosts = posts.filter { it.authorId == currentUser?.id }
                _state.update { it.copy(posts = myPosts) }
            }
        }
    }

    fun onEvent(event: MyPostsEvent) {
        when (event) {
            is MyPostsEvent.DeletePost -> deletePost(event.postId)
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            deletePostUseCase(postId)
        }
    }
}

data class MyPostsState(
    val currentUserId: String = "",
    val posts: List<Post> = emptyList()
)

sealed class MyPostsEvent {
    data class DeletePost(val postId: String) : MyPostsEvent()
}
