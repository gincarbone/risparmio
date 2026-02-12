package com.youandmedia.risparmio.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.youandmedia.risparmio.util.APP_VERSION
import com.youandmedia.risparmio.util.BgColor
import com.youandmedia.risparmio.util.GradientBlue
import com.youandmedia.risparmio.util.GradientPurple
import java.io.File

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Impostazioni", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Spacer(modifier = Modifier.height(16.dp))

        // App info card
        Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 8.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GradientBlue, GradientPurple)))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Risparmio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Versione $APP_VERSION", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gestisci le tue finanze personali in modo semplice ed efficace.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions section
        Text("Azioni", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7F8C8D),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Condividi App",
                    subtitle = "Condividi l'APK con i tuoi amici",
                    iconColor = Color(0xFF45B7D1),
                    onClick = { shareApk(context) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Esporta Report PDF",
                    subtitle = "Esporta il report mensile in formato PDF",
                    iconColor = Color(0xFFE74C3C),
                    onClick = { exportMonthlyPdf(context) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Pulisci Cache",
                    subtitle = "Libera spazio eliminando file temporanei",
                    iconColor = Color(0xFFF39C12),
                    onClick = {
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, "Cache pulita!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info section
        Text("Informazioni", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7F8C8D),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
            Column {
                InfoRow("Versione App", APP_VERSION)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow("Sviluppatore", "You&Media")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow("Piattaforma", "Android ${android.os.Build.VERSION.RELEASE}")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow("Architettura", "Kotlin + Jetpack Compose")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Credits
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Made with", color = Color.Gray, fontSize = 12.sp)
                Text("You&Media", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C3E50))
                Text("youandmedia.it", color = Color(0xFF45B7D1), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3E50))
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF7F8C8D))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3E50))
    }
}

private fun shareApk(context: Context) {
    try {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val sourceApk = File(appInfo.sourceDir)
        val cacheDir = File(context.cacheDir, "apk")
        cacheDir.mkdirs()
        val destApk = File(cacheDir, "Risparmio.apk")
        sourceApk.copyTo(destApk, overwrite = true)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destApk)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Condividi Risparmio"))
    } catch (e: Exception) {
        Toast.makeText(context, "Errore nella condivisione: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun exportMonthlyPdf(context: Context) {
    try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        canvas.drawText("Risparmio - Report Mensile", 50f, 60f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false

        val now = java.time.YearMonth.now()
        val monthName = now.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ITALIAN)
            .replaceFirstChar { it.uppercase() }
        canvas.drawText("Mese: $monthName ${now.year}", 50f, 100f, paint)
        canvas.drawText("Data generazione: ${java.time.LocalDate.now()}", 50f, 120f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Riepilogo", 50f, 170f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false

        val db = (context.applicationContext as com.youandmedia.risparmio.RisparmioApp).database
        val zone = java.time.ZoneId.systemDefault()
        val startOfMonth = now.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

        // We need to run DB queries on a background thread but PDF generation is called from main thread
        // Use a simple thread + handler approach
        kotlinx.coroutines.runBlocking {
            val expenses = db.expenseDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val incomes = db.incomeDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val fixedIncomes = db.fixedIncomeDao().getAllOnce()
            val fixedExpenses = db.fixedExpenseDao().getAllOnce()

            val totalFixedInc = fixedIncomes.sumOf { it.amount }
            val totalFixedExp = fixedExpenses.sumOf { it.amount }
            val totalExp = expenses.sumOf { it.amount }
            val totalInc = incomes.sumOf { it.amount }
            val balance = totalFixedInc - totalFixedExp - totalExp + totalInc

            var y = 200f
            canvas.drawText("Entrate Fisse: \u20AC${String.format("%.2f", totalFixedInc)}", 50f, y, paint); y += 22f
            canvas.drawText("Uscite Fisse: \u20AC${String.format("%.2f", totalFixedExp)}", 50f, y, paint); y += 22f
            canvas.drawText("Spese del Mese: \u20AC${String.format("%.2f", totalExp)}", 50f, y, paint); y += 22f
            canvas.drawText("Entrate del Mese: \u20AC${String.format("%.2f", totalInc)}", 50f, y, paint); y += 22f

            paint.isFakeBoldText = true
            paint.textSize = 15f
            y += 10f
            canvas.drawText("Bilancio: \u20AC${String.format("%.2f", balance)}", 50f, y, paint)

            // Category breakdown
            y += 40f
            paint.textSize = 16f
            canvas.drawText("Dettaglio Spese per Categoria", 50f, y, paint)

            paint.textSize = 13f
            paint.isFakeBoldText = false
            y += 25f

            val byCat = expenses.groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            for ((cat, amt) in byCat) {
                val pct = if (totalExp > 0) amt / totalExp * 100 else 0.0
                canvas.drawText("$cat: \u20AC${String.format("%.2f", amt)} (${String.format("%.1f", pct)}%)", 50f, y, paint)
                y += 20f
                if (y > 780f) break
            }

            // Expense list
            y += 15f
            paint.isFakeBoldText = true
            paint.textSize = 16f
            if (y < 700f) {
                canvas.drawText("Lista Spese", 50f, y, paint)
                paint.textSize = 11f
                paint.isFakeBoldText = false
                y += 20f

                for (exp in expenses.sortedByDescending { it.date }) {
                    val dateStr = java.time.Instant.ofEpochMilli(exp.date).atZone(zone).toLocalDate().toString()
                    val line = "$dateStr  ${exp.category}  \u20AC${String.format("%.2f", exp.amount)}  ${exp.description}"
                    canvas.drawText(line, 50f, y, paint)
                    y += 16f
                    if (y > 800f) break
                }
            }
        }

        pdfDoc.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "Risparmio_Report_${java.time.YearMonth.now()}.pdf")
        pdfDoc.writeTo(file.outputStream())
        pdfDoc.close()

        Toast.makeText(context, "PDF salvato in Downloads: ${file.name}", Toast.LENGTH_LONG).show()

        // Open the PDF
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(openIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Errore generazione PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
