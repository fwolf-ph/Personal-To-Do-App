package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Task
import com.example.ui.theme.AppThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAppScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val timeUntilMidnight by viewModel.timeUntilMidnight.collectAsStateWithLifecycle()
    val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) }

    val dailyTasks = remember(tasks) { tasks.filter { it.isDaily } }
    val regularTasks = remember(tasks) { tasks.filter { !it.isDaily } }

    val completedDailyCount = remember(dailyTasks) { dailyTasks.count { it.isCompleted } }
    val totalDailyCount = remember(dailyTasks) { dailyTasks.size }
    
    val completedRegularCount = remember(regularTasks) { regularTasks.count { it.isCompleted } }
    val totalRegularCount = remember(regularTasks) { regularTasks.size }

    val isDark = isSystemInDarkTheme()
    val liquidBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.15f else 0.35f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.1f else 0.25f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(liquidBackgroundBrush)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val (icon, titleText) = when (currentTab) {
                                0 -> Icons.Default.CheckCircle to "Daily Checklist"
                                1 -> Icons.Default.Timer to "Fokus-Timer"
                                else -> Icons.Default.BarChart to "Statistiken"
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Einstellungen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentTab == 0) {
                    val isDark = isSystemInDarkTheme()
                    val glassBorderBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.40f else 0.95f),
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.30f)
                        )
                    )

                    Card(
                        modifier = Modifier
                            .testTag("add_task_fab")
                            .clickable { showAddTaskDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            width = 1.2.dp,
                            brush = glassBorderBrush
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = if (isDark) 0.08f else 0.30f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.45f),
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.10f else 0.30f),
                                            Color.White.copy(alpha = if (isDark) 0.03f else 0.15f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Task",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add Task",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                val isDark = isSystemInDarkTheme()
                val glassBorderBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
                        Color.White.copy(alpha = if (isDark) 0.08f else 0.25f)
                    )
                )
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(24.dp)
                            clip = true
                        }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.05f else 0.20f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.08f else 0.25f),
                                    Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = glassBorderBrush,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Checklist"
                            )
                        },
                        label = { Text("Checklist", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.Timer else Icons.Outlined.Timer,
                                contentDescription = "Fokus"
                            )
                        },
                        label = { Text("Fokus", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                contentDescription = "Statistiken"
                            )
                        },
                        label = { Text("Statistik", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    },
                    label = "tab_transition",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Gradient Header / Dashboard Panel
                                DashboardHeader(
                                    timeUntilMidnight = timeUntilMidnight,
                                    completedDailyCount = completedDailyCount,
                                    totalDailyCount = totalDailyCount
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Navigation Tabs
                                TabRow(
                                    selectedTabIndex = pagerState.currentPage,
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Tab(
                                        selected = pagerState.currentPage == 0,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(0)
                                            }
                                        },
                                        text = { Text("Daily Routine", fontWeight = FontWeight.SemiBold) },
                                        icon = {
                                            Icon(
                                                imageVector = if (pagerState.currentPage == 0) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                                                contentDescription = null
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_daily")
                                    )
                                    Tab(
                                        selected = pagerState.currentPage == 1,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(1)
                                            }
                                        },
                                        text = { Text("One-Time", fontWeight = FontWeight.SemiBold) },
                                        icon = {
                                            Icon(
                                                imageVector = if (pagerState.currentPage == 1) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                                contentDescription = null
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_regular")
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Smooth swipable tab content
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) { page ->
                                    when (page) {
                                        0 -> TaskListSection(
                                            tasks = dailyTasks,
                                            onToggle = { viewModel.toggleTask(it) },
                                            onDelete = { viewModel.deleteTask(it) },
                                            onReorder = { viewModel.reorderTasks(it) },
                                            isEmpty = dailyTasks.isEmpty(),
                                            isDailyTab = true,
                                            onAddClick = { showAddTaskDialog = true }
                                        )
                                        1 -> TaskListSection(
                                            tasks = regularTasks,
                                            onToggle = { viewModel.toggleTask(it) },
                                            onDelete = { viewModel.deleteTask(it) },
                                            onReorder = { viewModel.reorderTasks(it) },
                                            isEmpty = regularTasks.isEmpty(),
                                            isDailyTab = false,
                                            onAddClick = { showAddTaskDialog = true }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            FocusTab(viewModel = viewModel, isDark = isDark)
                        }
                        2 -> {
                            StatisticsTab(viewModel = viewModel, tasks = tasks, isDark = isDark)
                        }
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, description, isDaily ->
                    viewModel.addTask(title, description, isDaily)
                    showAddTaskDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentTheme = currentTheme,
                onThemeSelected = { viewModel.setThemeOption(it) },
                onResetDailyTasks = { viewModel.forceResetDailyTasks() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
fun DashboardHeader(
    timeUntilMidnight: String,
    completedDailyCount: Int,
    totalDailyCount: Int
) {
    val isDark = isSystemInDarkTheme()
    val cardBgColor = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }
    
    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.32f else 0.95f),
            Color.White.copy(alpha = if (isDark) 0.05f else 0.22f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.2.dp,
            brush = glassBorderBrush
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.25f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.15f else 0.40f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.08f else 0.25f),
                            Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Focus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Build small habits, yield big goals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Countdown Badge
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Reset timer",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Resets in: $timeUntilMidnight",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Progress Section - Full-width Daily Habits bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Habits",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$completedDailyCount/$totalDailyCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val dailyProgress = if (totalDailyCount > 0) completedDailyCount.toFloat() / totalDailyCount else 0f
                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// Drag-and-drop state controller for Jetpack Compose LazyColumn
class DragDropState(
    val lazyListState: LazyListState
) {
    var draggedTaskId by mutableStateOf<Int?>(null)
        private set
    var dragOffset by mutableStateOf(0f)
        private set

    var getTasks: (() -> List<Task>)? = null
    var onMove: ((Int, Int) -> Unit)? = null
    var onDragEnd: (() -> Unit)? = null

    fun onDragStart(taskId: Int) {
        draggedTaskId = taskId
        dragOffset = 0f
    }

    fun onDrag(dragAmount: Float) {
        dragOffset += dragAmount
        val currentTaskId = draggedTaskId ?: return
        val tasks = getTasks?.invoke() ?: return
        val currentDraggedIndex = tasks.indexOfFirst { it.id == currentTaskId }
        if (currentDraggedIndex == -1) return

        val layoutInfo = lazyListState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val draggedItem = visibleItems.firstOrNull { it.index == currentDraggedIndex } ?: return
        
        val draggedItemCenter = draggedItem.offset + draggedItem.size / 2 + dragOffset

        val targetItem = visibleItems.firstOrNull { item ->
            draggedItemCenter.toInt() in item.offset..(item.offset + item.size) && 
                item.index != currentDraggedIndex && 
                item.index in tasks.indices
        }

        if (targetItem != null) {
            onMove?.invoke(currentDraggedIndex, targetItem.index)
            dragOffset -= (targetItem.offset - draggedItem.offset)
        }
    }

    fun onDragEnd() {
        draggedTaskId = null
        dragOffset = 0f
        onDragEnd?.invoke()
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    getTasks: () -> List<Task>,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
): DragDropState {
    val state = remember(lazyListState) {
        DragDropState(lazyListState)
    }
    state.getTasks = getTasks
    state.onMove = onMove
    state.onDragEnd = onDragEnd
    return state
}

@Composable
fun TaskListSection(
    tasks: List<Task>,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onReorder: (List<Task>) -> Unit,
    isEmpty: Boolean,
    isDailyTab: Boolean,
    onAddClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (isEmpty) {
        val isDark = isSystemInDarkTheme()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Text Typography
            Text(
                text = if (isDailyTab) "Noch keine Gewohnheiten!" else "Alles erledigt!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isDailyTab) {
                    "Erstelle tägliche Gewohnheiten, die sich jeden Tag automatisch zurücksetzen."
                } else {
                    "Füge Aufgaben mit detaillierten Notizen hinzu und behalte offline den Überblick."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        var localTasks by remember(tasks) { mutableStateOf(tasks) }
        val lazyListState = rememberLazyListState()

        val dragDropState = rememberDragDropState(
            lazyListState = lazyListState,
            getTasks = { localTasks },
            onMove = { fromIndex, toIndex ->
                if (fromIndex in localTasks.indices && toIndex in localTasks.indices) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val newList = localTasks.toMutableList()
                    val item = newList.removeAt(fromIndex)
                    newList.add(toIndex, item)
                    localTasks = newList
                }
            },
            onDragEnd = {
                onReorder(localTasks)
            }
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("task_list_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(localTasks, key = { _, task -> task.id }) { index, task ->
                val isDragged = task.id == dragDropState.draggedTaskId
                val translationY = if (isDragged) dragDropState.dragOffset else 0f
                val zIndex = if (isDragged) 10f else 1f

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            this.translationY = translationY
                            this.shadowElevation = if (isDragged) 8f else 0f
                        }
                        .zIndex(zIndex)
                        .then(if (isDragged) Modifier else Modifier.animateItem())
                ) {
                    TaskItemCard(
                        task = task,
                        onToggle = { onToggle(task) },
                        onDelete = { onDelete(task) },
                        onDragStart = { dragDropState.onDragStart(task.id) },
                        onDrag = { amount -> dragDropState.onDrag(amount) },
                        onDragEnd = { dragDropState.onDragEnd() }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) (if (task.isCompleted) 0.15f else 0.32f) else (if (task.isCompleted) 0.50f else 0.95f)),
            Color.White.copy(alpha = if (isDark) (if (task.isCompleted) 0.02f else 0.05f) else (if (task.isCompleted) 0.10f else 0.22f))
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.2.dp,
            brush = glassBorderBrush
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant completion state indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) {
                            if (task.isDaily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    }
                    .testTag("checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Drag Handle for easy reordering instead of individual arrow buttons
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(28.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDragStart()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Delete Action Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String, isDaily: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDaily by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Checklist Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Task Type Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Reset",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unchecks automatically at midnight.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDaily,
                            onCheckedChange = { isDaily = it },
                            modifier = Modifier.testTag("task_type_switch")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description, isDaily)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("dialog_add_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SettingsDialog(
    currentTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit,
    onResetDailyTasks: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Einstellungen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: App Actions
                Text(
                    text = "Aktionen",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Action: Reset Daily Tasks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                        )
                        .clickable {
                            onResetDailyTasks()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Routine zurücksetzen",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Setzt alle täglichen Aufgaben zurück",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                // Section: Theme Color Picker
                Text(
                    text = "Themefarbe",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeOption.values().forEach { option ->
                        val isSelected = option == currentTheme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .clickable { onThemeSelected(option) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(option.primaryColorLight)
                                        .border(
                                            width = 1.dp,
                                            color = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                )

                                Text(
                                    text = option.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Ausgewählt",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_settings_confirm_button")
            ) {
                Text(text = "Fertig", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF262423) else Color(0xFFFDF8F6)
    )
}

@Composable
fun StatisticsTab(
    viewModel: TaskViewModel,
    tasks: List<Task>,
    isDark: Boolean
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    
    val dailyTasks = tasks.filter { it.isDaily }
    val regularTasks = tasks.filter { !it.isDaily }
    
    val completedDaily = dailyTasks.count { it.isCompleted }
    val completedRegular = regularTasks.count { it.isCompleted }
    val dailyCompletionRate = if (dailyTasks.isNotEmpty()) completedDaily.toFloat() / dailyTasks.size else 0f

    val focusStats by viewModel.lastSixDaysFocusMinutes.collectAsStateWithLifecycle()
    val routineStats by viewModel.lastSixDaysRoutineRates.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        // Welcome Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.25f)
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.05f else 0.20f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.15f else 0.35f),
                                    Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Dein Fortschritt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (totalTasks > 0) {
                                "Du hast heute bereits $completedTasks von $totalTasks Aufgaben erfolgreich erledigt. Bleib dran!"
                            } else {
                                "Füge Aufgaben hinzu, um deinen Fortschritt zu verfolgen."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }



        // Beautiful Visual Productivity Level / Chart Card (Routine Daily Only)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.25f)
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.05f else 0.20f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.08f else 0.25f),
                                    Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Routine-Produktivität",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Aktivität der täglichen Routine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Fake visual chart with rounded columns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val daysOfWeek = (5 downTo 0).map { daysAgo ->
                                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
                                if (daysAgo == 0) "Heute" else when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                                    java.util.Calendar.MONDAY -> "Mo"
                                    java.util.Calendar.TUESDAY -> "Di"
                                    java.util.Calendar.WEDNESDAY -> "Mi"
                                    java.util.Calendar.THURSDAY -> "Do"
                                    java.util.Calendar.FRIDAY -> "Fr"
                                    java.util.Calendar.SATURDAY -> "Sa"
                                    java.util.Calendar.SUNDAY -> "So"
                                    else -> ""
                                }
                            }
                            
                            routineStats.forEachIndexed { idx, value ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${(value * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (idx == routineStats.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((value * 80).coerceAtLeast(6f).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (idx == routineStats.lastIndex) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = daysOfWeek[idx],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (idx == routineStats.lastIndex) FontWeight.Bold else FontWeight.Normal,
                                        color = if (idx == routineStats.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Focus Timer Minutes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.25f)
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.05f else 0.20f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = if (isDark) 0.08f else 0.25f),
                                    Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Fokus-Timer Verlauf",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Verwendete Minuten pro Tag",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Dynamic bar chart with real SharedPreferences values
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxMin = (focusStats.maxOrNull() ?: 0).coerceAtLeast(1)
                            
                            val daysOfWeek = (5 downTo 0).map { daysAgo ->
                                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
                                if (daysAgo == 0) "Heute" else when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                                    java.util.Calendar.MONDAY -> "Mo"
                                    java.util.Calendar.TUESDAY -> "Di"
                                    java.util.Calendar.WEDNESDAY -> "Mi"
                                    java.util.Calendar.THURSDAY -> "Do"
                                    java.util.Calendar.FRIDAY -> "Fr"
                                    java.util.Calendar.SATURDAY -> "Sa"
                                    java.util.Calendar.SUNDAY -> "So"
                                    else -> ""
                                }
                            }
                            
                            focusStats.forEachIndexed { idx, minutes ->
                                val fraction = minutes.toFloat() / maxMin.toFloat()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${minutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (idx == focusStats.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((fraction * 80).coerceAtLeast(6f).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (idx == focusStats.lastIndex) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = daysOfWeek[idx],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (idx == focusStats.lastIndex) FontWeight.Bold else FontWeight.Normal,
                                        color = if (idx == focusStats.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusTab(
    viewModel: TaskViewModel,
    isDark: Boolean
) {
    val configuredDuration by viewModel.focusTimerDurationSeconds.collectAsStateWithLifecycle()
    val timeLeft by viewModel.timerTimeLeftSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val progress = if (configuredDuration > 0) timeLeft.toFloat() / configuredDuration.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Bleibe fokussiert und arbeite deine Aufgaben ab.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Timer Duration Adjustment UI (only visible when not running)
        AnimatedVisibility(
            visible = !isRunning,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dauer einstellen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newTime = (configuredDuration - 300).coerceAtLeast(300)
                            viewModel.setFocusTimerDurationSeconds(newTime)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minus 5 Minuten",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = "${configuredDuration / 60} Min",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(
                        onClick = {
                            val newTime = (configuredDuration + 300).coerceAtMost(10800)
                            viewModel.setFocusTimerDurationSeconds(newTime)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Plus 5 Minuten",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Quick preset pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { mins ->
                        val isSelected = (configuredDuration / 60) == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    }
                                )
                                .clickable {
                                    viewModel.setFocusTimerDurationSeconds(mins * 60)
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${mins} Min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Huge glass timer disk
        Card(
            modifier = Modifier.size(260.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.35f else 0.90f),
                        Color.White.copy(alpha = if (isDark) 0.08f else 0.25f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.05f else 0.20f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.12f else 0.35f),
                                Color.White.copy(alpha = if (isDark) 0.02f else 0.10f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(230.dp),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                // Countdown text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isRunning) "Fokus aktiv" else "Bereit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause Button
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Pause" else "Starten",
                    fontWeight = FontWeight.Bold
                )
            }

            // Reset Button
            OutlinedButton(
                onClick = {
                    viewModel.resetTimer()
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Zurücksetzen"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zurücksetzen",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

