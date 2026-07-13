package com.example.ui

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DateUtils
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.ui.theme.AppThemeOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val prefs = application.getSharedPreferences("todo_settings", Context.MODE_PRIVATE)

    private val _focusTimerDurationSeconds = MutableStateFlow(prefs.getInt("focus_duration_seconds", 1500))
    val focusTimerDurationSeconds: StateFlow<Int> = _focusTimerDurationSeconds.asStateFlow()

    private val _timerTimeLeftSeconds = MutableStateFlow<Int?>(null)
    val timerTimeLeftSeconds: StateFlow<Int> = _timerTimeLeftSeconds
        .map { it ?: _focusTimerDurationSeconds.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500)

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    fun setFocusTimerDurationSeconds(seconds: Int) {
        prefs.edit().putInt("focus_duration_seconds", seconds).apply()
        _focusTimerDurationSeconds.value = seconds
        if (!_isTimerRunning.value) {
            _timerTimeLeftSeconds.value = seconds
            prefs.edit().putInt("timer_paused_left", -1).apply()
        }
    }

    private fun scheduleAlarm(triggerAtMillis: Long) {
        try {
            val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            if (alarmManager != null) {
                val intent = Intent(getApplication(), TimerReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm() {
        try {
            val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            if (alarmManager != null) {
                val intent = Intent(getApplication(), TimerReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerForegroundCompletionEffects() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            Toast.makeText(getApplication(), "Der Fokus-Timer ist abgelaufen! Gut gemacht!", Toast.LENGTH_LONG).show()
        }

        // Vibrate
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Play sound
        try {
            val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = android.media.RingtoneManager.getRingtone(getApplication(), notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTimerLoop(startTimestamp: Long, secondsAtStart: Int) {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            try {
                while (_isTimerRunning.value) {
                    val elapsed = (System.currentTimeMillis() - startTimestamp) / 1000
                    val timeLeft = maxOf(0, secondsAtStart - elapsed.toInt())
                    _timerTimeLeftSeconds.value = timeLeft
                    
                    val totalElapsed = secondsAtStart - timeLeft
                    val lastAccumulated = prefs.getInt("timer_accumulated_stats_seconds", 0)
                    val delta = totalElapsed - lastAccumulated
                    if (delta > 0) {
                        addFocusTime(delta)
                        prefs.edit().putInt("timer_accumulated_stats_seconds", totalElapsed).apply()
                    }

                    if (timeLeft <= 0) {
                        _isTimerRunning.value = false
                        _timerTimeLeftSeconds.value = _focusTimerDurationSeconds.value
                        prefs.edit()
                            .putBoolean("timer_is_running", false)
                            .putInt("timer_accumulated_stats_seconds", 0)
                            .putInt("timer_paused_left", -1)
                            .apply()
                        cancelAlarm()
                        triggerForegroundCompletionEffects()
                        break
                    }
                    delay(500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        val secondsLeft = _timerTimeLeftSeconds.value ?: _focusTimerDurationSeconds.value
        val startTime = System.currentTimeMillis()
        
        prefs.edit()
            .putBoolean("timer_is_running", true)
            .putLong("timer_start_timestamp", startTime)
            .putInt("timer_seconds_at_start", secondsLeft)
            .putInt("timer_accumulated_stats_seconds", 0)
            .putInt("timer_paused_left", -1)
            .apply()
            
        scheduleAlarm(startTime + secondsLeft * 1000L)
        startTimerLoop(startTime, secondsLeft)
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        cancelAlarm()

        val secondsAtStart = prefs.getInt("timer_seconds_at_start", 0)
        val lastAccumulated = prefs.getInt("timer_accumulated_stats_seconds", 0)
        val currentLeft = _timerTimeLeftSeconds.value
        if (currentLeft != null) {
            val totalElapsed = secondsAtStart - currentLeft
            val delta = totalElapsed - lastAccumulated
            if (delta > 0) {
                addFocusTime(delta)
            }
            prefs.edit()
                .putBoolean("timer_is_running", false)
                .putInt("timer_accumulated_stats_seconds", 0)
                .putInt("timer_paused_left", currentLeft)
                .apply()
        } else {
            prefs.edit()
                .putBoolean("timer_is_running", false)
                .putInt("timer_accumulated_stats_seconds", 0)
                .putInt("timer_paused_left", -1)
                .apply()
        }
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        cancelAlarm()

        val secondsAtStart = prefs.getInt("timer_seconds_at_start", 0)
        val lastAccumulated = prefs.getInt("timer_accumulated_stats_seconds", 0)
        val currentLeft = _timerTimeLeftSeconds.value
        if (currentLeft != null) {
            val totalElapsed = secondsAtStart - currentLeft
            val delta = totalElapsed - lastAccumulated
            if (delta > 0) {
                addFocusTime(delta)
            }
        }
        prefs.edit()
            .putBoolean("timer_is_running", false)
            .putInt("timer_accumulated_stats_seconds", 0)
            .putInt("timer_paused_left", -1)
            .apply()
        _timerTimeLeftSeconds.value = _focusTimerDurationSeconds.value
    }

    private val _focusStatsUpdateTrigger = MutableStateFlow(0)

    val lastSixDaysFocusMinutes: StateFlow<List<Int>> = _focusStatsUpdateTrigger
        .map {
            (5 downTo 0).map { daysAgo ->
                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
                val key = "focus_sec_${cal.get(java.util.Calendar.YEAR)}_${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                prefs.getInt(key, 0) / 60
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = List(6) { 0 }
        )

    fun addFocusTime(seconds: Int) {
        val cal = java.util.Calendar.getInstance()
        val key = "focus_sec_${cal.get(java.util.Calendar.YEAR)}_${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
        val currentSec = prefs.getInt(key, 0)
        prefs.edit().putInt(key, currentSec + seconds).apply()
        _focusStatsUpdateTrigger.value++
    }

    val allTasks: StateFlow<List<Task>>
    val lastSixDaysRoutineRates: StateFlow<List<Float>>

    private val _timeUntilMidnight = MutableStateFlow(DateUtils.getRemainingTimeUntilMidnight())
    val timeUntilMidnight: StateFlow<String> = _timeUntilMidnight.asStateFlow()

    private val _selectedTheme = MutableStateFlow(
        try {
            AppThemeOption.valueOf(prefs.getString("selected_theme", AppThemeOption.SAGE.name) ?: AppThemeOption.SAGE.name)
        } catch (e: Exception) {
            AppThemeOption.SAGE
        }
    )
    val selectedTheme: StateFlow<AppThemeOption> = _selectedTheme.asStateFlow()

    fun setThemeOption(themeOption: AppThemeOption) {
        prefs.edit().putString("selected_theme", themeOption.name).apply()
        _selectedTheme.value = themeOption
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        
        allTasks = repository.allTasks
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        lastSixDaysRoutineRates = allTasks
            .map { tasks ->
                val dailyTasks = tasks.filter { it.isDaily }
                val completedDaily = dailyTasks.count { it.isCompleted }
                val rate = if (dailyTasks.isNotEmpty()) {
                    completedDaily.toFloat() / dailyTasks.size.toFloat()
                } else {
                    0f
                }
                val todayCal = java.util.Calendar.getInstance()
                val todayKey = "routine_rate_${todayCal.get(java.util.Calendar.YEAR)}_${todayCal.get(java.util.Calendar.DAY_OF_YEAR)}"
                prefs.edit().putFloat(todayKey, rate).apply()

                (5 downTo 0).map { daysAgo ->
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
                    val key = "routine_rate_${cal.get(java.util.Calendar.YEAR)}_${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                    if (daysAgo == 0) {
                        rate
                    } else {
                        prefs.getFloat(key, 0f)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = List(6) { 0f }
            )

        // Run auto-reset check on start
        viewModelScope.launch {
            repository.checkAndResetDailyTasks()
        }

        // Periodically update the countdown and check for daily resets in case midnight passes
        viewModelScope.launch {
            while (true) {
                _timeUntilMidnight.value = DateUtils.getRemainingTimeUntilMidnight()
                repository.checkAndResetDailyTasks() // Also reset dynamically if midnight passes
                delay(30000L) // every 30 seconds
            }
        }

        // Restore timer state
        viewModelScope.launch {
            val isRunning = prefs.getBoolean("timer_is_running", false)
            val startTimestamp = prefs.getLong("timer_start_timestamp", 0L)
            val secondsAtStart = prefs.getInt("timer_seconds_at_start", 0)
            val pausedLeft = prefs.getInt("timer_paused_left", -1)

            if (isRunning && startTimestamp > 0L && secondsAtStart > 0) {
                val elapsed = (System.currentTimeMillis() - startTimestamp) / 1000
                if (elapsed >= secondsAtStart) {
                    // Timer finished in background
                    val lastAccumulated = prefs.getInt("timer_accumulated_stats_seconds", 0)
                    val finalDelta = maxOf(0, secondsAtStart - lastAccumulated)
                    if (finalDelta > 0) {
                        addFocusTime(finalDelta)
                    }
                    prefs.edit()
                        .putBoolean("timer_is_running", false)
                        .putInt("timer_accumulated_stats_seconds", 0)
                        .putInt("timer_paused_left", -1)
                        .apply()
                    _timerTimeLeftSeconds.value = _focusTimerDurationSeconds.value
                    _isTimerRunning.value = false
                } else {
                    // Timer is still running
                    val timeLeft = secondsAtStart - elapsed.toInt()
                    _timerTimeLeftSeconds.value = timeLeft
                    startTimerLoop(startTimestamp, secondsAtStart)
                }
            } else if (pausedLeft > 0) {
                _timerTimeLeftSeconds.value = pausedLeft
            } else {
                _timerTimeLeftSeconds.value = _focusTimerDurationSeconds.value
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        isDaily: Boolean
    ) {
        viewModelScope.launch {
            val list = allTasks.value.filter { it.isDaily == isDaily }
            val maxOrder = list.maxOfOrNull { it.displayOrder } ?: 0
            val task = Task(
                title = title,
                description = description,
                isDaily = isDaily,
                displayOrder = maxOrder + 1
            )
            repository.insert(task)
        }
    }

    fun reorderTasks(orderedList: List<Task>) {
        viewModelScope.launch {
            orderedList.forEachIndexed { index, task ->
                repository.update(task.copy(displayOrder = index))
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun forceResetDailyTasks() {
        viewModelScope.launch {
            // Allow user to manually reset in case they want a fresh start
            val dailyTasks = allTasks.value.filter { it.isDaily }
            for (task in dailyTasks) {
                val resetTask = task.copy(
                    isCompleted = false,
                    lastCompletedTimestamp = 0L
                )
                repository.update(resetTask)
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
