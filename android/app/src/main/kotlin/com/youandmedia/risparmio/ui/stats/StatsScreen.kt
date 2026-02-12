package com.youandmedia.risparmio.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youandmedia.risparmio.util.BgColor
import com.youandmedia.risparmio.util.GradientBlue
import com.youandmedia.risparmio.util.GradientPurple
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val chartColors = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
    Color(0xFFFFA07A), Color(0xFF98D8C8), Color(0xFFF7DC6F),
    Color(0xFFBB8FCE), Color(0xFF85C1E9), Color(0xFFE74C3C),
    Color(0xFF2ECC71), Color(0xFF3498DB), Color(0xFFF39C12),
    Color(0xFF1ABC9C)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiche") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Month selector
            MonthSelector(
                state = state,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() }
            )

            // Summary header card
            SummaryHeaderCard(state)

            Spacer(modifier = Modifier.height(16.dp))

            // Quick stats row
            QuickStatsRow(state)

            Spacer(modifier = Modifier.height(20.dp))

            // 6-month trend chart
            if (state.monthTrends.isNotEmpty()) {
                Text(
                    "Andamento 6 Mesi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrendChart(
                    trends = state.monthTrends,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                )

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B6B))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Spese", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.width(16.dp))
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ECDC4))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Entrate", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (state.categoryStats.isNotEmpty()) {
                // Donut chart
                Text(
                    "Ripartizione Spese",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedDonutChart(
                    stats = state.categoryStats,
                    totalSpent = state.totalSpent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Category breakdown
                Text(
                    "Dettaglio Categorie",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                state.categoryStats.forEachIndexed { index, stat ->
                    CategoryCard(
                        stat = stat,
                        color = chartColors[index % chartColors.size],
                        maxAmount = state.categoryStats.first().amount
                    )
                }
            } else {
                // Empty state
                EmptyState()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TrendChart(
    trends: List<MonthTrend>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(trends) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (trends.isEmpty()) return@Canvas

            val maxVal = trends.maxOf { maxOf(it.totalExpenses, it.totalIncomes) }
            if (maxVal <= 0) return@Canvas

            val barGroupWidth = size.width / trends.size
            val barWidth = barGroupWidth * 0.3f
            val gap = barWidth * 0.15f
            val chartHeight = size.height - 30.dp.toPx()

            trends.forEachIndexed { index, trend ->
                val centerX = barGroupWidth * index + barGroupWidth / 2

                // Expense bar (left)
                val expHeight = (trend.totalExpenses / maxVal * chartHeight * animProgress.value).toFloat()
                drawRoundRect(
                    color = Color(0xFFFF6B6B),
                    topLeft = Offset(centerX - barWidth - gap / 2, chartHeight - expHeight),
                    size = Size(barWidth, expHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Income bar (right)
                val incHeight = (trend.totalIncomes / maxVal * chartHeight * animProgress.value).toFloat()
                drawRoundRect(
                    color = Color(0xFF4ECDC4),
                    topLeft = Offset(centerX + gap / 2, chartHeight - incHeight),
                    size = Size(barWidth, incHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Month label
                val monthLabel = trend.month.month.getDisplayName(JavaTextStyle.SHORT, Locale.ITALIAN)
                    .replaceFirstChar { it.uppercase() }
                val labelResult = textMeasurer.measure(
                    text = monthLabel,
                    style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = monthLabel,
                    style = TextStyle(color = Color.Gray, fontSize = 10.sp),
                    topLeft = Offset(centerX - labelResult.size.width / 2, chartHeight + 6.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun MonthSelector(
    state: StatsUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mese precedente", tint = GradientPurple)
        }
        Text(
            "${state.selectedMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() }} ${state.selectedMonth.year}",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mese successivo", tint = GradientPurple)
        }
    }
}

@Composable
fun SummaryHeaderCard(state: StatsUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(GradientBlue, GradientPurple)))
                .padding(24.dp)
        ) {
            Column {
                Text("Spesa Totale", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    String.format("\u20AC %.2f", state.totalSpent),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryMiniStat(
                        label = "Entrate",
                        value = String.format("\u20AC %.2f", state.totalFixedIncomes + state.totalIncomes),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4ECDC4)
                    )
                    SummaryMiniStat(
                        label = "Bilancio",
                        value = String.format("\u20AC %.2f", state.totalFixedIncomes - state.totalFixedExpenses - state.totalSpent + state.totalIncomes),
                        icon = Icons.Default.AccountBalance,
                        color = Color(0xFFF7DC6F)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryMiniStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun QuickStatsRow(state: StatsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "Media/giorno",
            value = String.format("\u20AC%.1f", state.avgDailyExpense),
            icon = Icons.Default.CalendarToday,
            color = Color(0xFF45B7D1),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Transazioni",
            value = "${state.transactionCount}",
            icon = Icons.Default.Receipt,
            color = Color(0xFFBB8FCE),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Top categoria",
            value = state.topCategory,
            icon = Icons.Default.Star,
            color = Color(0xFFF7DC6F),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(title, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnimatedDonutChart(
    stats: List<CategoryStat>,
    totalSpent: Double,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val canvasSize = minOf(size.width, size.height)
        val strokeWidth = 40.dp.toPx()
        val radius = canvasSize / 2 - strokeWidth / 2 - 10.dp.toPx()
        val centerX = size.width / 2
        val centerY = size.height / 2
        val arcRect = Size(radius * 2, radius * 2)
        val topLeft = Offset(centerX - radius, centerY - radius)

        // Background ring
        drawArc(
            color = Color(0xFFF0F0F0),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Animated segments
        var startAngle = -90f
        val total = stats.sumOf { it.amount }
        if (total > 0) {
            stats.forEachIndexed { index, stat ->
                val sweepAngle = (stat.amount / total * 360).toFloat() * animProgress.value
                val color = chartColors[index % chartColors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle.coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }
        }

        // Gap lines between segments
        startAngle = -90f
        if (stats.size > 1 && total > 0) {
            stats.forEach { stat ->
                val sweepAngle = (stat.amount / total * 360).toFloat() * animProgress.value
                startAngle += sweepAngle
                val angleRad = Math.toRadians(startAngle.toDouble())
                val innerR = radius - strokeWidth / 2
                val outerR = radius + strokeWidth / 2
                drawLine(
                    color = Color.White,
                    start = Offset(
                        centerX + (innerR * kotlin.math.cos(angleRad)).toFloat(),
                        centerY + (innerR * kotlin.math.sin(angleRad)).toFloat()
                    ),
                    end = Offset(
                        centerX + (outerR * kotlin.math.cos(angleRad)).toFloat(),
                        centerY + (outerR * kotlin.math.sin(angleRad)).toFloat()
                    ),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Center text
        val totalText = String.format("\u20AC%.0f", totalSpent)
        val totalResult = textMeasurer.measure(
            text = totalText,
            style = TextStyle(
                color = Color(0xFF2C3E50),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        drawText(
            textMeasurer = textMeasurer,
            text = totalText,
            style = TextStyle(color = Color(0xFF2C3E50), fontSize = 24.sp, fontWeight = FontWeight.Bold),
            topLeft = Offset(centerX - totalResult.size.width / 2, centerY - totalResult.size.height / 2 - 8.dp.toPx())
        )

        val labelResult = textMeasurer.measure(
            text = "totale",
            style = TextStyle(color = Color.Gray, fontSize = 12.sp)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "totale",
            style = TextStyle(color = Color.Gray, fontSize = 12.sp),
            topLeft = Offset(centerX - labelResult.size.width / 2, centerY + totalResult.size.height / 2 - 6.dp.toPx())
        )
    }
}

@Composable
fun CategoryCard(
    stat: CategoryStat,
    color: Color,
    maxAmount: Double
) {
    val barProgress = remember { Animatable(0f) }
    LaunchedEffect(stat) {
        barProgress.snapTo(0f)
        barProgress.animateTo(
            targetValue = if (maxAmount > 0) (stat.amount / maxAmount).toFloat() else 0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stat.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        String.format("\u20AC %.2f", stat.amount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFF0F0F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barProgress.value)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(color.copy(alpha = 0.7f), color)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${stat.transactionCount} ${if (stat.transactionCount == 1) "transazione" else "transazioni"}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        String.format("%.1f%%", stat.percentage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.InsertChart,
            contentDescription = null,
            tint = Color(0xFFBDC3C7),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Nessuna spesa registrata",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF7F8C8D)
        )
        Text(
            "Le statistiche appariranno quando\naggiungerai delle spese",
            fontSize = 13.sp,
            color = Color(0xFFBDC3C7),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
