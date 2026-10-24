package com.stitch.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitch.app.ui.theme.*

@Composable
fun ScreenTimeGauge(
    usedMinutes: Long,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val progress = (usedMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_progress"
    )

    val gaugeColor = when {
        progress > 0.9f -> StitchError
        progress > 0.7f -> StitchAccent
        else -> StitchPrimary
    }

    val hours = usedMinutes / 60
    val minutes = usedMinutes % 60
    val percentInt = (progress * 100).toInt()

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()

            // Background circle track
            drawCircle(
                color = StitchBorder,
                radius = size.minDimension / 2 - strokeWidth / 2,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            drawArc(
                color = gaugeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SCREEN TIME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${hours}h",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${minutes}m",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$percentInt% of ${dailyGoalMinutes / 60}h limit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (percentInt > 90) StitchError else StitchTextSecondary
            )
        }
    }
}
