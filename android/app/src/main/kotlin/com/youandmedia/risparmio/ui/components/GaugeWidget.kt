package com.youandmedia.risparmio.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@OptIn(ExperimentalTextApi::class)
@Composable
fun GaugeWidget(
    value: Double,
    entrateFisse: Double,
    entrateMese: Double,
    usciteFisse: Double,
    usciteMese: Double,
    modifier: Modifier = Modifier
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 2000)
        )
    }

    val residuo = max(0.0, entrateFisse - usciteFisse + entrateMese - usciteMese)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(10.dp)
    ) {
        val centerX = size.width / 2 + 50.dp.toPx()
        val centerY = size.height / 2
        val arcSize = 170.dp.toPx()
        val strokeWidth = 20.dp.toPx()

        // Title "Risparmio"
        val titleResult = textMeasurer.measure(
            text = "Risparmio",
            style = TextStyle(
                color = Color(0x89000000),
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )
        )
        drawText(
            textLayoutResult = titleResult,
            topLeft = Offset(
                (size.width - titleResult.size.width) / 2 - 120.dp.toPx(),
                (size.height - titleResult.size.height) / 2 - 70.dp.toPx()
            )
        )

        // Amount text
        val amountText = String.format("%.1f", residuo)
        val amountResult = textMeasurer.measure(
            text = "\u20AC$amountText",
            style = TextStyle(
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        drawText(
            textLayoutResult = amountResult,
            topLeft = Offset(
                centerX - amountResult.size.width / 2,
                centerY - amountResult.size.height / 2
            )
        )

        // Background arc
        drawArc(
            color = Color(0x1F000000),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(centerX - arcSize / 2, centerY - arcSize / 2),
            size = Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = Color(0xFF673AB7),
            startAngle = 140f,
            sweepAngle = 260f * animatedValue.value,
            useCenter = false,
            topLeft = Offset(centerX - arcSize / 2, centerY - arcSize / 2),
            size = Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Side info items
        val items = listOf(
            Triple("$entrateFisse", "Entrate fisse", Color(0xFF448AFF)),
            Triple("$entrateMese", "Altre entrate", Color(0xFF448AFF)),
            Triple("-$usciteFisse", "Uscite Fisse", Color(0xFFFF5252)),
            Triple("-$usciteMese", "Uscite del Mese", Color(0xFFFF5252))
        )

        val baseX = size.width - 330.dp.toPx()
        val spaceBetween = 10.dp.toPx()

        items.forEachIndexed { index, (amount, description, color) ->
            val y = size.height - (128 - index * 39).dp.toPx() - spaceBetween

            // Colored dot
            drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(baseX, y))

            // Amount text
            val amtResult = textMeasurer.measure(
                text = amount,
                style = TextStyle(color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
            drawText(textLayoutResult = amtResult, topLeft = Offset(baseX + 15.dp.toPx(), y - 8.dp.toPx()))

            // Description text
            val descResult = textMeasurer.measure(
                text = description,
                style = TextStyle(color = Color.Black, fontSize = 12.sp)
            )
            drawText(textLayoutResult = descResult, topLeft = Offset(baseX + 15.dp.toPx(), y + 11.dp.toPx()))
        }
    }
}
