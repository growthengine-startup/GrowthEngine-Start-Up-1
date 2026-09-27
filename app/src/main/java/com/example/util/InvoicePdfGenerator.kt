package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.InvoiceEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates professional GST-compliant PDF invoices with:
 * - Company logo placeholder / header
 * - Structured invoice layout
 * - Tax breakdown (CGST/SGST or IGST)
 * - Bank details section
 * - Shareable via Android share intent
 */
object InvoicePdfGenerator {

    private const val TAG = "InvoicePdfGenerator"
    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f

    private val currencyFmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("en-IN"))

    // Colors
    private val DARK_INK = Color.parseColor("#0B1320")
    private val GOLD = Color.parseColor("#DFA133")
    private val GOLD_DARK = Color.parseColor("#B87E1B")
    private val GOLD_LIGHT_BG = Color.parseColor("#FEF9EF")
    private val BORDER_COLOR = Color.parseColor("#E2E8F0")
    private val TEXT_PRIMARY = Color.parseColor("#0F172A")
    private val TEXT_SECONDARY = Color.parseColor("#64748B")
    private val TEXT_TERTIARY = Color.parseColor("#94A3B8")
    private val SUCCESS_GREEN = Color.parseColor("#10B981")
    private val ERROR_RED = Color.parseColor("#EF4444")
    private val WARNING_AMBER = Color.parseColor("#F59E0B")

    /**
     * Generate a PDF invoice and return the file path.
     */
    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        businessName: String = "GrowthEngine Enterprise",
        businessGstin: String = "",
        businessAddress: String = "",
        businessPhone: String = "",
        businessEmail: String = "",
        bankName: String = "HDFC Bank",
        bankAccount: String = "50200049102",
        bankIfsc: String = "HDFC0001245",
        bankUpi: String = "growthengine@hdfcbank"
    ): File? {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN

            // ── Header Background ──
            val headerPaint = Paint().apply {
                color = DARK_INK
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, headerPaint)

            // ── Company Name ──
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(businessName, MARGIN, 40f, titlePaint)

            // ── Gold accent line ──
            val goldLinePaint = Paint().apply {
                color = GOLD
                strokeWidth = 3f
            }
            canvas.drawLine(MARGIN, 52f, MARGIN + 80f, 52f, goldLinePaint)

            // ── Company Details ──
            val smallWhitePaint = Paint().apply {
                color = Color.parseColor("#C0C8D8")
                textSize = 9f
                isAntiAlias = true
            }
            if (businessGstin.isNotBlank()) {
                canvas.drawText("GSTIN: $businessGstin", MARGIN, 68f, smallWhitePaint)
            }
            if (businessAddress.isNotBlank()) {
                canvas.drawText(businessAddress, MARGIN, 80f, smallWhitePaint)
            }
            val contactLine = listOf(businessPhone, businessEmail).filter { it.isNotBlank() }.joinToString(" • ")
            if (contactLine.isNotBlank()) {
                canvas.drawText(contactLine, MARGIN, 92f, smallWhitePaint)
            }

            // ── TAX INVOICE badge ──
            val badgePaint = Paint().apply {
                color = GOLD
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val badgeRect = RectF(PAGE_WIDTH - 190f, 20f, PAGE_WIDTH - MARGIN, 48f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, badgePaint)

            val badgeTextPaint = Paint().apply {
                color = DARK_INK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("TAX INVOICE [RULE 46]", badgeRect.centerX(), 38f, badgeTextPaint)

            // ── Invoice Number + Date ──
            val invoiceNumPaint = Paint().apply {
                color = DARK_INK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("# ${invoice.invoiceNumber}", PAGE_WIDTH - MARGIN, 68f, invoiceNumPaint)

            val datePaint = Paint().apply {
                color = TEXT_SECONDARY
                textSize = 9f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("Date: ${dateFmt.format(Date(invoice.dateEpoch))}", PAGE_WIDTH - MARGIN, 82f, datePaint)
            canvas.drawText("Due: ${dateFmt.format(Date(invoice.dueDateEpoch))}", PAGE_WIDTH - MARGIN, 94f, datePaint)

            y = 120f

            // ── Billed To / Ship To ──
            val sectionLabelPaint = Paint().apply {
                color = GOLD_DARK
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.1f
                isAntiAlias = true
            }
            val bodyBoldPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 10f
                isAntiAlias = true
            }
            val bodyLightPaint = Paint().apply {
                color = TEXT_SECONDARY
                textSize = 9f
                isAntiAlias = true
            }

            // Light background for billing section
            val billBgPaint = Paint().apply {
                color = GOLD_LIGHT_BG
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(MARGIN, y, PAGE_WIDTH / 2f - 10f, y + 90f),
                6f, 6f, billBgPaint
            )

            canvas.drawText("BILLED TO", MARGIN + 10f, y + 16f, sectionLabelPaint)
            canvas.drawText(invoice.partyName, MARGIN + 10f, y + 34f, bodyBoldPaint)
            canvas.drawText("GSTIN: ${invoice.partyGstin.ifBlank { "Unregistered" }}", MARGIN + 10f, y + 50f, bodyPaint)
            canvas.drawText("State: ${invoice.partyState}", MARGIN + 10f, y + 64f, bodyLightPaint)
            if (invoice.partyPhone.isNotBlank()) {
                canvas.drawText("Phone: ${invoice.partyPhone}", MARGIN + 10f, y + 78f, bodyLightPaint)
            }

            // Supply details
            canvas.drawRoundRect(
                RectF(PAGE_WIDTH / 2f + 10f, y, PAGE_WIDTH - MARGIN, y + 90f),
                6f, 6f, billBgPaint
            )
            canvas.drawText("SUPPLY DETAILS", PAGE_WIDTH / 2f + 20f, y + 16f, sectionLabelPaint)

            val supplyType = if (invoice.isInterState) "Inter-State (IGST)" else "Intra-State (CGST+SGST)"
            canvas.drawText("Type: $supplyType", PAGE_WIDTH / 2f + 20f, y + 34f, bodyPaint)
            canvas.drawText("Place of Supply: ${invoice.partyState}", PAGE_WIDTH / 2f + 20f, y + 50f, bodyPaint)

            val statusText = invoice.paymentStatus
            val statusColor = when (statusText) {
                "PAID" -> SUCCESS_GREEN
                "PARTIAL" -> WARNING_AMBER
                "OVERDUE" -> ERROR_RED
                else -> ERROR_RED
            }
            val statusPaint = Paint().apply {
                color = statusColor
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("Status: $statusText", PAGE_WIDTH / 2f + 20f, y + 68f, statusPaint)

            if (!invoice.eWayBillNumber.isNullOrBlank()) {
                canvas.drawText("e-Way Bill: ${invoice.eWayBillNumber}", PAGE_WIDTH / 2f + 20f, y + 82f, bodyLightPaint)
            }

            y += 110f

            // ── Items Table Header ──
            val tableHeaderBg = Paint().apply {
                color = DARK_INK
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 26f), 4f, 4f, tableHeaderBg)

            val tableHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("DESCRIPTION", MARGIN + 8f, y + 17f, tableHeaderPaint)

            val rightHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("AMOUNT", PAGE_WIDTH - MARGIN - 8f, y + 17f, rightHeaderPaint)

            y += 32f

            // ── Item Row ──
            canvas.drawText(invoice.itemsSummary, MARGIN + 8f, y + 14f, bodyPaint)

            val amountRightPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("₹${currencyFmt.format(invoice.subtotal)}", PAGE_WIDTH - MARGIN - 8f, y + 14f, amountRightPaint)

            canvas.drawText("${invoice.itemsCount} items", MARGIN + 8f, y + 28f, bodyLightPaint)

            y += 40f

            // ── Separator ──
            val borderPaint = Paint().apply {
                color = BORDER_COLOR
                strokeWidth = 1f
            }
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, borderPaint)

            y += 16f

            // ── Tax Breakdown ──
            val taxSectionX = PAGE_WIDTH / 2f + 20f

            canvas.drawText("TAX BREAKDOWN", taxSectionX, y, sectionLabelPaint)
            y += 18f

            val labelPaint = Paint().apply {
                color = TEXT_SECONDARY
                textSize = 10f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 10f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            // Subtotal
            canvas.drawText("Taxable Amount", taxSectionX, y, labelPaint)
            canvas.drawText("₹${currencyFmt.format(invoice.subtotal)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
            y += 16f

            // Discount
            if (invoice.discount > 0) {
                canvas.drawText("Discount", taxSectionX, y, labelPaint)
                canvas.drawText("-₹${currencyFmt.format(invoice.discount)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
                y += 16f
            }

            // GST
            if (invoice.isInterState) {
                canvas.drawText("IGST (18%)", taxSectionX, y, labelPaint)
                canvas.drawText("₹${currencyFmt.format(invoice.igstAmount)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
                y += 16f
            } else {
                canvas.drawText("CGST (9%)", taxSectionX, y, labelPaint)
                canvas.drawText("₹${currencyFmt.format(invoice.cgstAmount)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
                y += 16f
                canvas.drawText("SGST (9%)", taxSectionX, y, labelPaint)
                canvas.drawText("₹${currencyFmt.format(invoice.sgstAmount)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
                y += 16f
            }

            y += 4f
            canvas.drawLine(taxSectionX, y, PAGE_WIDTH - MARGIN, y, borderPaint)
            y += 16f

            // Total
            val totalLabelPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val totalValuePaint = Paint().apply {
                color = DARK_INK
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("TOTAL", taxSectionX, y, totalLabelPaint)
            canvas.drawText("₹${currencyFmt.format(invoice.totalAmount)}", PAGE_WIDTH - MARGIN - 8f, y, totalValuePaint)

            y += 20f

            // Amount Paid & Balance
            canvas.drawText("Amount Paid", taxSectionX, y, labelPaint)
            canvas.drawText("₹${currencyFmt.format(invoice.amountPaid)}", PAGE_WIDTH - MARGIN - 8f, y, valuePaint)
            y += 16f

            val balancePaint = Paint().apply {
                color = if (invoice.balanceDue > 0) ERROR_RED else SUCCESS_GREEN
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("Balance Due", taxSectionX, y, totalLabelPaint)
            canvas.drawText("₹${currencyFmt.format(invoice.balanceDue)}", PAGE_WIDTH - MARGIN - 8f, y, balancePaint)

            y += 40f

            // ── Bank Details ──
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, borderPaint)
            y += 18f

            val bankBg = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 80f), 6f, 6f, bankBg)

            canvas.drawText("BANK DETAILS FOR PAYMENT", MARGIN + 10f, y + 16f, sectionLabelPaint)
            canvas.drawText("Bank: $bankName", MARGIN + 10f, y + 34f, bodyPaint)
            canvas.drawText("A/C No: $bankAccount | IFSC: $bankIfsc", MARGIN + 10f, y + 50f, bodyPaint)
            canvas.drawText("UPI: $bankUpi", MARGIN + 10f, y + 66f, bodyPaint)

            y += 100f

            // ── Notes ──
            if (!invoice.notes.isNullOrBlank()) {
                canvas.drawText("NOTES", MARGIN, y, sectionLabelPaint)
                y += 16f
                canvas.drawText(invoice.notes!!, MARGIN, y, bodyLightPaint)
                y += 20f
            }

            // ── Footer ──
            val footerY = PAGE_HEIGHT - 40f
            canvas.drawLine(MARGIN, footerY - 10f, PAGE_WIDTH - MARGIN, footerY - 10f, borderPaint)

            val footerPaint = Paint().apply {
                color = TEXT_TERTIARY
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText("Generated by GrowthEngine ERP • Computer generated invoice", MARGIN, footerY, footerPaint)

            val footerRightPaint = Paint().apply {
                color = TEXT_TERTIARY
                textSize = 8f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("Page 1 of 1", PAGE_WIDTH - MARGIN, footerY, footerRightPaint)

            // ── Gold accent at very bottom ──
            val goldBarPaint = Paint().apply {
                color = GOLD
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, PAGE_HEIGHT - 4f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), goldBarPaint)

            document.finishPage(page)

            // Save to file
            val pdfDir = File(context.cacheDir, "invoices")
            pdfDir.mkdirs()
            val pdfFile = File(pdfDir, "Invoice_${invoice.invoiceNumber.replace("/", "_")}.pdf")
            FileOutputStream(pdfFile).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            Log.i(TAG, "PDF generated: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
            return pdfFile

        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate PDF", e)
            return null
        }
    }

    /**
     * Generate PDF and share it via Android share intent.
     */
    fun shareInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        businessName: String = "GrowthEngine Enterprise",
        businessGstin: String = "",
        businessAddress: String = "",
        businessPhone: String = "",
        businessEmail: String = ""
    ) {
        val pdfFile = generateInvoicePdf(
            context = context,
            invoice = invoice,
            businessName = businessName,
            businessGstin = businessGstin,
            businessAddress = businessAddress,
            businessPhone = businessPhone,
            businessEmail = businessEmail
        )

        if (pdfFile == null || !pdfFile.exists()) {
            Log.e(TAG, "PDF file not generated")
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val subject = "Invoice ${invoice.invoiceNumber} — ₹${
            NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
                maximumFractionDigits = 0
            }.format(invoice.totalAmount)
        } | $businessName"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Please find attached invoice ${invoice.invoiceNumber} from $businessName.\n\nTotal: ₹${currencyFmt.format(invoice.totalAmount)}\nDue: ₹${currencyFmt.format(invoice.balanceDue)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Invoice PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
