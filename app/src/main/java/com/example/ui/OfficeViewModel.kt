package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AttendanceDao
import com.example.database.AttendanceEntity
import com.example.database.OfficeDatabase
import com.example.database.UserDao
import com.example.database.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    LOGIN,
    STAFF_DASHBOARD,
    ADMIN_DASHBOARD
}

class OfficeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = OfficeDatabase.getInstance(application)
    private val userDao: UserDao = db.userDao()
    private val attendanceDao: AttendanceDao = db.attendanceDao()

    // Screen navigation state
    var currentScreen by mutableStateOf(AppScreen.LOGIN)
        private set

    // Authenticated user state
    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    // UI operational feedback
    var loginErrorMessage by mutableStateOf<String?>(null)
    var successNotificationMessage by mutableStateOf<String?>(null)

    // Flow for all users
    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow for all attendance registry
    val allAttendance: StateFlow<List<AttendanceEntity>> = attendanceDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combines Attendance and Users to show a detailed view for Admin dashboard
    val detailedAttendanceList: StateFlow<List<DetailedAttendance>> = combine(allAttendance, allUsers) { records, listUsers ->
        records.map { record ->
            val user = listUsers.find { it.id == record.userId }
            DetailedAttendance(
                record = record,
                userName = user?.fullName ?: "Unknown User",
                userDesignation = user?.designation ?: "Employee",
                username = user?.username ?: "deleted"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live state flow for today's user attendance
    private val _todayUserRecord = MutableStateFlow<AttendanceEntity?>(null)
    val todayUserRecord: StateFlow<AttendanceEntity?> = _todayUserRecord.asStateFlow()

    // History flow of attendance for currently logged-in user
    private val _currentUserHistory = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val currentUserHistory: StateFlow<List<AttendanceEntity>> = _currentUserHistory.asStateFlow()

    init {
        // Automatically check if database has any users. If a fresh setup is detected, Room's DB Callback
        // handles seeding, but we trigger a simple verification query to wake up the DB.
        viewModelScope.launch {
            userDao.getUserCount()
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        loginErrorMessage = null
        successNotificationMessage = null
    }

    fun login(usernameInput: String, passwordInput: String) {
        val uName = usernameInput.trim()
        val pHash = passwordInput.trim()

        if (uName.isEmpty() || pHash.isEmpty()) {
            loginErrorMessage = "Username and password cannot be empty"
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByUsername(uName)
            if (user == null) {
                loginErrorMessage = "User direct mismatch. Account does not exist."
                return@launch
            }

            if (!user.isActive) {
                loginErrorMessage = "Access Denied. Your account has been disabled by Administrator."
                return@launch
            }

            if (user.passwordHash == pHash) {
                currentUser = user
                loginErrorMessage = null
                if (user.role == "ADMIN") {
                    navigateTo(AppScreen.ADMIN_DASHBOARD)
                } else {
                    loadStaffData(user)
                    navigateTo(AppScreen.STAFF_DASHBOARD)
                }
            } else {
                loginErrorMessage = "Invalid username or password. Try again."
            }
        }
    }

    fun logout() {
        currentUser = null
        _todayUserRecord.value = null
        _currentUserHistory.value = emptyList()
        navigateTo(AppScreen.LOGIN)
    }

    private fun loadStaffData(user: UserEntity) {
        viewModelScope.launch {
            // Read today's date in yyyy-MM-dd
            val todayDate = getCurrentDateString()
            val todayRecord = attendanceDao.getRecordForUserAndDate(user.id, todayDate)
            _todayUserRecord.value = todayRecord

            // Connect historical log
            attendanceDao.getRecordsForUser(user.id).collect { history ->
                _currentUserHistory.value = history
            }
        }
    }

    fun staffCheckIn() {
        val user = currentUser ?: return
        val todayStr = getCurrentDateString()
        val timeStr = getCurrentTimeString()

        viewModelScope.launch {
            val existing = attendanceDao.getRecordForUserAndDate(user.id, todayStr)
            if (existing != null) {
                successNotificationMessage = "Already checked-in today!"
                return@launch
            }

            // Determine if LATE (Standard starting hours: 09:00:00 AM)
            val isLate = isAfterStartingTime(timeStr, 9, 0)
            val computedStatus = if (isLate) "LATE" else "PRESENT"

            val newRecord = AttendanceEntity(
                userId = user.id,
                date = todayStr,
                checkInTime = timeStr,
                checkOutTime = null,
                status = computedStatus,
                workHours = null,
                notes = if (isLate) "Late arrival (Checked-in at $timeStr)" else "On-time"
            )

            attendanceDao.insertAttendance(newRecord)
            _todayUserRecord.value = newRecord
            loadStaffData(user)
            successNotificationMessage = "Successfully checked-in at $timeStr"
        }
    }

    fun staffCheckOut() {
        val user = currentUser ?: return
        val todayStr = getCurrentDateString()
        val timeStr = getCurrentTimeString()

        viewModelScope.launch {
            val record = attendanceDao.getRecordForUserAndDate(user.id, todayStr)
            if (record == null) {
                successNotificationMessage = "No check-in record found for today!"
                return@launch
            }

            if (record.checkOutTime != null) {
                successNotificationMessage = "Already checked-out today!"
                return@launch
            }

            // Calculate hours between check-in and check-out
            val checkInStr = record.checkInTime ?: "09:00:00"
            val workHoursFormatted = calculateDuration(checkInStr, timeStr)

            val updatedRecord = record.copy(
                checkOutTime = timeStr,
                workHours = workHoursFormatted
            )

            attendanceDao.updateAttendance(updatedRecord)
            _todayUserRecord.value = updatedRecord
            loadStaffData(user)
            successNotificationMessage = "Successfully checked-out at $timeStr ($workHoursFormatted)"
        }
    }

    // ADMINISTRATIVE SERVICES

    fun addNewStaff(usernameInput: String, passwordInput: String, fullNameInput: String, designationInput: String) {
        val uname = usernameInput.trim().lowercase(Locale.ROOT)
        val pass = passwordInput.trim()
        val name = fullNameInput.trim()
        val desig = designationInput.trim()

        if (uname.isEmpty() || pass.isEmpty() || name.isEmpty() || desig.isEmpty()) {
            successNotificationMessage = "Error: All fields are mandatory!"
            return
        }

        viewModelScope.launch {
            val exists = userDao.getUserByUsername(uname)
            if (exists != null) {
                successNotificationMessage = "Error: Username '$uname' is already occupied!"
                return@launch
            }

            val newUser = UserEntity(
                username = uname,
                passwordHash = pass,
                fullName = name,
                designation = desig,
                role = "STAFF",
                isActive = true
            )

            userDao.insertUser(newUser)
            successNotificationMessage = "Successfully added new staff '$name'!"
        }
    }

    fun editStaffDetails(userId: Long, newFullName: String, newDesignation: String, newStatus: Boolean) {
        viewModelScope.launch {
            val user = userDao.getUserById(userId) ?: return@launch
            val updatedUser = user.copy(
                fullName = newFullName.trim(),
                designation = newDesignation.trim(),
                isActive = newStatus
            )
            userDao.updateUser(updatedUser)
            successNotificationMessage = "Successfully updated details for ${user.fullName}!"
        }
    }

    /**
     * Direct Admin Password Reset Service
     * Staff cannot edit self password; Admin controls it here.
     */
    fun resetStaffPassword(userId: Long, newPasswordRaw: String) {
        val pass = newPasswordRaw.trim()
        if (pass.isEmpty()) {
            successNotificationMessage = "Error: Password cannot be empty!"
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(passwordHash = pass)
                userDao.updateUser(updatedUser)
                successNotificationMessage = "Password successfully reset for ${user.fullName}!"
            }
        }
    }

    // EXCEL / CSV REPORT GENERATION LOGIC

    suspend fun getAttendanceReportCsv(): String {
        val records = attendanceDao.getAllRecordsSync()
        val users = userDao.getAllSync()

        val csvBuilder = StringBuilder()
        // CSV Header (with Excel compatibility UTF-8 Byte Order Mark if required, but Standard CSV text works great)
        csvBuilder.append("Date,Employee Name,Username,Designation,Check In,Check Out,Status,Work Hours,Notes\n")

        for (rec in records) {
            val employee = users.find { it.id == rec.userId }
            val name = escapeCsv(employee?.fullName ?: "Deleted Staff")
            val uName = escapeCsv(employee?.username ?: "deleted")
            val desig = escapeCsv(employee?.designation ?: "N/A")
            val date = escapeCsv(rec.date)
            val cIn = escapeCsv(rec.checkInTime ?: "-")
            val cOut = escapeCsv(rec.checkOutTime ?: "-")
            val status = escapeCsv(rec.status)
            val hours = escapeCsv(rec.workHours ?: "-")
            val notes = escapeCsv(rec.notes ?: "-")

            csvBuilder.append("$date,$name,$uName,$desig,$cIn,$cOut,$status,$hours,$notes\n")
        }
        return csvBuilder.toString()
    }

    suspend fun getStaffRosterCsv(): String {
        val users = userDao.getAllSync()

        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Full Name,Username,Role,Designation,Account Status\n")

        for (u in users) {
            val uid = u.id
            val name = escapeCsv(u.fullName)
            val uname = escapeCsv(u.username)
            val role = escapeCsv(u.role)
            val desig = escapeCsv(u.designation)
            val status = if (u.isActive) "ACTIVE" else "DISABLED"

            csvBuilder.append("$uid,$name,$uname,$role,$desig,$status\n")
        }
        return csvBuilder.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            str = str.replace("\"", "\"\"")
            str = "\"$str\""
        }
        return str
    }

    // TIME & UTILITY CALCULATORS

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun isAfterStartingTime(timeString: String, hourLimit: Int, minuteLimit: Int): Boolean {
        return try {
            val parts = timeString.split(":")
            val hour = parts[0].toInt()
            val min = parts[1].toInt()
            if (hour > hourLimit) {
                true
            } else if (hour == hourLimit) {
                min > minuteLimit
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateDuration(inTime: String, outTime: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateIn = format.parse(inTime) ?: return "-"
            val dateOut = format.parse(outTime) ?: return "-"

            var diff = dateOut.time - dateIn.time
            if (diff < 0) {
                // handles shifts wrapping over midnight
                val c = Calendar.getInstance()
                c.time = dateOut
                c.add(Calendar.DATE, 1)
                diff = c.time.time - dateIn.time
            }

            val totalMinutes = diff / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            "${hours}h ${minutes}m"
        } catch (e: Exception) {
            "N/A"
        }
    }
}

data class DetailedAttendance(
    val record: AttendanceEntity,
    val userName: String,
    val userDesignation: String,
    val username: String
)
