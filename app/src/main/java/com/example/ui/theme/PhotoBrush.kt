package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

// Primary branding colors
val BrandBackground = Color(0xFF070B14)
val BrandSurface = Color(0xFF0F1724)
val BrandCard = Color(0xFF121C2E)
val BrandPrimary = Color(0xFF00D4FF)   // Neon Cyan
val BrandSecondary = Color(0xFF4F46E5) // Indigo
val BrandAccent = Color(0xFF00FFA3)    // Neon Green
val BrandWarning = Color(0xFFFFB547)   // Amber
val BrandDanger = Color(0xFFFF5C5C)    // Soft Red
val BrandMuted = Color(0xFF94A3B8)

// Dynamic artistic gradients based on photo theme
val GradientGraduation = listOf(Color(0xFF8C1D40), Color(0xFFD21F3C), Color(0xFFFFB547)) // Stanford Crimson & Gold
val GradientCamping = listOf(Color(0xFF0F1A30), Color(0xFFD35400), Color(0xFFF39C12))   // Firelight & Night Sky
val GradientSunset = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2), Color(0xFFF000FF))    // SF Sunset Purple-Magenta
val GradientBaking = listOf(Color(0xFF3E2723), Color(0xFF8D6E63), Color(0xFFD7CCC8))    // Kitchen Warm Bread & Cocoa
val GradientBirthday = listOf(Color(0xFF1F1C2C), Color(0xFF928DAB), Color(0xFF00D4FF))  // Cosmic Sparklers & Silver Glow
val GradientWinter = listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC))    // Tahoe Cabin Frost & Sapphire
val GradientSchool = listOf(Color(0xFF0B5345), Color(0xFF117A65), Color(0xFFF4D03F))    // Autumn Green & Schoolbus Yellow
val GradientSplash = listOf(Color(0xFF148F77), Color(0xFF3498DB), Color(0xFF00FFA3))    // Pool Water Splash Turquoise
val GradientThanksgiving = listOf(Color(0xFF5D4037), Color(0xFFE64A19), Color(0xFFFFA000)) // Golden Warm Roast Pumpkin

val PhotoGradients = listOf(
    GradientGraduation,
    GradientCamping,
    GradientSunset,
    GradientBaking,
    GradientBirthday,
    GradientWinter,
    GradientSchool,
    GradientSplash,
    GradientThanksgiving
)

@Composable
fun GenerativePhotoCanvas(
    brushIndex: Int,
    modifier: Modifier = Modifier,
    showOverlays: Boolean = true
) {
    val grads = PhotoGradients.getOrElse(brushIndex) {
        listOf(BrandSecondary, BrandPrimary)
    }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(grads.first(), BrandBackground),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Render custom themed geometric details
            when (brushIndex) {
                0 -> { // Graduation Cap Toss: Diagonal vectors and rising white stars
                    drawCircle(
                        color = grads[1].copy(alpha = 0.4f),
                        radius = width * 0.45f,
                        center = Offset(width * 0.2f, height * 0.8f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 8f,
                        center = Offset(width * 0.3f, height * 0.3f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = 12f,
                        center = Offset(width * 0.7f, height * 0.2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 15f,
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                }
                1 -> { // Campfire Stories: Radiating concentric heat waves or rings
                    drawCircle(
                        color = grads[1].copy(alpha = 0.35f),
                        radius = width * 0.3f,
                        center = Offset(width * 0.5f, height * 0.65f)
                    )
                    drawCircle(
                        color = grads[2].copy(alpha = 0.5f),
                        radius = width * 0.15f,
                        center = Offset(width * 0.5f, height * 0.7f)
                    )
                    // Draw tiny fire particles
                    drawCircle(Color.White.copy(alpha = 0.8f), 6f, Offset(width * 0.45f, height * 0.4f))
                    drawCircle(Color.Yellow.copy(alpha = 0.6f), 8f, Offset(width * 0.55f, height * 0.35f))
                    drawCircle(Color.White.copy(alpha = 0.7f), 4f, Offset(width * 0.52f, height * 0.5f))
                }
                2 -> { // Golden Gate Sunset: Horizontal layout lines/horizon
                    drawLine(
                        color = grads[2].copy(alpha = 0.6f),
                        start = Offset(0f, height * 0.6f),
                        end = Offset(width, height * 0.6f),
                        strokeWidth = 6f
                    )
                    drawCircle(
                        color = grads[1].copy(alpha = 0.4f),
                        radius = width * 0.35f,
                        center = Offset(width * 0.8f, height * 0.5f)
                    )
                }
                3 -> { // Secret Recipe Cookies: Nested organic blobs
                    drawCircle(
                        color = grads[1].copy(alpha = 0.5f),
                        radius = width * 0.28f,
                        center = Offset(width * 0.35f, height * 0.45f)
                    )
                    drawCircle(
                        color = grads[0].copy(alpha = 0.3f),
                        radius = width * 0.22f,
                        center = Offset(width * 0.65f, height * 0.55f)
                    )
                }
                4 -> { // Birthday Candle Sparkler: Concentric glow ring & cross flares
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 20f,
                        center = Offset(width * 0.5f, height * 0.42f)
                    )
                    drawCircle(
                        color = BrandPrimary.copy(alpha = 0.3f),
                        radius = width * 0.4f,
                        center = Offset(width * 0.5f, height * 0.42f)
                    )
                    drawLine(
                        color = BrandPrimary.copy(alpha = 0.6f),
                        start = Offset(width * 0.1f, height * 0.42f),
                        end = Offset(width * 0.9f, height * 0.42f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = BrandPrimary.copy(alpha = 0.6f),
                        start = Offset(width * 0.5f, height * 0.1f),
                        end = Offset(width * 0.5f, height * 0.82f),
                        strokeWidth = 3f
                    )
                }
                5 -> { // Tahoe Cabin Winter: Layered frost circles on top
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = width * 0.25f,
                        center = Offset(width * 0.2f, height * 0.3f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = width * 0.3f,
                        center = Offset(width * 0.75f, height * 0.65f)
                    )
                    // Draw frosty geometric shapes or flakes
                    drawCircle(Color.White.copy(alpha = 0.9f), 10f, Offset(width * 0.4f, height * 0.5f))
                    drawCircle(Color.White.copy(alpha = 0.7f), 6f, Offset(width * 0.6f, height * 0.35f))
                    drawCircle(Color.White.copy(alpha = 0.8f), 8f, Offset(width * 0.25f, height * 0.7f))
                }
                6 -> { // First Day of High School: Vertical columns representing entry gates
                    drawLine(
                        color = grads[1].copy(alpha = 0.4f),
                        start = Offset(width * 0.33f, 0f),
                        end = Offset(width * 0.33f, height),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = grads[1].copy(alpha = 0.4f),
                        start = Offset(width * 0.66f, 0f),
                        end = Offset(width * 0.66f, height),
                        strokeWidth = 10f
                    )
                }
                7 -> { // Backyard Water Splash: Splashes of cyan and light green rings
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = width * 0.45f,
                        center = Offset(width * 0.5f, height * 0.5f),
                        style = Stroke(width = 8f)
                    )
                    drawCircle(
                        color = grads[2].copy(alpha = 0.4f),
                        radius = width * 0.25f,
                        center = Offset(width * 0.5f, height * 0.5f),
                        style = Stroke(width = 4f)
                    )
                }
                8 -> { // Thanksgiving dinner: Golden tables and radial gradients
                    drawCircle(
                        color = grads[2].copy(alpha = 0.45f),
                        radius = width * 0.4f,
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                    drawCircle(
                        color = grads[0].copy(alpha = 0.3f),
                        radius = width * 0.2f,
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                }
                else -> {
                    // Classic cyber glowing arcs
                    drawCircle(
                        color = BrandPrimary.copy(alpha = 0.2f),
                        radius = width * 0.3f,
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                }
            }

            // Beautiful glow scanning lines overlays
            if (showOverlays) {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, height * 0.1f),
                    end = Offset(width, height * 0.15f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, height * 0.9f),
                    end = Offset(width, height * 0.82f),
                    strokeWidth = 2f
                )
            }
        }
    }
}
