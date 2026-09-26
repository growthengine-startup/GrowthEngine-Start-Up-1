package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.GrowthEngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowthEngineTheme {
                val context = LocalContext.current
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val businessName by viewModel.businessName.collectAsStateWithLifecycle()
                val businessRegion by viewModel.businessRegion.collectAsStateWithLifecycle()
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
                val showAuthModal by viewModel.showAuthModal.collectAsStateWithLifecycle()

                // Subscription & Billing States
                val currentSubscription by viewModel.currentSubscription.collectAsStateWithLifecycle()
                val billingHistory by viewModel.billingHistory.collectAsStateWithLifecycle()
                val showCheckoutModal by viewModel.showCheckoutModal.collectAsStateWithLifecycle()
                val checkoutTier by viewModel.checkoutTier.collectAsStateWithLifecycle()
                val checkoutCycle by viewModel.checkoutCycle.collectAsStateWithLifecycle()
                val selectedReceipt by viewModel.selectedReceiptForView.collectAsStateWithLifecycle()

                val invoices by viewModel.invoices.collectAsStateWithLifecycle()
                val customers by viewModel.customers.collectAsStateWithLifecycle()
                val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
                val products by viewModel.products.collectAsStateWithLifecycle()
                val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
                val expenses by viewModel.expenses.collectAsStateWithLifecycle()
                val batches by viewModel.batches.collectAsStateWithLifecycle()
                val copilotMessages by viewModel.copilotMessages.collectAsStateWithLifecycle()
                val isCopilotThinking by viewModel.isCopilotThinking.collectAsStateWithLifecycle()

                // Supabase States
                val supabaseSyncState by viewModel.supabaseSyncState.collectAsStateWithLifecycle()
                val supabaseMessage by viewModel.supabaseMessage.collectAsStateWithLifecycle()

                // Dialog states
                val selectedInvoice by viewModel.selectedInvoiceForView.collectAsStateWithLifecycle()
                val selectedCustomerForKhata by viewModel.selectedCustomerForKhata.collectAsStateWithLifecycle()
                val showCreateInvoice by viewModel.showCreateInvoiceDialog.collectAsStateWithLifecycle()
                val showRecordPayment by viewModel.showRecordPaymentDialog.collectAsStateWithLifecycle()
                val stockAdjustProduct by viewModel.showStockAdjustDialog.collectAsStateWithLifecycle()
                val showAddProduct by viewModel.showAddProductDialog.collectAsStateWithLifecycle()
                val showAddCustomer by viewModel.showAddCustomerDialog.collectAsStateWithLifecycle()
                val showAddExpense by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
                val showNewBatch by viewModel.showNewBatchDialog.collectAsStateWithLifecycle()
                val whatsAppText by viewModel.whatsAppReminderText.collectAsStateWithLifecycle()

                // POS states
                val posCart by viewModel.posCart.collectAsStateWithLifecycle()
                val posWholesale by viewModel.posUseWholesalePrice.collectAsStateWithLifecycle()
                val posSuccess by viewModel.posCheckoutSuccess.collectAsStateWithLifecycle()

                if (!isUserLoggedIn) {
                    // Show Landing / Onboarding Screen
                    LandingScreen(
                        onStartFreeClick = {
                            viewModel.setShowAuthModal(true)
                        },
                        onSignInClick = {
                            viewModel.setShowAuthModal(true)
                        },
                        onDirectDashboardClick = {
                            viewModel.loginWithGoogle()
                        }
                    )
                } else {
                    // =========================================================
                    // STANDARD MSME BUSINESS INTERFACE (GrowthEngine ERP)
                    // =========================================================
                    // Handle Back button to return to Dashboard if in secondary module or close drawer
                    BackHandler(enabled = drawerState.isOpen || currentTab != AppNavTab.DASHBOARD) {
                        if (drawerState.isOpen) {
                            coroutineScope.launch { drawerState.close() }
                        } else {
                            viewModel.setTab(AppNavTab.DASHBOARD)
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawerContent(
                                currentTab = currentTab,
                                businessName = businessName,
                                businessRegion = businessRegion,
                                planTier = currentSubscription.tier,
                                onSelectTab = { tab ->
                                    viewModel.setTab(tab)
                                },
                                onCloseDrawer = {
                                    coroutineScope.launch { drawerState.close() }
                                },
                                onSignOut = {
                                    viewModel.logout()
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                AppTopBar(
                                    businessName = businessName,
                                    businessRegion = businessRegion,
                                    planTier = currentSubscription.tier,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onOpenCopilot = {
                                        viewModel.setTab(AppNavTab.COPILOT)
                                    },
                                    onOpenSupabase = {
                                        viewModel.setTab(AppNavTab.SUPABASE_SYNC)
                                    },
                                    onOpenBilling = {
                                        viewModel.setTab(AppNavTab.PLANS_BILLING)
                                    },
                                    onOpenSettings = {
                                        viewModel.setTab(AppNavTab.SETTINGS)
                                    },
                                    onSignOut = {
                                        viewModel.logout()
                                    }
                                )
                            },
                            bottomBar = {
                                AppBottomNav(
                                    currentTab = currentTab,
                                    onTabSelected = { viewModel.setTab(it) },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
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
                                when (currentTab) {
                                    AppNavTab.DASHBOARD -> {
                                        DashboardScreen(
                                            businessName = businessName,
                                            invoices = invoices,
                                            customers = customers,
                                            suppliers = suppliers,
                                            products = products,
                                            onNavigate = { viewModel.setTab(it) },
                                            onViewInvoice = { viewModel.openInvoiceView(it) },
                                            onOpenCreateInvoice = { viewModel.setShowCreateInvoice(true) },
                                            onOpenRecordPayment = { viewModel.setShowRecordPayment(true) },
                                            onOpenAddCustomer = { viewModel.setShowAddCustomer(true) },
                                            onOpenAddProduct = { viewModel.setShowAddProduct(true) }
                                        )
                                    }
                                    AppNavTab.COPILOT -> {
                                        CopilotScreen(
                                            messages = copilotMessages,
                                            isThinking = isCopilotThinking,
                                            onSendQuery = { viewModel.sendCopilotQuery(it) },
                                            onNavigate = { viewModel.setTab(it) },
                                            onGeneratePaymentReminder = {
                                                val overdueCustomer = customers.firstOrNull { it.overdueDays > 0 } ?: customers.firstOrNull()
                                                if (overdueCustomer != null) {
                                                    viewModel.generateWhatsAppReminder(overdueCustomer)
                                                }
                                            }
                                        )
                                    }
                                    AppNavTab.INVOICING -> {
                                        InvoicingScreen(
                                            invoices = invoices,
                                            onViewInvoice = { viewModel.openInvoiceView(it) },
                                            onCreateInvoiceClick = { viewModel.setShowCreateInvoice(true) }
                                        )
                                    }
                                    AppNavTab.POS -> {
                                        PosScreen(
                                            products = products,
                                            cart = posCart,
                                            isWholesale = posWholesale,
                                            successMessage = posSuccess,
                                            onAddItem = { viewModel.addPosItem(it) },
                                            onUpdateQuantity = { id, d -> viewModel.updatePosQuantity(id, d) },
                                            onTogglePriceTier = { viewModel.togglePosPriceTier() },
                                            onClearCart = { viewModel.clearPosCart() },
                                            onCheckout = { mode -> viewModel.checkoutPos(mode) },
                                            onDismissSuccess = { viewModel.clearPosSuccess() }
                                        )
                                    }
                                    AppNavTab.QUOTATIONS -> {
                                        QuotationsScreen(
                                            invoices = invoices,
                                            customers = customers,
                                            onCreateQuotationClick = { viewModel.setShowCreateInvoice(true) },
                                            onConvertToInvoice = { viewModel.convertQuotationToInvoice(it) },
                                            onViewQuotation = { viewModel.openInvoiceView(it) },
                                            onShareQuotation = {
                                                Toast.makeText(context, "Quotation ${it.invoiceNumber} shared via WhatsApp", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    AppNavTab.ORDERS -> {
                                        OrdersScreen(
                                            invoices = invoices,
                                            customers = customers,
                                            onCreateOrderClick = { viewModel.setShowCreateInvoice(true) },
                                            onViewOrderDetails = { order ->
                                                Toast.makeText(context, "Sales Order ${order.orderNumber} - Status: ${order.status.label}", Toast.LENGTH_SHORT).show()
                                            },
                                            onUpdateOrderStatus = { id, st ->
                                                Toast.makeText(context, "Order $id updated to ${st.label}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    AppNavTab.CUSTOMERS -> {
                                        CustomersScreen(
                                            customers = customers,
                                            invoices = invoices,
                                            onAddNewCustomer = { viewModel.setShowAddCustomer(true) },
                                            onRecordPayment = {
                                                viewModel.openCustomerKhata(it)
                                                viewModel.setShowRecordPayment(true)
                                            },
                                            onSendWhatsAppReminder = { viewModel.generateWhatsAppReminder(it) },
                                            onCreateInvoiceForCustomer = {
                                                viewModel.openCustomerKhata(it)
                                                viewModel.setShowCreateInvoice(true)
                                            },
                                            onViewInvoice = { viewModel.openInvoiceView(it) }
                                        )
                                    }
                                    AppNavTab.PURCHASES -> {
                                        PurchasesScreen(
                                            suppliers = suppliers,
                                            onAddPurchaseBillClick = { viewModel.setShowAddExpense(true) },
                                            onViewBillDetails = { bill ->
                                                Toast.makeText(context, "Purchase Bill ${bill.billNumber} • ITC: ₹${MainViewModel.formatCurrencyPlain(bill.itcGstAmount)}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    AppNavTab.SUPPLIERS -> {
                                        SuppliersScreen(
                                            suppliers = suppliers,
                                            onAddNewSupplier = { viewModel.setShowAddCustomer(true) },
                                            onRecordPayout = { supp ->
                                                viewModel.recordSupplierPayout(supp, -supp.outstandingBalance, "UPI", "PAYOUT-${System.currentTimeMillis() % 10000}")
                                                Toast.makeText(context, "Payout recorded for ${supp.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            onCreatePurchaseForSupplier = {
                                                viewModel.setShowAddExpense(true)
                                            }
                                        )
                                    }
                                    AppNavTab.INVENTORY -> {
                                        InventoryScreen(
                                            products = products,
                                            onAdjustStock = { viewModel.setShowStockAdjust(it) },
                                            onAddNewProduct = { viewModel.setShowAddProduct(true) }
                                        )
                                    }
                                    AppNavTab.MANUFACTURING -> {
                                        ManufacturingScreen(
                                            batches = batches,
                                            onUpdateBatchStatus = { id, s -> viewModel.updateBatchStatus(id, s) },
                                            onNewBatchClick = { viewModel.setShowNewBatch(true) }
                                        )
                                    }
                                    AppNavTab.KHATA -> {
                                        KhataLedgerScreen(
                                            customers = customers,
                                            suppliers = suppliers,
                                            invoices = invoices,
                                            payments = emptyList(),
                                            onRecordInwardPayment = { viewModel.setShowRecordPayment(true) },
                                            onRecordOutwardPayout = { viewModel.setShowAddExpense(true) },
                                            onSelectPartyForLedger = { party ->
                                                if (party.type == "CUSTOMER") {
                                                    viewModel.setTab(AppNavTab.CUSTOMERS)
                                                } else {
                                                    viewModel.setTab(AppNavTab.SUPPLIERS)
                                                }
                                            }
                                        )
                                    }
                                    AppNavTab.EXPENSES -> {
                                        ExpensesScreen(
                                            expenses = expenses,
                                            onAddExpenseClick = { viewModel.setShowAddExpense(true) }
                                        )
                                    }
                                    AppNavTab.EMPLOYEES -> {
                                        EmployeesScreen(
                                            onAddEmployeeClick = {
                                                Toast.makeText(context, "Employee registration form opened", Toast.LENGTH_SHORT).show()
                                            },
                                            onRecordAdvance = { staff ->
                                                Toast.makeText(context, "Salary Advance voucher recorded for ${staff.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            onMarkAttendance = { staff, st ->
                                                Toast.makeText(context, "${staff.name} marked as $st", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    AppNavTab.REPORTS -> {
                                        ReportsScreen(
                                            invoices = invoices,
                                            expenses = expenses
                                        )
                                    }
                                    AppNavTab.ANALYTICS -> {
                                        AnalyticsScreen(
                                            invoices = invoices,
                                            expenses = expenses
                                        )
                                    }
                                    AppNavTab.AUTOMATION -> {
                                        AutomationScreen(
                                            onTriggerAllAutomations = {
                                                // Trigger automated routines
                                            }
                                        )
                                    }
                                    AppNavTab.SUPABASE_SYNC -> {
                                        SupabaseSyncScreen(
                                            supabaseService = viewModel.supabaseService,
                                            syncState = supabaseSyncState,
                                            statusMessage = supabaseMessage,
                                            invoicesCount = invoices.size,
                                            customersCount = customers.size,
                                            productsCount = products.size,
                                            expensesCount = expenses.size,
                                            onSyncNow = { viewModel.syncWithSupabase() },
                                            onTestConnection = { viewModel.testSupabaseConnection() }
                                        )
                                    }
                                    AppNavTab.PLANS_BILLING -> {
                                        PlansBillingScreen(
                                            currentSubscription = currentSubscription,
                                            billingHistory = billingHistory,
                                            invoicesCount = invoices.size,
                                            productsCount = products.size,
                                            razorpayService = viewModel.razorpayService,
                                            onOpenCheckout = { tier, cycle -> viewModel.openCheckout(tier, cycle) },
                                            onToggleAutoRenew = { viewModel.toggleAutoRenew() },
                                            onViewReceipt = { viewModel.openReceiptView(it) }
                                        )
                                    }
                                    AppNavTab.SETTINGS -> {
                                        SettingsScreen(
                                            currentSubscription = currentSubscription,
                                            onNavigateToBilling = { viewModel.setTab(AppNavTab.PLANS_BILLING) }
                                        )
                                    }
                                }
                            }
                        }

                        // Dialogs & Modals
                        selectedInvoice?.let { inv ->
                            GstInvoiceDetailDialog(
                                invoice = inv,
                                onDismiss = { viewModel.openInvoiceView(null) },
                                onShare = {
                                Toast.makeText(context, "Invoice ${inv.invoiceNumber} shared via WhatsApp / Email", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    whatsAppText?.let { text ->
                        WhatsAppReminderDialog(
                            reminderText = text,
                            onDismiss = { viewModel.clearWhatsAppReminder() }
                        )
                    }

                    if (showCreateInvoice) {
                        CreateInvoiceDialog(
                            customers = customers,
                            onDismiss = { viewModel.setShowCreateInvoice(false) },
                            onSave = { party, itemsSummary, subtotal, isInterState, mode, days, notes ->
                                viewModel.saveNewInvoice(party, itemsSummary, subtotal, isInterState, mode, days, notes)
                            }
                        )
                    }

                    if (showRecordPayment) {
                        val targetParty = selectedCustomerForKhata ?: customers.firstOrNull()
                        if (targetParty != null) {
                            RecordPaymentDialog(
                                party = targetParty,
                                onDismiss = {
                                    viewModel.setShowRecordPayment(false)
                                    viewModel.openCustomerKhata(null)
                                },
                                onSave = { party, amt, mode, ref, notes ->
                                    viewModel.recordKhataPayment(party, amt, mode, ref, notes)
                                }
                            )
                        }
                    }

                    stockAdjustProduct?.let { product ->
                        StockAdjustDialog(
                            product = product,
                            onDismiss = { viewModel.setShowStockAdjust(null) },
                            onSave = { id, stock -> viewModel.adjustStock(id, stock) }
                        )
                    }

                    if (showAddProduct) {
                        AddProductDialog(
                            onDismiss = { viewModel.setShowAddProduct(false) },
                            onSave = { name, sku, hsn, cat, unit, purchase, wholesale, mrp, gst, stock, minR, supp ->
                                viewModel.addProduct(name, sku, hsn, cat, unit, purchase, wholesale, mrp, gst, stock, minR, supp)
                            }
                        )
                    }

                    if (showAddCustomer) {
                        AddCustomerDialog(
                            onDismiss = { viewModel.setShowAddCustomer(false) },
                            onSave = { name, trade, gstin, phone, addr, state, code, limit, days ->
                                viewModel.addCustomer(name, trade, gstin, phone, addr, state, code, limit, days)
                            }
                        )
                    }

                    if (showAddExpense) {
                        AddExpenseDialog(
                            onDismiss = { viewModel.setShowAddExpense(false) },
                            onSave = { title, cat, amt, isGst, gstAmt, mode, vendor ->
                                viewModel.addExpense(title, cat, amt, isGst, gstAmt, mode, vendor)
                            }
                        )
                    }

                    if (showNewBatch) {
                        NewBatchDialog(
                            onDismiss = { viewModel.setShowNewBatch(false) },
                            onSave = { good, qty, unit, raw, cost ->
                                viewModel.createManufacturingBatch(good, qty, unit, raw, cost)
                            }
                        )
                    }

                    // Razorpay Checkout Modal
                    if (showCheckoutModal && checkoutTier != null) {
                        RazorpayCheckoutModal(
                            tier = checkoutTier!!,
                            cycle = checkoutCycle,
                            businessName = businessName,
                            razorpayService = viewModel.razorpayService,
                            onDismiss = { viewModel.dismissCheckout() },
                            onPaymentSuccess = { receipt ->
                                viewModel.activateSubscription(receipt)
                            }
                        )
                    }

                    // Billing / Subscription Receipt Detail Dialog
                    selectedReceipt?.let { receipt ->
                        SubscriptionReceiptDialog(
                            receipt = receipt,
                            businessName = businessName,
                            onDismiss = { viewModel.openReceiptView(null) }
                        )
                    }
                }
            }

            // Authentication Modal Dialog
            if (showAuthModal) {
                AuthModal(
                    initialBusinessName = businessName,
                    initialRegion = businessRegion,
                    onDismiss = { viewModel.setShowAuthModal(false) },
                    onGoogleLoginSuccess = { email, name, bName, region ->
                        viewModel.loginWithGoogle(email, name, bName, region)
                    },
                    onPasswordLoginSuccess = { name, region, email ->
                        viewModel.login(name, region, email)
                    }
                )
            }
        }
    }
}
}
