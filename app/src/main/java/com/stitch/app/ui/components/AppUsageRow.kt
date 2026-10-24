package com.stitch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitch.app.data.model.AppUsageInfo
import com.stitch.app.ui.theme.*

@Composable
fun AppUsageRow(
    usageInfo: AppUsageInfo,
    maxMinutes: Long
) {
    val progress = if (maxMinutes > 0) (usageInfo.usageMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f) else 0f

    val categoryColor = when (usageInfo.category) {
        "Social" -> StitchSocialColor
        "Entertainment" -> StitchMediaColor
        "Productivity" -> StitchSecondary
        else -> StitchPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usageInfo.appName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        text = usageInfo.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextPrimary
                    )
                    Text(
                        text = usageInfo.category,
                        fontSize = 11.sp,
                        color = StitchTextTertiary
                    )
                }
            }

            Text(
                text = usageInfo.formattedUsage(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = categoryColor,
            trackColor = StitchBorder
        )
    }
}
