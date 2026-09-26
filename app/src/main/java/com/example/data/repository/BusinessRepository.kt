package com.example.data.repository

import com.example.data.local.GrowthEngineDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BusinessRepository(private val db: GrowthEngineDatabase) {

    private val invoiceDao = db.invoiceDao()
    private val productDao = db.productDao()
    private val partyDao = db.partyDao()
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()
    private val manufacturingDao = db.manufacturingDao()
    private val copilotDao = db.copilotDao()

    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val pendingInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getPendingInvoices()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    val allParties: Flow<List<PartyEntity>> = partyDao.getAllParties()
    val customers: Flow<List<PartyEntity>> = partyDao.getCustomers()
    val suppliers: Flow<List<PartyEntity>> = partyDao.getSuppliers()

    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allManufacturingBatches: Flow<List<ManufacturingOrderEntity>> = manufacturingDao.getAllBatches()
    val allCopilotMessages: Flow<List<CopilotMessageEntity>> = copilotDao.getAllMessages()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedSampleDataIfNeeded()
        }
    }

    suspend fun insertInvoice(invoice: InvoiceEntity): Long = invoiceDao.insertInvoice(invoice)
    suspend fun updateInvoice(invoice: InvoiceEntity) = invoiceDao.updateInvoice(invoice)
    suspend fun deleteInvoice(id: Long) = invoiceDao.deleteInvoice(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)
    suspend fun updateProductStock(id: Long, newStock: Double) = productDao.updateStock(id, newStock)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun insertParty(party: PartyEntity): Long = partyDao.insertParty(party)
    suspend fun updateParty(party: PartyEntity) = partyDao.updateParty(party)
    suspend fun reducePartyOutstanding(partyId: Long, amount: Double) = partyDao.reduceOutstanding(partyId, amount)

    suspend fun insertPayment(payment: PaymentEntity): Long {
        val id = paymentDao.insertPayment(payment)
        // Also reduce customer outstanding
        reducePartyOutstanding(payment.partyId, payment.amount)
        return id
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)

    suspend fun insertBatch(batch: ManufacturingOrderEntity): Long = manufacturingDao.insertBatch(batch)
    suspend fun updateBatchStatus(id: Long, status: String) = manufacturingDao.updateBatchStatus(id, status)

    suspend fun insertCopilotMessage(message: CopilotMessageEntity): Long = copilotDao.insertMessage(message)
    suspend fun clearCopilotHistory() = copilotDao.clearHistory()

    private suspend fun seedSampleDataIfNeeded() {
        if (partyDao.getCount() == 0) {
            // Seed Parties
            val p1 = partyDao.insertParty(
                PartyEntity(
                    name = "Tata AutoComp Systems Ltd",
                    tradeName = "Tata AutoComp Pune Plant",
                    type = "CUSTOMER",
                    gstin = "27AAACT2948P1ZV",
                    panNumber = "AAACT2948P",
                    phone = "+91 98220 44921",
                    email = "procurement@tataautocomp.com",
                    address = "Plot No. 14, Rajiv Gandhi Infotech Park, Hinjewadi, Pune",
                    stateName = "Maharashtra",
                    stateCode = "27",
                    creditLimit = 1500000.0,
                    outstandingBalance = 485000.0,
                    paymentTermsDays = 30,
                    overdueDays = 34
                )
            )

            val p2 = partyDao.insertParty(
                PartyEntity(
                    name = "Gujarat Tooling & Die Works",
                    tradeName = "Gujarat Tooling Co",
                    type = "CUSTOMER",
                    gstin = "24AAAFG5829H1Z8",
                    panNumber = "AAAFG5829H",
                    phone = "+91 97241 83920",
                    email = "accounts@gujarattooling.in",
                    address = "Phase II, GIDC Vatva, Ahmedabad",
                    stateName = "Gujarat",
                    stateCode = "24",
                    creditLimit = 800000.0,
                    outstandingBalance = 215400.0,
                    paymentTermsDays = 21,
                    overdueDays = 18
                )
            )

            val p3 = partyDao.insertParty(
                PartyEntity(
                    name = "Mahalakshmi Agro Machinery",
                    tradeName = "Mahalakshmi Agro Works",
                    type = "CUSTOMER",
                    gstin = "27AABCM8291F1ZX",
                    panNumber = "AABCM8291F",
                    phone = "+91 94225 10928",
                    email = "order@mahalakshmiagro.com",
                    address = "Shiroli MIDC, Kolhapur",
                    stateName = "Maharashtra",
                    stateCode = "27",
                    creditLimit = 500000.0,
                    outstandingBalance = 92000.0,
                    paymentTermsDays = 15,
                    overdueDays = 8
                )
            )

            val p4 = partyDao.insertParty(
                PartyEntity(
                    name = "Royal Hydraulics & Spares",
                    tradeName = "Royal Hydraulics Chennai",
                    type = "CUSTOMER",
                    gstin = "33AAACR7293Q1ZG",
                    panNumber = "AAACR7293Q",
                    phone = "+91 98401 22938",
                    email = "contact@royalhydraulics.in",
                    address = "Ambattur Industrial Estate, Chennai",
                    stateName = "Tamil Nadu",
                    stateCode = "33",
                    creditLimit = 600000.0,
                    outstandingBalance = 0.0,
                    paymentTermsDays = 30,
                    overdueDays = 0
                )
            )

            val p5 = partyDao.insertParty(
                PartyEntity(
                    name = "Shree Ganesh Hardware Mart",
                    tradeName = "Ganesh Hardware Retail",
                    type = "CUSTOMER",
                    gstin = "URP-UNREGISTERED",
                    panNumber = "BMOPG1928K",
                    phone = "+91 99214 77123",
                    email = "ganeshhardware.nsk@gmail.com",
                    address = "Main Market Road, Nashik",
                    stateName = "Maharashtra",
                    stateCode = "27",
                    creditLimit = 100000.0,
                    outstandingBalance = 18500.0,
                    paymentTermsDays = 15,
                    overdueDays = 45
                )
            )

            // Suppliers
            partyDao.insertParty(
                PartyEntity(
                    name = "Jindal Steel & Power Ltd",
                    tradeName = "JSPL Industrial Sales",
                    type = "SUPPLIER",
                    gstin = "27AAACJ4928L1ZW",
                    panNumber = "AAACJ4928L",
                    phone = "+91 22 6189 4000",
                    email = "sales.west@jindalsteel.com",
                    address = "Nariman Point, Mumbai",
                    stateName = "Maharashtra",
                    stateCode = "27",
                    creditLimit = 2500000.0,
                    outstandingBalance = -340000.0,
                    paymentTermsDays = 30
                )
            )

            partyDao.insertParty(
                PartyEntity(
                    name = "SKF Bearings India Ltd",
                    tradeName = "SKF Industrial Division",
                    type = "SUPPLIER",
                    gstin = "27AAACS1827M1Z1",
                    panNumber = "AAACS1827M",
                    phone = "+91 20 6611 2500",
                    email = "orders.india@skf.com",
                    address = "Chinchwad, Pune",
                    stateName = "Maharashtra",
                    stateCode = "27",
                    creditLimit = 1000000.0,
                    outstandingBalance = -112000.0,
                    paymentTermsDays = 30
                )
            )
        }

        if (productDao.getCount() == 0) {
            productDao.insertProduct(
                ProductEntity(
                    name = "Heavy Duty Flange Bearing Unit (UCF-208)",
                    sku = "BRG-UCF208",
                    hsnCode = "8482",
                    category = "Mechanical Spares",
                    unit = "Pcs",
                    purchasePrice = 1200.0,
                    wholesalePrice = 1650.0,
                    mrp = 1950.0,
                    gstRatePercent = 18.0,
                    currentStock = 42.0,
                    minReorderLevel = 20.0,
                    preferredSupplier = "SKF Bearings India Ltd"
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Hardened Chrome Plated Shaft 25mm",
                    sku = "SFT-CR25",
                    hsnCode = "8483",
                    category = "Raw Materials",
                    unit = "Mtr",
                    purchasePrice = 650.0,
                    wholesalePrice = 920.0,
                    mrp = 1100.0,
                    gstRatePercent = 18.0,
                    currentStock = 14.0, // Low stock trigger
                    minReorderLevel = 30.0,
                    preferredSupplier = "Jindal Steel & Power Ltd"
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Industrial Brass Bushing Kit (ID 20mm)",
                    sku = "BSH-BR20",
                    hsnCode = "7419",
                    category = "Precision Components",
                    unit = "Box",
                    purchasePrice = 1800.0,
                    wholesalePrice = 2400.0,
                    mrp = 2850.0,
                    gstRatePercent = 18.0,
                    currentStock = 58.0,
                    minReorderLevel = 25.0,
                    preferredSupplier = "Kalyan Internal Foundry"
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "High Tensile M12 Fastener Bolts (Grade 8.8)",
                    sku = "FST-M12G8",
                    hsnCode = "7318",
                    category = "Hardware & Fasteners",
                    unit = "Box",
                    purchasePrice = 320.0,
                    wholesalePrice = 480.0,
                    mrp = 580.0,
                    gstRatePercent = 18.0,
                    currentStock = 110.0,
                    minReorderLevel = 40.0,
                    preferredSupplier = "Precision Fasteners Ludhiana"
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Hydraulic Piston Cylinder 100 Ton",
                    sku = "HYD-CYL100T",
                    hsnCode = "8412",
                    category = "Heavy Machinery",
                    unit = "Pcs",
                    purchasePrice = 14500.0,
                    wholesalePrice = 19800.0,
                    mrp = 23500.0,
                    gstRatePercent = 18.0,
                    currentStock = 6.0,
                    minReorderLevel = 5.0,
                    preferredSupplier = "In-House Assembly"
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Synthetic Industrial Gear Lubricant ISO 460",
                    sku = "LUB-ISO460",
                    hsnCode = "2710",
                    category = "Lubricants",
                    unit = "Ltr",
                    purchasePrice = 380.0,
                    wholesalePrice = 520.0,
                    mrp = 620.0,
                    gstRatePercent = 18.0,
                    currentStock = 5.0, // Low stock trigger
                    minReorderLevel = 15.0,
                    preferredSupplier = "Castrol Industrial Distribution"
                )
            )
        }

        if (invoiceDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2425/0842",
                    invoiceType = "TAX_INVOICE",
                    partyId = 1L,
                    partyName = "Tata AutoComp Systems Ltd",
                    partyGstin = "27AAACT2948P1ZV",
                    partyPhone = "+91 98220 44921",
                    partyState = "Maharashtra (27)",
                    isInterState = false,
                    dateEpoch = now - (15 * day),
                    dueDateEpoch = now + (15 * day),
                    itemsSummary = "200 Pcs Heavy Duty Flange Bearing + 50 Mtr Shaft",
                    itemsCount = 2,
                    subtotal = 330000.0,
                    discount = 0.0,
                    cgstAmount = 29700.0,
                    sgstAmount = 29700.0,
                    igstAmount = 0.0,
                    totalAmount = 389400.0,
                    amountPaid = 200000.0,
                    balanceDue = 189400.0,
                    paymentStatus = "PARTIAL",
                    paymentMode = "NEFT_RTGS",
                    eWayBillNumber = "EB27492810384",
                    notes = "Payment via RTGS per PO #TAC-8821. 30 days credit terms."
                )
            )

            invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2425/0839",
                    invoiceType = "TAX_INVOICE",
                    partyId = 2L,
                    partyName = "Gujarat Tooling & Die Works",
                    partyGstin = "24AAAFG5829H1Z8",
                    partyPhone = "+91 97241 83920",
                    partyState = "Gujarat (24)",
                    isInterState = true,
                    dateEpoch = now - (35 * day),
                    dueDateEpoch = now - (14 * day),
                    itemsSummary = "10 Pcs Hydraulic Piston Cylinder 100 Ton",
                    itemsCount = 1,
                    subtotal = 182542.37,
                    discount = 0.0,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 32857.63,
                    totalAmount = 215400.0,
                    amountPaid = 0.0,
                    balanceDue = 215400.0,
                    paymentStatus = "OVERDUE",
                    paymentMode = "CREDIT",
                    eWayBillNumber = "EB24192801938",
                    notes = "Overdue by 14 days. WhatsApp payment reminder dispatched."
                )
            )

            invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2425/0845",
                    invoiceType = "TAX_INVOICE",
                    partyId = 3L,
                    partyName = "Mahalakshmi Agro Machinery",
                    partyGstin = "27AABCM8291F1ZX",
                    partyPhone = "+91 94225 10928",
                    partyState = "Maharashtra (27)",
                    isInterState = false,
                    dateEpoch = now - (8 * day),
                    dueDateEpoch = now + (7 * day),
                    itemsSummary = "50 Box Industrial Brass Bushing Kit",
                    itemsCount = 1,
                    subtotal = 77966.10,
                    discount = 0.0,
                    cgstAmount = 7016.95,
                    sgstAmount = 7016.95,
                    igstAmount = 0.0,
                    totalAmount = 92000.0,
                    amountPaid = 0.0,
                    balanceDue = 92000.0,
                    paymentStatus = "UNPAID",
                    paymentMode = "CREDIT",
                    eWayBillNumber = null,
                    notes = "Consignment dispatched via VRL Logistics Kolhapur LR# 99482."
                )
            )

            invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2425/0830",
                    invoiceType = "TAX_INVOICE",
                    partyId = 4L,
                    partyName = "Royal Hydraulics & Spares",
                    partyGstin = "33AAACR7293Q1ZG",
                    partyPhone = "+91 98401 22938",
                    partyState = "Tamil Nadu (33)",
                    isInterState = true,
                    dateEpoch = now - (22 * day),
                    dueDateEpoch = now - (7 * day),
                    itemsSummary = "75 Box High Tensile M12 Fastener Bolts",
                    itemsCount = 1,
                    subtotal = 121016.95,
                    discount = 0.0,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 21783.05,
                    totalAmount = 142800.0,
                    amountPaid = 142800.0,
                    balanceDue = 0.0,
                    paymentStatus = "PAID",
                    paymentMode = "UPI",
                    eWayBillNumber = "EB33918204910",
                    notes = "Fully cleared via UPI. UPI Ref: UPI/492819284729."
                )
            )

            invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "QT-2425/0114",
                    invoiceType = "QUOTATION",
                    partyId = 1L,
                    partyName = "Larsen & Toubro Heavy Fab",
                    partyGstin = "27AAACL0140P1ZL",
                    partyPhone = "+91 22 6705 9999",
                    partyState = "Maharashtra (27)",
                    isInterState = false,
                    dateEpoch = now - (2 * day),
                    dueDateEpoch = now + (28 * day),
                    itemsSummary = "Turnkey Fabrication Package for 50 Heavy Assemblies",
                    itemsCount = 1,
                    subtotal = 635593.22,
                    discount = 0.0,
                    cgstAmount = 57203.39,
                    sgstAmount = 57203.39,
                    igstAmount = 0.0,
                    totalAmount = 750000.0,
                    amountPaid = 0.0,
                    balanceDue = 750000.0,
                    paymentStatus = "UNPAID",
                    paymentMode = "CREDIT",
                    eWayBillNumber = null,
                    notes = "Quotation valid for 30 days. Includes freight up to Powai works."
                )
            )
        }

        if (expenseDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "MIDC Industrial Shed Lease (Month)",
                    category = "Factory Rent",
                    amount = 85000.0,
                    dateEpoch = now - (5 * day),
                    isGstClaimable = true,
                    gstAmount = 15300.0,
                    paymentMode = "NEFT_RTGS",
                    vendorName = "MIDC Bhosari Estate Office"
                )
            )

            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "MSEDCL Industrial Power Bill",
                    category = "Utilities & Power",
                    amount = 42600.0,
                    dateEpoch = now - (10 * day),
                    isGstClaimable = false,
                    gstAmount = 0.0,
                    paymentMode = "UPI",
                    vendorName = "Maharashtra State Electricity Distribution"
                )
            )

            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Safexpress B2B Dispatch Freight",
                    category = "Freight & Logistics",
                    amount = 18400.0,
                    dateEpoch = now - (3 * day),
                    isGstClaimable = true,
                    gstAmount = 920.0,
                    paymentMode = "UPI",
                    vendorName = "Safexpress Logistics Pune"
                )
            )

            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "CNC Lathe Tungsten Carbide Inserts",
                    category = "Machine Spares",
                    amount = 14200.0,
                    dateEpoch = now - (7 * day),
                    isGstClaimable = true,
                    gstAmount = 2556.0,
                    paymentMode = "CHEQUE",
                    vendorName = "Sandvik Coromant Authorized Distributor"
                )
            )

            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Skilled Machinists Weekly Overtime & Wages",
                    category = "Staff Wages",
                    amount = 32000.0,
                    dateEpoch = now - (4 * day),
                    isGstClaimable = false,
                    gstAmount = 0.0,
                    paymentMode = "NEFT_RTGS",
                    vendorName = "Internal Payroll"
                )
            )
        }

        if (manufacturingDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            manufacturingDao.insertBatch(
                ManufacturingOrderEntity(
                    batchCode = "BATCH-24-098",
                    finishedGoodName = "Hydraulic Piston Cylinder 100 Ton",
                    targetQuantity = 15.0,
                    unit = "Pcs",
                    status = "IN_PRODUCTION",
                    rawMaterialsUsedSummary = "Chrome Shaft 25mm (30m), Seal Kits (15 sets), Hydraulic Oil (45L)",
                    estimatedCostPerUnit = 13800.0,
                    startDateEpoch = now - (3 * day),
                    targetDateEpoch = now + (2 * day)
                )
            )

            manufacturingDao.insertBatch(
                ManufacturingOrderEntity(
                    batchCode = "BATCH-24-097",
                    finishedGoodName = "Custom Brass Bushing Spec (ID 20mm)",
                    targetQuantity = 120.0,
                    unit = "Box",
                    status = "COMPLETED",
                    rawMaterialsUsedSummary = "Phosphor Bronze Ingot (60kg), Cutting Coolant (12L)",
                    estimatedCostPerUnit = 1720.0,
                    startDateEpoch = now - (8 * day),
                    targetDateEpoch = now - (1 * day)
                )
            )

            manufacturingDao.insertBatch(
                ManufacturingOrderEntity(
                    batchCode = "BATCH-24-099",
                    finishedGoodName = "Heavy Duty Flange Bearing Unit (UCF-208)",
                    targetQuantity = 80.0,
                    unit = "Pcs",
                    status = "QUALITY_CHECK",
                    rawMaterialsUsedSummary = "Cast Iron Housings (80 Pcs), Ball Bearing Inserts (80 Pcs)",
                    estimatedCostPerUnit = 1140.0,
                    startDateEpoch = now - (2 * day),
                    targetDateEpoch = now + (1 * day)
                )
            )
        }

        if (copilotDao.getCount() == 0) {
            copilotDao.insertMessage(
                CopilotMessageEntity(
                    sender = "COPILOT",
                    messageText = "Namaste! I am your GrowthEngine AI Business Copilot. I have audited your current financial metrics for Kalyan Engineering:\n\n• ₹7,92,400 Total Receivables locked in Khata, with ₹2,15,400 overdue from Gujarat Tooling.\n• 2 Inventory SKUs running below reorder threshold (Hardened Chrome Shaft & Gear Lubricant).\n• Net estimated GST Liability for GSTR-3B: ₹48,444 payable after ₹18,776 ITC deduction.\n\nTap any quick action below or ask me anything regarding working capital, debtors recovery, or production scheduling.",
                    actionType = "PAYMENT_REMINDER",
                    actionPayload = "Gujarat Tooling & Die Works: ₹2,15,400"
                )
            )
        }
    }
}
