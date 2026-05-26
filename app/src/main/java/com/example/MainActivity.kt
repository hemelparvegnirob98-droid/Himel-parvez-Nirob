package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.AttendanceEntity
import com.example.database.UserEntity
import com.example.ui.AppScreen
import com.example.ui.DetailedAttendance
import com.example.ui.ExportHelper
import com.example.ui.OfficeViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val viewModel: OfficeViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Monitor success or global error notes to trigger Toast notifications
    LaunchedEffect(viewModel.successNotificationMessage) {
        viewModel.successNotificationMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.successNotificationMessage = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (viewModel.currentScreen) {
                AppScreen.LOGIN -> LoginScreen(viewModel)
                AppScreen.STAFF_DASHBOARD -> StaffDashboardScreen(viewModel)
                AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. LOGIN SCREEN
// -------------------------------------------------------------

// -------------------------------------------------------------
// BENTO GRID THEME COLOR CONSTANTS
// -------------------------------------------------------------
val BentoBg = Color(0xFFF7F9FC)
val BentoPrimary = Color(0xFF2563EB)      // Blue 600
val BentoIndigo = Color(0xFF4338CA)       // Indigo 700
val BentoSlate900 = Color(0xFF0F172A)     // Slate 900
val BentoBorder = Color(0xFFE2E8F0)       // Slate 200
val BentoTextMain = Color(0xFF1E293B)     // Slate 800
val BentoTextSec = Color(0xFF64748B)      // Slate 500

val BentoGreen = Color(0xFF10B981)        // Emerald 500
val BentoGreenLight = Color(0xFFECFDF5)   // Emerald 50
val BentoOrange = Color(0xFFF97316)       // Orange 500
val BentoOrangeLight = Color(0xFFFFEDD5)  // Orange 50
val BentoRed = Color(0xFFEF4444)          // Red 500
val BentoRedLight = Color(0xFFFEE2E2)     // Red 50
val BentoBlueLight = Color(0xFFEFF6FF)    // Blue 50

@Composable
fun LoginScreen(viewModel: OfficeViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header as a Bento element
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(BentoBlueLight, shape = RoundedCornerShape(18.dp))
                .border(1.5.dp, BentoBorder, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Shield Admin Lock Logo",
                tint = BentoPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Small tracking accent category
        Text(
            text = "ADMIN DASHBOARD",
            color = BentoPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Text(
            text = "Smart Login Pro",
            color = BentoSlate900,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "অফিস উপস্থিতি ও অ্যাকাউন্ট ম্যানেজমেন্ট সিস্টেম 💼",
            color = BentoTextSec,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Bento-style Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .border(BorderStroke(1.dp, BentoBorder), shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "লগইন করুন / Staff Access",
                    color = BentoSlate900,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 18.dp)
                )

                // Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("ব্যবহারকারীর নাম / Username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = BentoTextSec
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("পাসওয়ার্ড / Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Icon",
                            tint = BentoTextSec
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Search else Icons.Default.Lock,
                                contentDescription = "Toggle visibility",
                                tint = BentoTextSec
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                // Error notes
                viewModel.loginErrorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = BentoRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bento themed Blue Accent button
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "প্রবেশ করুন / Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bento formatted credential block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BentoBlueLight, shape = RoundedCornerShape(16.dp))
                        .border(1.dp, BentoBorder, shape = RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "🔑 DEMO ACCESS ACCOUNT SAMPLES",
                            fontSize = 10.sp,
                            color = BentoPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("ADMIN PANEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoTextSec)
                                Text("admin / admin123", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("STAFF PORTAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoTextSec)
                                Text("hemel / hemel123", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. STAFF WORKSPACE & ATTENDANCE DASHBOARD
// -------------------------------------------------------------

@Composable
fun StaffDashboardScreen(viewModel: OfficeViewModel) {
    val user = viewModel.currentUser ?: return
    val todayRecord by viewModel.todayUserRecord.collectAsStateWithLifecycle()
    val history by viewModel.currentUserHistory.collectAsStateWithLifecycle()

    var showLockInfoAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // Staff Dashboard Top Panel Header (Bento Styled White Header Area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, BentoBorder)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MEMBER PORTAL • মোবাইল উপস্থিতি",
                        color = BentoPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.fullName,
                        color = BentoSlate900,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${user.designation} | STAFF",
                        color = BentoTextSec,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .background(BentoBg, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, BentoBorder, shape = RoundedCornerShape(12.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out",
                        tint = BentoSlate900
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant Bento Gradient Card for Today's attendance tracker
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), spotColor = BentoPrimary.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(colors = listOf(BentoPrimary, BentoIndigo)))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TODAY'S PUNCH CARD",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "আজকের উপস্থিতি ট্র্যাকার",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                val dateToday = viewModel.getCurrentDateString()
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = dateToday,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Three floating semi-transparent inner cells (glassmorphism bento sub-blocks)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Cell 1: Check In
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("⏰ প্রবেশ (In)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = todayRecord?.checkInTime ?: "--:--:--",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Cell 2: Check Out
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🚪 প্রস্থান (Out)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = todayRecord?.checkOutTime ?: "--:--:--",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Cell 3: Status
                                val statusText = todayRecord?.status ?: "PENDING"
                                val pillBg = when (statusText) {
                                    "PRESENT" -> Color(0xFF10B981)
                                    "LATE" -> Color(0xFFF59E0B)
                                    "ON_LEAVE" -> Color(0xFF3B82F6)
                                    else -> Color.White.copy(alpha = 0.12f)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(pillBg, shape = RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🏷️ অবস্থা State", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = statusText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Work Hours tracker inside gradient block
                            if (todayRecord?.workHours != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Time Tracking", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "আজকের মোট অফিস কর্ম সময়: ${todayRecord?.workHours}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Bento pair buttons side-by-side
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hasCheckedIn = todayRecord?.checkInTime != null
                    val hasCheckedOut = todayRecord?.checkOutTime != null

                    // Check In block
                    Button(
                        onClick = { viewModel.staffCheckIn() },
                        enabled = !hasCheckedIn,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .graphicsLayer {
                                shadowElevation = if (!hasCheckedIn) 4f else 0f
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoGreen,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.35f),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "CheckIn")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("চেক-ইন (In)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Check Out block
                    Button(
                        onClick = { viewModel.staffCheckOut() },
                        enabled = hasCheckedIn && !hasCheckedOut,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .graphicsLayer {
                                shadowElevation = if (hasCheckedIn && !hasCheckedOut) 4f else 0f
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoRed,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.35f),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "CheckOut")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("চেক-আউট (Out)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Beautiful Warning / Policy Block matching bento box styling exactly
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLockInfoAlert = true }
                        .border(1.dp, Color(0xFFFDBA74), shape = RoundedCornerShape(24.dp)), // Orange 300
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoOrangeLight) // Orange 50 background
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD8A8), shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Lock Info Policy Warning",
                                tint = BentoOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "নিরাপত্তা নীতি নির্দেশিকা / Security Guidelines",
                                color = Color(0xFFC2410C), // Dark Orange 700 text
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "কর্মীরা নিজেরা পাসওয়ার্ড পরিবর্তন করতে পারবে না। পাসওয়ার্ড রিসেটের প্রয়োজনেই অফিস অ্যাডমিনের সাথে যোগাযোগ করুন।",
                                color = Color(0xFF9A3412), // Orange 800 subtext
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Header for Staff Log history (as Bento Title)
            item {
                Text(
                    text = "উপস্থিতি ইতিহাস বুক / Attendance Ledger",
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900
                )
            }

            // Attendance list
            if (history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorder, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Empty", tint = Color.LightGray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("কোনো রেকর্ড পাওয়া যায়নি!", color = BentoTextSec, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            } else {
                items(history) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorder, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = entry.date,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ In: ${entry.checkInTime ?: "--:--:--"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BentoTextSec
                                    )
                                    Text(
                                        text = "🚪 Out: ${entry.checkOutTime ?: "--:--:--"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BentoTextSec
                                    )
                                }
                                if (entry.workHours != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(BentoBlueLight, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "⏳ কর্মকাল Duration: ${entry.workHours}",
                                                fontSize = 11.sp,
                                                color = BentoPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Colored Chip Status
                            val statusBg = when (entry.status) {
                                "PRESENT" -> BentoGreenLight
                                "LATE" -> BentoOrangeLight
                                "ON_LEAVE" -> BentoBlueLight
                                else -> Color.LightGray.copy(alpha = 0.15f)
                            }
                            val statusColor = when (entry.status) {
                                "PRESENT" -> BentoGreen
                                "LATE" -> BentoOrange
                                "ON_LEAVE" -> BentoPrimary
                                else -> BentoTextSec
                            }

                            Box(
                                modifier = Modifier
                                    .background(statusBg, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, statusColor.copy(alpha = 0.25f), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = entry.status,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLockInfoAlert) {
        AlertDialog(
            onDismissRequest = { showLockInfoAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = BentoOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নিরাপত্তা নীতি / Security Rule", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "পাসওয়ার্ড পরিবর্তনের উইন্ডো কর্মীদের জন্য অবরুদ্ধ। এটি করপোরেট লগইন রিপোর্টের অখণ্ডতা সুরক্ষার জন্য করা হয়েছে। যদি আপনার পাসওয়ার্ড পরিবর্তন করা অত্যন্ত জরুরি হয়, তবে অ্যাডমিনের কাছে রিকোয়েস্ট পেশ করুন।"
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLockInfoAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("টিক আছে / Got It")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 3. ADMIN PANEL CONTROL CENTRAL
// -------------------------------------------------------------

@Composable
fun AdminDashboardScreen(viewModel: OfficeViewModel) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val detailedAttendance by viewModel.detailedAttendanceList.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog flags
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var selectedUserForEdit by remember { mutableStateOf<UserEntity?>(null) }
    var selectedUserForResetPassword by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // Administrative Bento styled white header area with profile avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, BentoBorder)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ADMIN DASHBOARD CENTRAL",
                        color = BentoPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Smart Login Pro",
                        color = BentoSlate900,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar bubble 'A'
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(BentoBlueLight, shape = CircleShape)
                            .border(1.5.dp, BentoPrimary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = BentoPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .background(BentoBg, shape = RoundedCornerShape(10.dp))
                            .border(1.dp, BentoBorder, shape = RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = BentoSlate900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Live stats counter bar (Bento styled grid)
        val staffCount = users.filter { it.role == "STAFF" }.size
        val todayStr = viewModel.getCurrentDateString()
        val todayRecords = detailedAttendance.filter { it.record.date == todayStr }
        val presentToday = todayRecords.filter { it.record.status == "PRESENT" || it.record.status == "LATE" }.size
        val lateCount = todayRecords.filter { it.record.status == "LATE" }.size
        val absentCount = (staffCount - presentToday).coerceAtLeast(0)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("স্টাফ / Accounts", staffCount.toString(), BentoPrimary, Modifier.weight(1f))
            StatCard("উপস্থিত / Present", presentToday.toString(), BentoGreen, Modifier.weight(1f))
            StatCard("দেরি / Late", lateCount.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
            StatCard("অনুপস্থিত / Absent", absentCount.toString(), BentoRed, Modifier.weight(1f))
        }

        // Action Options Selector Tabs (Bento Segmented Style TabRow)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = BentoSlate900,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BentoPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("কর্মী বাহিনী / Staff", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("রেকর্ড লগ / Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("এক্সেল ডাউনলোড / Export", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        // Dynamic Panel switcher
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> StaffDirectoryPanel(
                    users = users.filter { it.role == "STAFF" },
                    onEditClick = { selectedUserForEdit = it },
                    onResetPasswordClick = { selectedUserForResetPassword = it },
                    onAddUserTrigger = { showAddStaffDialog = true }
                )
                1 -> AttendanceLogsPanel(detailedAttendance)
                2 -> ExportExcelPanel(viewModel)
            }
        }
    }

    // ------------------------------------
    // DIALOGS & OVERLAYS
    // ------------------------------------

    // 1. ADD NEW STAFF DIALOG
    if (showAddStaffDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newFullName by remember { mutableStateOf("") }
        var newDesignation by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = {
                Text(
                    "নতুন স্টাফ যোগ করুন / Add Staff member",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B2A4A)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newFullName,
                        onValueChange = { newFullName = it },
                        label = { Text("স্টাফের পূর্ণ নাম / Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesignation,
                        onValueChange = { newDesignation = it },
                        label = { Text("পদবী / Designation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("ইউজার নেম / Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("লগইন পাসওয়ার্ড / Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewStaff(newUsername, newPassword, newFullName, newDesignation)
                        showAddStaffDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5))
                ) {
                    Text("সংরক্ষণ করুন / Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text("বাতিল / Cancel")
                }
            }
        )
    }

    // 2. EDIT STAFF DETAILS DIALOG
    selectedUserForEdit?.let { targetUser ->
        var editFullName by remember { mutableStateOf(targetUser.fullName) }
        var editDesignation by remember { mutableStateOf(targetUser.designation) }
        var editIsActive by remember { mutableStateOf(targetUser.isActive) }

        AlertDialog(
            onDismissRequest = { selectedUserForEdit = null },
            title = {
                Text(
                    "স্টাফ বিবরণ এডিট / Edit Staff Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B2A4A)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("পূর্ণ নাম / Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesignation,
                        onValueChange = { editDesignation = it },
                        label = { Text("পদবী / Designation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable { editIsActive = !editIsActive },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editIsActive,
                            onCheckedChange = { editIsActive = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00ADB5))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("অ্যাকাউন্ট সচল আছে / Account Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("অনুমতি বন্ধ করতে আনচেক করুন (ম্যানেজড স্টাফ)", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editStaffDetails(targetUser.id, editFullName, editDesignation, editIsActive)
                        selectedUserForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5))
                ) {
                    Text("আপডেট আপডেট / Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForEdit = null }) {
                    Text("বন্ধ / Close")
                }
            }
        )
    }

    // 3. ADMIN PASSWORD RESET OVERVIEW DIALOG (Target Policy Constraint)
    selectedUserForResetPassword?.let { targetUser ->
        var newSecretPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedUserForResetPassword = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Password Security Reset", tint = Color(0xFFE74C3C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("পাসওয়ার্ড রিসেট করুন / Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "স্টাফ: ${targetUser.fullName} (${targetUser.username})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B2A4A)
                    )
                    Text(
                        text = "কর্মীরা নিজেরা পাসওয়ার্ড পরিবর্তন করতে পারবে না। এখানে নতুন নতুন পাসওয়ার্ড টাইপ করে সরাসরি সেট করে দিন।",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newSecretPassword,
                        onValueChange = { newSecretPassword = it },
                        label = { Text("নতুন পাসওয়ার্ড / New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStaffPassword(targetUser.id, newSecretPassword)
                        selectedUserForResetPassword = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Text("রিসেট করুন / Reset Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForResetPassword = null }) {
                    Text("বাতিল / Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// HELPER ADMIN PANELS COMPONENTS
// -------------------------------------------------------------

@Composable
fun StaffDirectoryPanel(
    users: List<UserEntity>,
    onEditClick: (UserEntity) -> Unit,
    onResetPasswordClick: (UserEntity) -> Unit,
    onAddUserTrigger: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "কর্মীবাহিনীর তালিকা / Employee Directory",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900
            )

            // Add Staff Premium Button
            Button(
                onClick = onAddUserTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("যোগ করুন", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, BentoBorder, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("কোনো কর্মী অ্যাকাউন্ট যোগ করা হয়নি।", color = BentoTextSec, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { staff ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorder, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = staff.fullName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⚡ পদবী: ${staff.designation}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "👤 ইউজার: ${staff.username}  | পাসওয়ার্ড হ্যাশ: ${staff.passwordHash}",
                                        fontSize = 11.sp,
                                        color = BentoTextSec
                                    )
                                }

                                // Active Status Badge
                                val badgeBg = if (staff.isActive) BentoGreenLight else BentoRedLight
                                val badgeColor = if (staff.isActive) BentoGreen else BentoRed
                                val badgeText = if (staff.isActive) "ACTIVE" else "BLOCKED"
                                Box(
                                    modifier = Modifier
                                        .background(badgeBg, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BentoBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Action items: Edit and Direct Reset Secrets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reset Password trigger
                                TextButton(
                                    onClick = { onResetPasswordClick(staff) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BentoRed),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password reset icon", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("পাসওয়ার্ড রিসেট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Edit details trigger
                                Button(
                                    onClick = { onEditClick(staff) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoBg, contentColor = BentoSlate900),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .border(1.dp, BentoBorder, shape = RoundedCornerShape(10.dp)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit icon", modifier = Modifier.size(13.dp), tint = BentoSlate900)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("এডিট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
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
fun AttendanceLogsPanel(records: List<DetailedAttendance>) {
    Column {
        Text(
            text = "উপস্থিতি ট্র্যাকিং / Global Attendance Registry",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = BentoSlate900
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, BentoBorder, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("আজ বা অতীতে কোনো লগ পাওয়া যায়নি।", color = BentoTextSec, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { detail ->
                    val rec = detail.record
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorder, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = detail.userName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900
                                    )
                                    Text(
                                        text = "${detail.userDesignation} (u: ${detail.username})",
                                        fontSize = 11.sp,
                                        color = BentoTextSec
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(BentoBg, shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🗓️ তারিখ: ${rec.date}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate900
                                        )
                                    }
                                }

                                // Status indicator
                                val statusBg = when (rec.status) {
                                    "PRESENT" -> BentoGreenLight
                                    "LATE" -> BentoOrangeLight
                                    "ON_LEAVE" -> BentoBlueLight
                                    else -> Color.LightGray.copy(alpha = 0.15f)
                                }
                                val statusColor = when (rec.status) {
                                    "PRESENT" -> BentoGreen
                                    "LATE" -> BentoOrange
                                    "ON_LEAVE" -> BentoPrimary
                                    else -> BentoTextSec
                                }

                                Box(
                                    modifier = Modifier
                                        .background(statusBg, shape = RoundedCornerShape(10.dp))
                                        .border(1.dp, statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = rec.status,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BentoBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    Column {
                                        Text("⏰ Check In", fontSize = 11.sp, color = BentoTextSec, fontWeight = FontWeight.Bold)
                                        Text(rec.checkInTime ?: "--:--:--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                                    }
                                    Column {
                                        Text("🚪 Check Out", fontSize = 11.sp, color = BentoTextSec, fontWeight = FontWeight.Bold)
                                        Text(rec.checkOutTime ?: "--:--:--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                                    }
                                }

                                if (rec.workHours != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("⌛ Work Duration", fontSize = 11.sp, color = BentoTextSec, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .background(BentoBlueLight, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = rec.workHours,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = BentoPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            rec.notes?.let { note ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BentoBg, shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "📝 মন্তব্য Note: $note",
                                        fontSize = 11.sp,
                                        color = BentoSlate900,
                                        fontWeight = FontWeight.Medium
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
fun ExportExcelPanel(viewModel: OfficeViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorder, shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEAFAF1), shape = RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFD4EFDF), shape = RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Excel Sheets Icon",
                    tint = Color(0xFF107C41), // Proper Excel Forest Green
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "এক্সেল রিপোর্ট ডাউনলোড ও শেয়ার",
                color = BentoSlate900,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Spreadsheet (.csv) Core Engine",
                color = BentoTextSec,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "সকল কর্মীদের তথ্য এবং দৈনিক উপস্থিতির বিস্তারিত রেকর্ড এক ক্লিকেই মাইক্রোসফট এক্সেল বা গুগল শীটে ওপেন করার জন্য কমা-ডিলিমিটেড ফাইলে এক্সপোর্ট করুন।",
                color = BentoSlate900,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Export Attendance logs Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val csv = viewModel.getAttendanceReportCsv()
                        val rawDate = viewModel.getCurrentDateString().replace("-", "")
                        ExportHelper.exportToExcel(
                            context = context,
                            fileName = "Attendance_Report_$rawDate.csv",
                            csvContent = csv
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)), // Classic Excel Green
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = "Excel Icon")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "উপস্থিতি রিপোর্ট এক্সেল ডাউনলোড (Attendance Excel)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Export Staff accounts Directory Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val csv = viewModel.getStaffRosterCsv()
                        ExportHelper.exportToExcel(
                            context = context,
                            fileName = "Staff_Directory_List.csv",
                            csvContent = csv
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, BentoBorder, shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = BentoBg, contentColor = BentoSlate900),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "Roster Icon", tint = BentoSlate900)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "স্টাফ বা কর্মী তালিকা এক্সেল ডাউনলোড (Roster Excel)",
                        color = BentoSlate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BentoBorder)
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = "ফাইলটি সরাসরি Microsoft Excel, Google Sheets, LibreOffice অথবা WPS Office এ সম্পূর্ণ কলাম বিন্যাস সহ স্বয়ংক্রিয়ভাবে ওপেন হবে।",
                fontSize = 11.sp,
                color = BentoTextSec,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}


@Composable
fun StatCard(label: String, count: String, colorAccent: Color, modifier: Modifier = Modifier) {
    val icon = when {
        label.contains("স্টাফ") || label.contains("Accounts") || label.contains("Staff") -> Icons.Default.Person
        label.contains("উপস্থিত") || label.contains("Present") -> Icons.Default.Check
        label.contains("দেরি") || label.contains("Late") -> Icons.Default.Warning
        else -> Icons.Default.Close
    }
    Card(
        modifier = modifier
            .border(BorderStroke(1.dp, BentoBorder), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colorAccent.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colorAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BentoSlate900,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextSec,
                textAlign = TextAlign.Center
            )
        }
    }
}

