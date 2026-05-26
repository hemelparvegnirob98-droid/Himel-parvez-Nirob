package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getRecordForUserAndDate(userId: Long, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY date DESC")
    fun getRecordsForUser(userId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    suspend fun getAllRecordsSync(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getRecordsForDateSync(date: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getRecordsForDateFlow(date: String): Flow<List<AttendanceEntity>>
}
