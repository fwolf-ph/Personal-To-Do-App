package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isDaily: Boolean = false, // If true, it is a daily checklist item that resets
    val lastCompletedTimestamp: Long = 0L,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

