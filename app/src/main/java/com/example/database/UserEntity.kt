package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // unique username for login
    val passwordHash: String, // password
    val fullName: String, // employee full name
    val designation: String, // software engineer, accountant, etc.
    val role: String, // "ADMIN" or "STAFF"
    val isActive: Boolean = true // true if active, can be disabled by admin
)
