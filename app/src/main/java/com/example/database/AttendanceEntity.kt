package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: String, // YYYY-MM-DD
    val checkInTime: String?, // HH:mm:ss
    val checkOutTime: String?, // HH:mm:ss
    val status: String, // "PRESENT", "LATE", "ABSENT", "ON_LEAVE"
    val workHours: String?, // e.g. "8h 30m"
    val notes: String? = null // generic remarks
)
