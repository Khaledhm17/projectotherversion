package com.example.projectotherversion.presentation.screens.dashboard

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.model.User
import com.example.projectotherversion.presentation.components.CitySpinner
import com.example.projectotherversion.presentation.components.ProfessionSpinner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToComplaints: () -> Unit,
    onNavigateToWorkRequests: () -> Unit,
    onNavigateToMyPosts: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // تحديث البيانات فور ظهور الشاشة (يضمن تحديث التقييم عند العودة)
    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.RefreshData)
    }

    val userRole = state.currentUser?.role ?: ""
    val isAdmin = userRole == "ADMIN"
    val isCraftsman = userRole == "CRAFTSMAN"
    val isClient = userRole == "CLIENT"

    var showAddPostDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(DashboardEvent.UpdatePostImageUri(uri))
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("تسجيل الخروج", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد أنك تريد تسجيل الخروج؟") },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(DashboardEvent.Logout) }) { Text("خروج") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("إلغاء") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(user = state.currentUser)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("الرئيسية") },
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )

                if (isAdmin) {
                    NavigationDrawerItem(
                        label = { Text("إدارة الحسابات") },
                        icon = { Icon(Icons.Default.AccountBox, null) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        label = { Text("إدارة الشكاوى") },
                        icon = { Icon(Icons.Default.Warning, null) },
                        selected = false,
                        onClick = { onNavigateToComplaints(); scope.launch { drawerState.close() } }
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("تقديم شكوى") },
                        icon = { Icon(Icons.Default.Info, null) },
                        selected = false,
                        onClick = { onNavigateToComplaints(); scope.launch { drawerState.close() } }
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isCraftsman) "طلبات العمل" else "صندوق الرسائل")
                                if (state.unreadWorkRequestsCount > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(state.unreadWorkRequestsCount.toString(), color = MaterialTheme.colorScheme.onError)
                                    }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Email, null) },
                        selected = false,
                        onClick = {
                            viewModel.onEvent(DashboardEvent.ResetWorkRequestsCount)
                            onNavigateToWorkRequests()
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("منشوراتي") },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        selected = false,
                        onClick = { onNavigateToMyPosts(); scope.launch { drawerState.close() } }
                    )
                }

                NavigationDrawerItem(
                    label = { Text("الإعدادات") },
                    icon = { Icon(Icons.Default.Settings, null) },
                    selected = false,
                    onClick = { onNavigateToSettings(); scope.launch { drawerState.close() } }
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("تسجيل الخروج", color = MaterialTheme.colorScheme.error) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = { showLogoutConfirm = true }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isAdmin) "لوحة الإدارة" else "ArtisanApp", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!isAdmin) {
                    FloatingActionButton(onClick = { showAddPostDialog = true }) {
                        Icon(Icons.Default.Add, "إضافة منشور")
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }

                when {
                    isAdmin -> AdminDashboardContent(state) { id, b -> viewModel.onEvent(DashboardEvent.BlockUser(id, b)) }
                    isClient -> ClientDashboardContent(state, viewModel::onEvent, onNavigateToChat)
                    isCraftsman -> CraftsmanDashboardContent(state, viewModel::onEvent, onNavigateToChat) { viewModel.onEvent(DashboardEvent.DeletePost(it)) }
                    else -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                }
            }

            if (showAddPostDialog) {
                AddPostDialog(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onDismiss = { showAddPostDialog = false },
                    onPickImage = { imagePicker.launch("image/*") }
                )
            }
        }
    }
}

@Composable
fun DrawerHeader(user: User?) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(24.dp)) {
        Column {
            if (!user?.profileImage.isNullOrBlank()) {
                AsyncImage(
                    model = user?.profileImage,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = user?.name ?: "مستخدم", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            val roleAr = when(user?.role) {
                "ADMIN" -> "مدير النظام"
                "CLIENT" -> "زبون"
                "CRAFTSMAN" -> "حرفي"
                else -> "مستخدم"
            }
            Text(text = roleAr, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun AdminDashboardContent(state: DashboardState, onBlockUser: (String, Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("إدارة مستخدمي النظام", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (state.users.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لا يوجد مستخدمين لعرضهم") }
        }
        LazyColumn {
            items(state.users.filter { it.role != "ADMIN" }) { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (!user.profileImage.isNullOrBlank()) {
                            AsyncImage(
                                model = user.profileImage,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            val r = if (user.role == "CLIENT") "زبون" else "حرفي"
                            Text("$r - ${user.city}", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onBlockUser(user.id, !user.isBlocked) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        ) {
                            Text(if (user.isBlocked) "إلغاء الحظر" else "حظر")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDashboardContent(state: DashboardState, onEvent: (DashboardEvent) -> Unit, onContact: (String, String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) { CitySpinner(state.filterCity, onCitySelected = { onEvent(DashboardEvent.UpdateFilterCity(it)) }) }
            Box(modifier = Modifier.weight(1f)) { ProfessionSpinner(state.filterProfession, onProfessionSelected = { onEvent(DashboardEvent.UpdateFilterProfession(it)) }) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val posts = state.posts.filter {
            it.type == "SERVICE" &&
                    it.city == state.filterCity &&
                    it.profession == state.filterProfession
        }
        if (posts.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لا توجد خدمات متاحة حالياً") }
        LazyColumn { items(posts) { post -> PostCard(post, state.users, false, onContact, {}) } }
    }
}

@Composable
fun CraftsmanDashboardContent(state: DashboardState, onEvent: (DashboardEvent) -> Unit, onContact: (String, String) -> Unit, onDelete: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = state.selectedTab) {
            Tab(selected = state.selectedTab == 0, onClick = { onEvent(DashboardEvent.UpdateSelectedTab(0)) }, text = { Text("طلبات الزبائن") })
            Tab(selected = state.selectedTab == 1, onClick = { onEvent(DashboardEvent.UpdateSelectedTab(1)) }, text = { Text("خدماتي") })
        }
        val posts = if (state.selectedTab == 0) {
            state.posts.filter {
                it.type == "REQUEST" &&
                        it.city == state.currentUser?.city &&
                        it.profession == state.currentUser?.profession
            }
        } else {
            state.posts.filter { it.type == "SERVICE" && it.authorId == state.currentUser?.id }
        }
        if (posts.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لا توجد منشورات لعرضها") }
        LazyColumn(modifier = Modifier.padding(16.dp)) { items(posts) { post -> PostCard(post, state.users, state.selectedTab == 1, onContact) { onDelete(post.id) } } }
    }
}

@Composable
fun PostCard(post: Post, users: List<User>, isOwn: Boolean, onContact: (String, String) -> Unit, onDelete: () -> Unit) {
    val author = users.find { it.id == post.authorId }
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails && author != null) {
        UserDetailDialog(user = author, onDismiss = { showDetails = false })
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!author?.profileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = author?.profileImage,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, fontWeight = FontWeight.Bold)
                        if (author?.role == "CRAFTSMAN") {
                            Spacer(Modifier.width(8.dp))
                            // نمرر القيم مباشرة من كائن المؤلف الموجود في قائمة المستخدمين المحدثة
                            RatingStars(author.totalRating, author.ratingCount)
                        }
                    }
                    Text("${post.city} - ${post.profession}", fontSize = 12.sp)
                }

                if (author?.role == "CRAFTSMAN") {
                    TextButton(onClick = { showDetails = true }) {
                        Text("التفاصيل", color = Color(0xFF1565C0))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(post.description)

            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.End) {
                if (isOwn) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                else Button(onClick = { onContact(post.authorId, post.authorName) }) { Text("تواصل الآن") }
            }
        }
    }
}

@Composable
fun RatingStars(total: Double, count: Int) {
    // التعديل: تحويل count إلى Double لضمان دقة القسمة وظهور الكسور
    val avg = if (count > 0) total / count.toDouble() else 0.0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        // عرض رقم واحد بعد الفاصلة
        Text(text = "%.1f".format(avg), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = " ($count)", fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun UserDetailDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الحرفي", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (!user.profileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(80.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(user.profession ?: "حرفي", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                RatingStars(user.totalRating, user.ratingCount)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(user.city)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun AddPostDialog(state: DashboardState, onEvent: (DashboardEvent) -> Unit, onDismiss: () -> Unit, onPickImage: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة منشور") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.currentUser?.role == "CLIENT") {
                    Text("ما هي الحرفة المطلوبة؟", fontWeight = FontWeight.Bold)
                    ProfessionSpinner(state.selectedProfession, onProfessionSelected = { onEvent(DashboardEvent.UpdateSelectedProfession(it)) })
                }
                OutlinedTextField(value = state.postDescription, onValueChange = { onEvent(DashboardEvent.UpdatePostDescription(it)) }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())

                Button(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.postImageUri != null) "تم اختيار صورة" else "إضافة صورة")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val prof = if (state.currentUser?.role == "CLIENT") state.selectedProfession else state.currentUser?.profession ?: ""
                val type = if (state.currentUser?.role == "CLIENT") "REQUEST" else "SERVICE"
                val post = Post("", state.currentUser?.id ?: "", state.currentUser?.name ?: "", type, prof, state.postDescription, state.currentUser?.city ?: "", null, 0L)
                onEvent(DashboardEvent.CreatePost(post, state.postImageUri))
                onDismiss()
            }) { Text("نشر") }
        }
    )
}