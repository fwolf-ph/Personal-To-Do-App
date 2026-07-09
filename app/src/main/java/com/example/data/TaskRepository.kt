package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompletion(task: Task) {
        val now = System.currentTimeMillis()
        if (!task.isCompleted) {
            // Marking as completed
            val updatedTask = task.copy(
                isCompleted = true,
                lastCompletedTimestamp = now
            )
            taskDao.updateTask(updatedTask)
        } else {
            // Unmarking as completed
            val updatedTask = task.copy(
                isCompleted = false,
                lastCompletedTimestamp = 0L
            )
            taskDao.updateTask(updatedTask)
        }
    }

    suspend fun checkAndResetDailyTasks() {
        val dailyTasks = taskDao.getDailyTasksDirect()
        for (task in dailyTasks) {
            val completedToday = DateUtils.isToday(task.lastCompletedTimestamp)
            
            if (task.isCompleted && !completedToday) {
                // Completed on a previous day, need to reset
                val resetTask = task.copy(
                    isCompleted = false,
                    lastCompletedTimestamp = 0L
                )
                taskDao.updateTask(resetTask)
            }
        }
    }
}
