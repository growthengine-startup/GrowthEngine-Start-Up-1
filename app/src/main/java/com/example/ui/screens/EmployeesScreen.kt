package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class StaffMemberModel(
    val id: Long,
    val name: String,
    val role: String,
    val phone: String,
    val monthlySalary: Double,
    val advancePaid: Double,
    val attendanceStatus: String,
    val aadhaarLast4: String,
    val joinDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    onAddEmployeeClick: () -> Unit,
    onRecordAdvance: (StaffMemberModel) -> Unit,
    onMarkAttendance: (StaffMemberModel, String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val employees = remember {
        mutableStateListOf(
            StaffMemberModel(1, "Ramesh Shinde", "Senior Accountant (Munim)", "+91 98231 00291", 45000.0, 5000.0, "PRESENT", "4921", "Jan 2023"),
            StaffMemberModel(2, "Suresh Patil", "CNC Shop Floor Supervisor", "+91 97654 88123", 38000.0, 0.0, "PRESENT", "8820", "Aug 2023"),
            StaffMemberModel(3, "Ganesh Kulkarni", "Store & Inventory Manager", "+91 94220 33918", 32000.0, 2500.0, "PRESENT", "1104", "Feb 2024"),
            StaffMemberModel(4, "Pooja Deshmukh", "Billing & GST Executive", "+91 98810 55492", 28000.0, 0.0, "PRESENT", "7392", "May 2024"),
            StaffMemberModel(5, "Anil Jadhav", "Dispatch & Logistics Handler", "+91 99228 11940", 22000.0, 1000.0, "ON_LEAVE", "6629", "Nov 2024")
        )
    }

    val totalMonthlyPayroll = employees.sumOf { it.monthlySalary }
    val presentCount = employees.count { it.attendanceStatus == "PRESENT" }

    val filteredEmployees = employees.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddEmployeeClick,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                shape = RoundedCornerShape(12.dp),
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Employee", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_employee")
            )
        },
        containerColor = BackgroundWhite,
        modifier = Modifier.testTag("employees_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Surface(
                color = BackgroundWhite,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "HUMAN RESOURCES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Staff & Payroll Register",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${employees.size} Staff • $presentCount Present Today",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Monthly Payroll", fontSize = 9.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalMonthlyPayroll)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_employee_input"),
                        placeholder = { Text("Search employee by name, role or phone...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEmployees, key = { it.id }) { employee ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = GrowthEngineGoldContainer,
                                        shape = CircleShape,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = employee.name.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthEngineGoldDark,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = employee.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${employee.role} • Joined ${employee.joinDate}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    color = if (employee.attendanceStatus == "PRESENT") SuccessGreenContainer else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = employee.attendanceStatus,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (employee.attendanceStatus == "PRESENT") SuccessGreenDark else WarningAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Salary: ₹${MainViewModel.formatCurrencyPlain(employee.monthlySalary)}/mo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                if (employee.advancePaid > 0) {
                                    Text("Advance: ₹${MainViewModel.formatCurrencyPlain(employee.advancePaid)}", fontSize = 11.sp, color = ErrorRedDark, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Aadhaar: **** **** ${employee.aadhaarLast4}", fontSize = 10.sp, color = TextTertiary)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onRecordAdvance(employee) },
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Record Advance", fontSize = 11.sp, color = DarkInk)
                                    }

                                    Button(
                                        onClick = {
                                            val newStatus = if (employee.attendanceStatus == "PRESENT") "ON_LEAVE" else "PRESENT"
                                            onMarkAttendance(employee, newStatus)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Attendance", fontSize = 11.sp, color = Color.White)
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
