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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class StaffMemberModel(
    val id: Long,
    val name: String,
    val role: String, // "Store Manager", "Accountant / Munim", "CNC Machine Operator", "Sales Executive", "Dispatch Assistant"
    val phone: String,
    val monthlySalary: Double,
    val advancePaid: Double,
    val attendanceStatus: String, // "PRESENT", "ABSENT", "ON_LEAVE"
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
    val totalAdvances = employees.sumOf { it.advancePaid }
    val presentCount = employees.count { it.attendanceStatus == "PRESENT" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddEmployeeClick,
                containerColor = ImperialNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Employee", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_employee")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvoryBackground)
                .padding(paddingValues)
                .testTag("employees_screen")
        ) {
            // Header
            Surface(
                color = WarmIvorySurface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Staff & Payroll Management",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Text(
                                text = "${employees.size} Staff Members • $presentCount Present Today",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Monthly CTC Payroll", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalMonthlyPayroll)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
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
                            focusedBorderColor = ImperialNavy,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BackgroundWhite,
                            unfocusedContainerColor = BackgroundWhite
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(employees.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true)
                }, key = { it.id }) { staff ->
                    EmployeeCard(
                        staff = staff,
                        onRecordAdvance = { onRecordAdvance(staff) },
                        onToggleAttendance = {
                            val newStatus = if (staff.attendanceStatus == "PRESENT") "ABSENT" else "PRESENT"
                            val index = employees.indexOfFirst { it.id == staff.id }
                            if (index != -1) {
                                employees[index] = staff.copy(attendanceStatus = newStatus)
                            }
                            onMarkAttendance(staff, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeCard(
    staff: StaffMemberModel,
    onRecordAdvance: () -> Unit,
    onToggleAttendance: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = staff.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = staff.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkInk
                        )
                        Text(
                            text = "${staff.role} • Joined ${staff.joinDate}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${staff.phone} • Aadhaar: ****${staff.aadhaarLast4}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    color = if (staff.attendanceStatus == "PRESENT") ForestGreen.copy(alpha = 0.15f) else TerracottaRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onToggleAttendance() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (staff.attendanceStatus == "PRESENT") Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (staff.attendanceStatus == "PRESENT") ForestGreen else TerracottaRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = staff.attendanceStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (staff.attendanceStatus == "PRESENT") ForestGreen else TerracottaRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Salary: ₹${MainViewModel.formatCurrencyPlain(staff.monthlySalary)}/mo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkInk
                    )
                    if (staff.advancePaid > 0) {
                        Text(
                            text = "Advance Drawn: ₹${MainViewModel.formatCurrencyPlain(staff.advancePaid)}",
                            fontSize = 11.sp,
                            color = TerracottaRed
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRecordAdvance,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Pay Advance / Salary", fontSize = 11.sp)
                }
            }
        }
    }
}
