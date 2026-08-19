package com.example.competition.ui.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.ui.theme.QuizPalette
import com.example.competition.ui.theme.QuizRadii

/** Standard app.component. */
val DefaultGradientColors = listOf(
    QuizPalette.NightMid,
    QuizPalette.Night,
    QuizPalette.NightDeep
)
val DeepGradientColors = listOf(
    QuizPalette.Night,
    QuizPalette.NightDeep,
    QuizPalette.NightDeep
)

/**
 * Translucent-backdrop wrapper: layered vertical gradient plus a few soft
 * radial glows. Every screen renders through it for a consistent arena.
 */
@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = DefaultGradientColors,
    decorative: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        if (decorative) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QuizPalette.Gold.copy(alpha = 0.20f),
                                QuizPalette.Gold.copy(alpha = 0f)
                            )
                        )
                    )
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-70).dp, y = 40.dp)
                    .size(320.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QuizPalette.Violet.copy(alpha = 0.16f),
                                QuizPalette.Violet.copy(alpha = 0f)
                            )
                        )
                    )
            )
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp, y = (-120).dp)
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QuizPalette.Info.copy(alpha = 0.12f),
                                QuizPalette.Info.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

/** Reusable pulse scale (breathing) animation. */
@Composable
fun rememberPulseScale(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    ).value
}

/** Glowing ring that draws itself in as an entrance flourish. */
@Composable
fun GlowRing(
    modifier: Modifier = Modifier,
    color: Color = QuizPalette.Gold,
    strokeWidth: Dp = 2.dp
) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val sweep by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(1100, easing = EaseOutCubic),
        label = "ring"
    )
    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = radius,
            style = Stroke(width = stroke * 0.6f)
        )
        drawArc(
            brush = Brush.sweepGradient(
                List(8) { color.copy(alpha = 0.15f + it * 0.08f) }
            ),
            startAngle = 0f,
            sweepAngle = 360f * sweep,
            useCenter = false,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/** Glassy (translucent-white) card with a hairline border. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = QuizRadii.lg,
    color: Color = QuizPalette.Glass,
    borderColor: Color = QuizPalette.GlassBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

/** Composable helper: applies press feedback (scale) to a clickable base. */
@Composable
private fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press"
    )
    return this.scale(scale)
}

/** Pressable glassy card — used for options and list cells. */
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = QuizRadii.md,
    color: Color = QuizPalette.Glass,
    borderColor: Color = QuizPalette.GlassBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interactionSource)
            .clip(shape)
            .background(color)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(contentPadding),
        content = content
    )
}

/** Primary golden CTA button. */
@Composable
fun QuizPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "press"
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        label = "alpha"
    )
    Row(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clip(QuizRadii.md)
            .background(
                Brush.linearGradient(
                    listOf(QuizPalette.GoldPeak, QuizPalette.Gold, QuizPalette.GoldDeep)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = QuizPalette.NightDeep,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Outlined overlay button (secondary action). */
@Composable
fun QuizSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press"
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        label = "alpha"
    )
    Row(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clip(QuizRadii.md)
            .background(QuizPalette.Gold.copy(alpha = 0.10f))
            .border(1.dp, QuizPalette.Gold.copy(alpha = 0.4f), QuizRadii.md)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Compact tag / pill. */
@Composable
fun QuizChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = QuizPalette.Info,
    emoji: String? = null
) {
    Row(
        modifier = modifier
            .clip(QuizRadii.md)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.45f), QuizRadii.md)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/** Standard "back | title" header with a tactile chevron button. */
@Composable
fun ScreenHeader(
    backLabel: String,
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.9f else 1f,
            animationSpec = tween(120),
            label = "backPress"
        )
        Box(
            modifier = Modifier
                .size(42.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(QuizPalette.Gold.copy(alpha = 0.12f))
                .border(1.dp, QuizPalette.Gold.copy(alpha = 0.45f), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawLine(
                    color = QuizPalette.Gold,
                    start = Offset(size.width * 0.65f, size.height * 0.22f),
                    end = Offset(size.width * 0.35f, size.height * 0.5f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = QuizPalette.Gold,
                    start = Offset(size.width * 0.35f, size.height * 0.5f),
                    end = Offset(size.width * 0.65f, size.height * 0.78f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Box(modifier = Modifier.size(42.dp))
    }
}

/** Centered value-over-label stat cell used across stat cards. */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    valueFontSize: TextUnit = 22.sp,
    labelFontSize: TextUnit = 11.sp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = labelFontSize,
            color = QuizPalette.TextSecondary
        )
    }
}

/** Friendly empty state for flat lists. */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(QuizPalette.Glass)
                .border(1.dp, QuizPalette.GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 38.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = QuizPalette.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/** Counts up (or down) to the target number. */
@Composable
fun AnimatedCount(
    target: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.White
) {
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "count"
    )
    Text(
        text = "$animated",
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/** Slim animated progress bar with rounded ends. */
@Composable
fun QuizProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    colors: List<Color> = listOf(QuizPalette.Gold, QuizPalette.GoldDeep),
    trackColor: Color = QuizPalette.GlassBorder
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(250, easing = EaseOutCubic),
        label = "progress"
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(colors))
        )
    }
}