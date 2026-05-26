package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, AttendanceEntity::class], version = 1, exportSchema = false)
abstract class OfficeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: OfficeDatabase? = null

        fun getInstance(context: Context): OfficeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfficeDatabase::class.java,
                    "smart_office_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate the Database inside a coroutine
                        val scope = CoroutineScope(Dispatchers.IO)
                        scope.launch {
                            val instanceRef = getInstance(context)
                            prepopulateDatabase(instanceRef.userDao(), instanceRef.attendanceDao())
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(userDao: UserDao, attendanceDao: AttendanceDao) {
            // Seed Users
            val adminId = userDao.insertUser(
                UserEntity(
                    username = "admin",
                    passwordHash = "admin123",
                    fullName = "Admin Supervisor",
                    designation = "System Administrator",
                    role = "ADMIN",
                    isActive = true
                )
            )

            val hemelId = userDao.insertUser(
                UserEntity(
                    username = "hemel",
                    passwordHash = "hemel123",
                    fullName = "Hemel Parveg",
                    designation = "Senior Android Developer",
                    role = "STAFF",
                    isActive = true
                )
            )

            val nirobId = userDao.insertUser(
                UserEntity(
                    username = "nirob",
                    passwordHash = "nirob123",
                    fullName = "Nirob Hasan",
                    designation = "HR Executive",
                    role = "STAFF",
                    isActive = true
                )
            )

            val pavelId = userDao.insertUser(
                UserEntity(
                    username = "pavel",
                    passwordHash = "pavel123",
                    fullName = "Pavel Ahmed",
                    designation = "Product Architect",
                    role = "STAFF",
                    isActive = true
                )
            )

            // Seed Yesterday's data: 2026-05-25
            val yesterday = "2026-05-25"
            attendanceDao.insertAttendance(
                AttendanceEntity(
                    userId = hemelId,
                    date = yesterday,
                    checkInTime = "09:12:00",
                    checkOutTime = "18:05:00",
                    status = "LATE",
                    workHours = "8h 53m",
                    notes = "Traffic at airport road"
                )
            )
            attendanceDao.insertAttendance(
                AttendanceEntity(
                    userId = nirobId,
                    date = yesterday,
                    checkInTime = "08:52:00",
                    checkOutTime = "17:01:00",
                    status = "PRESENT",
                    workHours = "8h 09m",
                    notes = null
                )
            )
            attendanceDao.insertAttendance(
                AttendanceEntity(
                    userId = pavelId,
                    date = yesterday,
                    checkInTime = null,
                    checkOutTime = null,
                    status = "ON_LEAVE",
                    workHours = null,
                    notes = "Emergency personal leave"
                )
            )

            // Seed Today's running check-ins: 2026-05-26
            val today = "2026-05-26"
            attendanceDao.insertAttendance(
                AttendanceEntity(
                    userId = hemelId,
                    date = today,
                    checkInTime = "08:48:00",
                    checkOutTime = null,
                    status = "PRESENT",
                    workHours = null,
                    notes = "On-time arrival"
                )
            )
            attendanceDao.insertAttendance(
                AttendanceEntity(
                    userId = nirobId,
                    date = today,
                    checkInTime = "09:25:00",
                    checkOutTime = null,
                    status = "LATE",
                    workHours = null,
                    notes = "Local bus delayed"
                )
            )
            // pavel is not check-in yet on 2026-05-26 (status count would be ABSENT or default)
        }
    }
}
