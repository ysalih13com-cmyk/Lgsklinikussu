package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryLight

enum class AvatarState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    PAUSED
}

@Composable
fun TeacherAvatar(
    state: AvatarState,
    title: String = "Prof. Dr. Yusuf Salih",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    // Speaking mouth animation
    val mouthHeightAnim by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth"
    )

    // Thinking dot alpha
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dots"
    )

    // Listening mic pulse
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(52.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f - 3f

                // Head background
                drawCircle(
                    color = Color(0xFFE0F2FE),
                    radius = r,
                    center = Offset(cx, cy)
                )
                // Head border
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Eyes
                val eyeY = cy - 6.dp.toPx()
                val eyeOffset = 10.dp.toPx()
                val eyeRadius = 3.5.dp.toPx()
                drawCircle(
                    color = Color(0xFF0C4A6E),
                    radius = eyeRadius,
                    center = Offset(cx - eyeOffset, eyeY)
                )
                drawCircle(
                    color = Color(0xFF0C4A6E),
                    radius = eyeRadius,
                    center = Offset(cx + eyeOffset, eyeY)
                )

                // Glasses rim
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 7.dp.toPx(),
                    center = Offset(cx - eyeOffset, eyeY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 7.dp.toPx(),
                    center = Offset(cx + eyeOffset, eyeY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawLine(
                    color = Color(0xFF0284C7),
                    start = Offset(cx - 3.dp.toPx(), eyeY),
                    end = Offset(cx + 3.dp.toPx(), eyeY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Mouth
                val mouthY = cy + 10.dp.toPx()
                val mH = if (state == AvatarState.SPEAKING) mouthHeightAnim else 5f
                val mW = 20.dp.toPx()
                drawOval(
                    color = Color(0xFF0284C7),
                    topLeft = Offset(cx - mW / 2f, mouthY - mH / 2f),
                    size = Size(mW, mH)
                )

                // Listening badge or thinking indicators
                if (state == AvatarState.LISTENING) {
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 4.dp.toPx() * micPulse,
                        center = Offset(size.width - 6.dp.toPx(), 8.dp.toPx())
                    )
                } else if (state == AvatarState.THINKING) {
                    val dotY = size.height - 5.dp.toPx()
                    drawCircle(color = Color(0xFF0284C7).copy(alpha = dotAlpha), radius = 2.dp.toPx(), center = Offset(cx - 7.dp.toPx(), dotY))
                    drawCircle(color = Color(0xFF0284C7).copy(alpha = dotAlpha), radius = 2.dp.toPx(), center = Offset(cx, dotY))
                    drawCircle(color = Color(0xFF0284C7).copy(alpha = dotAlpha), radius = 2.dp.toPx(), center = Offset(cx + 7.dp.toPx(), dotY))
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val statusText = when (state) {
                AvatarState.SPEAKING -> "🗣️ Konuyu anlatıyor..."
                AvatarState.THINKING -> "🧠 Düşünüyor ve hazırlıyor..."
                AvatarState.LISTENING -> "🎙️ Seni dinliyor..."
                AvatarState.PAUSED -> "⏸️ Duraklatıldı"
                AvatarState.IDLE -> "🩺 Hazır, seni dinliyor"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
