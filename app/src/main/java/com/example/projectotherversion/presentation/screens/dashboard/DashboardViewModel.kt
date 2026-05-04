package com.example.projectotherversion.presentation.screens.dashboard

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.domain.usecase.auth.LogoutUseCase
import com.example.projectotherversion.domain.usecase.message.GetNotificationsCountUseCase
import com.example.projectotherversion.domain.usecase.post.CreatePostUseCase
import com.example.projectotherversion.domain.usecase.post.DeletePostUseCase
import com.example.projectotherversion.domain.usecase.post.GetAllPostsUseCase
import com.example.projectotherversion.domain.usecase.user.BlockUserUseCase
import com.example.projectotherversion.domain.usecase.user.GetAllUsersUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getNotificationsCountUseCase: GetNotificationsCountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private var lastSeenMessagesCount = 0

    init {
        loadCurrentUser()
        observePosts()
        observeUsers()
        observeNotifications()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()
                Log.d("DashboardVM", "Logged in as: ${user?.name}, Role: ${user?.role}")
                _state.update {
                    it.copy(
                        currentUser = user,
                        filterCity = user?.city ?: "الوادي",
                        filterProfession = user?.profession ?: "بناء",
                        selectedProfession = "بناء"
                    )
                }
            } catch (e: Exception) {
                Log.e("DashboardVM", "Error loading current user: ${e.message}")
            }
        }
    }

    private fun observePosts() {
        getAllPostsUseCase()
            .onEach { posts ->
                Log.d("DashboardVM", "Posts updated from DB: ${posts.size}")
                _state.update { it.copy(posts = posts, error = null) }
            }
            .catch { e ->
                Log.e("DashboardVM", "Error observing posts: ${e.message}")
                _state.update { it.copy(error = "فشل في جلب المنشورات: تأكد من جودة الإنترنت") }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUsers() {
        getAllUsersUseCase()
            .onEach { users ->
                Log.d("DashboardVM", "Users updated: ${users.size}")
                _state.update { it.copy(users = users) }
            }
            .catch { e ->
                Log.e("DashboardVM", "Error observing users: ${e.message}")
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNotifications() {
        _state.map { it.currentUser?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                getNotificationsCountUseCase(userId)
                    .catch { emit(0) }
            }.onEach { totalMessages ->
                _state.update { it.copy(
                    unreadWorkRequestsCount = (totalMessages - lastSeenMessagesCount).coerceAtLeast(0)
                ) }
            }.launchIn(viewModelScope)
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.CreatePost -> createPost(event.post, event.imageUri)
            is DashboardEvent.DeletePost -> deletePost(event.postId)
            is DashboardEvent.BlockUser -> blockUser(event.userId, event.blocked)
            DashboardEvent.Logout -> logout()
            is DashboardEvent.UpdateFilterCity -> _state.update { it.copy(filterCity = event.city) }
            is DashboardEvent.UpdateFilterProfession -> _state.update { it.copy(filterProfession = event.profession) }
            is DashboardEvent.UpdateSelectedTab -> _state.update { it.copy(selectedTab = event.tab) }
            is DashboardEvent.UpdatePostDescription -> _state.update { it.copy(postDescription = event.description, error = null) }
            is DashboardEvent.UpdatePostImageUri -> _state.update { it.copy(postImageUri = event.uri, error = null) }
            is DashboardEvent.ClearPostForm -> _state.update { it.copy(postDescription = "", postImageUri = null, error = null) }
            DashboardEvent.ResetWorkRequestsCount -> {
                viewModelScope.launch {
                    val userId = _state.value.currentUser?.id ?: return@launch
                    getNotificationsCountUseCase(userId).take(1).collect { total ->
                        lastSeenMessagesCount = total
                        _state.update { it.copy(unreadWorkRequestsCount = 0) }
                    }
                }
            }
            is DashboardEvent.UpdateSelectedProfession -> _state.update { it.copy(selectedProfession = event.profession) }
        }
    }

    private fun createPost(post: Post, imageUri: Uri?) {
        viewModelScope.launch {
            _state.update { it.copy(isCreatingPost = true, error = null) }
            val result = createPostUseCase(post, imageUri)
            if (result.isSuccess) {
                _state.update { it.copy(isCreatingPost = false, postDescription = "", postImageUri = null) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "فشل النشر"
                Log.e("DashboardVM", "Create post error: $errorMsg")
                _state.update { it.copy(isCreatingPost = false, error = errorMsg) }
            }
        }
    }

    private fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                deletePostUseCase(postId)
            } catch (e: Exception) {
                Log.e("DashboardVM", "Delete post error: ${e.message}")
            }
        }
    }

    private fun blockUser(userId: String, blocked: Boolean) {
        // 1. فحص الصلاحيات قبل الإرسال (أمان إضافي)
        val currentUser = _state.value.currentUser
        if (currentUser?.role != "ADMIN") {
            _state.update { it.copy(error = "لا تملك صلاحية مسؤول للنظام") }
            return
        }

        Log.d("DashboardVM", "بدأت عملية الحظر للمستخدم: $userId")

        viewModelScope.launch {
            // 2. استدعاء الـ UseCase واستقبال النتيجة من نوع Result
            val result = blockUserUseCase(userId, blocked)

            result.onSuccess {
                Log.d("DashboardVM", "نجحت العملية في السيرفر")

                // 3. التحديث المحلي للـ State لضمان استجابة الواجهة فوراً
                _state.update { currentState ->
                    val updatedUsers = currentState.users.map { user ->
                        if (user.id == userId) {
                            user.copy(isBlocked = blocked)
                        } else {
                            user
                        }
                    }
                    currentState.copy(users = updatedUsers, error = null)
                }
            }.onFailure { error ->
                Log.e("DashboardVM", "فشلت العملية: ${error.message}")
                _state.update { it.copy(error = "فشل تحديث حالة الحظر: ${error.message}") }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.update { it.copy(isLoggedOut = true) }
        }
    }
}

data class DashboardState(
    val currentUser: User? = null,
    val posts: List<Post> = emptyList(),
    val users: List<User> = emptyList(),
    val filterCity: String = "الوادي",
    val filterProfession: String = "بناء",
    val selectedTab: Int = 0,
    val postDescription: String = "",
    val postImageUri: Uri? = null,
    val isCreatingPost: Boolean = false,
    val isLoggedOut: Boolean = false,
    val error: String? = null,
    val unreadWorkRequestsCount: Int = 0,
    val selectedProfession: String = "بناء"
)

sealed class DashboardEvent {
    data class CreatePost(val post: Post, val imageUri: Uri?) : DashboardEvent()
    data class DeletePost(val postId: String) : DashboardEvent()
    data class BlockUser(val userId: String, val blocked: Boolean) : DashboardEvent()
    object Logout : DashboardEvent()
    data class UpdateFilterCity(val city: String) : DashboardEvent()
    data class UpdateFilterProfession(val profession: String) : DashboardEvent()
    data class UpdateSelectedTab(val tab: Int) : DashboardEvent()
    data class UpdatePostDescription(val description: String) : DashboardEvent()
    data class UpdatePostImageUri(val uri: Uri?) : DashboardEvent()
    object ClearPostForm : DashboardEvent()
    object ResetWorkRequestsCount : DashboardEvent()
    data class UpdateSelectedProfession(val profession: String) : DashboardEvent()
}
