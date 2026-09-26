package com.example.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.admin.screens.AdminAuthGateScreen
import com.example.ui.components.AdminDrawerContent
import com.example.ui.components.AdminTopBar
import com.example.ui.screens.AdminSubTab
import com.example.ui.screens.AdminSystemScreen
import com.example.ui.theme.GrowthEngineTheme
import com.example.ui.theme.ImperialNavy
import kotlinx.coroutines.launch

class AdminActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GrowthEngineTheme {
                val context = LocalContext.current
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                var currentSubTab by remember { mutableStateOf(AdminSubTab.OVERVIEW) }

                val isAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
                val currentAdminEmail by viewModel.currentAdminEmail.collectAsStateWithLifecycle()
                val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
                val adminRole by viewModel.adminRole.collectAsStateWithLifecycle()

                // Real-time Data Streams
                val businesses by viewModel.businesses.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val aiLogs by viewModel.aiLogs.collectAsStateWithLifecycle()
                val globalAiMetrics by viewModel.globalAiMetrics.collectAsStateWithLifecycle()
                val broadcasts by viewModel.broadcasts.collectAsStateWithLifecycle()
                val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
                val supportTickets by viewModel.supportTickets.collectAsStateWithLifecycle()
                val systemConfig by viewModel.systemConfig.collectAsStateWithLifecycle()

                if (!isAuthenticated) {
                    AdminAuthGateScreen(
                        authorizedEmail = viewModel.authorizedAdminEmail,
                        errorMessage = authError,
                        onGoogleAdminLogin = { email ->
                            viewModel.authenticateWithGoogle(email)
                        },
                        onServiceKeyLogin = { key, email ->
                            viewModel.authenticateWithServiceKey(key, email)
                        },
                        onClearError = {
                            viewModel.clearAuthError()
                        },
                        onExitAdmin = {
                            finish()
                        }
                    )
                } else {
                    BackHandler(enabled = drawerState.isOpen) {
                        if (drawerState.isOpen) {
                            coroutineScope.launch { drawerState.close() }
                        } else {
                            finish()
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AdminDrawerContent(
                                adminEmail = currentAdminEmail,
                                activeTab = currentSubTab.name,
                                onSelectAdminSection = { sectionName ->
                                    try {
                                        currentSubTab = AdminSubTab.valueOf(sectionName)
                                    } catch (e: Exception) {
                                        // Default fallback
                                    }
                                    coroutineScope.launch { drawerState.close() }
                                },
                                onSwitchToBusinessView = {
                                    finish() // Return to standard user app
                                },
                                onCloseDrawer = {
                                    coroutineScope.launch { drawerState.close() }
                                },
                                onSignOut = {
                                    viewModel.signOutAdmin()
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                AdminTopBar(
                                    adminEmail = currentAdminEmail,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onSwitchToBusinessView = {
                                        finish()
                                    },
                                    onOpenSupabase = {
                                        Toast.makeText(context, "Supabase Cloud Database Connected (RLS Active)", Toast.LENGTH_SHORT).show()
                                    },
                                    onSignOut = {
                                        viewModel.signOutAdmin()
                                    }
                                )
                            },
                            contentWindowInsets = WindowInsets.safeDrawing,
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AdminSystemScreen(
                                    adminRole = adminRole,
                                    businesses = businesses,
                                    transactions = transactions,
                                    aiLogs = aiLogs,
                                    globalAiMetrics = globalAiMetrics,
                                    broadcasts = broadcasts,
                                    auditLogs = auditLogs,
                                    supportTickets = supportTickets,
                                    systemConfig = systemConfig,
                                    currentSubTab = currentSubTab,
                                    onSelectSubTab = { selected ->
                                        currentSubTab = selected
                                    },
                                    onUpdateBusiness = { updatedBiz ->
                                        viewModel.updateBusinessAccount(updatedBiz)
                                    },
                                    onAdjustAiQuota = { biz, quota, bonus, throttled ->
                                        viewModel.adjustBusinessAiQuota(biz, quota, bonus, throttled)
                                    },
                                    onSendBroadcast = { bc ->
                                        viewModel.sendAdminBroadcast(bc)
                                    },
                                    onRefundTransaction = { tx ->
                                        viewModel.refundAdminTransaction(tx)
                                    },
                                    onUpdateTicketStatus = { ticketId, status ->
                                        viewModel.updateSupportTicketStatus(ticketId, status)
                                    },
                                    onToggleMaintenance = {
                                        viewModel.toggleMaintenanceMode()
                                    },
                                    onUpdateSystemAnnouncement = { announcement ->
                                        viewModel.updateSystemAnnouncement(announcement)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
