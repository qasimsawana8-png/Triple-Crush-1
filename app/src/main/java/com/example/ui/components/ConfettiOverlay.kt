package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.data.model.Particle

@Composable
fun ConfettiOverlay(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    val progress = remember(particles) { Animatable(0f) }

    LaunchedEffect(particles) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (p in particles) {
            val posX = p.startX * canvasWidth + (p.vx * t * 2.5f)
            val posY = if (p.startY < 0) {
                // Falling confetti
                p.startY * canvasHeight + (p.vy * t * 2.5f)
            } else {
                // Explosive match burst
                p.startY * canvasHeight + (p.vy * t * 2f) + (0.5f * 980f * t * t * 0.4f)
            }

            val alpha = (1f - t).coerceIn(0f, 1f)

            drawCircle(
                color = Color(p.color).copy(alpha = alpha),
                radius = p.size * (1f - t * 0.4f),
                center = Offset(posX, posY)
            )
        }
    }
}
